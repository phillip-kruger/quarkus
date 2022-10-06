import { LitElement, html, css} from 'lit';
import { until } from 'lit/directives/until.js';
import { JsonRpcController } from 'controller/jsonrpc-controller.js';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';
import '@vaadin/grid';

/**
 * This component shows the Arc Beans
 */
export class QwcArcBeans extends LitElement {
  static methodName = "getBeans";
  jsonRpcController = new JsonRpcController(this, "ArC");

  static styles = css`
        .arctable {
          height: 100%;
          padding-bottom: 10px;
        }

        code {
          font-size: 80%;
        }

        .annotation {
          color: grey;
        }

        .producer {
          color: #e94cb6;
        }

        .badge {
          font-size: 80%;
          color: grey;
          padding: 4px 8px;
          text-align: center;
          border-radius: 5px;
        }
        
        .class {
          background-color: lightblue;
        }

        .method {
          background-color: lightgreen;
        }

        .synthetic {
          background-color: lightgrey;
        }

        .interceptor {
          background-color: lightpink;
        }
        `;

  static properties = {
    _beans: {state: true}
  };
  
  connectedCallback() {
    super.connectedCallback();
    this.jsonRpcController.request(QwcArcBeans.methodName);
  }

  onJsonRpcResponse(result){
    this._beans = result;
  }

  render() {
    return html`${until(this._renderJsonRpcResponse(), html`<span>Loading ArC beans...</span>`)}`;
  }

  _renderJsonRpcResponse(){
    if(this._beans){
        return html`
        <vaadin-grid .items="${this._beans}" class="arctable" >

          <vaadin-grid-column 
            header="Bean"
            path="providerType.name"
            ${columnBodyRenderer(this._beanRenderer, [])}
            resizable>
          </vaadin-grid-column>
          
          <vaadin-grid-column 
            header="Kind"
            path="kind"
            ${columnBodyRenderer(this._kindRenderer, [])}
            resizable>
          </vaadin-grid-column>

          <vaadin-grid-column 
            header="Associated Interceptors"
            path="interceptors"
            ${columnBodyRenderer(this._interceptorsRenderer, [])}
            resizable>
          </vaadin-grid-column>
        </vaadin-grid>
        
        `;
    }
  }

  _beanRenderer(bean){
    return html`
      <code class="annotation">@${bean.scope.simpleName}</code>
      ${bean.nonDefaultQualifiers.map(qualifier=>
        html`${this._qualifierRenderer(qualifier)}`
      )}
      <br/><code>${bean.providerType.name}</code>
    `;
  };

  _kindRenderer(bean){
    return html`
      <span class="badge ${bean.kind.toLowerCase()}">${this._camelize(bean.kind)}</span>
      ${bean.declaringClass
        ? html`<br/><code class="producer">${bean.declaringClass.simpleName}.${bean.memberName}()</code>`
        : html`<br/><code class="producer">${bean.memberName}</code>`
      }
    `;
  };

  _interceptorsRenderer(bean){
    if(bean.interceptors && bean.interceptors.length > 0){
      return html`
        ${bean.interceptorInfos.map(interceptor=>
          html`<code>${interceptor.interceptorClass.name}</code> <span class="badge interceptor">${interceptor.priority}</span><br/>`
        )}
      `;
    }
  }

  _qualifierRenderer(qualifier){
    return html`<br/><code class="annotation">${qualifier.simpleName}</code>`;
  }

  _camelize(str) {
    return str.replace(/(?:^\w|[A-Z]|\b\w|\s+)/g, function(match, index) {
      if (+match === 0) return "";
      return index === 0 ? match.toUpperCase() : match.toLowerCase();
    });
  }

}
customElements.define('qwc-arc-beans', QwcArcBeans);