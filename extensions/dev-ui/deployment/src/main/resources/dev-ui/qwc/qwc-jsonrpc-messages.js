import { LitElement, html, css} from 'lit';
import {repeat} from 'lit/directives/repeat.js';
import {LogController} from 'log-controller';

/**
 * This component represent the Dev UI Json RPC Message log
 */
export class QwcJsonrpcMessages extends LitElement {
    
    logControl = new LogController(this, "json rpc");
    
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
        _zoom: {state:true},
        _increment: {state: false},
        _followLog: {state: false}
    };
    
    constructor() {
        super();
        this._messages = [];
        this._zoom = parseFloat(1.0);
        this._increment = parseFloat(0.05);
        this._followLog = true;
        this.logControl
                .addItem("Zoom out", "font-awesome-solid:magnifying-glass-minus", "grey", (e) => {
                    this._zoomOut();
                }).addItem("Zoom in", "font-awesome-solid:magnifying-glass-plus", "grey", (e) => {
                    this._zoomIn();
                }).addItem("Clear", "font-awesome-solid:trash-can", "#FF004A", (e) => {
                    this._clearLog();
                }).addFollow("Follow log", true , (e) => {
                    this._toggleFollowLog(e);
                });
    }
    
    connectedCallback() {
        super.connectedCallback();
        
        document.addEventListener('jsonRPCLogEntryEvent', (e) => { 
            this._addLogEntry(e.detail);
        }, false);
    }
    
    disconnectedCallback() {
        super.disconnectedCallback();
        document.removeEventListener('jsonRPCLogEntryEvent', this._handleLogEntryEvent);
        document.removeEventListener('jsonRPCStateChangeEvent', this._handleStateChangeEvent);
    }
    
    render() {
        
        return html`<code class="log" style="font-size: ${this._zoom}em;">
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
    
    _toggleFollowLog(e){
        this._followLog = e;
        this._scrollToBottom();   
    }
    
    _addLogEntry(entry){
        this._messages = [
            ...this._messages,
            entry
        ];
        
        this._scrollToBottom();   
    }
    
    async _scrollToBottom(){
        if(this._followLog){
            await this.updateComplete;
            
            const last = Array.from(
                this.shadowRoot.querySelectorAll('.logEntry')
            ).pop();
            
            last.scrollIntoView({
                 behavior: "smooth",
                 block: "end"
            });
        }
    }
    
    
    _clearLog(){
        this._messages = [];
    }
    
    _zoomOut(){
        this._zoom = parseFloat(parseFloat(this._zoom) - parseFloat(this._increment)).toFixed(2);
    }
    
    _zoomIn(){
        this._zoom = parseFloat(parseFloat(this._zoom) + parseFloat(this._increment)).toFixed(2);
    }
}

customElements.define('qwc-jsonrpc-messages', QwcJsonrpcMessages);