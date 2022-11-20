
class ConnectionState {
    static Disconnected = new ConnectionState("disconnected");
    static Connecting = new ConnectionState("connecting");
    static Connected = new ConnectionState("connected");
    static Paused = new ConnectionState("paused");

    constructor(state) {
      this.state = state;
    }

    toString(){
        return this.state;
    }
}

class MessageDirection {
    static Up = new MessageDirection("up");
    static Down = new MessageDirection("down");
    static Stationary = new MessageDirection("stationary");
    
    constructor(direction) {
      this.direction = direction;
    }

    toString(){
        return this.direction;
    }
}

/**
 * This class allow a proxy to the JsonRPC messages. 
 * Callers will call the json-rpc method they want to call (even though the method does not exist on this class) and the proxy will translate that to
 * a json RPC Message format and send it over web socket to the server, returning a promise, that will resolve once the websocket replies.
 */
export class JsonRpc {
    static promiseQueue = new Map(); // Keep track of promise waiting for a response
    static initQueue = []; // If message came in and we do not have a connection yet, we queue here
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

                        let method = prop.toString();

                        let params = new Object();
                        if(args.length > 0){
                            params = args[0];
                        }

                        let promise = this._createPromise();
                        JsonRpc.promiseQueue.set(uid,promise);

                        // Make a JsonRPC Call to the server
                        var message = new Object();
                        message.jsonrpc = "2.0";
                        message.method = this._extensionName + "." + method;
                        message.params = params;
                        message.id = uid;
                
                        var jsonrpcpayload = JSON.stringify(message);
                        if (JsonRpc.webSocket.readyState !== WebSocket.OPEN) {
                            JsonRpc.initQueue.push(jsonrpcpayload)
                        } else {
                            JsonRpc.webSocket.send(jsonrpcpayload);
                            JsonRpc.dispatchMessageLogEntry(ConnectionState.Connected, MessageDirection.Up, jsonrpcpayload);
                        }

                        return promise;
                    }
                }else{
                    return Reflect.get(target, prop);
                }
            }
        })
    }
    
    _createPromise() {

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
    
        return promise;
    }
    
    static connect() {
        JsonRpc.dispatchStateChange(ConnectionState.Connecting);
        JsonRpc.dispatchMessageLogEntry(ConnectionState.Connecting, MessageDirection.Stationary, "Connecting to " + JsonRpc.serverUri);
        JsonRpc.webSocket = new WebSocket(JsonRpc.serverUri);

        JsonRpc.webSocket.onopen = function (event) {
            JsonRpc.dispatchStateChange(ConnectionState.Connected);
            JsonRpc.dispatchMessageLogEntry(ConnectionState.Connected, MessageDirection.Stationary, "Connected to " + JsonRpc.serverUri);
            while (JsonRpc.initQueue.length > 0) {
                JsonRpc.webSocket.send(JsonRpc.initQueue.pop())
            }
        };

        JsonRpc.webSocket.onmessage = function (event) {
            var response = JSON.parse(event.data);
            if (JsonRpc.promiseQueue.has(response.id)) {
                var promise = JsonRpc.promiseQueue.get(response.id);
                promise.resolve_ex(response);
                JsonRpc.promiseQueue.delete(response.id);
                var jsonrpcpayload = JSON.stringify(response);
                JsonRpc.dispatchMessageLogEntry(ConnectionState.Connected, MessageDirection.Down, jsonrpcpayload);
            }
        }

        JsonRpc.webSocket.onclose = function(event) {
            JsonRpc.dispatchStateChange(ConnectionState.Disconnected);
            JsonRpc.dispatchMessageLogEntry(ConnectionState.Disconnected, MessageDirection.Stationary, "Closed connection to " + JsonRpc.serverUri);
            setTimeout(function() {
                JsonRpc.connect();
            }, 1000);
        };

        JsonRpc.webSocket.onerror = function (error) {
            JsonRpc.dispatchMessageLogEntry(ConnectionState.Disconnected, MessageDirection.Stationary, "Error from " + JsonRpc.serverUri + " [" + error + "]");
            JsonRpc.webSocket.close();
        }
    }
    
    static dispatchMessageLogEntry(connectionState, direction, message){
        var logEntry = new Object();
        logEntry.id = Math.floor(Math.random() * 999999);
        let now = new Date();
        logEntry.date = now.toDateString();
        logEntry.time = now.toLocaleTimeString('en-US');
        logEntry.direction = direction.toString();
        logEntry.connectionState = connectionState.toString();
        logEntry.message = message;
        const event = new CustomEvent('jsonRPCLogEntryEvent', { detail: logEntry });
        document.dispatchEvent(event);
    }
    
    static dispatchStateChange(connectionState){
        JsonRpc.connectionState = connectionState.toString();
        const event = new CustomEvent('jsonRPCStateChangeEvent', { detail: connectionState.toString() });
        document.dispatchEvent(event);
    }
}