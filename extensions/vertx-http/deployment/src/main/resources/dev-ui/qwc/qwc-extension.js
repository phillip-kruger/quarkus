import {LitElement, html, css} from 'https://unpkg.com/lit@2.3.0/index.js?module';

/**
 * This component represent one extension
 * It's a card on the extension board
 */
export class QwcExtension extends LitElement {
  
  static styles = css`
        .card {
          border-radius: 5px;
        }

        .active {
          box-shadow: 0 4px 8px 0 rgba(0,0,0,0.2);
          transition: 0.3s;
        }
        .active:hover {
          box-shadow: 0 8px 16px 0 rgba(0,0,0,0.2);
        }
        .inactive {
          box-shadow: 0 1px 2px 0 rgba(0,0,0,0.2);
        }
        .container {
          color: grey;
        }
        .container h4 {
          font-weight: bold;
          background-color: rgba(28,110,164,0.03);
          border-bottom: 1px solid rgba(28,110,164,0.1);
          padding: 10px 10px;
          color: #4695EB;
        }
        .container p {
          padding: 10px 10px;
        }
        `;

  static properties = {
    name: {type: String},
    description: {type: String},
    guide: {type: String},
    class: {type: String},
  };
  
  render() {
    return html`
    
      <div class="card ${this.class}">
        <div class="container">
          <h4>${this.name}</h4>
          <p>${this.description}</p>
        </div>
      </div>`;
  }

  _guide(e) {
    window.open(this.guide, '_blank').focus();
  }

  _configuration(e) {
    console.log("configuration: " + e);
  }
}

customElements.define('qwc-extension', QwcExtension);