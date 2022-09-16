import { LitElement, html, css } from 'https://unpkg.com/lit@2.3.1/index.js?module';
import { until } from 'https://unpkg.com/lit@2.3.1/directives/until.js?module';
import { JsonRpcController } from './../controller/jsonrpc-controller.js';

/**
 * This component shows the application name and version and Quarkus version
 */
export class QwcVersionInfo extends LitElement {
  static methodName = "getVersionInfo";
  jsonRpcController = new JsonRpcController(this);

  static styles = css`
        span {
          cursor: pointer;
        }
        span:hover {
          color: #ff004a;
        }`;

  static properties = {
    _applicationInfo: {state: true}
  };

  connectedCallback() {
    super.connectedCallback();  
    this.jsonRpcController.request(QwcVersionInfo.methodName);
  }

  render() {
    return html`${until(this._renderJsonRpcResponse(), html`<span>Loading...</span>`)}`;
  }

  _renderJsonRpcResponse(){
    if(this._applicationInfo){
      return html`<div>${this._applicationInfo.applicationName} ${this._applicationInfo.applicationVersion} (powered by <span @click="${this._quarkus}">Quarkus ${this._applicationInfo.quarkusVersion}</span>)</div>`;
    }
  }

  onJsonRpcResponse(result){
    this._applicationInfo = result;
  }

  _quarkus(e) {
    window.open("https://quarkus.io", '_blank').focus();
  }
}
customElements.define('qwc-version-info', QwcVersionInfo);