import { LitElement, html, css} from 'lit';

/**
 * This component shows the Arc Fired Events
 */
export class QwcArcFiredEvents extends LitElement {

    static styles = css`
        .todo {
            font-size: small;
            color: #4695EB;
            padding-left: 10px;
            background: white;
            height: 100%;
        }`;

    static properties = {
        _firedEvents: {attribute: false}
    };
  
    connectedCallback() {
        super.connectedCallback();
    }

    render() {
        return html`<div class="todo">Loading fired events...</div>`;
    }
}
customElements.define('qwc-arc-fired-events', QwcArcFiredEvents);