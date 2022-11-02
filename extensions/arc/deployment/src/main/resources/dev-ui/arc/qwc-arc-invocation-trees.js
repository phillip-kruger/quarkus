import { LitElement, html, css} from 'lit';

/**
 * This component shows the Arc Invocation Trees
 */
export class QwcArcInvocationTrees extends LitElement {
  
  static styles = css`
        .todo {
            font-size: small;
            color: #4695EB;
            padding-left: 10px;
            background: white;
            height: 100%;
        }`;

    static properties = {
    
    };
  
    connectedCallback() {
        super.connectedCallback();
    }

    render() {
        return html`<div class="todo">Loading invocation trees...</div>`;
    }
}
customElements.define('qwc-arc-invocation-trees', QwcArcInvocationTrees);