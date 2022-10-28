import { LitElement, html, css} from 'lit';
import { until } from 'lit/directives/until.js';
import { JsonRpcController } from 'jsonrpc-controller';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';
import '@vaadin/grid';

/**
 * This component shows the Arc Interceptors
 */
export class QwcArcInterceptors extends LitElement {
  static methodName = "getInterceptors";
  jsonRpcController = new JsonRpcController(this, "ArC");

  static styles = css`
        .arctable {
          height: 100%;
          padding-bottom: 10px;
        }

        code {
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
        `;

  static properties = {
    _interceptors: {state: true}
  };
  
  connectedCallback() {
    super.connectedCallback();
    this.jsonRpcController.request(QwcArcInterceptors.methodName);
  }

  onJsonRpcResponse(result){
    this._interceptors = result;

    let foo = JSON.stringify(this._interceptors);
    console.log(foo);
  }

  render() {
    return html`${until(this._renderJsonRpcResponse(), html`<span>Loading ArC interceptors...</span>`)}`;
  }

  _renderJsonRpcResponse(){
    if(this._interceptors){
      return html`
      <vaadin-grid .items="${this._interceptors}" class="arctable" theme="no-border">
        <vaadin-grid-column auto-width
          header="Interceptor Class"
          ${columnBodyRenderer(this._classRenderer, [])}
          resizable>
        </vaadin-grid-column>

        <vaadin-grid-column auto-width
          header="Priority"
          ${columnBodyRenderer(this._priorityRenderer, [])}
          resizable>
        </vaadin-grid-column>

        <vaadin-grid-column auto-width
          header="Bindings"
          ${columnBodyRenderer(this._bindingsRenderer, [])}
          resizable>
        </vaadin-grid-column>

        <vaadin-grid-column auto-width
          header="Interception Types"
          ${columnBodyRenderer(this._typeRenderer, [])}
          resizable>
        </vaadin-grid-column>
      </vaadin-grid>
      `;
    }
  }

  _classRenderer(bean){
    return html`
      <code>${bean.interceptorClass.name}</code>
    `;
  }

  _priorityRenderer(bean){
    return html`
      <span class="badge">${bean.priority}</span>  
    `;
  }

  _bindingsRenderer(bean){
      return html`
        ${bean.bindings.map(binding=>
          html`<code class="annotation" title="${binding.name}">${binding.simpleName}</code><br/>`
        )}
      `;
  }

  _typeRenderer(bean){
    return html`${bean.intercepts}<br/>
    <code>${bean.interceptorClass.simpleName}</code><code class="method">#${bean.methodName}()</code>`;
  }

}
customElements.define('qwc-arc-interceptors', QwcArcInterceptors);