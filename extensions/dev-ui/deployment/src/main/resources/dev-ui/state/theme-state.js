import { themes } from 'devui-data';
import { LitState } from 'lit-element-state';

class ThemeState extends LitState {

    constructor() {
        super();
        this._themes = themes;
    }

    static get stateVars() {
        return {
            theme: {}
        };
    }
    
    changeTo(themeName){
        const newTheme = new Object();
        newTheme.name = themeName;
        
        var colorMap;
        
        if(themeName==="dark"){
            newTheme.icon = "moon";
            colorMap = this._themes.dark;
        }else{
            newTheme.icon = "sun";
            colorMap = this._themes.light;
        }
        
        for (const [key, value] of Object.entries(colorMap)) {
            document.body.style.setProperty(key, value);
            if(key === "--quarkus-blue"){
                newTheme.quarkusBlue = value;
            }else if(key === "--quarkus-red"){
                newTheme.quarkusRed = value;
            }else if(key === "--quarkus-center"){
                newTheme.quarkusCenter = value;
            }
        }
        
        return newTheme;
    }
}

export const themeState = new ThemeState();