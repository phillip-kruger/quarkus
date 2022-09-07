import { LitElement, html, css} from 'https://unpkg.com/lit@2.3.1/index.js?module';
import { until } from 'https://unpkg.com/lit@2.3.1/directives/until.js?module';
import { Router } from 'https://unpkg.com/@vaadin/router@1.7.4/dist/vaadin-router.min.js?module';
import { JsonRpcController } from './../controller/jsonrpc-controller.js';

import 'https://unpkg.com/@vaadin/icon@23.1.6/vaadin-icon.js?module';

/**
 * This component represent the Dev UI left menu
 * It loads the menuitems from the the server and dynamicallt add the routes and import the relevant components
 */
export class QwcMenu extends LitElement {
    jsonRpcController = new JsonRpcController(this);
    router = new Router(document.querySelector('qwc-page'));

    static styles = css`

            .menu {
                height: 100%;
                display: flex;
                flex-direction: column;
                padding-top: 20px;
            }
            
            .menuSizeControl {
                align-self: flex-end;
                cursor: pointer;
                color: #E8E8E8;
                height: 30px;
                width: 30px;
            }
            
            .menuSizeControl:hover {
                filter: brightness(50%);
            }

            .item {
                display: flex;
                flex-direction: row;
                align-items:center;
                padding-top: 10px;
                padding-bottom: 10px;
                padding-left: 5px;
                gap: 10px;
                cursor: pointer;
                border-left: 5px solid transparent;
                color: #4695EB;
                height:30px;
                text-decoration: none;
            }
            
            .item:hover{
                border-left: 5px solid #4695EB;
            }

            .item-text:hover{
                filter: brightness(50%);
            }

            .selected {
                border-left: 5px solid #4695EB;
                cursor: default;
                border-bottom: 1px solid rgba(28,110,164,0.05);
                border-top: 1px solid rgba(28,110,164,0.05);
                background: #eff7ff;
                background: -moz-linear-gradient(left, #eff7ff 0%, #FFFFFF 100%);
                background: -webkit-linear-gradient(left, #eff7ff 0%, #FFFFFF 100%);
                background: linear-gradient(to right, #eff7ff 0%, #FFFFFF 100%);
            }
        `;

    static properties = {
        _show: {state: true},
        _selectedPage: {attribute: false},
        _selectedPageLabel: {attribute: false},
        _menuItems: {state: true},
        _width: {state: true}
    };
    
    constructor() {
        super();
        
        this.jsonRpcController.request("getMenuItems");
        const storedState = localStorage.getItem("qwc-menu-state");
        this._selectedPage = "qwc-extensions"; // TODO: Remember ?
        this._selectedPageLabel = this._getPageDisplayName(this._selectedPage);
        this._menuItems = null;
        if(storedState && storedState === "small"){
            this._smaller();
        }else{
            this._larger();
        }
    }
    
    onJsonRpcResponse(result){
        
        var routes = [];
        var pagename = result[0].webcomponent;
        var route = {};
        route.path = "/";
        route.component = pagename;
        routes.push({...route});   

        result.forEach(menuItem => {
            var pagename = menuItem.webcomponent;

            import('./' + pagename + '.js');
            
            var route = {};
            route.path = "/" + this._getPageRef(pagename);
            route.component = pagename;
            routes.push({...route});   
        });

        this.router.setRoutes(routes);
        this._menuItems = result;
    }

    render() {
        return html`${until(this._renderJsonRpcResponse(), html`<span>Loading...</span>`)}`;
    }

    _renderJsonRpcResponse(){
        if(this._menuItems){
            return html`
                <div class="menu" style="width: ${this._width}px;">
                    ${this._menuItems.map((menuItem) =>
                        html`${this._renderItem(menuItem.icon, menuItem.webcomponent)}`
                    )}
                
                    ${this._renderIcon("chevron-left", "smaller")}
                    ${this._renderIcon("chevron-right", "larger")}
            </div>`;
        }
    }

    _renderItem(pageicon, pagename){
        let displayName = "";
        if(this._show){
            displayName = this._getPageDisplayName(pagename);
        }
        let pageRef = this._getPageRef(pagename);
        const selected = this._selectedPage == pagename;
        let classnames = "item";
        if(selected){
            classnames = "item selected";
        }
        
        return html`
        <a class="${classnames}" href="/${pageRef}" @mouseup="${this._showPage}">
            <vaadin-icon icon="font-awesome-solid:${pageicon}"></vaadin-icon>
            <span class="item-text" data-page="${pagename}">${displayName}</span>
        </a>
        `;        
    }

    _renderIcon(icon, action){
        if((action == "smaller" && this._show) || (action == "larger" && !this._show)){
            return html`
                <vaadin-icon class="menuSizeControl" icon="font-awesome-solid:${icon}" @click="${this._changeMenuSize}" data-action="${action}"></vaadin-icon>
            `;
        }
    }

    _showPage(e){
        this._selectedPage = e.target.dataset.page;
    }

    _changeMenuSize(e){
        if(e.target.dataset.action === "smaller"){
            this._smaller();
        }else{
            this._larger();
        }
        this.requestUpdate();
    }

    _smaller() {
        this._show = false;
        this._width = 70;
        localStorage.setItem('qwc-menu-state', "small");
    }

    _larger() {
        this._show = true;
        this._width = 250;
        localStorage.setItem('qwc-menu-state', "large");
    }

    _getPageDisplayName(pageName){
        pageName = pageName.substring(pageName.indexOf('-') + 1); 
        pageName = pageName.charAt(0).toUpperCase() + pageName.slice(1);
        return pageName.replaceAll('-', ' ');
    }

    _getPageRef(pageName){
        return pageName.substring(pageName.indexOf('-') + 1); 
    }
}

customElements.define('qwc-menu', QwcMenu);