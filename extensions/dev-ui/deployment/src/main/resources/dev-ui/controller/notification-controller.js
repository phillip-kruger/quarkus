export class NotificationController {
  host;
  
  constructor(host) {
    (this.host = host).addController(this);
    this.timeFormatter = new Intl.DateTimeFormat("en" , {
      timeStyle: "medium",
      dateStyle: "short"
    });
  }

  showInfoMessage(message, title = "", autoclose = true){
    this.showMessage("info", message, title);
  }

  showSuccessMessage(message, title = "", autoclose = true){
    this.showMessage("success", message, title);
  }

  showWarningMessage(message, title = "", autoclose = true){
    this.showMessage("warning", message, title);
  }

  showErrorMessage(message, title = "", autoclose = true){
    this.showMessage("danger", message, title);
  }

  showMessage(icon, message, title = "", autoclose = true){
    var notification = new Object();
    notification.title = title;
    notification.type  = icon;
    notification.message = message;
    notification.autoclose = autoclose;
    notification.time = this.timeFormatter.format(Date.now());
    
    const event = new CustomEvent('notification', { 
      detail: notification 
    });
    document.dispatchEvent(event);
  }
}