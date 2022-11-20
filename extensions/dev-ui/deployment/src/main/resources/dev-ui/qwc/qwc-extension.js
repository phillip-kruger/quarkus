import { LitElement, html, css} from 'lit';
import '@vaadin/icon';

/**
 * This component represent one extension
 * It's a card on the extension board
 */
export class QwcExtension extends LitElement {
  
  static styles = css`
        .card {
          height: 100%;
          display: flex;
          flex-direction: column;
          justify-content: space-between;
          border: 1px solid rgba(58,126,199,0.2);
          border-radius: 4px;
          width: 300px;
        }

        .card-header {
          height: 25px;
          display: flex;
          flex-direction: row;
          justify-content: space-between;
          align-items: center;
          padding: 10px 10px;
          background-color: rgba(28,110,164,0.03);
          border-bottom: 1px solid rgba(28,110,164,0.1);
        }

        .card-content {
          color: grey;
          display: flex;
          flex-direction: column;
          justify-content: flex-start;
          padding: 10px 10px;
          height: 100%;
        }

        .card-content slot {
          display: flex;
          flex-flow: column wrap;
          padding-top: 5px;
        }
        
        .card-footer {
          height: 20px;
          padding: 10px 10px;
          color: grey;
          display: flex;
          flex-direction: row;
          justify-content: space-between;
          visibility:hidden;
        }

        .active:hover {
          box-shadow: 0 4px 8px 0 rgba(0,0,0,0.2);
        }

        .active .card-header{
          color: #54789F;
        }
        
        .active:hover .card-footer, .active:hover .guide {
            visibility:visible;
        }

        .inactive .card-header{
          color: grey;
        }
    
        .inactive:hover .card-footer, .inactive:hover .guide {
            visibility:visible;
        }
    
        .guide{
            visibility:hidden;
        }

        .icon {
          font-size: x-small;
          cursor: pointer;
        }
        `;

  static properties = {
    name: {type: String},
    namespace: {type: String},
    description: {type: String},
    guide: {type: String},
    class: {type: String},
    artifact: {type: String},
    shortName: {type: String},
    keywords: {},
    status: {type: String},
    configFilter: {},
    categories: {},
    unlisted: {type: Boolean},
    builtWith: {type: String},
    providesCapabilities: {},
    extensionDependencies: {}
    // Codestart codestart;
  };
  
  render() {
    return html`
      <div class="card ${this.class}">
        ${this._headerTemplate()}
        ${this._contentTemplate()}
        ${this._footerTemplate()}
      </div>`;
  }

  _headerTemplate() {
    return html`<div class="card-header">
      <h4>${this.name}</h4>
        ${this.guide?
          html`<vaadin-icon class="icon guide" icon="font-awesome-solid:book" @click="${this._guide}" title="Go to the ${this.name} guide"></vaadin-icon>`:
          html``
        }
      </div>
  `;

  }

  _contentTemplate() {
    return html`<div class="card-content">
      ${this.description}
      <slot name="link"></slot>
    </div>`;
  }

  _footerTemplate() {

    return html`
      <div class="card-footer">
        <vaadin-icon class="icon" icon="font-awesome-solid:pen-to-square" @click="${this._configuration}" title="Configuration for the ${this.name} extension"></vaadin-icon>  
        <vaadin-icon class="icon" icon="font-awesome-solid:ellipsis-vertical" @click="${this._more}" title="More about the ${this.name} extension"></vaadin-icon>
      </div>
      `;
  }

  _guide(e) {
    window.open(this.guide, '_blank').focus();
  }

  _configuration(e) {
    console.log("Show config with filter: " + this.configFilter);
  }

  _more(e){
    console.log("name: " + this.name);
    console.log("namespace: " + this.namespace);
    console.log("description: " + this.description);
    console.log("guide: " + this.guide);
    console.log("artifact: " + this.artifact);
    console.log("shortName: " + this.shortName);
    console.log("keywords: " + this.keywords);
    console.log("status: " + this.status);
    console.log("configFilter: " + this.configFilter);
    console.log("categories: " + this.categories);
    console.log("unlisted: " + this.unlisted);
    console.log("builtWith: " + this.builtWith);
    console.log("providesCapabilities: " + this.providesCapabilities);
    console.log("extensionDependencies: " + this.extensionDependencies);
  }
}

customElements.define('qwc-extension', QwcExtension);