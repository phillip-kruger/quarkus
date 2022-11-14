/**
 * This class allow a proxy to the JsonRPC messages. 
 * Callers will call the json-rpc method they want to call (even if the method does not exist) and the proxt will translate that to
 * a json RPC Message format and send it over web socket
 * 
 * This class will also call back to deliver the results. The callback method is the json-rpc method + "Response".
 */
export class JsonRpcController {
    static messageQueue = new Map(); // Keep track of messages waiting for a response
    static initQueue = []; // If message came in and we do not have a connection yet, we queue here
    static messageCounter = 0;
    static webSocket;

    _host;
    _extensionName;

    constructor(host, extensionName) {
        this._extensionName = extensionName;
        (this._host = host).addController(this);

        if (!JsonRpcController.webSocket) {
            var serverUri;
            if (window.location.protocol === "https:") {
                serverUri = "wss:";
            } else {
                serverUri = "ws:";
            }

            var currentPath = window.location.pathname;
            currentPath = currentPath.substring(0, currentPath.indexOf('/dev')) + "/dev-ui";
            serverUri += "//" + window.location.host + currentPath + "/json-rpc-ws";

            JsonRpcController.webSocket = new WebSocket(serverUri);

            JsonRpcController.webSocket.onopen = function (event) {
                while (JsonRpcController.initQueue.length > 0) {
                    JsonRpcController.webSocket.send(JsonRpcController.initQueue.pop())
                }
            };

            JsonRpcController.webSocket.onmessage = function (event) {
                var response = JSON.parse(event.data);
                if (JsonRpcController.messageQueue.has(response.id)) {
                    var requestDetails = JsonRpcController.messageQueue.get(response.id);
                    var owner = requestDetails.owner;

                    var responseMethodName = requestDetails.method + "Response";
                    try {
                        owner[responseMethodName](response.result, response.id);
                    } catch (error) {
                        console.log(" ERROR  [" + responseMethodName + "] ! " + error);
                    }
                    JsonRpcController.messageQueue.delete(response.id);
                }
            }

            JsonRpcController.webSocket.onerror = function (error) {
                console.log("We got an error !!!!! [" + error + "]");
            }
        }

        return new Proxy(this, {
            get(target, prop) {
                const origMethod = target[prop];

                if (typeof origMethod == 'undefined') {
                    return function (...args) {

                        let method = prop.toString();

                        let params = new Object();
                        if(args.length > 0){
                            params = args[0];
                        }

                        // Make a JsonRPC Call to the server
                        var uid = JsonRpcController.messageCounter++;
                        JsonRpcController.messageQueue.set(uid, {
                            owner: this._host,
                            name: this._extensionName,
                            method: method,
                        });
                        var message = new Object();
                        message.jsonrpc = "2.0";
                        message.method = this._extensionName + "." + method;
                        message.params = params;
                        message.id = uid;
                
                        var jsonrpcpayload = JSON.stringify(message);
                        if (JsonRpcController.webSocket.readyState !== WebSocket.OPEN) {
                            JsonRpcController.initQueue.push(jsonrpcpayload)
                        } else {
                            JsonRpcController.webSocket.send(jsonrpcpayload);
                        }
                        return uid; // If you want to keep tract of this request
                    }
                }else{
                    return Reflect.get(target, prop);
                }
            }
        })
    }
}