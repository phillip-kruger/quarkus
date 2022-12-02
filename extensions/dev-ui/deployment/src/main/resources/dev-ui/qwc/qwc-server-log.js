import { LitElement, html, css} from 'lit';
import { repeat } from 'lit/directives/repeat.js';
import { LogController } from 'log-controller';
import { JsonRpc } from 'jsonrpc';
import '@vaadin/icon';

/**
 * This component represent the Server Log
 * 
 * TODO: Add clickable links
 * 
 */
export class QwcServerLog extends LitElement {
    
    logControl = new LogController(this, "server");
    jsonRpc = new JsonRpc("DevUI");
    
    static styles = css`
        .log {
            background: #2C2C2C;
            width: 100%;
            height: 100%;
            max-height: 100%;
            display: flex;
            flex-direction:column;
            color: white;
        }
        .line {
            margin-top: 10px;
            margin-bottom: 10px;
            border-top: 1px dashed #54789F;
            color: transparent;
        }
    
        .badge {
            display: inline-block;
            padding: .25em .4em;
            font-size: 75%;
            font-weight: 700;
            line-height: 1;
            text-align: center;
            white-space: nowrap;
            vertical-align: baseline;
            border-radius: .25rem;
            transition: color .15s ease-in-out,background-color .15s ease-in-out,border-color .15s ease-in-out,box-shadow .15s ease-in-out;
        }
        .badge-info {
            color: #fff;
            background-color: #17a2b8;
        }
    
        .text-warn {
            color: orange;
        }
        .text-error{
            color: red;
        }
        .text-info{
            color: #4695EB;
        }
        .text-debug{
            color: #306C40;
        }
        .text-normal{
            color: #B0D0B0;
        }
        .text-logger{
            color: #437084;
        }
        .text-source{
            color: #3554C3;
        }
        .text-file {
            color: grey;
        }
        .text-process{
            color: #437084;
        }
        .text-thread{
            color: #72BB3E;
        }
    `;

    static properties = {
        _messages: {state:true},
        _zoom: {state:true},
        _increment: {state: false},
        _followLog: {state: false},
        _observer: {state:false},
        
        _shouldRenderLevelIcon: {state:true},
        _shouldRenderSequenceNumber: {state:true},
        _shouldRenderHostName: {state:true},
        _shouldRenderDate: {state:true},
        _shouldRenderTime: {state:true},
        _shouldRenderLevel: {state:true},
        _shouldRenderLoggerNameShort: {state:true},
        _shouldRenderLoggerName: {state:true},
        _shouldRenderLoggerClassName: {state:true},
        _shouldRenderSourceClassNameFull: {state:true},
        _shouldRenderSourceClassNameFullShort: {state:true},
        _shouldRenderSourceClassName: {state:true},
        _shouldRenderSourceMethodName: {state:true},
        _shouldRenderSourceFileName: {state:true},
        _shouldRenderSourceLineNumber: {state:true},
        _shouldRenderProcessId: {state:true},
        _shouldRenderProcessName: {state:true},
        _shouldRenderThreadId: {state:true},
        _shouldRenderThreadName: {state:true},
        _shouldRenderMessage: {state:true}
    };

    constructor() {
        super();
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
                
        this._messages = [];
        this._zoom = parseFloat(1.0);
        this._increment = parseFloat(0.05);
        this._followLog = true;
        
        this._shouldRenderLevelIcon = true;
        this._shouldRenderSequenceNumber = false;
        this._shouldRenderHostName = false;
        this._shouldRenderDate = true;
        this._shouldRenderTime = true;
        this._shouldRenderLevel = true;
        this._shouldRenderLoggerNameShort = false;
        this._shouldRenderLoggerName = false;
        this._shouldRenderLoggerClassName = false;
        this._shouldRenderSourceClassNameFull = false;
        this._shouldRenderSourceClassNameFullShort = true;
        this._shouldRenderSourceClassName = false;
        this._shouldRenderSourceMethodName = false;
        this._shouldRenderSourceFileName = false;
        this._shouldRenderSourceLineNumber = false;
        this._shouldRenderProcessId = false;
        this._shouldRenderProcessName = false;
        this._shouldRenderThreadId = false;
        this._shouldRenderThreadName = true;
        this._shouldRenderMessage = true;
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
                    (message) => message.sequenceNumber,
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
            var ts = new Date(message.timestamp);
            var isoDateTime = new Date(ts.getTime() - (ts.getTimezoneOffset() * 60000)).toISOString();
            
            var level = message.level.toUpperCase();
            if (level === "WARNING" || level === "WARN"){
                level = "warn";
            }else if (level === "SEVERE" || level === "ERROR"){
                level = "error";
            }else if (level === "INFO"){
                level = "info";
            }else if (level === "DEBUG"){
                level = "debug";
            }else {
                level = "normal";
            }
            
            return html`
                ${this._renderLevelIcon(level)}
                ${this._renderSequenceNumber(message.sequenceNumber)}
                ${this._renderHostName(message.hostName)}
                ${this._renderDate(isoDateTime)}
                ${this._renderTime(isoDateTime)}
                ${this._renderLevel(level, message.level)}
                ${this._renderLoggerNameShort(message.loggerNameShort)}
                ${this._renderLoggerName(message.loggerName)}
                ${this._renderLoggerClassName(message.loggerClassName)}
                ${this._renderSourceClassNameFull(message.sourceClassNameFull)}
                ${this._renderSourceClassNameFullShort(message.sourceClassNameFullShort)}
                ${this._renderSourceClassName(message.sourceClassName)}
                ${this._renderSourceMethodName(message.sourceMethodName)}
                ${this._renderSourceFileName(message.sourceFileName)}
                ${this._renderSourceLineNumber(message.sourceLineNumber)}
                ${this._renderProcessId(message.processId)}
                ${this._renderProcessName(message.processName)}
                ${this._renderThreadId(message.threadId)}
                ${this._renderThreadName(message.threadName)}
                ${this._renderMessage(level, message.formattedMessage)}
            `;
        }
    }
    
    _renderLevelIcon(level){
        if(this._shouldRenderLevelIcon){
            
            if (level === "warn"){
                return html`<vaadin-icon icon="font-awesome-solid:circle-exclamation" class="text-warn"></vaadin-icon>`;
            }else if (level === "error"){
                return html`<vaadin-icon icon="font-awesome-solid:radiation" class="text-error"></vaadin-icon>`;
            }else if (level === "info"){
                return html`<vaadin-icon icon="font-awesome-solid:circle-info" class="text-info"></vaadin-icon>`;
            }else if (level === "debug"){
                return html`<vaadin-icon icon="font-awesome-solid:bug" class="text-debug"></vaadin-icon>`;
            }else {
                return html`<vaadin-icon icon="font-awesome-solid:circle" class="text-normal"></vaadin-icon>`;
            }
        }
    }
    
    _renderSequenceNumber(sequenceNumber){
        if(this._shouldRenderSequenceNumber){
            return html`<span title='Sequence number' class="badge badge-info">${sequenceNumber}</span>`;
        }
    }
    
    _renderHostName(hostName){
        if(this._shouldRenderHostName){
            return html`<span title='Host name'>${hostName}</span>`;
        }
    }
    
    _renderDate(timestamp){
        if(this._shouldRenderDate){
            return html`<span title='Date'>${timestamp.slice(0, 10)}</span>`;
        }
    }
    
    _renderTime(timestamp){
        if(this._shouldRenderTime){
            return html`<span title='Time'>${timestamp.slice(11, 23).replace(".", ",")}</span>`;
        }
    }
    
    _renderLevel(level, leveldisplay){
        if(this._shouldRenderLevel){
            return html`<span title='Level' class='text-${level}'>${leveldisplay}</span>`;
        }
    }
    
    _renderLoggerNameShort(loggerNameShort){
        if(this._shouldRenderLoggerNameShort){
            return html`<span title='Logger name (short)' class='text-logger'>[${loggerNameShort}]</span>`;
        }
    }
    
    _renderLoggerName(loggerName){
        if(this._shouldRenderLoggerName){
            return html`<span title='Logger name' class='text-logger'>[${loggerName}]</span>`;
        }
    }
    
    _renderLoggerClassName(loggerClassName){
        if(this._shouldRenderLoggerClassName){
            return html`<span title='Logger class name' class='text-logger'>[${loggerClassName}]</span>`;
        }
    }
    
    _renderSourceClassNameFull(sourceClassNameFull){
        if(this._shouldRenderSourceClassNameFull){
            return html`<span title='Source full class name' class='text-source'>[${sourceClassNameFull}]</span>`;
        }
    }
    
    _renderSourceClassNameFullShort(sourceClassNameFullShort){
        if(this._shouldRenderSourceClassNameFullShort){
            return html`<span title='Source full class name (short)' class='text-source'>[${sourceClassNameFullShort}]</span>`;
        }
    }
    
    _renderSourceClassName(sourceClassName){
        if(this._shouldRenderSourceClassName){
            return html`<span title='Source class name' class='text-source'>[${sourceClassName}]</span>`;
        }
    }
    
    _renderSourceMethodName(sourceMethodName){
        if(this._shouldRenderSourceMethodName){
            return html`<span title='Source method name' class='text-source'>${sourceMethodName}</span>`;
        }
    }
    
    _renderSourceFileName(sourceFileName){
        if(this._shouldRenderSourceFileName){
            return html`<span title='Source file name' class='text-file'>${sourceFileName}</span>`;
        }
    }
    
    _renderSourceLineNumber(sourceLineNumber){
        if(this._shouldRenderSourceLineNumber){
            return html`<span title='Source line number' class='text-source'>(line:${sourceLineNumber})</span>`;
        }
    }
    
    _renderProcessId(processId){
        if(this._shouldRenderProcessId){
            return html`<span title='Process Id' class='text-process'>(${processId})</span>`;
        }
    }
    
    _renderProcessName(processName){
        if(this._shouldRenderProcessName){
            return html`<span title='Process name' class='text-process'>(${processName})</span>`;
        }
    }
    
    _renderThreadId(threadId){
        if(this._shouldRenderThreadId){
            return html`<span title='Thread Id' class='text-thread'>(${threadId})</span>`;
        }
    }
    
    _renderThreadName(threadName){
        if(this._shouldRenderThreadName){
            return html`<span title='Thread name' class='text-thread'>(${threadName})</span>`;
        }
    }
    
    _renderMessage(level, message){
        if(this._shouldRenderMessage){
            // Make links clickable
            if(message.includes("http://")){
                message = this._makeLink(message, "http://");
            }
            if(message.includes("https://")){
                message = this._makeLink(message, "https://");
            }
            
            // Make sure multi line is supported
            if(message.includes('\n')){
                var htmlifiedLines = [];
                var lines = message.split('\n');
                for (var i = 0; i < lines.length; i++) {
                    var line = lines[i];
                    line = line.replace(/ /g, '\u00a0');
                    if(i === lines.length-1){
                        htmlifiedLines.push(line);
                    }else{
                        htmlifiedLines.push(line + '<br/>');
                    }
                }
                message = htmlifiedLines.join('');
            }
        
            return html`<span title="Message" class='text-${level}'>${message}</span>`;
        }
    }
    
    _makeLink(message, protocol){
        var url = message.substring(message.indexOf(protocol));
        if(url.includes(" ")){
            url = url.substr(0,url.indexOf(' '));
        }
        var link = "<a href='" + url + "' class='text-primary' target='_blank'>" + url + "</a>";

        return message.replace(url, link);    
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
            this._observer = this.jsonRpc.streamLog().onNext(jsonRpcResponse => {
                this._addLogEntry(jsonRpcResponse.result);
            });
            
            
        }else{
            this._observer.cancel();
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

customElements.define('qwc-server-log', QwcServerLog);