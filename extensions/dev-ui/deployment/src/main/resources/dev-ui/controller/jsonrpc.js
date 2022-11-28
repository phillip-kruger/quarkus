class ConnectionState {
    static Disconnected = new ConnectionState("disconnected");
    static Connecting = new ConnectionState("connecting");
    static Connected = new ConnectionState("connected");
    static Paused = new ConnectionState("paused");

    constructor(state) {
        this.state = state;
    }

    toString() {
        return this.state;
    }
}

class Level {
    static Info = new Level("info");
    static Warning = new Level("warning");
    static Error = new Level("error");

    constructor(level) {
        this.level = level;
    }

    toString() {
        return this.level;
    }
}

class MessageDirection {
    static Up = new MessageDirection("up");
    static Down = new MessageDirection("down");
    static Stationary = new MessageDirection("stationary");

    constructor(direction) {
        this.direction = direction;
    }

    toString() {
        return this.direction;
    }
}

class MessageType {
    static Init = new MessageType("Init");
    static Response = new MessageType("Response");
    static Void = new MessageType("Void");
    static SubscriptionMessage = new MessageType("SubscriptionMessage");

    constructor(messageType) {
        this.messageType = messageType;
    }

    toString() {
        return this.messageType;
    }
}

class Observer {
    constructor(id) {
        this.id = id;
    }
    
    onNext(callback){
        this.onNextCallback = callback;
        return this;
    }
    
    onError(callback){
        this.onErrorCallback = callback;
        return this;
    }
    
    cancel(){
        JsonRpc.observerQueue.delete(this.id);
        JsonRpc.cancelSubscription(this.id);
    }
}

/**
 * This class allow a proxy to the JsonRPC messages. 
 * Callers will call the json-rpc method they want to call (even though the method does not exist on this class) and the proxy will translate that to
 * a json RPC Message format and send it over web socket to the server, returning a promise, that will resolve once the websocket replies.
 */
export class JsonRpc {
    static promiseQueue = new Map(); // Keep track of promise waiting for a response
    static observerQueue = new Map(); // Keep track of subscriptions waiting for a responses
    static initQueue = []; // If message came in and we do not have a connection yet, we queue here
    static knownSubscription = []; // List of known subscription we receive on init, so we know when to return a Subject (vs a Promise) in the Proxy
    static messageCounter = 0;
    static webSocket;
    static serverUri;
    static connectionState;

    _extensionName;

    constructor(extensionName) {
        this._extensionName = extensionName;
        if (!JsonRpc.webSocket) {
            if (window.location.protocol === "https:") {
                JsonRpc.serverUri = "wss:";
            } else {
                JsonRpc.serverUri = "ws:";
            }
            var currentPath = window.location.pathname;
            currentPath = currentPath.substring(0, currentPath.indexOf('/dev')) + "/dev-ui";
            JsonRpc.serverUri += "//" + window.location.host + currentPath + "/json-rpc-ws";
            JsonRpc.connect();
        }

        return new Proxy(this, {

            get(target, prop) {

                const origMethod = target[prop];

                if (typeof origMethod == 'undefined') {
                    return function (...args) {
                        var uid = JsonRpc.messageCounter++;

                        let method = this._extensionName + "." + prop.toString();

                        let params = new Object();
                        if (args.length > 0) {
                            params = args[0];
                        }

                        // Make a JsonRPC Call to the server
                        var message = new Object();
                        message.jsonrpc = "2.0";
                        message.method = method;
                        message.params = params;
                        message.id = uid;

                        var jsonrpcpayload = JSON.stringify(message);

                        if (JsonRpc.knownSubscription.includes(method)) {
                            // Observer
                            var observer = new Observer(uid);
                            JsonRpc.observerQueue.set(uid, observer);
                            JsonRpc.sendJsonRPCMessage(jsonrpcpayload);
                            return observer;
                        } else {
                            // Promise
                            var _resolve, _reject;
                            var promise = new Promise((resolve, reject) => {
                                _reject = reject;
                                _resolve = resolve;
                            });
                            promise.resolve_ex = (value) => {
                                _resolve(value);
                            };
                            promise.reject_ex = (value) => {
                                _reject(value);
                            };
                            JsonRpc.promiseQueue.set(uid, promise);
                            JsonRpc.sendJsonRPCMessage(jsonrpcpayload);
                            return promise;
                        }
                    }
                } else {
                    return Reflect.get(target, prop);
                }
            }
        })
    }

    static sendJsonRPCMessage(jsonrpcpayload) {
        if (JsonRpc.webSocket.readyState !== WebSocket.OPEN) {
            JsonRpc.initQueue.push(jsonrpcpayload);
        } else {
            JsonRpc.webSocket.send(jsonrpcpayload);
            JsonRpc.dispatchMessageLogEntry(ConnectionState.Connected, Level.Info, MessageDirection.Up, jsonrpcpayload);
        }
    }

    static cancelSubscription(id) {
        var message = new Object();
        message.jsonrpc = "2.0";
        message.method = "unsubscribe";
        message.params = {};
        message.id = id;

        var jsonrpcpayload = JSON.stringify(message);
        JsonRpc.sendJsonRPCMessage(jsonrpcpayload);
    }

    static connect() {
        JsonRpc.dispatchStateChange(ConnectionState.Connecting);
        JsonRpc.dispatchMessageLogEntry(ConnectionState.Connecting, Level.Info, MessageDirection.Stationary, "Connecting to " + JsonRpc.serverUri);
        JsonRpc.webSocket = new WebSocket(JsonRpc.serverUri);

        JsonRpc.webSocket.onopen = function (event) {
            JsonRpc.dispatchStateChange(ConnectionState.Connected);
            JsonRpc.dispatchMessageLogEntry(ConnectionState.Connected, Level.Info, MessageDirection.Stationary, "Connected to " + JsonRpc.serverUri);
            while (JsonRpc.initQueue.length > 0) {
                JsonRpc.webSocket.send(JsonRpc.initQueue.pop());
            }
        };

        JsonRpc.webSocket.onmessage = function (event) {
            var response = JSON.parse(event.data);
            var devUiResponse = response.result;
            var messageType = devUiResponse.messageType;

            if (messageType === MessageType.Init.toString()) { // Init message
                JsonRpc.knownSubscription = devUiResponse.object;
                var jsonrpcpayload = JSON.stringify(JsonRpc.knownSubscription);
                JsonRpc.dispatchMessageLogEntry(ConnectionState.Connected, Level.Info, MessageDirection.Down, "Connection initialised. Avaliable subscriptions: " + jsonrpcpayload);
            } else if (messageType === MessageType.Void.toString()) { // Void response, typically used on initial subscription
                // Do nothing
            } else if (messageType === MessageType.Response.toString()) { // Normal Request-Response
                if (JsonRpc.promiseQueue.has(response.id)) {
                    var promise = JsonRpc.promiseQueue.get(response.id);
                    var userData = devUiResponse.object;
                    response.result = userData;

                    promise.resolve_ex(response);
                    JsonRpc.promiseQueue.delete(response.id);
                    var jsonrpcpayload = JSON.stringify(response);
                    JsonRpc.dispatchMessageLogEntry(ConnectionState.Connected, Level.Info, MessageDirection.Down, jsonrpcpayload);
                } else {
                    JsonRpc.dispatchMessageLogEntry(ConnectionState.Connected, Level.Warning, MessageDirection.Down, "Initial normal request not found [ " + devUiResponse.messageType + "], " + event.data);
                }
            } else if (messageType === MessageType.SubscriptionMessage.toString()) { // Subscription message
                if (JsonRpc.observerQueue.has(response.id)) {
                    var observer = JsonRpc.observerQueue.get(response.id);
                    var userData = devUiResponse.object;
                    response.result = userData;
                    observer.onNextCallback(response);
                    var jsonrpcpayload = JSON.stringify(response);
                    JsonRpc.dispatchMessageLogEntry(ConnectionState.Connected, Level.Info, MessageDirection.Down, jsonrpcpayload);
                } else {
                    // Let's cancel as we do not have someone interested in this anymore
                    JsonRpc.cancelSubscription(response.id);
                    JsonRpc.dispatchMessageLogEntry(ConnectionState.Connected, Level.Warning, MessageDirection.Stationary, "Auto unsubscribe from  [" + response.id + "] as no one is listening anymore ");
                }
            } else {
                JsonRpc.dispatchMessageLogEntry(ConnectionState.Connected, Level.Warning, MessageDirection.Down, "Unknown type [" + devUiResponse.messageType + "], " + event.data);
            }

        }

        JsonRpc.webSocket.onclose = function (event) {
            JsonRpc.dispatchStateChange(ConnectionState.Disconnected);
            JsonRpc.dispatchMessageLogEntry(ConnectionState.Disconnected, Level.Warning, MessageDirection.Stationary, "Closed connection to " + JsonRpc.serverUri);
            setTimeout(function () {
                JsonRpc.connect();
            }, 1000);
        };

        JsonRpc.webSocket.onerror = function (error) {
            JsonRpc.dispatchMessageLogEntry(ConnectionState.Disconnected, Level.Error, MessageDirection.Stationary, "Error from " + JsonRpc.serverUri);
            JsonRpc.webSocket.close();
        }
    }

    static dispatchMessageLogEntry(connectionState, level, direction, message) {
        var logEntry = new Object();
        logEntry.id = Math.floor(Math.random() * 999999);
        let now = new Date();
        logEntry.date = now.toDateString();
        logEntry.time = now.toLocaleTimeString('en-US');
        logEntry.direction = direction.toString();
        logEntry.connectionState = connectionState.toString();
        logEntry.level = level.toString();
        logEntry.message = message;
        const event = new CustomEvent('jsonRPCLogEntryEvent', {detail: logEntry});
        document.dispatchEvent(event);
    }

    static dispatchStateChange(connectionState) {
        JsonRpc.connectionState = connectionState.toString();
        const event = new CustomEvent('jsonRPCStateChangeEvent', {detail: connectionState.toString()});
        document.dispatchEvent(event);
    }
}