import { LitElement, html, css } from 'https://unpkg.com/lit@2.3.1/index.js?module';
import { until } from 'https://unpkg.com/lit@2.3.1/directives/until.js?module';
import { JsonRpcController } from './../controller/jsonrpc-controller.js';

/**
 * This component shows the Arc Invocation Trees
 */
export class QwcArcInvocationTrees extends LitElement {
  //static methodName = "getDevServices";
  jsonRpcController = new JsonRpcController(this);

  static styles = css`
        .todo {
            font-size: small;
            color: #4695EB;
            padding-left: 10px;
            background: white;
            height: 100%;
        }`;

  static properties = {
    _services: {state: true}
  };
  
  connectedCallback() {
    super.connectedCallback();
    //this.jsonRpcController.request(QwcInvocationTrees.methodName);
  }

  onJsonRpcResponse(result){
    this._services = result;
  }

  render() {
    return html`<div class="todo">Loading invocation trees...</div>`;
  }

  _renderJsonRpcResponse(){
    if(this._services){
      return html`<div class="todo">${this._services}</div>`;
    }
  }

}
customElements.define('qwc-arc-invocation-trees', QwcArcInvocationTrees);