import { LitElement, html, css} from 'lit';
import { until } from 'lit/directives/until.js';
import { JsonRpcController } from 'controller/jsonrpc-controller.js';

/**
 * This component shows the Arc Beans
 */
export class QwcArcBeans extends LitElement {
  static methodName = "getAllBeans";
  jsonRpcController = new JsonRpcController(this, "ArC");

  static styles = css`
        .todo {
            font-size: small;
            color: #4695EB;
            padding-left: 10px;
            background: white;
            height: 100%;
        }`;

  static properties = {
    _beans: {state: true}
  };
  
  connectedCallback() {
    super.connectedCallback();
    this.jsonRpcController.request(QwcArcBeans.methodName);
  }

  onJsonRpcResponse(result){
    this._beans = result;
  }

  render() {
    return html`${until(this._renderJsonRpcResponse(), html`<span>Loading ArC beans...</span>`)}`;
  }

  _renderJsonRpcResponse(){
    if(this._beans){
      return html`<div class="todo">${this._beans}</div>`;
    }
  }

}
customElements.define('qwc-arc-beans', QwcArcBeans);