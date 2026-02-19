import { jsonRPCSubscriptions } from 'devui-jsonrpc-data';
import { jsonRPCMethods } from 'devui-jsonrpc-data';
import { connectionState } from 'connection-state';
import { RouterController } from 'router-controller';
import { notifier } from 'notifier';

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
    static Response = new MessageType("Response");
    static Void = new MessageType("Void");
    static SubscriptionMessage = new MessageType("SubscriptionMessage");
    static HotReload = new MessageType("HotReload");

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
        // Set default error handler that logs and shows notification
        this.onErrorCallback = this._defaultErrorHandler;
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

    _defaultErrorHandler(response) {
        const errorMessage = response.error?.message || 'Unknown subscription error';
        const errorType = response.error?.type || 'Error';
        console.error('Unhandled subscription error:', response);
        notifier.showErrorMessage('[' + errorType + '] ' + errorMessage);
    }

    toString() {
        return "Observer for + " + this.id;
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
    static messageCounter = 0;
    static webSocket;
    static serverUri;
    static retryCount = 0;
    static maxRetries = 10;
    static requestTimeout = 30000; // 30 seconds timeout for requests
    static timeoutCheckInterval = null;

    _extensionName;
    _logTraffic;

    /**
     * 
     * @param {type} host the component using this.
     *  In the case of full extension pages, the extension namespace will be used and can be found on the router. However, sometimes
     *   extensions might have multiple services, in that case a serviceIdentifier can be used
     *  In the case of Menu items, the menu id (what is registered in the router) is used. 
     *   Again serviceIdentifier can allow multiple backends
     *  In the case of cards or logs, the namespace will be passed in as an attribute (as this component is not registered with the router)
     *   Again serviceIdentifier can allow multiple backends  
     * @param {type} logTraffic - if traffic should be logged in the Dev UI Log (json-rpc log)
     * @param {type} serviceIdentifier - if needed, a backend service identifier
     * @returns {Proxy}
     */
    constructor(host, logTraffic=true, serviceIdentifier=null) {
        
        // First check if host is a String
        if (typeof host === 'string' || host instanceof String){
            this._setExtensionName(host, serviceIdentifier);
        }else {
        
            var page = RouterController.componentMap.get(host.tagName.toLowerCase());

            if (page){
                // For pages
                this._setExtensionName(page.namespace, serviceIdentifier);
            } else {
                // For cards and logs
                this._setExtensionName(host.getAttribute("namespace"), serviceIdentifier);
            }
        }
        
        this._logTraffic = logTraffic;
        if (!JsonRpc.webSocket) {
            if (window.location.protocol === "https:") {
                JsonRpc.serverUri = "wss:";
            } else {
                JsonRpc.serverUri = "ws:";
            }
            JsonRpc.serverUri += "//" + window.location.host + RouterController.getBasePath() + "/json-rpc-ws";
            JsonRpc.connect();
        }

        return new Proxy(this, {

            get(target, prop) {

                const origMethod = target[prop];

                if (typeof origMethod === 'undefined') {
                    return function (...args) {
                        var uid = JsonRpc.messageCounter++;

                        let method = this._extensionName + "_" + prop.toString();

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

                        if (jsonRPCSubscriptions.includes(method)) {
                            if(JsonRpc.observerQueue.has(uid)){
                                JsonRpc.observerQueue.get(uid).observer.cancel();
                            }
                            // Observer
                            var observer = new Observer(uid);
                            JsonRpc.observerQueue.set(uid, {
                                observer: observer,
                                log: this._logTraffic
                            });
                            JsonRpc.sendJsonRPCMessage(jsonrpcpayload, this._logTraffic);
                            return observer;                
                        } else if(jsonRPCMethods.includes(method)){
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
                            JsonRpc.promiseQueue.set(uid, {
                                promise: promise,
                                log: this._logTraffic,
                                timestamp: Date.now(),
                                method: method
                            });
                            JsonRpc.sendJsonRPCMessage(jsonrpcpayload, this._logTraffic);
                            return promise;
                        } else {
                            // Method not found - return rejected promise with error
                            console.error("JSON-RPC method not found: " + method);
                            JsonRpc.dispatchMessageLogEntry(Level.Error, MessageDirection.Stationary,
                                "Method not found: " + method + ". Check that the method is registered.");
                            notifier.showErrorMessage("Method not found: " + method);
                            return Promise.reject({
                                error: {
                                    code: -32601,
                                    message: "Method not found: " + method,
                                    type: "MethodNotFound",
                                    method: method
                                }
                            });
                        }
                    };
                } else {
                    return Reflect.get(target, prop);
                }
            }
        });
    }

    _setExtensionName(discoveredNamespace, serviceIdentifier){
        if(serviceIdentifier){
            this._extensionName = discoveredNamespace + "-" + serviceIdentifier;
        }else {
            this._extensionName = discoveredNamespace;
        }
    }

    getExtensionName(){
        return this._extensionName;
    }

    static sendJsonRPCMessage(jsonrpcpayload, log=true) {
        if (JsonRpc.webSocket.readyState !== WebSocket.OPEN) {
            JsonRpc.initQueue.push(jsonrpcpayload);
        } else {
            JsonRpc.webSocket.send(jsonrpcpayload);
            if(log){
                JsonRpc.dispatchMessageLogEntry(Level.Info, MessageDirection.Up, jsonrpcpayload);
            }
        }
    }

    static cancelSubscription(id, log=true) {
        var message = new Object();
        message.jsonrpc = "2.0";
        message.method = "unsubscribe";
        message.params = {};
        message.id = id;

        var jsonrpcpayload = JSON.stringify(message);
        JsonRpc.sendJsonRPCMessage(jsonrpcpayload, log);
    }

    static connect() {
        if(!connectionState.current.isConnecting && !connectionState.current.isConnected){ // Don't connect if already in progress or connected
            connectionState.connecting(JsonRpc.serverUri);
            JsonRpc.dispatchMessageLogEntry(Level.Info, MessageDirection.Stationary, "Connecting to " + JsonRpc.serverUri);
            JsonRpc.webSocket = new WebSocket(JsonRpc.serverUri);

            JsonRpc.webSocket.onopen = function (event) {
                connectionState.connected(JsonRpc.serverUri);
                JsonRpc.retryCount = 0;
                JsonRpc.maxRetries = 10;
                JsonRpc.dispatchMessageLogEntry(Level.Info, MessageDirection.Stationary, "Connected to " + JsonRpc.serverUri);
                while (JsonRpc.initQueue.length > 0) {
                    JsonRpc.webSocket.send(JsonRpc.initQueue.pop());
                }
                // Start timeout checker if not already running
                JsonRpc.startTimeoutChecker();
            };

            JsonRpc.webSocket.onmessage = function (event) {
                var response = JSON.parse(event.data);
                var devUiResponse = response.result;
                var devUiError = response.error;
                if (!devUiResponse && response.error) {
                    if (JsonRpc.promiseQueue.has(response.id)) {
                        var saved = JsonRpc.promiseQueue.get(response.id);
                        var promise = saved.promise;
                        promise.reject_ex(response);
                        JsonRpc.promiseQueue.delete(response.id);
                        JsonRpc.log(saved,response);
                    }else if(JsonRpc.observerQueue.has(response.id)){
                        var saved = JsonRpc.observerQueue.get(response.id);
                        var observer = saved.observer;
                        response.error = devUiError;
                        if (typeof observer.onErrorCallback === "function") { 
                            observer.onErrorCallback(response);
                        }
                        JsonRpc.log(saved,response);
                    }

                    return;
                }

                var messageType = devUiResponse.messageType;
                
                if (messageType === MessageType.Void.toString()) { // Void response, typically used on initial subscription
                    // Do nothing
                } else if (messageType === MessageType.HotReload.toString() || messageType === MessageType.Response.toString()) {
                    if (messageType === MessageType.HotReload.toString()){
                        connectionState.hotreload(JsonRpc.serverUri);
                        connectionState.connected(JsonRpc.serverUri);
                    }    
                    if (JsonRpc.promiseQueue.has(response.id)) {
                        var saved = JsonRpc.promiseQueue.get(response.id);
                        var promise = saved.promise;
                        var userData = devUiResponse.object;
                        response.result = userData;

                        promise.resolve_ex(response);
                        JsonRpc.promiseQueue.delete(response.id);
                        JsonRpc.log(saved,response);
                    } else {
                        JsonRpc.dispatchMessageLogEntry(Level.Warning, MessageDirection.Down, "Initial normal request not found [ " + devUiResponse.messageType + "], " + event.data);
                    }
                } else if (messageType === MessageType.SubscriptionMessage.toString()) { // Subscription message
                    if (JsonRpc.observerQueue.has(response.id)) {
                        var saved = JsonRpc.observerQueue.get(response.id);
                        var observer = saved.observer;
                        var userData = devUiResponse.object;
                        response.result = userData;
                        observer.onNextCallback(response);
                        JsonRpc.log(saved,response);
                    } else {
                        // Let's cancel as we do not have someone interested in this anymore
                        JsonRpc.cancelSubscription(response.id);
                        JsonRpc.dispatchMessageLogEntry(Level.Warning, MessageDirection.Stationary, "Auto unsubscribe from  [" + response.id + "] as no one is listening anymore ");
                    }
                } else {
                    JsonRpc.dispatchMessageLogEntry(Level.Warning, MessageDirection.Down, "Unknown type [" + devUiResponse.messageType + "], " + event.data);
                }
            };
        }

        JsonRpc.webSocket.onclose = function (event) {
            connectionState.disconnected(JsonRpc.serverUri);
            JsonRpc.dispatchMessageLogEntry(Level.Warning, MessageDirection.Stationary, "Closed connection to " + JsonRpc.serverUri);
            // Stop timeout checker when disconnected
            JsonRpc.stopTimeoutChecker();
            if (JsonRpc.retryCount < JsonRpc.maxRetries) {
                JsonRpc.reconnect();
            } else {
                // Dispatch a custom event when the maximum retries have been reached
                const maxRetriesEvent = new CustomEvent('max-retries-reached', {
                    detail: { message: "Failed to reconnect after multiple attempts." }
                });
                document.dispatchEvent(maxRetriesEvent);
                // Show prominent error notification to the user
                notifier.showPrimaryErrorMessage(
                    'Lost connection to Dev UI server. Please restart the application or refresh the page.',
                    'middle',
                    0  // duration 0 = persistent until dismissed
                );
                JsonRpc.dispatchMessageLogEntry(Level.Error, MessageDirection.Stationary,
                    "Max retries reached. Connection to Dev UI server lost.");
                // Reject all pending promises with connection error
                JsonRpc.rejectAllPending("Connection lost after max retries");
            }
        };

        JsonRpc.webSocket.onerror = function (error) {
            JsonRpc.dispatchMessageLogEntry(Level.Error, MessageDirection.Stationary, "Error from " + JsonRpc.serverUri);
            JsonRpc.webSocket.close();
        };
    }

    static dispatchMessageLogEntry(level, direction, message) {
        var logEntry = new Object();
        logEntry.id = Math.floor(Math.random() * 999999);
        let now = new Date();
        logEntry.date = now.toDateString();
        logEntry.time = now.toLocaleTimeString('en-US');
        logEntry.direction = direction.toString();
        logEntry.connectionState = connectionState.current.name;
        logEntry.level = level.toString();
        logEntry.message = message;
        const event = new CustomEvent('jsonRPCLogEntryEvent', {detail: logEntry});
        document.dispatchEvent(event);
    }
    
    static log(o, response){
        var log = o.log;
        if(log){
            var jsonrpcpayload = JSON.stringify(response);
            JsonRpc.dispatchMessageLogEntry(Level.Info, MessageDirection.Down, jsonrpcpayload);
        }
    }
    
    static reconnect() {
        const delay = Math.min(1000 * Math.pow(1.5, JsonRpc.retryCount), 5000); // Exponential backoff with max 5 seconds delay
        setTimeout(() => {
            JsonRpc.connect();
            JsonRpc.retryCount++;
        }, delay);
    }

    static startTimeoutChecker() {
        if (JsonRpc.timeoutCheckInterval === null) {
            JsonRpc.timeoutCheckInterval = setInterval(() => {
                JsonRpc.cleanupStaleRequests();
            }, 5000); // Check every 5 seconds
        }
    }

    static stopTimeoutChecker() {
        if (JsonRpc.timeoutCheckInterval !== null) {
            clearInterval(JsonRpc.timeoutCheckInterval);
            JsonRpc.timeoutCheckInterval = null;
        }
    }

    static cleanupStaleRequests() {
        const now = Date.now();
        JsonRpc.promiseQueue.forEach((value, key) => {
            if (now - value.timestamp > JsonRpc.requestTimeout) {
                const methodName = value.method || 'unknown';
                JsonRpc.dispatchMessageLogEntry(Level.Warning, MessageDirection.Stationary,
                    "Request timed out for method: " + methodName + " (id: " + key + ")");
                value.promise.reject_ex({
                    error: {
                        code: -32003,
                        message: "Request timed out after " + (JsonRpc.requestTimeout / 1000) + " seconds",
                        type: "TimeoutError",
                        method: methodName
                    }
                });
                JsonRpc.promiseQueue.delete(key);
            }
        });
    }

    static rejectAllPending(reason) {
        JsonRpc.promiseQueue.forEach((value, key) => {
            const methodName = value.method || 'unknown';
            value.promise.reject_ex({
                error: {
                    code: -32003,
                    message: reason,
                    type: "ConnectionError",
                    method: methodName
                }
            });
        });
        JsonRpc.promiseQueue.clear();

        // Also notify observers
        JsonRpc.observerQueue.forEach((value, key) => {
            if (typeof value.observer.onErrorCallback === "function") {
                value.observer.onErrorCallback({
                    error: {
                        code: -32003,
                        message: reason,
                        type: "ConnectionError"
                    }
                });
            }
        });
        JsonRpc.observerQueue.clear();
    }
}
