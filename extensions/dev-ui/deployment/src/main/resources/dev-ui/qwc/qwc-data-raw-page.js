import { LitElement, html, css} from 'lit';
import { RouterController } from 'router-controller';
import '@vanillawc/wc-codemirror';
import '@vanillawc/wc-codemirror/mode/javascript/javascript.js';

/**
 * This component renders build time data in raw json format
 */
export class QwcDataRawPage extends LitElement {
    
    static styles = css`
        .jsondata {
            height: 100%;
            overflow: scroll;
            padding-bottom: 100px;
        }
    `;

    static properties = {
        _buildTimeDataKey: {attribute: false},
        _buildTimeData: {attribute: false},
    };

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

        return html`<wc-codemirror class="jsondata" 
                mode='javascript'
                readonly>
                <script type="wc-content">
                    ${json}
                </script>
            </wc-codemirror>`;
    }

}
customElements.define('qwc-data-raw-page', QwcDataRawPage);