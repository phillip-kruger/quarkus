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
        .line {
            margin-top: 10px;
            margin-bottom: 10px;
            border-top: 1px dashed #54789F;
            color: transparent;
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
        this._jsonRPCLogEntryEvent = (event) => this._addLogEntry(event.detail);
        this.logControl
                .addToggle("On/off switch", true, (e) => {
                    this._toggleOnOffClicked(e);
                }).addItem("Zoom out", "font-awesome-solid:magnifying-glass-minus", "grey", (e) => {
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
        this._toggleOnOff(true);
    }
    
    disconnectedCallback() {
        super.disconnectedCallback();
        this._toggleOnOff(false);
    }
    
    render() {
        
        return html`<code class="log" style="font-size: ${this._zoom}em;">
                ${repeat(
                    this._messages,
                    (message) => message.id,
                    (message, index) => html`
                    <div class="logEntry">
                        ${this._renderLogEntry(message)}
                    </class>
                  `
                  )}
                </code>`;
        
    }

    _renderLogEntry(message){
        if(message.isLine){
            return html`<hr class="line"/>`;
        }else{
            return html`
                ${this._renderTimestamp(message.time)}
                ${this._renderDirection(message.level, message.direction)}
                ${this._renderMessage(message.level, message.message)}
            `;
        }
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
    
    _toggleOnOffClicked(e){
        this._toggleOnOff(e);
        // Add line on stop
        if(!e){
            var stopEntry = new Object();
            stopEntry.id = Math.floor(Math.random() * 999999);
            stopEntry.isLine = true;
            this._addLogEntry(stopEntry);
        }
    }
    
    _toggleOnOff(e){
        
        if(e){
            console.log("log streaming is on !");
            document.addEventListener('jsonRPCLogEntryEvent', this._jsonRPCLogEntryEvent, false);
        }else{
            console.log("log streaming is off !");
            document.removeEventListener('jsonRPCLogEntryEvent', this._jsonRPCLogEntryEvent, false);
        }
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