import { LitElement, html, css} from 'lit';
import {repeat} from 'lit/directives/repeat.js';


/**
 * This component represent the Dev UI Json RPC Message log
 */
export class QwcJsonrpcMessages extends LitElement {
    
    static styles = css`
        .log {
            background: #2C2C2C;
            width: 100%;
            display: flex;
            flex-direction:column;
            padding-bottom: 90px;
        }
        .disconnected {
            color:#F84139;
        }
        .connecting {
            color:#4085da;
        }
        .connected {
            color:#3BA143;
        }
    
        .disconnected-message {
            color:#B32828;
        }
        .connecting-message {
            color:#3f7f9b;
        }
        .connected-message {
            color:#ABBD78;
        }
    
        .timestamp {
            color: #C0C0C0;
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
                <code class="log">
                ${repeat(
                    this._messages,
                    (message) => message.id,
                    (message, index) => html`
                    <div class="logEntry">
                        ${this._renderTimestamp(message.time)}
                        ${this._renderDirection(message.connectionState, message.direction)}
                        ${this._renderMessage(message.connectionState, message.message)}
                    </class>
                  `
                  )}
                </code>`;
        
    }

    _renderDirection(connectionState, direction){
        let icon = "minus";
        if(direction === "up"){
            icon = "chevron-right";
        }else if(direction === "down"){
            icon = "chevron-left";
        }
        
        return html`<vaadin-icon class="${connectionState}" icon="font-awesome-solid:${icon}"></vaadin-icon>`;
    }

    _renderTimestamp(time){
        return html`<span class="timestamp">${time}</span>`;
    }
    
    _renderMessage(connectionState, message){
        return html`<span class="${connectionState}-message">${message}</span>`;
    }
}

customElements.define('qwc-jsonrpc-messages', QwcJsonrpcMessages);