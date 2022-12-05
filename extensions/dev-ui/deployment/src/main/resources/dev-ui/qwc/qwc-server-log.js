import { LitElement, html, css} from 'lit';
import { repeat } from 'lit/directives/repeat.js';
import { LogController } from 'log-controller';
import { JsonRpc } from 'jsonrpc';
import '@vaadin/icon';
import '@vaadin/dialog';
import '@vaadin/checkbox';
import '@vaadin/checkbox-group';
import { dialogHeaderRenderer, dialogRenderer } from '@vaadin/dialog/lit.js';

/**
 * This component represent the Server Log
 * 
 * TODO: Add clickable links
 * 
 */
export class QwcServerLog extends LitElement {
    
    logControl = new LogController(this, "server");
    jsonRpc = new JsonRpc("DevUI", false);
    
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
    
        .columnsDialog{
            
        }
        
        .levelsDialog{
            
        }
    `;

    static properties = {
        _messages: {state:true},
        _zoom: {state:true},
        _increment: {state: false},
        _followLog: {state: false},
        _observer: {state:false},
        _levelsDialogOpened: {state: true},
        _columnsDialogOpened: {state: true},
        _selectedColumns: {state: true},
    };

    constructor() {
        super();
        this.logControl
                .addToggle("On/off switch", true, (e) => {
                    this._toggleOnOffClicked(e);
                }).addItem("Log levels", "font-awesome-solid:layer-group", "grey", (e) => {
                    this._logLevels();
                }).addItem("Columns", "font-awesome-solid:table-columns", "grey", (e) => {
                    this._columns();
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
        this._levelsDialogOpened = false;
        this._columnsDialogOpened = false;
        this._selectedColumns = ['0','3','4','5','10','18','19'];
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
        
        return html`
                <vaadin-dialog class="levelsDialog"
                    header-title="Log levels"
                    .opened="${this._levelsDialogOpened}"
                    @opened-changed="${(e) => (this._levelsDialogOpened = e.detail.value)}"
                    ${dialogHeaderRenderer(() => html`
                        <vaadin-button theme="tertiary" @click="${() => (this._levelsDialogOpened = false)}">
                            <vaadin-icon icon="font-awesome-solid:xmark"></vaadin-icon>
                        </vaadin-button>
                    `,
                    []
                    )}
                    ${dialogRenderer(() => this._renderLevelsDialog(), "levels")}
                ></vaadin-dialog>
                <vaadin-dialog class="columnsDialog"
                    header-title="Columns"
                    .opened="${this._columnsDialogOpened}"
                    @opened-changed="${(e) => (this._columnsDialogOpened = e.detail.value)}"
                    ${dialogHeaderRenderer(() => html`
                        <vaadin-button theme="tertiary" @click="${() => (this._columnsDialogOpened = false)}">
                            <vaadin-icon icon="font-awesome-solid:xmark"></vaadin-icon>
                        </vaadin-button>
                    `,
                    []
                    )}
                    ${dialogRenderer(() => this._renderColumnsDialog(), "columns")}
                ></vaadin-dialog>
                <code class="log" style="font-size: ${this._zoom}em;">
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
        if(this._selectedColumns.includes('0')){
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
        if(this._selectedColumns.includes('1')){
            return html`<span title='Sequence number' class="badge badge-info">${sequenceNumber}</span>`;
        }
    }
    
    _renderHostName(hostName){
        if(this._selectedColumns.includes('2')){
            return html`<span title='Host name'>${hostName}</span>`;
        }
    }
    
    _renderDate(timestamp){
        if(this._selectedColumns.includes('3')){
            return html`<span title='Date'>${timestamp.slice(0, 10)}</span>`;
        }
    }
    
    _renderTime(timestamp){
        if(this._selectedColumns.includes('4')){
            return html`<span title='Time'>${timestamp.slice(11, 23).replace(".", ",")}</span>`;
        }
    }
    
    _renderLevel(level, leveldisplay){
        if(this._selectedColumns.includes('5')){
            return html`<span title='Level' class='text-${level}'>${leveldisplay}</span>`;
        }
    }
    
    _renderLoggerNameShort(loggerNameShort){
        if(this._selectedColumns.includes('6')){
            return html`<span title='Logger name (short)' class='text-logger'>[${loggerNameShort}]</span>`;
        }
    }
    
    _renderLoggerName(loggerName){
        if(this._selectedColumns.includes('7')){
            return html`<span title='Logger name' class='text-logger'>[${loggerName}]</span>`;
        }
    }
    
    _renderLoggerClassName(loggerClassName){
        if(this._selectedColumns.includes('8')){
            return html`<span title='Logger class name' class='text-logger'>[${loggerClassName}]</span>`;
        }
    }
    
    _renderSourceClassNameFull(sourceClassNameFull){
        if(this._selectedColumns.includes('9')){
            return html`<span title='Source full class name' class='text-source'>[${sourceClassNameFull}]</span>`;
        }
    }
    
    _renderSourceClassNameFullShort(sourceClassNameFullShort){
        if(this._selectedColumns.includes('10')){
            return html`<span title='Source full class name (short)' class='text-source'>[${sourceClassNameFullShort}]</span>`;
        }
    }
    
    _renderSourceClassName(sourceClassName){
        if(this._selectedColumns.includes('11')){
            return html`<span title='Source class name' class='text-source'>[${sourceClassName}]</span>`;
        }
    }
    
    _renderSourceMethodName(sourceMethodName){
        if(this._selectedColumns.includes('12')){
            return html`<span title='Source method name' class='text-source'>${sourceMethodName}</span>`;
        }
    }
    
    _renderSourceFileName(sourceFileName){
        if(this._selectedColumns.includes('13')){
            return html`<span title='Source file name' class='text-file'>${sourceFileName}</span>`;
        }
    }
    
    _renderSourceLineNumber(sourceLineNumber){
        if(this._selectedColumns.includes('14')){
            return html`<span title='Source line number' class='text-source'>(line:${sourceLineNumber})</span>`;
        }
    }
    
    _renderProcessId(processId){
        if(this._selectedColumns.includes('15')){
            return html`<span title='Process Id' class='text-process'>(${processId})</span>`;
        }
    }
    
    _renderProcessName(processName){
        if(this._selectedColumns.includes('16')){
            return html`<span title='Process name' class='text-process'>(${processName})</span>`;
        }
    }
    
    _renderThreadId(threadId){
        if(this._selectedColumns.includes('17')){
            return html`<span title='Thread Id' class='text-thread'>(${threadId})</span>`;
        }
    }
    
    _renderThreadName(threadName){
        if(this._selectedColumns.includes('18')){
            return html`<span title='Thread name' class='text-thread'>(${threadName})</span>`;
        }
    }
    
    _renderMessage(level, message){
        if(this._selectedColumns.includes('19')){
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
    
    _renderLevelsDialog(){
        return html`
            Hello levels
        `;
    }
    
    _renderColumnsDialog(){
        return html`<vaadin-checkbox-group
                        .value="${this._selectedColumns}"
                        @value-changed="${(e) => (this._selectedColumns = e.detail.value)}"
                        theme="vertical"
                    >
                        <vaadin-checkbox value="0" label="Level icon"></vaadin-checkbox>
                        <vaadin-checkbox value="1" label=Sequence number"></vaadin-checkbox>
                        <vaadin-checkbox value="2" label="Host name"></vaadin-checkbox>
                        <vaadin-checkbox value="3" label="Date"></vaadin-checkbox>
                        <vaadin-checkbox value="4" label="Time"></vaadin-checkbox>
                        <vaadin-checkbox value="5" label="Level"></vaadin-checkbox>
                        <vaadin-checkbox value="6" label="Logger name (short)"></vaadin-checkbox>
                        <vaadin-checkbox value="7" label="Logger name"></vaadin-checkbox>
                        <vaadin-checkbox value="8" label="Logger class name"></vaadin-checkbox>
                        <vaadin-checkbox value="9" label="Source full class name"></vaadin-checkbox>
                        <vaadin-checkbox value="10" label="Source full class name (short)"></vaadin-checkbox>
                        <vaadin-checkbox value="11" label="Source class name"></vaadin-checkbox>
                        <vaadin-checkbox value="12" label="Source method name"></vaadin-checkbox>
                        <vaadin-checkbox value="13" label="Source file name"></vaadin-checkbox>
                        <vaadin-checkbox value="14" label="Source line number"></vaadin-checkbox>
                        <vaadin-checkbox value="15" label="Process id"></vaadin-checkbox>
                        <vaadin-checkbox value="16" label="Process name"></vaadin-checkbox>
                        <vaadin-checkbox value="17" label="Thread id"></vaadin-checkbox>
                        <vaadin-checkbox value="18" label="Thread name"></vaadin-checkbox>
                        <vaadin-checkbox value="19" label="Message"></vaadin-checkbox>
                    </vaadin-checkbox-group>`;
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
    
    _logLevels(){
        this._levelsDialogOpened = true;
    }
    
    _columns(){
        this._columnsDialogOpened = true;
    }
}

customElements.define('qwc-server-log', QwcServerLog);