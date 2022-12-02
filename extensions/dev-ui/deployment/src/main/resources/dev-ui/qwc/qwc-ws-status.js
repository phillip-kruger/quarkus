import { LitElement, html, css} from 'lit';
import { JsonRpc } from 'jsonrpc';

/**
 * This component shows the status of the Web socket connection
 */
export class QwcWsStatus extends LitElement {
    jsonRpc = new JsonRpc("DevUI");
    
    static styles = css`
        :host {
            padding-left: 10px;
        }
    `;

    static properties = {
        _status: {state:true},
        _serverUri: {state:false},
    };
    
    constructor() {
        super();
        this._serverUri = JsonRpc.serverUri;
        this._status = JsonRpc.connectionState;
    }
    
    connectedCallback() {
        super.connectedCallback();
        
        document.addEventListener('jsonRPCStateChangeEvent', (e) => {
            this._status = e.detail;
        }, false);
    }
    
    disconnectedCallback() {
        super.disconnectedCallback();
        document.removeEventListener('jsonRPCStateChangeEvent', this._handleStateChangeEvent);
    }
    
    render() {
        let icon = "plug";
        let color = "grey";
        let message = "Checking status ";
        
        if(this._status === "connecting"){
            icon = "plug-circle-bolt";
            color = "blue";
            message = "Connecting to " + this._serverUri;
        }else if(this._status === "connected"){
            icon = "plug-circle-check";
            color = "green";
            message = "Connected to " + this._serverUri;
        }else if(this._status === "disconnected"){
            icon = "plug-circle-exclamation";
            color = "orange";
            message = "Disconnected from " + this._serverUri;
        }
        
        return html`<vaadin-icon title="${message}" style="color:${color}" icon="font-awesome-solid:${icon}"></vaadin-icon>`;
    }
}

customElements.define('qwc-ws-status', QwcWsStatus);