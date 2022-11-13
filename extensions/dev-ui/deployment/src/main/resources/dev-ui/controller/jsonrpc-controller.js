export class JsonRpcController {
  static messageQueue = new Map(); // Keep track of messages waiting for a response
  static initQueue = []; // If message came in and we do not have a connection yet, we queue here
  static messageCounter = 0;
  static webSocket;  
  
  host;
  name;

  constructor(host, name = "Internal") {
    this.name = name;
    (this.host = host).addController(this);
  
    if(!JsonRpcController.webSocket){
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
      
      JsonRpcController.webSocket.onopen = function(event) {
        while (JsonRpcController.initQueue.length > 0) {
          JsonRpcController.webSocket.send(JsonRpcController.initQueue.pop())
        } 
      };
  
      JsonRpcController.webSocket.onmessage = function (event) {
        var response = JSON.parse(event.data);
        if(JsonRpcController.messageQueue.has(response.id)){
            var requestDetails = JsonRpcController.messageQueue.get(response.id);
            var owner = requestDetails.owner;
          
            console.log("<---- name = " + requestDetails.name);
            console.log("<---- method = " + requestDetails.method);
            
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
  }

  hostConnected() {
    
  }

  hostDisconnected() {
    
  }

  /**
   * Make a JsonRPC Call to the server
   */
  request(method, params = new Object()){
    var uid = JsonRpcController.messageCounter++;
    JsonRpcController.messageQueue.set(uid, {
        owner: this.host,
        name: this.name,
        method: method,
    });
    var message = new Object();
    message.jsonrpc = "2.0";
    message.method  = this.name + "." + method;
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
}