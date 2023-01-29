import { LitElement, html, css} from 'lit';
import { RouterController } from 'router-controller';
import { themeState } from 'theme-state';
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
        .right-bar {
            display: flex;
            justify-content: flex-end;
            align-items: center;
        }
    
        .logo-title {
            display: flex;
            align-items: center;
            flex-direction: row;
        }
        .top-bar svg {
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
        }

        .logo-reload-click:hover {
            filter: brightness(90%);
        }

        .title {
            display: flex;
            align-items:center;
            font-size: x-large;
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
            padding-right: 10px;
        }
    
        .dayNightIcon {
            padding-right: 10px;
            cursor: pointer;
            color: #d0d02f;
        }
        vaadin-icon {
            vertical-align: unset;
        }
        `;

    static properties = {
        _title: {state: true},
        _rightSideNav: {state: true},
        _dayNightIcon: {state: true},
        _quarkusBlue: {state: true},
        _quarkusRed: {state: true},
        _quarkusCenter: {state: true},
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

    connectedCallback() {
        super.connectedCallback();
        this._setTheme(themeState.getCurrentTheme());
    }

    render() {
        return html`
        <div class="top-bar">
            <div class="logo-title">
                <div class="logo-reload-click">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024"><defs><style>.cls-1{fill:${this._quarkusBlue};}.cls-2{fill:${this._quarkusRed};}.cls-3{fill:${this._quarkusCenter};}</style></defs><title>Quarkus</title><polygon class="cls-1" points="669.34 180.57 512 271.41 669.34 362.25 669.34 180.57"/><polygon class="cls-2" points="354.66 180.57 354.66 362.25 512 271.41 354.66 180.57"/><polygon class="cls-3" points="669.34 362.25 512 271.41 354.66 362.25 512 453.09 669.34 362.25"/><polygon class="cls-1" points="188.76 467.93 346.1 558.76 346.1 377.09 188.76 467.93"/><polygon class="cls-2" points="346.1 740.44 503.43 649.6 346.1 558.76 346.1 740.44"/><polygon class="cls-3" points="346.1 377.09 346.1 558.76 503.43 649.6 503.43 467.93 346.1 377.09"/><polygon class="cls-1" points="677.9 740.44 677.9 558.76 520.57 649.6 677.9 740.44"/><polygon class="cls-2" points="835.24 467.93 677.9 377.09 677.9 558.76 835.24 467.93"/><polygon class="cls-3" points="520.57 649.6 677.9 558.76 677.9 377.09 520.57 467.93 520.57 649.6"/><path class="cls-1" d="M853.47,1H170.53C77.29,1,1,77.29,1,170.53V853.47C1,946.71,77.29,1023,170.53,1023h467.7L512,716.39,420.42,910H170.53C139.9,910,114,884.1,114,853.47V170.53C114,139.9,139.9,114,170.53,114H853.47C884.1,114,910,139.9,910,170.53V853.47C910,884.1,884.1,910,853.47,910H705.28l46.52,113H853.47c93.24,0,169.53-76.29,169.53-169.53V170.53C1023,77.29,946.71,1,853.47,1Z"/></svg>
                    <span class="logo-text" @click="${this._reload}">Dev UI</span>
                </div>
                <span class="title">${this._title}</span>
            </div>
            <div class="right-bar">
                ${this._rightSideNav}
                <span class="dayNightIcon" @click="${this._dayNightToggle}">
                    <vaadin-icon icon="font-awesome-solid:${this._dayNightIcon}"></vaadin-icon>
                </span>
            </div>
        </div>
        `;
    }

    _dayNightToggle(event){
        var name = "light";
        if(this._dayNightIcon === "sun"){
            name = "dark";           
        }
        themeState.theme = name;
        this._setTheme(themeState.changeTo(name));
    }I

    _setTheme(theme){
        this._dayNightIcon = theme.icon;
        this._quarkusBlue = theme.quarkusBlue;
        this._quarkusRed = theme.quarkusRed;
        this._quarkusCenter = theme.quarkusCenter;
        
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