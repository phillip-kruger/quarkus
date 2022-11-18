import { LitElement, html, css} from 'lit';

import '@vaadin/tabs';
import '@vaadin/icon';

/**
 * This component shows the Bottom Drawer
 */
export class QwcFooter extends LitElement {
    static styles = css`
        .footer {
            width: 100%;
            min-height: 50px;
            display: flex;
            flex-direction: column;
        }
        vaadin-tabs {
            background: #4695EB;
            color: white;
            border-radius: 15px 15px 0px 0px;
        }
        .footerContent {
            overflow: scroll;
        }
  `;

  static properties = {
     _content: {state: true},
     _display: {state: true},
  };
  
  constructor() {
    super();
    this._display = "none";
    this._content = "Initial content";
  }

  render() {
        return html`
            <div class=footer">
                <vaadin-tabs theme="minimal" @selected-changed="${this._selectedChanged}" @dblclick="${this._doubleClicked}">
                    <vaadin-tab></vaadin-tab>
                    <vaadin-tab></vaadin-tab>
                </vaadin-tabs>

                <div class="footerContent" style="display: ${this._display};">
                    <p>${this._content}</p>
                </div>
            </div>
        `
    }

    _doubleClicked(e){
        if(this._display === "none"){
            this._display = "block";
        }else{
            this._display = "none";
        }
    }

    _selectedChanged(e) {
        
        var json = JSON.stringify(e.detail);
        console.log("----------> _selectedChanged = " + json);
        this._content = html`This is the ${json} tab`;
    }

}
customElements.define('qwc-footer', QwcFooter);