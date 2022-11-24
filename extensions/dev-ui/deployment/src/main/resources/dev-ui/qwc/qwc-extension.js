import { LitElement, html, css} from 'lit';
import '@vaadin/icon';
import '@vaadin/dialog';
import { dialogHeaderRenderer, dialogRenderer } from '@vaadin/dialog/lit.js';

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
        _dialogOpened: {state: true},
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
        unlisted: {type: String},
        builtWith: {type: String},
        providesCapabilities: {},
        extensionDependencies: {},    
    };
    
    constructor() {
        super();
        this._dialogOpened = false;
    }

    render() {
        
        return html`
            <vaadin-dialog class="detailDialog"
                header-title="${this.name} extension details"
                .opened="${this._dialogOpened}"
                @opened-changed="${(e) => (this._dialogOpened = e.detail.value)}"
                ${dialogHeaderRenderer(
                  () => html`
                    <vaadin-button theme="tertiary" @click="${() => (this._dialogOpened = false)}">
                        <vaadin-icon icon="font-awesome-solid:xmark"></vaadin-icon>
                    </vaadin-button>
                  `,
                  []
                )}
                ${dialogRenderer(() => this._renderDialog(), this.name)}
            ></vaadin-dialog>

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
                <vaadin-icon class="icon" icon="font-awesome-solid:ellipsis-vertical" @click="${() => (this._dialogOpened = true)}" title="More about the ${this.name} extension"></vaadin-icon>
            </div>
        `;
    }

    _renderDialog(){
        return html`<table style="font-family: Red Hat Text;">
                <tr>
                    <td><b>Name</b></td>
                    <td>${this.name}</td>
                </tr>
                <tr>
                    <td><b>Namespace</b></td>
                    <td>${this.namespace}</td>
                </tr>
                <tr>
                    <td><b>Description</b></td>
                    <td>${this.description}</td>
                </tr>
                <tr>
                    <td><b>Guide</b></td>
                    <td>${this._renderGuideDetails()}</td>
                </tr>
                <tr>
                    <td><b>Artifact</b></td>
                    <td>${this._renderArtifact()}</td>
                </tr>
                <tr>
                    <td><b>Short name</b></td>
                    <td>${this.shortName}</td>
                </tr>        
                <tr>
                    <td><b>Keywords</b></td>
                    <td>${this._renderKeywordsDetails()}</td>
                </tr>        
                <tr>
                    <td><b>Status</b></td>
                    <td>${this.status}</td>
                </tr>        
                <tr>
                    <td><b>Config Filter</b></td>
                    <td>${this.configFilter}</td>
                </tr>
                <tr>
                    <td><b>Categories</b></td>
                    <td>${this.categories}</td>
                </tr>
                <tr>
                    <td><b>Unlisted</b></td>
                    <td>${this.unlisted}</td>
                </tr>
                <tr>
                    <td><b>Built with</b></td>
                    <td>${this.builtWith}</td>
                </tr>
                <tr>
                    <td><b>Provides capabilities</b></td>
                    <td>${this.providesCapabilities}</td>
                </tr>
                <tr>
                    <td><b>Extension dependencies</b></td>
                    <td>${this._renderExtensionDependencies()}</td>
                </tr>
            </table>
        `;
    }

    _renderGuideDetails() {
        return this.guide
          ? html`<span style="cursor:pointer" @click=${this._guide}>${this.guide}</span>`
          : html``;
    }

    _renderKeywordsDetails() {
        return this._renderCommaString(this.keywords);
    }

    _renderExtensionDependencies() {
        return this._renderCommaString(this.extensionDependencies);
    }

    _renderArtifact(){
        if(this.artifact){
            return html`<code>${this.artifact}</code>`;
        }else{
            return html``;
        }
    }

    _renderCommaString(cs){
        if(cs) {
            var arr = cs.split(',');
            return html`<ul>${arr.map(v => 
                html`<li>${v}</li>`
            )}</ul>`;
        }else{
            return html``;
        }
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