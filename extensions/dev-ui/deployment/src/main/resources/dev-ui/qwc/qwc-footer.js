import { LitElement, html, css} from 'lit';
import {LogController} from 'log-controller';

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
            overflow: hidden;
        }
    
        vaadin-menu-bar {
            background: rgb(44, 44, 44);
            color: white;
            border-radius: 0px 15px 0px 0px;
            max-width: 100%; 
            padding-right: 10px;
            --lumo-size-m: 10px;
            --lumo-space-xs: 0.5rem;
            width: fit-content;
        }
    
        .footerContent {
            overflow-y: scroll;
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
            cursor: row-resize;
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
        .controls {
            display: flex;
            flex-direction: row-reverse;
            width: 45%;
        }
        .tabs{
            display: flex;
            width: 45%;
            align-items: center;
        }
  `;

    static properties = {
        _height: {state: true},
        _visibility: {state: true},
        _slots: {state: true},
        _controlButtons: {state: false},
        _selectedTabIndex: {state: false},
        _originalHeight: {state: false},
        _originalMouseY: {state: false},
        _isOpeningOrClosing: {state: false},
    };

    constructor() {
        super();
        this._height = 250;
        this._visibility = "hidden";
        this._slots = new Map();
        this._controlButtons = [];
        this._selectedTabIndex = 0;
        this._originalHeight = 38;
        this._originalMouseY = 0;
        this._isOpeningOrClosing = false;
    }

    render() {
        return this._visibility === "visible"
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
                </div>
                ${this._renderLog()}
        `;
    }
    
    _renderLog(){
        return this._visibility === "visible"
            ? html`<div class="footerContent">
                <slot name="log" @slotchange=${this._handleLogSlotChange}></slot>
            </div>`
            : html`<div class="footerContent" style="display:none">
                <slot name="log" @slotchange=${this._handleLogSlotChange}></slot>
            </div>`;
    }
    
    _renderTabBar(){
        
        var arrow = "down";
        if(this._visibility === "hidden"){
            arrow = "up";
        }
        
        return html`<div class="tabs">
                <qwc-ws-status></qwc-ws-status>
                <vaadin-icon class="openIcon" icon="font-awesome-solid:chevron-${arrow}" 
                                            @click="${this._doubleClicked}">
                </vaadin-icon>
                ${this._renderTabs()}
            </div>    
        `;
    }

    _renderResizeButtons(){
        return html`
            <div class="resizeIcon" @mousedown="${this._mousedown}" style="visibility:${this._visibility}">
                <vaadin-icon icon="font-awesome-solid:up-down"></vaadin-icon>
            </div>
        `;
    }

    _renderControls(){
        return html`<div class="controls"><vaadin-menu-bar 
                        .items="${this._controlButtons}" 
                        @item-selected="${this._controlButtonClicked}" 
                        style="visibility:${this._visibility}">
                    </vaadin-menu-bar></div>`;
        
    }

    _renderTabs(){
        let titles = Array.from( this._slots.keys() );
        return html`<vaadin-tabs 
                        id="footerTabs" 
                        theme="small" 
                        @selected-changed="${this._selectedChanged}" 
                        style="visibility:${this._visibility}">
            ${titles.map((title) =>
                html`<vaadin-tab style="color: grey;">${title}</vaadin-tab>`
            )}
        </vaadin-tabs>`;
    }
    
    _handleLogSlotChange(e){
        if (this._slots.size === 0) {
           const childNodes = e.target.assignedElements();
            childNodes.forEach(c => {
                this._slots.set(c.title,c);
            });
            this._programmaticallySelectedChanged(this._selectedTabIndex);
            this.requestUpdate();
        }
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
        if(e.target.tagName.toLowerCase() !== "vaadin-menu-bar"){
            if(this._visibility === "hidden"){
                this._visibility = "visible";
            }else {
                this._visibility = "hidden"; 
            }   
            this._isOpeningOrClosing = true;
        }
    }

    _programmaticallySelectedChanged(index){
        var footerTabs = this.shadowRoot.getElementById('footerTabs');
        footerTabs.selected = index;
    }

    _selectedChanged(e) {
        var index = e.detail.value;
        if(this._isOpeningOrClosing){
            // Ignore the selection change on open/close
            this._isOpeningOrClosing = false;
            
            if(this._selectedTabIndex > 0){
                this._programmaticallySelectedChanged(this._selectedTabIndex);
            }
        }else{
        
            let selected = Array.from(this._slots.keys())[index];
            if(selected){
                this._selectedTabIndex = index;
                this._slots.forEach(function(value, key) {
                    if(key === selected){
                        if(value.hasAttribute("hidden")){
                            value.removeAttribute("hidden");
                        }
                    }else{
                        value.setAttribute("hidden", '');
                    }
                });
                this._controlButtons = LogController.getItemsForTab(selected);
            }
        }
    }
    
//    _scrollToBottom(){
//        if (this._followLog) {
//            const scrollingLog = this.shadowRoot.getElementById("footerContent");
//            scrollingLog.scrollIntoView({
//                 behavior: "smooth",
//                 block: "end",
//            });
//        }
//    }
    
    _controlButtonClicked(e){
        LogController.fireCallback(e);
    }
}
customElements.define('qwc-footer', QwcFooter);