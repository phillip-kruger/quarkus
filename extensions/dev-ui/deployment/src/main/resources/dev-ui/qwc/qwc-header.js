import { LitElement, html, css} from 'lit';
import { NotificationController } from 'controller/notification-controller.js';
import '@vaadin/icon';
import './qwc-version-info.js';

/**
 * This component represent the Dev UI Header
 */
export class QwcHeader extends LitElement {
    notificationController = new NotificationController(this);

    static styles = css`
        .top-bar {
            height: 60px;
        }
        .horizontal-flex {
            display: flex;
            align-items:center;
        }

        .top-bar img {
            height: 40px;
            padding: 10px;
        }

        .logo-right-actions {
            position: absolute;
            right: 10px;
        }
        
        .logo-reload-click {
            cursor: pointer;
            display: flex;
            align-items:center;
            font-size: xx-large;
            color: #20446B;
        }

        .logo-reload-click:hover {
            filter: brightness(90%);
        }

        .icon {
            width: 25px;
            height: 25px;
            cursor: pointer;
            padding-left: 5px;
        }

        .zulip {
            color: #5e91f6;
        }

        .twitter {
            color: #1DA1F2;
        }

        .github {
           color: #333;
        }

        .breadcrumb {
            display: flex;
            align-items:center;
            font-size: x-large;
            color: #6d7279;
            padding-left: 100px;
        }

        qwc-version-info {
            display: flex;
            justify-content: center;
            align-content: center;
            flex-direction: column;
            font-size: small;
            color: #20446B;
            padding-right: 10px;
        }
        `;

        static properties = {
            _breadcrumb: {state:true}
        };
        
    render() {
        return html`
        <div class="top-bar horizontal-flex">
            <span class="logo-reload-click" title="Click to reload" @click="${this._reload}"><img src="img/light_icon.svg"></img> Dev UI</span>
            <span class="breadcrumb">${this._breadcrumb}</span>
            
            <div class="logo-right-actions horizontal-flex">
                <qwc-version-info></qwc-version-info>
                
                <vaadin-icon class="icon zulip" icon="font-awesome-solid:comment" @click="${this._zulip}" title="Go to the Quarkus Zulip page"></vaadin-icon>
                <vaadin-icon class="icon twitter" icon="font-awesome-brands:twitter" @click="${this._twitter}" title="Go to the Quarkus Twitter page"></vaadin-icon>
                <vaadin-icon class="icon github" icon="font-awesome-brands:github" @click="${this._github}" title="Go to the Quarkus GitHub page"></vaadin-icon>
                <vaadin-icon class="icon" icon="font-awesome-solid:circle-half-stroke" @click="${this._dayNight}" title="Day / Night switch"></vaadin-icon>
            </div>
        </div>
        `;
    }

    connectedCallback() {
        super.connectedCallback();
        addEventListener('switchPage', (e) => { 
            // TODO: Only show on small menu ?
            this._breadcrumb = e.detail.display;
          }, false);
    }

    disconnectedCallback() {
        super.disconnectedCallback();
    }

    _twitter(e) {
        window.open("https://twitter.com/QuarkusIO", '_blank').focus();
    }

    _github(e) {
        window.open("https://github.com/quarkusio", '_blank').focus();
    }

    _zulip(e) {
        window.open("https://quarkusio.zulipchat.com", '_blank').focus();
    }
    
    _dayNight(e){
        this.notificationController.showInfoMessage("This is not implemented yet", "Day-night switch");
    }

    _reload(e){
        console.log("TODO: Reload");
    }
}

customElements.define('qwc-header', QwcHeader);