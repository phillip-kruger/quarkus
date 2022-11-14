import { LitElement, html, css} from 'lit';
import { RouterController } from 'router-controller';
import '@vaadin/tabs';

/**
 * This component represent the Dev UI Header
 */
export class QwcHeader extends LitElement {
    static styles = css`
        .top-bar {
            height: 70px;
            display: flex;
            align-items: center;
            flex-direction: row;
            justify-content: space-between;
        }
        .logo-title {
            display: flex;
            align-items: center;
            flex-direction: row;
        }
        .top-bar img {
            height: 45px;
            padding: 8px;
        }

        .logo-right-actions {
            display: flex;
            align-items:center;
            padding-right: 10px;
        }
        
        .logo-reload-click {
            cursor: pointer;
            display: flex;
            align-items:center;
            font-size: xx-large;
            color: #20446B;
        }

        .logo-reload-click:hover {
            filter: brightness(90%);
        }

        .title {
            display: flex;
            align-items:center;
            font-size: x-large;
            color: #6d7279;
            padding-left: 100px;
        }
        .submenu {
            padding-left: 10px;
            display: flex;
            flex-direction: row;
            justify-content: center;
        }
        
        .logo-text {
            line-height: 1;
        }
    
        .app-info {
            font-size: small;
            color: #6d7279;
            padding-right: 10px;
        }
        `;

    static properties = {
        _title: {state: true},
        _rightSideNav: {state: true},
        applicationName: {type: String},
        applicationVersion: {type: String},
    };

    constructor() {
        super();
        this._title = "Extensions";
        this._rightSideNav = "";
        
        window.addEventListener('vaadin-router-location-changed', (event) => {
            this._updateHeader(event);
        });
    }

    render() {
        return html`
        <div class="top-bar">
            <div class="logo-title">
                <div class="logo-reload-click">
                    <img src="img/light_icon.svg" @click="${this._reload}"></img> 
                    <span class="logo-text" @click="${this._reload}">Dev UI</span>
                </div>
                <span class="title">${this._title}</span>
            </div>
            ${this._rightSideNav}
        </div>
        `;
    }

    _updateHeader(event){
        var pageDetails = RouterController.parseLocationChangedEvent(event);
        
        this._title = pageDetails.title;
        var subMenu = pageDetails.subMenu;

        if(subMenu){
            this._rightSideNav = html`
                            <div class="submenu">
                                <vaadin-tabs selected="${subMenu.index}">
                                    ${subMenu.links.map(link =>
                                        html`<vaadin-tab><a href="${link.path}">${link.name}</a></vaadin-tab>`
                                    )}
                                </vaadin-tabs>
                            </div>`;
        }else{
            this._rightSideNav = html`<div class="app-info">${this.applicationName} ${this.applicationVersion}</div>`; // default
        }
    }

    _reload(e) {
        console.log("TODO: Reload");
    }
}
customElements.define('qwc-header', QwcHeader);