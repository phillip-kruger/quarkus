import { themes } from 'devui-data';

export class ThemeController {
    static currentTheme;
    
    host;
    _themes;
    constructor(host) {
        (this.host = host).addController(this);
        this._themes = themes;
        if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
            this._setTheme("dark");
        }else{
            this._setTheme("light");
        }
    
        window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e => {
            const newColorScheme = e.matches ? "dark" : "light";
            this._setTheme(newColorScheme);
        });
    }

    changeTheme(theme){
        this._setTheme(theme);
    }

    _setTheme(theme){
        var colorMap;
        var icon;
        var quarkusBlue;
        var quarkusRed;
        var quarkusCenter;
        
        if(theme==="dark"){
            icon = "moon";
            colorMap = this._themes.dark;
        }else{
            icon = "sun";
            colorMap = this._themes.light;
        }
        
        for (const [key, value] of Object.entries(colorMap)) {
            document.body.style.setProperty(key, value);
            if(key === "--quarkus-blue"){
                quarkusBlue = value;
            }else if(key === "--quarkus-red"){
                quarkusRed = value;
            }else if(key === "--quarkus-center"){
                quarkusCenter = value;
            }
        }
        
        // Also fire an event in case anyone is interested
        var themeDetails = new Object();
            themeDetails.theme = theme;
            themeDetails.icon  = icon;
            themeDetails.quarkusBlue = quarkusBlue;
            themeDetails.quarkusRed = quarkusRed;
            themeDetails.quarkusCenter = quarkusCenter;
        
        ThemeController.currentTheme = themeDetails;
        
        const event = new CustomEvent('themeChange', { 
          detail: themeDetails 
        });
        document.dispatchEvent(event);
        
        
        
    }  

}