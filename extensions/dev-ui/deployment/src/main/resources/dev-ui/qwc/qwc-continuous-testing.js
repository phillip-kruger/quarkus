import { LitElement, html, css} from 'lit';
import { until } from 'lit/directives/until.js';
import { JsonRpcController } from 'jsonrpc-controller';

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

  getContinuousTestingResponse(result){
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