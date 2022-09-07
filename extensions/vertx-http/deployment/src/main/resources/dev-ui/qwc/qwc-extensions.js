import { LitElement, html, css } from 'https://unpkg.com/lit@2.3.1/index.js?module';
import { until } from 'https://unpkg.com/lit@2.3.1/directives/until.js?module';
import { JsonRpcController } from './../controller/jsonrpc-controller.js';
// import 'https://unpkg.com/@vaadin/scroller@23.2.0/vaadin-scroller.js?module';
import './qwc-extension.js';

/**
 * This component create cards of all the extensions
 */
export class QwcExtensions extends LitElement {
  static methodName = "getExtensions";
  jsonRpcController = new JsonRpcController(this);

  static styles = css`
    .hr {
      border-bottom: 1px solid rgba(28,110,164,0.1);
    }
    .grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
      gap: 1rem;
      padding-left: 10px;
      padding-right: 10px;
      padding-bottom: 10px;
    }`;

  static properties = {
    _extensions: {state: true}
  };

  connectedCallback() {
    super.connectedCallback();
    this.jsonRpcController.request(QwcExtensions.methodName);
  }

  onJsonRpcResponse(result){
    this._extensions = result;
  }

  render() {
    return html`${until(this._renderJsonRpcResponse(), html`<span>Loading...</span>`)}`;
  }

  _renderJsonRpcResponse(){
    if(this._extensions){
      let active = this._extensions.active;
      let inactive = this._extensions.inactive;
      return html`<div class="page">
          <div class="grid">
            ${active.map(extension => html`
                <qwc-extension 
                    class="active"
                    name="${extension.name}" 
                    description="${extension.description}" 
                    guide="${extension.guide}">
                </qwc-extension>        
            `)}
          </div>
          
          <div class="hr"></div>
  
          <div class="grid">
            ${inactive.map(extension => html`
                <qwc-extension
                    class="inactive"
                    name="${extension.name}" 
                    description="${extension.description}" 
                    guide="${extension.guide}">
                </qwc-extension>        
            `)}
          </div>
        </div>`;
    }
  }

}
customElements.define('qwc-extensions', QwcExtensions);