import { LitElement, html, css} from 'lit';

/**
 * This component represent the Server Log
 */
export class QwcServerLog extends LitElement {
    static styles = css`
        .todo {
            font-size: small;
            color: #4695EB;
            padding-left: 10px;
            background: #2C2C2C;
            height: 100%;
        }
    `;

    render() {
        return html`<div class="todo">TODO: To be implemented</div>`;
    }

}

customElements.define('qwc-server-log', QwcServerLog);