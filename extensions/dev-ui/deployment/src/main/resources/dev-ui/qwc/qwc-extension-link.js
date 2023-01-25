import { LitElement, html, css} from 'lit';
import '@vaadin/icon';

/**
 * This component adds a custom link on the Extension card
 */
export class QwcExtensionLink extends LitElement {
  
    static styles = css`
        .extensionLink {
            display: flex;
            flex-direction: row;
            justify-content: space-between;
            align-items: center;
            color: var(--qwc-color-1, black);
            font-size: small;
            padding: 2px 5px;
            cursor: pointer;
            text-decoration: none;
        }
        .extensionLink:hover {
            filter: brightness(80%);
        }
        .icon {
            padding-right: 5px;
        }
        .iconAndName {
            display: flex;
            flex-direction: row;
            justify-content: flex-start;
            align-items: center;
            color: var(--qwc-color-1, black);
        }
        .iconAndName:hover {
            color: var(--qwc-logo-2, red);
        }
        .badge {
            color: var(--qwc-mute-1, grey);
            text-align: center;
            font-size: x-small;
            border-radius: 8px;
            padding: 2px 5px;
        }
    `;

    static properties = {
        extensionName: {type: String},
        iconName: {type: String},
        displayName: {type: String},
        label: {type: String},
        path:  {type: String},
        webcomponent: {type: String}, 
    };

    render() {
        let badge;

        if (this.label) {
            badge = html`<span class="badge">${this.label}</span>`;
        }

        let routerIgnore = false;
        if(this.webcomponent === ""){
            routerIgnore = true;
        }
        return html`
        <a class="extensionLink" href="${this.path}" ?router-ignore=${routerIgnore}>
            <span class="iconAndName">
                <vaadin-icon class="icon" icon="${this.iconName}"></vaadin-icon>
                ${this.displayName} 
            </span>
            ${badge} 
        </a>
        `;
    }
}
customElements.define('qwc-extension-link', QwcExtensionLink);