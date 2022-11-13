import { LitElement, html, css} from 'lit';
import { until } from 'lit/directives/until.js';

import { JsonRpcController } from 'jsonrpc-controller';

/**
 * This component shows the Build Steps Page
 */
export class QwcBuildSteps extends LitElement {
  static methodName = "getBuildSteps";
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
    _steps: {state: true}
  };
  
  connectedCallback() {
    super.connectedCallback();
    this.jsonRpcController.request(QwcBuildSteps.methodName);
  }

  getBuildStepsResponse(result){
    this._steps = result;
  }

  render() {
    return html`${until(this._renderJsonRpcResponse(), html`<span>Loading...</span>`)}`;
  }

  _renderJsonRpcResponse(){
    if(this._steps){
      return html`<div class="todo">${this._steps}</div>`;
    }
  }
}
customElements.define('qwc-build-steps', QwcBuildSteps);