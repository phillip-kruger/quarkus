import { LitElement, html, css} from 'lit';
import { until } from 'lit/directives/until.js';
import { JsonRpcController } from 'jsonrpc-controller';

/**
 * This component shows the Arc RemovedComponents
 */
export class QwcArcRemovedComponents extends LitElement {
  //static methodName = "getDevServices";



  jsonRpcController = new JsonRpcController(this, "ArC");

  static styles = css`
        `;

  static properties = {
    _services: {state: true}
  };
  
  connectedCallback() {
    super.connectedCallback();
    //this.jsonRpcController.request(QwcRemovedComponents.methodName);
  }

  onJsonRpcResponse(result){
    this._services = result;
  }

  render() {
    return html`<div class="todo">Loading removed components...</div>`;
  }

  _renderJsonRpcResponse(){
    if(this._services){
      return html`<div class="todo">${this._services}</div>`;
    }
  }

}
customElements.define('qwc-arc-removed-components', QwcArcRemovedComponents);