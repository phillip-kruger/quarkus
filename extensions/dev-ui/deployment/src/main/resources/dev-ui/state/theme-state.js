import { themes } from 'devui-data';
import { LitState } from 'lit-element-state';

class ThemeState extends LitState {

    currentTheme;

    constructor() {
        super();
        this._themes = themes;
        
        if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
            this.changeTo("dark");
        }else{
            this.changeTo("light");
        }
    
        window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e => {
            if(e.matches){
                this.changeTo("dark");
            }else{
                this.changeTo("light");    
            }
        });
    }

    static get stateVars() {
        return {
            theme: {}
        };
    }
    
    changeTo(themeName){
        
        var colorMap;
        var icon;
        var quarkusBlue;
        var quarkusRed;
        var quarkusCenter;
        
        if(themeName==="dark"){
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
        
        this.currentTheme = new Object();
        this.currentTheme.theme = themeName;
        this.currentTheme.icon  = icon;
        this.currentTheme.quarkusBlue = quarkusBlue;
        this.currentTheme.quarkusRed = quarkusRed;
        this.currentTheme.quarkusCenter = quarkusCenter;
        
        return this.currentTheme;
    }
    
    getCurrentTheme(){
        return this.currentTheme;
    }
    
}

export const themeState = new ThemeState();