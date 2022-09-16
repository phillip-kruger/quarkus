import { LitElement, html, css } from 'https://unpkg.com/lit@2.3.1/index.js?module';
import 'https://unpkg.com/@vaadin/icon@23.1.6/vaadin-icon.js?module';

/**
 * This component adds a custom link on the Extension card
 */
export class QwcExtensionLink extends LitElement {
  
  static styles = css`
   .extensionLink {
      display: flex;
      flex-direction: row;
      justify-content: flex-start;
      align-items: center;
      color: #1C3D61;
			font-size: small;
			padding: 2px 5px;
			cursor: pointer;
      text-decoration: none;
		}
		.extensionLink:hover {
			color: #ff004a;
			background-color: #f8fafc;
		}
    .icon {
      padding-right: 5px;
    }
  `;

  static properties = {
    extensionName: {type: String},
    iconName: {type: String},
    displayName: {type: String},
    label: {type: String},
    path:  {type: String}
  };

  render() {
   
    return html`
      <a class="extensionLink" href="${this.path}">
        <vaadin-icon class="icon" icon="${this.iconName}"></vaadin-icon>
        ${this.displayName}
      </a>
    `;
  }

}
customElements.define('qwc-extension-link', QwcExtensionLink);