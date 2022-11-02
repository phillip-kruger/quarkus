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

    constructor() {
        super();
        this._title = "Extensions";
        this._rightSideNav = "";
        
        window.addEventListener('vaadin-router-location-changed', (event) => {
            this._updateHeader();
        });
    }
    
    _updateHeader(){
        
        var title = "Extensions"; // default
        var rightSideNav = html`<div class="app-info">${this.applicationName} ${this.applicationVersion}</div>`; // default
        
        
        var location = RouterController.router.location;
        if(location.route){
            var currentRoutePath = location.route.path;
            if(currentRoutePath.includes('/dev-ui/')){
                var currentPage = currentRoutePath.substring(currentRoutePath.indexOf('/dev-ui/') + 8);
                if(currentPage.includes('/')){
                    // This is a submenu
                    var extension = currentPage.substring(0, currentPage.lastIndexOf("/"));
                    title = this._formatTitle(extension);
                    
                    const links = [];
                    var startOfPath = currentRoutePath.substring(0, currentRoutePath.lastIndexOf("/"));
                    var routes = RouterController.router.getRoutes();

                    var counter = 0;
                    var index = 0;
                    routes.forEach((route) => {
                        if(route.path.startsWith(startOfPath)){
                            links.push(route);
                            if(route.name === location.route.name){
                                index = counter;
                            }
                            counter = counter + 1;
                        }
                    });
                    
                    if (links && links.length > 1) {
                        rightSideNav = html`
                            <div class="submenu">
                                <vaadin-tabs selected="${index}">
                                    ${links.map(link =>
                                        html`<vaadin-tab><a href="${link.path}">${link.name}</a></vaadin-tab>`
                                    )}
                                </vaadin-tabs>
                            </div>`;
                    }
                }else{
                    // This is a main section
                    title = this._formatTitle(currentPage);
                }
            }
        }
        
        this._title = title;
        this._rightSideNav = rightSideNav;
    }

    _formatTitle(title) {
        title = title.charAt(0).toUpperCase() + title.slice(1);
        return title.split("-").join(" ");
    }

    _reload(e) {
        console.log("TODO: Reload");
    }
}
customElements.define('qwc-header', QwcHeader);