import { LitElement, html, css} from 'lit';
import {repeat} from 'lit/directives/repeat.js';
import { JsonRpc } from 'jsonrpc';
import '@vaadin/scroller';

/**
 * This component represent the Dev UI Json RPC Message log
 */
export class QwcJsonrpcMessages extends LitElement {
    jsonRpc = new JsonRpc("Internal");
    
    static styles = css`
        vaadin-scroller {
            height: 100%;
            width: 100%;
            background: #2C2C2C;
        }
        .log {
            background: #2C2C2C;
            padding-left: 10px;
            height: 100%;
            width: 100%;
            display: flex;
            flex-direction:column;
        }
        .icon {
            color: green;
        }
        .timestamp {
            color: #94BFEE;
        }
        .message {
            color: #7CB4AA;
        }
    `;

    static properties = {
        _messages: {state:true},
    };
    
    constructor() {
        super();
        this._messages = [];
    }
    
    connectedCallback() {
        super.connectedCallback();
        console.log("Adding event listeners");
        
        document.addEventListener('jsonRPCLogEntryEvent', (e) => { 
            this._messages = [
                ...this._messages,
                e.detail,
            ];
            
        }, false);
        
        document.addEventListener('jsonRPCStateChangeEvent', (e) => {
            console.log(">>> " + e.detail);
        }, false);
    }
    
    disconnectedCallback() {
        super.disconnectedCallback();
        console.log("Removing event listeners");
        document.removeEventListener('jsonRPCLogEntryEvent', this._handleLogEntryEvent);
        document.removeEventListener('jsonRPCStateChangeEvent', this._handleStateChangeEvent);
    }
    
    render() {
        
        return html`
            <vaadin-scroller>
                <code class="log">
                ${repeat(
                    this._messages,
                    (message) => message.id,
                    (message, index) => html`
                    <div class="logEntry">
                        ${this._renderTimestamp(message.time)}
                        ${this._renderDirection(message.direction)}
                        ${this._renderMessage(message.message)}
                    </class>
                  `
                  )}
                </code>
        </vaadin-scroller>`;
        
    }

    _renderDirection(direction){
        let icon = "circle";
        if(direction === "up"){
            icon = "chevron-right";
        }else if(direction === "down"){
            icon = "chevron-left";
        }
        return html`<vaadin-icon class="icon" icon="font-awesome-solid:${icon}"></vaadin-icon>`;
    }

    _renderTimestamp(time){
        return html`<span class="timestamp">${time}</span>`;
    }
    
    _renderMessage(message){
        return html`<span class="message">${message}</span>`;
    }
}

customElements.define('qwc-jsonrpc-messages', QwcJsonrpcMessages);