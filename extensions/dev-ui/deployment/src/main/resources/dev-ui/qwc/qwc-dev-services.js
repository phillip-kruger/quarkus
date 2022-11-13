import { LitElement, html, css} from 'lit';
import { until } from 'lit/directives/until.js';
import { JsonRpcController } from 'jsonrpc-controller';

/**
 * This component shows the Dev Services Page
 */
export class QwcDevServices extends LitElement {
  static methodName = "getDevServices";
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
    this.jsonRpcController.request(QwcDevServices.methodName);
  }

  getDevServicesResponse(result){
    this._services = result;
  }

  render() {
    return html`${until(this._renderJsonRpcResponse(), html`<span>Loading...</span>`)}`;
  }

  _renderJsonRpcResponse(){
    if(this._services){
      return html`<div class="todo">${this._services}</div>`;
    }
  }

}
customElements.define('qwc-dev-services', QwcDevServices);