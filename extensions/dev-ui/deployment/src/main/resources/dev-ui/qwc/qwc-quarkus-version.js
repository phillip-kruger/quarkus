import { LitElement, html, css} from 'lit';
import { until } from 'lit/directives/until.js';

import { JsonRpcController } from 'jsonrpc-controller';

/**
 * This component shows the Quarkus version
 */
export class QwcQuarkusVersion extends LitElement {
  static methodName = "getQuarkusVersion";
  jsonRpcController = new JsonRpcController(this);

  static styles = css`
        span {
          cursor: pointer;
          width: 100%;
          display: flex;
          flex-direction: row;
          justify-content: flex-start;
          font-size: x-small;
          color: #6d7279;
          padding-left: 2px;
        }
        span:hover {
          color: #ff004a;
        }`;

  static properties = {
    _version: {state: true}
  };

  connectedCallback() {
    super.connectedCallback();  
    this.jsonRpcController.request(QwcQuarkusVersion.methodName);
  }

  render() {
    return html`${until(this._renderJsonRpcResponse(), html`<span>Loading...</span>`)}`;
  }

  _renderJsonRpcResponse(){
    if(this._version){
      return html`<span @click="${this._quarkus}">${this._version}</span>`;
    }
  }

  onJsonRpcResponse(result){
    this._version = result;
  }

  _quarkus(e) {
    window.open("https://quarkus.io", '_blank').focus();
  }
}
customElements.define('qwc-quarkus-version', QwcQuarkusVersion);