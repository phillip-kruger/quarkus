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
            height: 100%;
            max-height: 100%;
            display: flex;
            flex-direction:column;
        }
        .error {
            color:#B32828;
        }
        .warning {
            color:#ff9900;
        }
        .info {
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
        
        document.addEventListener('jsonRPCLogEntryEvent', (e) => { 
            this._messages = [
                ...this._messages,
                e.detail,
            ];
            
        }, false);
        
    }
    
    disconnectedCallback() {
        super.disconnectedCallback();
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
                        ${this._renderDirection(message.level, message.direction)}
                        ${this._renderMessage(message.level, message.message)}
                    </class>
                  `
                  )}
                </code>`;
        
    }

    _renderDirection(level, direction){
        let icon = "minus";
        if(direction === "up"){
            icon = "chevron-right";
        }else if(direction === "down"){
            icon = "chevron-left";
        }
        
        return html`<vaadin-icon class="${level}" icon="font-awesome-solid:${icon}"></vaadin-icon>`;
    }

    _renderTimestamp(time){
        return html`<span class="timestamp">${time}</span>`;
    }
    
    _renderMessage(level, message){
        return html`<span class="${level}">${message}</span>`;
    }
}

customElements.define('qwc-jsonrpc-messages', QwcJsonrpcMessages);