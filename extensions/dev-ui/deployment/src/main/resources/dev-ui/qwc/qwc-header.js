import { LitElement, html, css} from 'lit';
import '@vaadin/tabs';
import '@qwc/quarkus-version';

/**
 * This component represent the Dev UI Header
 */
export class QwcHeader extends LitElement {
    static extensions = new Map();
    static extensionNames = new Map();

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

        .logo-text-version {
            width: 100%;
            height: 100%;
            display: flex;
            flex-direction: column;
            justify-content: flex-start;
        }
        
        `;

    static properties = {
        _title: {state:true},
        _links: {state:true},
        _selectedComponent: {state: true}
    };
    
    constructor(){
        super();
        
        document.addEventListener('extensions', (e) => { 
          var extensions = e.detail;

          extensions.forEach((extension) => { 
            extension.links.forEach((link) => {
                if(link.component){
                    let key = link.component.slice(0, -3);
                    QwcHeader.extensions.set(key, extension.links);
                    QwcHeader.extensionNames.set(key, extension.name);
                }
            });
           });  
        }, false);
    
      }

    render() {
        return html`
        <div class="top-bar">
            <div class="logo-title">
                <div class="logo-reload-click">
                    <img src="img/light_icon.svg" @click="${this._reload}"></img> 
                    <div class="logo-text-version">
                        <span class="logo-text" @click="${this._reload}">Dev UI</span>
                        <qwc-quarkus-version></qwc-quarkus-version>
                    </div>
                </div>
                <span class="title">${this._title}</span>
            </div>
            ${this._renderTabs()}
        </div>
        `;
    }

    connectedCallback() {
        super.connectedCallback();
        addEventListener('switchPage', (e) => { 

            // Set the Title
            if(QwcHeader.extensionNames && QwcHeader.extensionNames.size > 0 && QwcHeader.extensionNames.has(e.detail.component)){
                this._title = QwcHeader.extensionNames.get(e.detail.component);
            } else {
                this._title = e.detail.display;
            }
            
            // Add the links
            if(QwcHeader.extensions && QwcHeader.extensions.size > 0 && QwcHeader.extensions.has(e.detail.component)){
                this._links = QwcHeader.extensions.get(e.detail.component);
                this._selectedComponent = e.detail.component;
            }else {
                this._links = null;
                this._selectedComponent = null;
            }

          }, false);
    }

    _renderTabs(){
        if(this._links){
            let js = this._selectedComponent + ".js";
            const index = this._links.findIndex(object => {
                return object.component === js;
            });

            return html`
            <div class="submenu">
                <vaadin-tabs theme="minimal" selected="${index}">
                    ${this._links.map(link => 
                        html`<vaadin-tab><a href="${link.path}">${link.displayName}</a></vaadin-tab>`
                    )}
                </vaadin-tabs>
            </div>`;
        }
    }

    disconnectedCallback() {
        super.disconnectedCallback();
    }

    _reload(e){
        console.log("TODO: Reload");
    }
}

customElements.define('qwc-header', QwcHeader);