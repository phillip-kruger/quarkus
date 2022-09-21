import { LitElement, html, css} from 'lit';
import { until } from 'lit/directives/until.js';

import { JsonRpcController } from 'controller/jsonrpc-controller.js';
import { RouterController } from 'controller/router-controller.js';

import '@vaadin/icon';

/**
 * This component represent the Dev UI left menu
 * It loads the menuitems from the the server and dynamicallt add the routes and import the relevant components
 */
export class QwcMenu extends LitElement {
    jsonRpcController = new JsonRpcController(this);
    routerController = new RouterController(this);

    static styles = css`

            .menu {
                height: 100%;
                display: flex;
                flex-direction: column;
            }
            
            .menuSizeControl {
                align-self: flex-end;
                cursor: pointer;
                color: #E8E8E8;
                height: 60px;
                width: 30px;
                padding-top:30px;
            }
            
            .menuSizeControl:hover {
                color: #ff004a;
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
                border-left: 5px solid #ff004a;
                color: #ff004a;
			    background-color: #f8fafc;
            }

            .item-text:hover{
                color: #ff004a;
            }

            .selected {
                border-left: 5px solid #4695EB;
                color: #20446B;
                cursor: default;
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
        this._selectedPage = "qwc-extensions";
        this._selectedPageLabel = this.routerController.getPageDisplayName(this._selectedPage);
        this._menuItems = null;
        if(storedState && storedState === "small"){
            this._smaller();
        }else{
            this._larger();
        }
    }
    
    connectedCallback() {
        super.connectedCallback();

        addEventListener('switchPage', (e) => { 
            if(this._componentIsInMenu(e.detail.component)){
                this._selectedPage = e.detail.component;
                this._selectedPageLabel = e.detail.display;
            }
          }, false);
    }

    onJsonRpcResponse(result){
        var pagename = result[0].webcomponent;
        
        this.routerController.addRoute("", pagename);

        result.forEach(menuItem => {
            var pagename = menuItem.webcomponent;
            var defaultSelection = menuItem.defaultSelection;
            var componentLink = './' + pagename + '.js';
            import(componentLink);
            var page = this.routerController.getPageRef(pagename);

            this.routerController.addRoute(page, pagename, defaultSelection);
        });

        this._menuItems = result;
    }

    render() {
        return html`${until(this._renderJsonRpcResponse(), html`<span>Loading...</span>`)}`;
    }

    _componentIsInMenu(component){
        
        let isInMenu = false;
        this._menuItems.forEach(menuItem => {
            if(component === menuItem.webcomponent){
                isInMenu = true;
            }
        });
        return isInMenu;
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
            displayName = this.routerController.getPageDisplayName(pagename);
        }
        let base = this.routerController.getBasePath();
        let pageRef = base + '/' + this.routerController.getPageRef(pagename);
        const selected = this._selectedPage == pagename;
        let classnames = "item";
        if(selected){
            classnames = "item selected";
        }

        return html`
        <a class="${classnames}" href="${pageRef}">
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
        this._width = 50;
        localStorage.setItem('qwc-menu-state', "small");
    }

    _larger() {
        this._show = true;
        this._width = 250;
        localStorage.setItem('qwc-menu-state', "large");
    }
}

customElements.define('qwc-menu', QwcMenu);