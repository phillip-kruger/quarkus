import { LitElement, html, css} from 'lit';
import { until } from 'lit/directives/until.js';
import { JsonRpcController } from 'jsonrpc-controller';
import '@vaadin/grid';
import '@vaadin/grid/vaadin-grid-tree-column.js';
import '@vaadin/button';
import '@vaadin/checkbox';

/**
 * This component shows the Arc Invocation Trees
 */
export class QwcArcInvocationTrees extends LitElement {
  jsonRPC = new JsonRpcController(this, "ArC");
  
  static styles = css`
        .menubar {
            display: flex;
            justify-content: flex-start;
            align-items: center;
            padding-left: 5px;
        }
        .button {
            background-color: transparent;
            cursor: pointer;
        }
        .arctable {
            height: 100%;
            padding-bottom: 10px;
        }`;

    static properties = {
        _invocations: {state: true}
    };
  
    connectedCallback() {
        super.connectedCallback();
        this.jsonRPC.getLastInvocations();
    }
  
    render() {
        return html`${until(this._renderInvocations(), html`<span>Loading ArC invocation trees...</span>`)}`;
    }
  
    _renderInvocations(){
        if(this._invocations){
            return html`<div class="menubar">
                    <vaadin-button theme="small" @click=${this._refresh} class="button">
                        <vaadin-icon icon="font-awesome-solid:rotate"></vaadin-icon> Refresh
                    </vaadin-button> 
                    <vaadin-button theme="small" @click=${this._clear} class="button">
                        <vaadin-icon icon="font-awesome-solid:trash-can"></vaadin-icon> Clear
                    </vaadin-button> 
                    <vaadin-checkbox theme="small" label="Filter out Quarkus beans" @click=${this._toggleFilter}></vaadin-checkbox>
                </div>
                <vaadin-grid .items="${this._invocations}" class="arctable" theme="no-border">
                    <vaadin-grid-column auto-width
                        header="Start"
                        path="startTime"
                        resizable>
                    </vaadin-grid-column>

                </vaadin-grid>`;
        }
    }
    
    _refresh(){
        this.jsonRPC.getLastInvocations();
    }
    
    getLastInvocationsResponse(result){
        this._invocations = result;
    }
    
    _clear(){
        this.jsonRPC.clearLastInvocations();
    }
    
    clearLastInvocationsResponse(result){
        this._invocations = result;
    }
}
customElements.define('qwc-arc-invocation-trees', QwcArcInvocationTrees);