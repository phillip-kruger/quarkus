import { LitElement, html, css} from 'lit';
import { menuItems } from 'internal-data';
import { RouterController } from 'router-controller';
import '@vaadin/icon';
import 'qwc/qwc-quarkus-version.js';
import 'qwc/qwc-ws-status.js';

/**
 * This component represent the Dev UI left menu
 * It creates the menuItems during build and dynamically add the routes and import the relevant components
 */
export class QwcMenu extends LitElement {
    
    static styles = css`
            .left {
                height: 100%;
                display: flex;
                flex-direction: column;
                justify-content: space-between;
            }

            .bottom {
                display: flex;
                justify-content: space-between;
                align-items: center;
                height: 46px;
                background: rgb(44, 44, 44);
                height: 38px;
                border-radius: 15px 0px 0px;
                margin-left: 5px;
            }

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
                color: #54789F;
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

            qwc-quarkus-version {
                padding-bottom: 5px;
                width: 100%;
                text-align: center;
            }
        `;

    static properties = {
        _show: {state: true},
        _selectedPage: {attribute: false},
        _selectedPageLabel: {attribute: false},
        _menuItems: {state: true},
        _width: {state: true},
        version: {type: String},
    };
    
    constructor() {
        super();
        this._menuItems = menuItems;
        
        this._menuItems.forEach(menuItem => {
            var pagename = menuItem.webcomponent;
            var defaultSelection = menuItem.defaultSelection;
            var componentLink = './' + pagename + '.js';
            import(componentLink);
            RouterController.addMenuRoute(pagename, defaultSelection);
        });
        
        window.addEventListener('vaadin-router-location-changed', (event) => {
            this._updateSelection(event);
        });

        const storedState = localStorage.getItem("qwc-menu-state");
        this._selectedPage = "qwc-extensions"; // default
        this._selectedPageLabel = "Extensions"; // default
        if(storedState && storedState === "small"){
            this._smaller();
        }else{
            this._larger();
        }
    }
    
    _updateSelection(event){
        var pageDetails = RouterController.parseLocationChangedEvent(event);
        this._selectedPage = pageDetails.component;
        this._selectedPageLabel = pageDetails.title;
    }

    render() {
        if(this._menuItems){
            return html`
                <div class="left">
                    <div class="menu" style="width: ${this._width}px;">
                        ${this._menuItems.map((menuItem) =>
                            html`${this._renderItem(menuItem.icon, menuItem.webcomponent)}`
                        )}
                        ${this._renderIcon("chevron-left", "smaller")}
                        ${this._renderIcon("chevron-right", "larger")}
                    </div>
                    
                    ${this._renderBottom()}
                </div>`;
        }
    }

    _renderBottom(){
        return html`<div class="bottom">
            <qwc-ws-status></qwc-ws-status>
            ${this._renderVersion()}
        </div>`;
    }

    _renderVersion(){
        if(this._show){
            return html`<qwc-quarkus-version version="${this.version}"></qwc-quarkus-version>`;
        }
    }

    _renderItem(pageicon, pagename){
        let displayName = "";
        if(this._show){
            displayName = RouterController.displayMenuItem(pagename);
        }
        let pageRef = RouterController.pageRefWithBase(pagename);
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

    _componentIsInMenu(component){
        
        let isInMenu = false;
        this._menuItems.forEach(menuItem => {
            if(component === menuItem.webcomponent){
                isInMenu = true;
            }
        });
        return isInMenu;
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