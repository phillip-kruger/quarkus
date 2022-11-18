import { LitElement, html, css} from 'lit';
import '@vaadin/split-layout';
/**
 * This component shows the Main content
 */
export class QwcMain extends LitElement {
    static styles = css`
        .pageAndFooter {
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            height:100%;
        }
        #pageslot ::slotted(*) {
            overflow: scroll;
        }
        #footerslot ::slotted(*) {
            
        }
    `;

    static properties = {
    
    };
  
    constructor() {
        super();
    }

    render() {
        return html`
            <div class="pageAndFooter">
                <div id="pageslot">
                    <slot name="page"></slot>
                </div>
                <div id="footerslot">
                    <slot name="footer"></slot>
                <div>
            </div>
        `;
  }

}
customElements.define('qwc-main', QwcMain);