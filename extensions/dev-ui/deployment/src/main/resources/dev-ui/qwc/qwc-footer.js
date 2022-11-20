import { LitElement, html, css} from 'lit';

import '@vaadin/tabs';
import '@vaadin/icon';

/**
 * This component shows the Bottom Drawer
 */
export class QwcFooter extends LitElement {
    static styles = css`
        :host{
            display: flex;
            flex-direction: column;
            min-height: 38px;
            padding-top: 1px;
        }
        .footer {
            background: rgb(44, 44, 44);
            margin-right: 5px;
        }
        vaadin-tabs {
            background: rgb(44, 44, 44);
            color: white;
            border-radius: 15px 15px 0px 0px;
        }
        .footerContent {
            overflow: scroll;
            margin-right: 5px;
            padding: 10px;
        }
  `;

    static properties = {
        _content: {state: true},
        _display: {state: true},
        _leftBorder: {state: true},
        _slots: {state: false},
    };

    constructor() {
        super();
        this._display = "none";
        this._content = "Initial content";
        this._leftBorder = "0";
        this._height = "0px";
        this._slots = new Map();        
    }

    
    render() {
        return html`
            <div class="footer" style="border-radius: ${this._leftBorder}px 15px 0px 0px;">
                ${this._renderTabBar()}
                <div class="footerContent" style="display: ${this._display};height:${this._height}">
                    <slot @slotchange=${this._handleSlotChange}></slot>
                </div>
            </div>
        `
    }
    
    _renderTabBar(){
        return html`
            <vaadin-tabs theme="small" @selected-changed="${this._selectedChanged}" @dblclick="${this._doubleClicked}">
                ${this._renderTabs()}
            </vaadin-tabs>
        `;
    }

    _renderTabs(){
        if (this._display !== "none") {
            let titles = Array.from( this._slots.keys() );
            return html`
                ${titles.map((title) =>
                    html`<vaadin-tab style="color: grey;">${title}</vaadin-tab>`
                )}`;
        }
    }
    
    _handleSlotChange(e){
        const childNodes = e.target.assignedElements();
        childNodes.forEach(c => {
            this._slots.set(c.title,c);
        });
    }

    _doubleClicked(e) {
        if (this._display === "none") {
            this._display = "block";
            this._leftBorder = "15";
            this._height = "250px";
        } else {
            this._display = "none";
            this._leftBorder = "0";
            this._height = "0px";
        }
    }

    _selectedChanged(e) {
        let index = e.detail.value;
        let selected = Array.from(this._slots.keys())[index];
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