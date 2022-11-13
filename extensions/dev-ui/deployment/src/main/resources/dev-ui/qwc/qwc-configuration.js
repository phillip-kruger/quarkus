import { LitElement, html, css} from 'lit';
import { until } from 'lit/directives/until.js';
import { JsonRpcController } from 'jsonrpc-controller';

/**
 * This component allows users to change the configuration
 */
export class QwcConfiguration extends LitElement {
  static methodName = "getAllConfiguration";
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
    _configurations: {state: true}
  };
  
  connectedCallback() {
    super.connectedCallback();
    this.jsonRpcController.request(QwcConfiguration.methodName);
  }

  getAllConfigurationResponse(result){
    this._configurations = result;
  }

  render() {
    return html`${until(this._renderJsonRpcResponse(), html`<span>Loading...</span>`)}`;
  }

  _renderJsonRpcResponse(){
    if(this._configurations){
      return html`<div class="todo">${this._configurations}</div>`;
    }
  }
  
}
customElements.define('qwc-configuration', QwcConfiguration);