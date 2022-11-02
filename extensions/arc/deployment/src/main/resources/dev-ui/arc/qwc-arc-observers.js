import { LitElement, html, css} from 'lit';
import { observers } from 'arc-data';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';
import '@vaadin/grid';
import '@vaadin/icon';

/**
 * This component shows the Arc Observers
 */
export class QwcArcObservers extends LitElement {
  
    static styles = css`
        .arctable {
            height: 100%;
            padding-bottom: 10px;
        }

        code {
            font-size: 80%;
        }

        .text {
            font-size: 80%;
        }

        .method {
            color: #e94cb6;
        }

        .annotation {
            color: grey;
        }

        .badge {
            font-size: 80%;
            color: grey;
            padding: 4px 8px;
            text-align: center;
            border-radius: 5px;
            background-color: lightblue;
        }

        vaadin-icon {
            font-size: 80%;
            color: grey;
        }
        `;

    static properties = {
        _observers: {attribute: false}
    };
  
    constructor() {
        super();
        this._observers = observers;
    }
  
    connectedCallback() {
        super.connectedCallback();
    }

    render() {
        if(this._observers){

            return html`
                <vaadin-grid .items="${this._observers}" class="arctable" theme="no-border">

                    <vaadin-grid-column auto-width
                        header="Source"
                        ${columnBodyRenderer(this._sourceRenderer, [])}
                        resizable>
                    </vaadin-grid-column>

                    <vaadin-grid-column auto-width
                        header="Observed Type / Qualifiers"
                        ${columnBodyRenderer(this._typeRenderer, [])}
                        resizable>
                    </vaadin-grid-column>

                    <vaadin-grid-column auto-width
                        header="Priority"
                        ${columnBodyRenderer(this._priorityRenderer, [])}
                        resizable>
                    </vaadin-grid-column>

                    <vaadin-grid-column auto-width
                        header="Reception"
                        ${columnBodyRenderer(this._receptionRenderer, [])}
                        resizable>
                    </vaadin-grid-column>

                    <vaadin-grid-column auto-width
                        header="Transaction Phase"
                        ${columnBodyRenderer(this._transactionPhaseRenderer, [])}
                        resizable>
                    </vaadin-grid-column>

                    <vaadin-grid-column auto-width 
                        header="Async"
                        ${columnBodyRenderer(this._asyncRenderer, [])}
                        resizable>
                    </vaadin-grid-column>

                </vaadin-grid>`;
        }
  }

  _sourceRenderer(bean){
    return html`
      <code>${bean.declaringClass.name}</code><code class="method">#${bean.methodName}()</code>
    `;
  }

  _typeRenderer(bean){
    return html`
      ${bean.qualifiers.map(qualifier=>
        html`${this._qualifierRenderer(qualifier)}`
      )}
      <code>${bean.observedType.name}</code>  
    `;
  }

  _qualifierRenderer(qualifier){
    if(qualifier){
      return html`<code class="annotation" title="${qualifier.name}">${qualifier.simpleName}</code><br/>`;
    }
  }

  _priorityRenderer(bean){
    return html`
      <span class="badge">${bean.priority}</span>  
    `;
  }

  _receptionRenderer(bean){
    return html`
      <span class="text">${this._camelize(bean.reception)}</span>  
    `;
  }

  _transactionPhaseRenderer(bean){
    return html`
      <span class="text">${this._camelize(bean.transactionPhase)}</span>  
    `;
  }

  _asyncRenderer(bean){
    if(bean.async === false){
      return html`
        <vaadin-icon icon="font-awesome-solid:xmark"></vaadin-icon>
      `;
    }else{
      return html`
        <vaadin-icon icon="font-awesome-solid:check"></vaadin-icon>
      `;
    }
  }

  _camelize(str) {
    const s = str.replace(/(?:^\w|[A-Z]|\b\w|\s+)/g, function(match, index) {
      if (+match === 0) return "";
      return index === 0 ? match.toUpperCase() : match.toLowerCase();
    });

    return s.replaceAll('_', ' ');
  }
}
customElements.define('qwc-arc-observers', QwcArcObservers);