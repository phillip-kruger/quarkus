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

    _extensionName;

    constructor(extensionName) {
        this._extensionName = extensionName;

        if (!JsonRpc.webSocket) {
            var serverUri;
            if (window.location.protocol === "https:") {
                serverUri = "wss:";
            } else {
                serverUri = "ws:";
            }

            var currentPath = window.location.pathname;
            currentPath = currentPath.substring(0, currentPath.indexOf('/dev')) + "/dev-ui";
            serverUri += "//" + window.location.host + currentPath + "/json-rpc-ws";

            JsonRpc.webSocket = new WebSocket(serverUri);

            JsonRpc.webSocket.onopen = function (event) {
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
                }
            }

            JsonRpc.webSocket.onerror = function (error) {
                var promise = JsonRpc.promiseQueue.get(response.id);
                promise.reject_ex(error); // TODO: create Json-RPC error ?
                JsonRpc.promiseQueue.delete(response.id);
            }
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
}