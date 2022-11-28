import { LitElement, html, css} from 'lit';
import { until } from 'lit/directives/until.js';
import { extensions } from 'internal-data';
import { RouterController } from 'router-controller';
import 'qwc/qwc-extension.js';
import 'qwc/qwc-extension-link.js';

/**
 * This component create cards of all the extensions
 */
export class QwcExtensions extends LitElement {
    
    static styles = css`
        .grid {
            display: flex;
            flex-wrap: wrap;
            gap: 20px;
            padding-left: 5px;
            padding-right: 10px;
        }
    `;

    static properties = {
        _extensions: {state: true},
    };

    constructor() {
        super();
        this._extensions = extensions;
        window.addEventListener('vaadin-router-location-changed', (event) => {
            var pageDetails = RouterController.parseLocationChangedEvent(event);
            console.log("||||| component = " + pageDetails.component);
            console.log("||||| path = " + pageDetails.path);
            console.log("||||| name = " + pageDetails.name);
            console.log("||||| title = " + pageDetails.title);
            console.log("||||| submenu = " + pageDetails.subMenu);
            
        });
    }

    render() {
        if(this._extensions){
            let active = this._extensions.active;
            let inactive = this._extensions.inactive;

            active.forEach(activeExtension => {
                activeExtension.pages.forEach(page => {   
                    if(page.embed){ // we need to register with the router
                        import(page.componentRef);
                        RouterController.addExtensionRoute(page);
                    }
                });
        });

        return html`<div class="grid">
            ${active.map(extension => this._renderActive(extension))}            
            ${inactive.map(extension => this._renderInactive(extension))}
          </div>`;
        }
    }
    
    _renderActive(extension){
        return html`
                <qwc-extension 
                    class="active"
                    name="${extension.name}" 
                    description="${extension.description}" 
                    guide="${extension.guide}"
                    namespace="${extension.namespace}"
                    artifact="${extension.artifact}"
                    shortName="${extension.shortName}"
                    keywords="${extension.keywords}"
                    status="${extension.status}"
                    configFilter="${extension.configFilter}"
                    categories="${extension.categories}"
                    unlisted="${extension.unlisted}"
                    builtWith="${extension.builtWith}"
                    providesCapabilities="${extension.providesCapabilities}"
                    extensionDependencies="${extension.extensionDependencies}">

                    ${extension.pages.map(page => html`
                        
                        <qwc-extension-link slot="link" 
                            extensionName="${extension.name}"
                            iconName="${page.icon}"
                            displayName="${page.title}"
                            label="${page.label}"
                            path="${page.id}" 
                            webcomponent="${page.componentLink}" >
                        </qwc-extension-link>
                    `)}

                </qwc-extension>

            `;
    }

    _renderInactive(extension){
        if(extension.unlisted === "false"){
            return html`
                <qwc-extension
                    class="inactive"
                    name="${extension.name}" 
                    description="${extension.description}" 
                    guide="${extension.guide}"
                    namespace="${extension.namespace}"
                    artifact="${extension.artifact}"
                    shortName="${extension.shortName}"
                    keywords="${extension.keywords}"
                    status="${extension.status}"
                    configFilter="${extension.configFilter}"
                    categories="${extension.categories}"
                    unlisted="${extension.unlisted}"
                    builtWith="${extension.builtWith}"
                    providesCapabilities="${extension.providesCapabilities}"
                    extensionDependencies="${extension.extensionDependencies}">
                </qwc-extension>        
            `;
        }
    }

}
customElements.define('qwc-extensions', QwcExtensions);