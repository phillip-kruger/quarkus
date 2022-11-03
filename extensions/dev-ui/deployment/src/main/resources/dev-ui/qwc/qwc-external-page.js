import { LitElement, html, css} from 'lit';
import { until } from 'lit/directives/until.js';
import { RouterController } from 'router-controller';
import '@vanillawc/wc-codemirror';
import '@vanillawc/wc-codemirror/mode/yaml/yaml.js';
import '@vanillawc/wc-codemirror/mode/properties/properties.js';
import '@vanillawc/wc-codemirror/mode/javascript/javascript.js';

/**
 * This component loads an external page
 */
export class QwcExternalPage extends LitElement {
    routerController = new RouterController(this);
    
    static styles = css`
        
    `;

    static properties = {
        _externalUrl: {type: String},
        _mode: {type: String},
        _contentType: {type: String},
    };

    constructor() {
        super();
    }

    connectedCallback() {
        super.connectedCallback();
        var metadata = this.routerController.getCurrentMetaData();
        if(metadata){
            this._externalUrl = metadata.externalUrl;
        }
        if(this._externalUrl){
            fetch(this._externalUrl)
                .then((res) => {
                        this._contentType = res.headers.get('content-type');
                        if(this._contentType.startsWith('application/yaml')){
                            this._mode = "yaml";
                        }else if(this._contentType.startsWith('application/json')){
                            this._mode = "javascript";
                        }else if(this._contentType.startsWith('text/html')){
                            this._mode = "html";
                        }else if(this._contentType.startsWith('application/pdf')){
                            this._mode = "pdf";
                        }else{
                            this._mode = "properties";
                        }
                    }
                );
        }
    }
    
    render() {
        return html`${until(this._loadExternal(), html`<span>Loading...</span>`)}`;
    }

    _loadExternal(){
        if(this._mode){
            if(this._mode == "html" || this._mode == "pdf"){
                return html`<object type='${this._contentType}'
                                data='${this._externalUrl}' 
                                width='100%' 
                                height='100%'>
                            </object>`;
            }else {
                return html`<wc-codemirror 
                                mode='${this._mode}'
                                src='${this._externalUrl}'
                                readonly>
                            </<wc-codemirror>`;
            }
        }   
    } 
}
customElements.define('qwc-external-page', QwcExternalPage);