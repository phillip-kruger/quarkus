export class JsonRpcController {
  static messageQueue = new Map(); // Keep track of messages waiting for a response
  static initQueue = []; // If message came in and we do not have a connection yet, we queue here
  static messageCounter = 0;
  static webSocket;  
  
  host;
  
  constructor(host) {
    (this.host = host).addController(this);
  
    if(!JsonRpcController.webSocket){
      var serverUri;
      if (window.location.protocol === "https:") {
        serverUri = "wss:";
      } else {
        serverUri = "ws:";
      }

      serverUri += "//" + window.location.host + "/devui-websocket";

      JsonRpcController.webSocket = new WebSocket(serverUri);
      
      JsonRpcController.webSocket.onopen = function(event) {
        while (JsonRpcController.initQueue.length > 0) {
          JsonRpcController.webSocket.send(JsonRpcController.initQueue.pop())
        } 
      };
  
      JsonRpcController.webSocket.onmessage = function (event) {
        var response = JSON.parse(event.data);
        
        if(JsonRpcController.messageQueue.has(response.id)){
          var owner = JsonRpcController.messageQueue.get(response.id);
          owner.onJsonRpcResponse(response.result);
          JsonRpcController.messageQueue.delete(response.id);
        }          
      }
  
      JsonRpcController.webSocket.onerror = function (error) {
          console.log(error);
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
    JsonRpcController.messageQueue.set(uid, this.host);

    var message = new Object();
    message.jsonrpc = "2.0";
    message.method  = method;
    message.params = params;
    message.id = uid;

    var jsonrpcpayload = JSON.stringify(message);

    if (JsonRpcController.webSocket.readyState !== WebSocket.OPEN) {
      JsonRpcController.initQueue.push(jsonrpcpayload)
    } else {
      JsonRpcController.webSocket.send(jsonrpcpayload);
    }
  }
}