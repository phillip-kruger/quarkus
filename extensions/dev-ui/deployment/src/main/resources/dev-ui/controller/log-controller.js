/**
 * Control buttons for the log(s) at the bottom
 */
export class LogController {
    static _controllers = new Map();
    
    host;
    tab;
    items = [];
    
    constructor(host, tab) {
        (this.host = host).addController(this);
        this.tab = tab.toLowerCase();
    }

    hostConnected() {
        LogController._controllers.set(this.tab, this);
    }

    hostDisconnected() {
        LogController._controllers.delete(this.tab);
    }
    
    addItem(title, icon, color, callback){
        var item = {
            component: this._createItem(icon, title, color),
            callback: callback,
            isToggle: false
        };
        this.items.push(item);
        return this;
    }
    
    addToggle(title, selected, callback){
        var item = {
            component: this._createToggle(title, selected),
            callback: callback,
            isToggle: true
        };
        this.items.push(item);
        return this;
    }
    
    addFollow(title, selected, callback){
        var item = {
            component: this._createFollow(title, selected),
            callback: callback,
            isToggle: true
        };
        this.items.push(item);
        return this;
    }
    
    _createItem(icon, title, color) {
        var style = `font-size: x-small;cursor: pointer;color: ${color};`;
        
        // style = style + `animation: rotation 3s infinite linear;`;
        
        
        const item = document.createElement('vaadin-context-menu-item');
        const vaadinicon = document.createElement('vaadin-icon');
        item.setAttribute('aria-label', `${title}`);
        vaadinicon.setAttribute('icon', `${icon}`);
        vaadinicon.setAttribute('style', `${style}`);
        vaadinicon.setAttribute('title', `${title}`);
        item.appendChild(vaadinicon);
        return item;
    }
    
    _createToggle(title, selected){
        var color = "grey";
        var icon = "font-awesome-solid:toggle-off";
        if(selected){
            color = "#4695EB";
            icon = "font-awesome-solid:toggle-on";
        }
        return this._createItem(icon,title,color);
    }
    
    _createFollow(title, selected){
        var color = "grey";
        var icon = "font-awesome-regular:circle";
        if(selected){
            color = "green";
            icon = "font-awesome-regular:circle-dot";
        }
        return this._createItem(icon,title,color);
    }
    
    static getItemsForTab(tabName){
        var t = tabName.toLowerCase();
        if(LogController._controllers.has(t)){
            return LogController._controllers.get(t).items;
        }else {
            return [];
        }
    }
    
    static fireCallback(e){
        if(e.detail.value.isToggle){
            if(e.detail.value.component.firstChild.icon.endsWith('-on')){
                // switching off
                e.detail.value.component.firstChild.icon = "font-awesome-solid:toggle-off";
                e.detail.value.component.firstChild.style.color = "grey";
                e.detail.value.callback(false);
            }else if(e.detail.value.component.firstChild.icon.endsWith('-off')){
                // switching on
                e.detail.value.component.firstChild.icon = "font-awesome-solid:toggle-on";
                e.detail.value.component.firstChild.style.color = "#4695EB";
                e.detail.value.callback(true); 
            }else if(e.detail.value.component.firstChild.icon.endsWith('circle-dot')){
                // switching off
                e.detail.value.component.firstChild.icon = "font-awesome-regular:circle";
                e.detail.value.component.firstChild.style.color = "grey";
                e.detail.value.callback(false);
            }else if(e.detail.value.component.firstChild.icon.endsWith('circle')){
                // switching on
                e.detail.value.component.firstChild.icon = "font-awesome-regular:circle-dot";
                e.detail.value.component.firstChild.style.color = "green";
                e.detail.value.callback(true); 
            }
            
        }else{
            e.detail.value.callback(e); 
        }
        
        
    }
}