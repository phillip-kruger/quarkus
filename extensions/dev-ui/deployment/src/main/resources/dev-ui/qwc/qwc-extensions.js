import { LitElement, html, css} from 'lit';
import { until } from 'lit/directives/until.js';

import { JsonRpcController } from 'jsonrpc-controller';
import { RouterController } from 'router-controller';
import '@qwc/extension';
import '@qwc/extension-link';

/**
 * This component create cards of all the extensions
 */
export class QwcExtensions extends LitElement {
    static methodName = "getExtensions";
    jsonRpcController = new JsonRpcController(this);
    routerController = new RouterController(this);

    static styles = css`
        .hr {
            border-bottom: 1px solid rgba(28,110,164,0.1);
        }
        .grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
            gap: 1rem;
            padding-left: 5px;
            width: 99%;
        }
    `;

    static properties = {
        _extensions: {state: true}
    };

    connectedCallback() {
        super.connectedCallback();
        this.jsonRpcController.request(QwcExtensions.methodName);
    }

    onJsonRpcResponse(result){
        this._extensions = result;
    }

    render() {
        return html`${until(this._renderJsonRpcResponse(), html`<span>Loading...</span>`)}`;
    }

    _renderJsonRpcResponse(){
        if(this._extensions){
            let active = this._extensions.active;
            let inactive = this._extensions.inactive;

      
            let base = this.routerController.getBasePath();
            active.forEach(activeExtension => {
            var extension = activeExtension.name.replace(/\s+/g, '-').toLowerCase();
            activeExtension.links.forEach(componentLink => {
                if(!componentLink.path){ // If path exist it's an external link, so no need to register with the router
                    var componentRef = './../' + extension + '/' + componentLink.component;
                    import(componentRef);
                    var pagename = componentLink.component.toLowerCase().slice(0, componentLink.component.lastIndexOf('.'));
                    var page = extension + "-" + componentLink.displayName.replace(/\s+/g, '-').toLowerCase();
                    this.routerController.addRoute(page, pagename);
                    componentLink['path'] =  base + '/' + page;
                }
            });
      });

      // Fire event that contains all active extensions
      const event = new CustomEvent('extensions', { 
            detail: active 
      });
      document.dispatchEvent(event);

      return html`<div class="page">
          <div class="grid">
            ${active.map(extension => 
              html`
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

                    ${extension.links.map(link => html`
                        
                        <qwc-extension-link slot="link" 
                                          extensionName="${extension.name}"
                                          iconName="${link.iconName}"
                                          displayName="${link.displayName}"
                                          label="${link.label}"
                                          path="${link.path}"
                                          webcomponent="${link.component}" >
                        </qwc-extension-link>
                    `)}

                </qwc-extension>

            `)}
            ${inactive.map(extension => html`
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
            `)}
          </div>
        </div>`;
    }
  }

}
customElements.define('qwc-extensions', QwcExtensions);