import { LitElement, html, css} from 'lit';

/**
 * This component shows the Quarkus version
 */
export class QwcQuarkusVersion extends LitElement {

    static styles = css`
        span {
            cursor: pointer;
            font-size: small;
            color: white;
        }
        span:hover {
            color: #ff004a;
        }`;

    static properties = {
        version: {type: String},
    };

    render() {
        return html`<span @click="${this._quarkus}">Quarkus ${this.version}</span>`;
    }

    _quarkus(e) {
        window.open("https://quarkus.io", '_blank').focus();
    }
}
customElements.define('qwc-quarkus-version', QwcQuarkusVersion);