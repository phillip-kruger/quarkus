import { LitElement, html, css} from 'lit';
import { until } from 'lit/directives/until.js';
import { unsafeHTML } from 'lit/directives/unsafe-html.js';
import { JsonRpcController } from 'jsonrpc-controller';

import '@vaadin/tabs';
import '@vaadin/tabs/vaadin-tab.js';
import '@vaadin/icon';

/**
 * This component shows the Bottom Drawer
 */
export class QwcFooter extends LitElement {
  static methodName = "getBottomDrawerItems";
  jsonRpcController = new JsonRpcController(this);

  static styles = css`
    .horizontal-flex {
      display: flex;
      align-items:center;
    }
    .bottom-bar {
      width: 100%;
      height: 30px;
      justify-content: space-between;
      background-color: rgba(28,110,164,0.03);
    }
    .open-close-button {
      cursor: pointer;
      font-size: small;
      color: #4695EB;
      padding-right: 10px;
    }
    .open-close-button:hover {
      filter: brightness(50%);
    }
    .open-close-icon {
      cursor: pointer;
      padding-right: 5px;
    }
    .bottom-content {
      height: 100%;
      width: 100%;
    }
    `;

  static properties = {
    _content: {state:true},
    _tabItems: {state: true},
    show: {type: Boolean},
    height: {type: Number}
  };
  
  constructor() {
    super();
    this.jsonRpcController.request(QwcFooter.methodName);
    this._content = "loading...";
    this._tabItems = null;
  }

  getBottomDrawerItemsResponse(result){
    this._tabItems = result;
  }

  render() {
    return html`${until(this._renderJsonRpcResponse(), html`<span>Loading...</span>`)}`;
  }

  _renderJsonRpcResponse(){
    if(this._tabItems){
      if(this.show){
        return html`
        <div style="height: ${this.height}px;">
          <div class="bottom-bar horizontal-flex" @dblclick="${this._close}">
            ${this._renderBottomTabs()}
            ${this._renderOpenCloseButton("chevron-down", "Close")}
          </div>
          ${this._renderBottomContent()}
        </div>`;
      }else{
        
        return html`
          <div class="bottom-bar horizontal-flex" @dblclick="${this._open}">
            ${this._renderBottomTabs()}
            ${this._renderOpenCloseButton("chevron-up", "Open")}
          </div>
          `;
      }
    }
  }

  _renderBottomTabs(){
      return html`
        <vaadin-tabs @selected-changed="${this._selectedChanged}" theme="minimal">
          ${this._tabItems.map((tabItem) =>
            html`${this._renderBottomTab(tabItem.webcomponent)}`
          )}
        </vaadin-tabs>
    `;    
  }

  _renderBottomTab(tabItem){

    if(this.show){
      var displayName = this._getTabDisplayName(tabItem);
      return html`
        <vaadin-tab>${displayName}</vaadin-tab>
      `;
    }
  }

  _renderBottomContent(){
    if(this.show){
      return html`<div class="bottom-content">${unsafeHTML(this._content)}</div>`;
    }
  }

  _selectedChanged(e) {
    if(this._tabItems){
      var i = e.detail.value;
      if(this._tabItems){
        var webcomponent = this._tabItems[i].webcomponent;
        
        import('./' + webcomponent + '.js');
        this._content = "<" + webcomponent + "></" + webcomponent + ">"
      }
    }
  }

  _renderOpenCloseButton(icon, text){
    if((this.show && text === "Close") || (!this.show && text === "Open")){
      return html`
          <div class="open-close-button horizontal-flex" @click="${this._switchOpenClose}">
            <vaadin-icon class="open-close-icon" icon="font-awesome-solid:${icon}"></vaadin-icon> ${text}
          </div>`;
    }
  }

  _switchOpenClose(e){
    if(this.show){
      this._close(e);
    }else{
      this._open(e);
    }
  }

  _open(e) {
    this.show = true;
    this.height = 250; // TODO: Remember
  }

  _close() {
    this.show = false;
    this.height = 30;
  }

  _getTabDisplayName(tabName){
    tabName = tabName.substring(tabName.indexOf('-') + 1); 
    tabName = tabName.charAt(0).toUpperCase() + tabName.slice(1);
    return tabName.replaceAll('-', ' ');
  }

}
customElements.define('qwc-footer', QwcFooter);