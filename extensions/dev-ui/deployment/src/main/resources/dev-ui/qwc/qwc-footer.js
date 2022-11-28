import { LitElement, html, css} from 'lit';

import '@vaadin/tabs';
import '@vaadin/icon';
import '@vaadin/menu-bar';
import 'qwc/qwc-ws-status.js';

/**
 * This component shows the Bottom Drawer
 * 
 */
export class QwcFooter extends LitElement {
        
    static styles = css`
        #footer {
            background: rgb(44, 44, 44);
            margin-right: 5px;
            margin-left: 5px;
            border-radius: 15px 15px 0px 0px;
            display: flex;
            flex-direction: column;
            min-height: 38px;
            overflow: hidden;
        }
        vaadin-tabs {
            background: rgb(44, 44, 44);
            color: white;
            border-radius: 15px 0px 0px 0px;
            max-width: 100%; 
            width: 45%;
            overflow: hidden;
        }
    
        vaadin-menu-bar {
            background: rgb(44, 44, 44);
            color: white;
            border-radius: 0px 15px 0px 0px;
            max-width: 100%; 
            width: 45%;
        }
    
        .footerContent {
            overflow: scroll;
            padding-left: 5px;
            padding-right: 5px;
            margin-bottom: 5px;
            margin-top: 5px;
        }
        
        .footerHeader {
            display: flex;
            justify-content: space-between;
            background: rgb(44, 44, 44);
            border-radius: 0px 15px 0px 0px;
            align-items: center;
            height: 38px;
        }
        .resizeIcon {
            cursor: ns-resize;
            font-size: x-small;
            color: grey;
            width: 100px;
            text-align: center;
        }
        .openIcon {
            cursor: pointer;
            color: #4695eb;
            margin-left: 15px;
            font-size: small;
        }
        
        .openIcon:hover {
            color: #ff004a;
        }
    
        .resizeIcon:hover {
            color: #4695eb;
        }
  `;

    static properties = {
        _selectedTabIndex: {state: false},
        _height: {state: true},
        _show: {state: true},
        _slots: {state: true},
        _controlButtons: {state: false},
        _originalHeight: {state: false},
        _originalMouseY: {state: false},
    };

    constructor() {
        super();
        this._selectedTabIndex = 0;
        this._height = 250;
        this._show = false;
        this._slots = new Map();
        this._controlButtons = [];
        this._originalHeight = 38;
        this._originalMouseY = 0;
    }

    render() {
          return this._show
            ? html`<div id="footer" style="height:${this._height}px">
                ${this._renderFooter()}
            </div>`
            : html`<div id="footer">
                ${this._renderFooter()}
            </div>`;
    }
    
    _renderFooter(){
        return html`<div class="footerHeader" @dblclick="${this._doubleClicked}">
                    ${this._renderTabBar()}
                    ${this._renderResizeButtons()}
                    ${this._renderControls()}
                    <qwc-ws-status></qwc-ws-status>
                </div>
                ${this._renderLog()}
        `;
    }
    
    _renderLog(){
        if (this._show) {
            return html `
                <div class="footerContent">
                    <slot name="log" @slotchange=${this._handleLogSlotChange}></slot>
                </div>`;
        }
    }
    
    _renderTabBar(){
        
        var arrow = "up";
        if(this._show){
            arrow = "down";
        }
        
        return html`
            <vaadin-icon class="openIcon" icon="font-awesome-solid:chevron-${arrow}" 
                                            @click="${this._doubleClicked}">
            </vaadin-icon>
            ${this._renderTabs()}
        `;
    }

    _renderResizeButtons(){
        if (this._show) {
            return html`
                <div class="resizeIcon" @mousedown="${this._mousedown}">
                    <vaadin-icon icon="font-awesome-solid:up-down"></vaadin-icon>
                </div>
            `;
        }
    }

    _renderControls(){
        if (this._show) {
            return html`
                <vaadin-menu-bar theme="end-aligned" .items="${this._controlButtons}" @item-selected="${this._controlButtonClicked}"></vaadin-menu-bar>
            `;
        }
    }

    _renderTabs(){
        if (this._show) {
            let titles = Array.from( this._slots.keys() );
            return html`<vaadin-tabs theme="small" @selected-changed="${this._selectedChanged}">
                ${titles.map((title) =>
                    html`<vaadin-tab style="color: grey;">${title}</vaadin-tab>`
                )}
            </vaadin-tabs>`;
        }
    }
    
    _handleLogSlotChange(e){
        const childNodes = e.target.assignedElements();
        childNodes.forEach(c => {
            this._slots.set(c.title,c);
        });
        this.requestUpdate();
    }

    _mousedown(e){
        this._originalHeight = this._height;
        this._originalMouseY = e.y;
        this.addEventListener('mousemove', this._mousemove);
        this.addEventListener('mouseup', this._mouseup);
    }

    _mousemove(e){
        const height = this._originalHeight - (e.y - this._originalMouseY);
        this._height = height;
        
        if(this._height<=70){
            this._mouseup();
            this._doubleClicked(e);
            this._height = this._originalHeight;
        }
    }

    _mouseup(){
        this.removeEventListener('mousemove', this._mousemove);
        this.removeEventListener('mouseup', this._mouseup);
    }

    _doubleClicked(e) {
        if (!this._show) {
            this._show = true;
        } else {
            this._show = false;
        }
    }

    _selectedChanged(e) {
        this._selectedTabIndex = e.detail.value;
        let selected = Array.from(this._slots.keys())[this._selectedTabIndex];
        this._slots.forEach(function(value, key) {
            if(key === selected){
                if(value.hasAttribute("hidden")){
                    value.removeAttribute("hidden");
                }
            }else{
                value.setAttribute("hidden", '');
            }
        });
    }
    
}
customElements.define('qwc-footer', QwcFooter);