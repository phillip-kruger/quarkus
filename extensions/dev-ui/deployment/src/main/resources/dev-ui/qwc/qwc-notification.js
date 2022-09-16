import { LitElement, html, css } from 'https://unpkg.com/lit@2.3.1/index.js?module';
import { until } from 'https://unpkg.com/lit@2.3.1/directives/until.js?module';
import 'https://unpkg.com/@vaadin/icon@23.1.6/vaadin-icon.js?module';

/**
 * This component shows any notification events
 * Work with the Notification controller
 */
export class QwcNotification extends LitElement {
  static styles = css`
    .danger {
      background-color: #f44336;
    }
    .success {
      background-color: #04AA6D;
    }
    .info {
      background-color: #2196F3;
    }
    .warning {
      background-color: #ff9800;
    }
    .danger-text {
      color: #f44336;
      border: 1px solid #f44336;
    }
    .success-text {
      color: #04AA6D;
      border: 1px solid #04AA6D;
    }
    .info-text {
      color: #2196F3;
      border: 1px solid #2196F3;
    }
    .warning-text {
      color: #ff9800;
      border: 1px solid #ff9800;
    }

    .notificationScroller {
      position: absolute;
      top: 35px;
      right: 20px;
      margin-left: 20px;
      max-width: 450px;
      height: 90vh;
      display: flex;
      flex-direction: column;
      justify-content: flex-start;
      align-items: center;
      gap: 10px;
      z-index: 999;
    }

    .callout {
      max-width: 400px;
      -webkit-box-shadow: 5px 5px 15px 5px #000000; 
      box-shadow: 5px 5px 15px 5px #000000;
    }

    .callout-header {
      display: flex;
      flex-direction: row;
      justify-content: space-between;
      align-items: center;
      padding: 10px 10px;
      font-size: 20px;
      color: white;
    }

    .callout-container {
      padding: 10px 10px;
      background-color: white;
    }

    .closeIcon {
      font-size: 15px;
      cursor: pointer;
    }

    .closeIcon:hover {
      color: lightgrey;
    }
    .hide {
      display: none;
    }
  `;

  static properties = {
    _notifications: {state: true},
    _counter:{state: true}
  };

  constructor(){
    super();
    this._notifications = new Map();
    this._counter = 0;
    // Receive notification
    document.addEventListener('notification', (e) => { 
      var uid = this._counter++;
      this._notifications.set(uid, e.detail);
    }, false);

  }

  connectedCallback() {
    super.connectedCallback();
  }

  render() {
    return html`${until(this._renderNotifications(), html``)}`;
  }

  _renderNotifications(){
    if(this._notifications && this._notifications.size > 0){
      const itemTemplates = [];
      this._notifications.forEach((value, key) => {
        itemTemplates.push(this._renderNotification(key, value));
      });
      return html`<div class="notificationScroller">
        ${itemTemplates}
        </div>`;
    }
  }

  _renderNotification(id, notification){
    let closeClass = "";
    if(notification.autoclose){
      setTimeout(() => {
        this._close(id);
      }, 4000);
      closeClass = "hide";
    }
    return html`
    <div class="callout ${notification.type}-text">
      <div class="callout-header ${notification.type}">
        ${notification.title}
        <vaadin-icon class="closeIcon ${closeClass}" icon="font-awesome-solid:xmark" @click="${() => this._close(id)}"></vaadin-icon>
      </div>
      <div class="callout-container">
        <p>${notification.message}</p>
      </div>
    </div>`;
  }

  _close(id){
    this._notifications.delete(id);
    this.requestUpdate();
  }
}
customElements.define('qwc-notification', QwcNotification);