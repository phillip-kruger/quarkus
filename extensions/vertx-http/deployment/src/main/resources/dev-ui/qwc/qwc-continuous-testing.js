import { LitElement, html, css } from 'https://unpkg.com/lit@2.3.1/index.js?module';
import { until } from 'https://unpkg.com/lit@2.3.1/directives/until.js?module';
import { JsonRpcController } from './../controller/jsonrpc-controller.js';

/**
 * This component shows the Continuous Testing Page
 */
export class QwcContinuousTesting extends LitElement {
  static methodName = "getContinuousTesting";
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
    _tests: {state: true}
  };
  
  connectedCallback() {
    super.connectedCallback();
    this.jsonRpcController.request(QwcContinuousTesting.methodName);
  }

  onJsonRpcResponse(result){
    this._tests = result;
  }

  render() {
    return html`${until(this._renderJsonRpcResponse(), html`<span>Loading...</span>`)}`;
  }

  _renderJsonRpcResponse(){
    if(this._tests){
      return html`<div class="todo">${this._tests}</div>`;
    }
  }

}
customElements.define('qwc-continuous-testing', QwcContinuousTesting);