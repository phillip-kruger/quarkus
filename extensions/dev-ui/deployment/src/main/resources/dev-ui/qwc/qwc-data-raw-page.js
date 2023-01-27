import { LitElement, html, css} from 'lit';
import { RouterController } from 'router-controller';
import { ThemeController } from 'theme-controller';
import '@vanillawc/wc-codemirror';
import '@vanillawc/wc-codemirror/mode/javascript/javascript.js';

/**
 * This component renders build time data in raw json format
 */
export class QwcDataRawPage extends LitElement {
    
    static styles = css`
        .codeBlock {
            display:flex;
            gap: 10px;
            flex-direction: column;
            padding-left: 10px;
            padding-right: 10px;
        }
        .jsondata {
            height: 100%;
            overflow: scroll;
            padding-bottom: 100px;
        }
    `;

    static properties = {
        _theme: {type: String},
        _buildTimeDataKey: {attribute: false},
        _buildTimeData: {attribute: false},
    };

    constructor() {
        super();
        this._theme = ThemeController.currentTheme.theme;
        
        // Receive theme change
        document.addEventListener('themeChange', (e) => { 
            this._theme = e.detail.theme;
        }, false);
    }

    connectedCallback() {
        super.connectedCallback();
        var extensionId = RouterController.currentExtensionId();
        if(extensionId){

            var metadata = RouterController.currentMetaData();
            if(metadata){

                this._buildTimeDataKey = metadata.buildTimeDataKey;
                
                let modulePath = extensionId + "-data";

                import(modulePath)
                .then(obj => {
                    this._buildTimeData = obj[this._buildTimeDataKey]; // TODO: Just use obj and allow multiple keys ?
                });
            }
        }
    }
    
    
    render() {

        var json = JSON.stringify(this._buildTimeData, null, '\t');

        return html`<div class="codeBlock">
                <wc-codemirror class="jsondata" 
                    mode='javascript'
                    theme='base16-${this._theme}'
                    readonly>
                    <link rel="stylesheet" href="/_static/wc-codemirror/theme/base16-${this._theme}.css">
                    <script type="wc-content">
                        ${json}
                    </script>
                </wc-codemirror>
            </div>`;
    }

}
customElements.define('qwc-data-raw-page', QwcDataRawPage);