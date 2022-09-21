import { LitElement, html, css} from 'lit';
import { until } from 'lit/directives/until.js';
import { JsonRpcController } from 'controller/jsonrpc-controller.js';

/**
 * This component shows the Arc Decorators
 */
export class QwcArcDecorators extends LitElement {
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
    //this.jsonRpcController.request(QwcDecorators.methodName);
  }

  onJsonRpcResponse(result){
    this._services = result;
  }

  render() {
    return html`<div class="todo">Loading decorators...</div>`;
  }

  _renderJsonRpcResponse(){
    if(this._services){
      return html`<div class="todo">${this._services}</div>`;
    }
  }

}
customElements.define('qwc-arc-decorators', QwcArcDecorators);