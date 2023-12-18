import { LitElement, html, css} from 'lit';

/**
 * Switch UI Component 
 */
export class QuiSwitch extends LitElement {
    
    static styles = css`
        label {
            color: pink;
        }
        input {
            appearance: none;
            position: relative;
            display: inline-block;
            background: var(--lumo-contrast-30pct);
            height: 1.65rem;
            width: 2.75rem;
            vertical-align: middle;
            border-radius: 2rem;
            box-shadow: 0px 1px 3px #0003 inset;
            transition: 0.25s linear background;
        }
        input::before {
            content: "";
            display: block;
            width: 1.25rem;
            height: 1.25rem;
            background: var(--lumo-base-color);
            border-radius: 1.2rem;
            position: absolute;
            top: 0.2rem;
            left: 0.2rem;
            box-shadow: 0px 1px 3px #0003;
            transition: 0.25s linear transform;
            transform: translateX(0rem);
        }
        input:focus {
            outline: none;
        }
        :checked {
            background: var(--lumo-success-color);
        }
        :checked::before {
            transform: translateX(1rem);
        }
        `;

    static properties = {
        // Tag attributes
        label: {type: String} // Optional label
    };

    constructor(){
        super();
        this.label = null;
    }

    connectedCallback() {
        super.connectedCallback()
    }
    
    render() {
        return html`<label>
                        <input type="checkbox" />
                        ${this._renderLabel()}
                    </label>`;
    }

    _renderLabel(){
        if(this.label){
            return html`${this.label}`;
        }
    }

}
customElements.define('qui-switch', QuiSwitch);