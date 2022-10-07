import { LitElement, html, css} from 'lit';
import { until } from 'lit/directives/until.js';

import { JsonRpcController } from 'jsonrpc-controller';

/**
 * This component shows the application name and version
 */
export class QwcAppInfo extends LitElement {
  static methodName = "getAppInfo";
  jsonRpcController = new JsonRpcController(this);

  static styles = css`
        div {
          font-size: small;
          color: #6d7279;
          padding-left: 10px;
        }
        `;

  static properties = {
    _appInfo: {state: true}
  };

  connectedCallback() {
    super.connectedCallback();  
    this.jsonRpcController.request(QwcAppInfo.methodName);
  }

  render() {
    return html`${until(this._renderJsonRpcResponse(), html`<div>Loading...</div>`)}`;
  }

  _renderJsonRpcResponse(){
    if(this._appInfo){
      return html`<div>${this._appInfo.name} ${this._appInfo.version}</div>`;
    }
  }

  onJsonRpcResponse(result){
    this._appInfo = result;
  }

}
customElements.define('qwc-app-info', QwcAppInfo);