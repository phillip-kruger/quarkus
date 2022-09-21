import { LitElement, html, css} from 'lit';

/**
 * This component represent the Dev UI Log
 */
export class QwcLog extends LitElement {
    static styles = css`
        .todo {
            font-size: small;
            color: #4695EB;
            padding-left: 10px;
            background: white;
            height: 100%;
        }
    `;

    render() {
        return html`<div class="todo">TODO: To be implemented</div>`;
    }

}

customElements.define('qwc-log', QwcLog);