import { LitElement, html, css} from 'lit';

/**
 * This component loads an external page
 */
export class QwcExternalPage extends LitElement {
  
    static styles = css`
        
    `;

    static properties = {
        externalUrl: {type: String},
        content: {type: String},
    };

    constructor() {
        super();
    }

    connectedCallback() {
        super.connectedCallback()
        fetch(externalUrl)
            .then((content) => {
                this.content = content;
            });
      }

    render() {
        return html`${this.content}`;
    }
}
customElements.define('qwc-external-page', QwcExternalPage);