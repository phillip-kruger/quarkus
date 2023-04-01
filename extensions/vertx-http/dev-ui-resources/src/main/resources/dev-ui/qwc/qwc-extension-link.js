import { LitElement, html, css} from 'lit';
import { JsonRpc } from 'jsonrpc';
import '@vaadin/icon';
import 'qui-badge';

/**
 * This component adds a custom link on the Extension card
 */
export class QwcExtensionLink extends LitElement {
  
    static styles = css`
        .extensionLink {
            display: flex;
            flex-direction: row;
            justify-content: space-between;
            align-items: center;
            font-size: small;
            padding: 2px 5px;
            cursor: pointer;
            text-decoration: none;
        }
        .extensionLink:hover {
            filter: brightness(80%);
        }
        .icon {
            padding-right: 5px;
        }
        .iconAndName {
            display: flex;
            flex-direction: row;
            justify-content: flex-start;
            align-items: center;
        }
    `;

    static properties = {
        extensionName: {type: String},
        iconName: {type: String},
        displayName: {type: String},
        staticLabel: {type: String},
        dynamicLabel: {type: String},
        streamingLabel: {type: String},
        streamingColor: {type: String},
        initStreamingColor: {type: String},
        path:  {type: String},
        webcomponent: {type: String},
        embed: {type: Boolean},
        externalUrl: {type: String},
        _effectiveLabel: {state: true},
        _effectiveColor: {state: true},
        _streamingLabelObserver: {state: false},
        _streamingColorObserver: {state: false},
    };

    constructor() {
        super();
        this._effectiveColor = "var(--lumo-contrast)";
    }
    
    connectedCallback() {
        super.connectedCallback();
        this.jsonRpc = new JsonRpc(this);
        if(this.streamingLabel){    
            this._streamingLabelObserver = this.jsonRpc[this.streamingLabel]().onNext(jsonRpcResponse => {
                this._effectiveLabel = jsonRpcResponse.result;
            });
        }else if(this.dynamicLabel){
            this.jsonRpc[this.dynamicLabel]().then(jsonRpcResponse => {
                this._effectiveLabel = jsonRpcResponse.result;
            });
        }else if(this.staticLabel){
            this._effectiveLabel = this.staticLabel;
        }
        
        if(this.initStreamingColor){
            this.jsonRpc[this.initStreamingColor]().then(jsonRpcResponse => {
                this._effectiveColor = jsonRpcResponse.result;
            });
        }
        if(this.streamingColor){
            this._streamingColorObserver = this.jsonRpc[this.streamingColor]().onNext(jsonRpcResponse => {
                this._effectiveColor = jsonRpcResponse.result;
            });
        }
        
    }

    disconnectedCallback() {
        if(this._streamingLabelObserver){
            this._streamingLabelObserver.cancel();
        }
        if(this._streamingColorObserver){
            this._streamingColorObserver.cancel();
        }
        super.disconnectedCallback()
    }

    render() {
        let routerIgnore = false;
        
        let p = this.path;
        let t = "_self";
        if(!this.embed){
            routerIgnore = true;
            p = this.externalUrl;
            t = "_blank";
        }
        return html`
        <a class="extensionLink" href="${p}" ?router-ignore=${routerIgnore} target="${t}" style="color:${this._effectiveColor}">
            <span class="iconAndName">
                <vaadin-icon class="icon" icon="${this.iconName}"></vaadin-icon>
                ${this.displayName} 
            </span>
            ${this._renderBadge()} 
        </a>
        `;
    }
    
    _renderBadge() {
        if (this._effectiveLabel) {
            return html`<qui-badge tiny pill><span>${this._effectiveLabel}</span></qui-badge>`;
        }
    }
}
customElements.define('qwc-extension-link', QwcExtensionLink);