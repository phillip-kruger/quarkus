import { LitElement, html, css} from 'lit';

import '@vaadin/tabs';
//import '@vaadin/tabsheet';

import { removedBeans } from 'arc-data';
import { removedComponents } from 'arc-data';
import { removedDecorators } from 'arc-data';
import { removedInterceptors } from 'arc-data';

/**
 * This component shows the Arc RemovedComponents
 */
export class QwcArcRemovedComponents extends LitElement {
    static styles = css`
    `;

    static properties = {
        _removedBeans: {attribute: false},
        _removedComponents: {attribute: false},
        _removedDecorators: {attribute: false},
        _removedInterceptors: {attribute: false},
        _content: {state: true},
    };

    constructor() {
        super();
        this._removedBeans = removedBeans;
        this._removedComponents = removedComponents;
        this._removedDecorators = removedDecorators;
        this._removedInterceptors = removedInterceptors;
        
        this._content = JSON.stringify(this._removedBeans);
    }

    render() {
        return html`
            <vaadin-tabs theme="minimal" @selected-changed="${this._selectedChanged}">
                <vaadin-tab>Removed beans</vaadin-tab>
                <vaadin-tab>Removed components</vaadin-tab>
                <vaadin-tab>Removed decorators</vaadin-tab>
                <vaadin-tab>Removed interceptors</vaadin-tab>
            </vaadin-tabs>

            <div class="content">
                <p>${this._content}</p>
            </div>
        `;
    }

    _selectedChanged(e) {
        
        var json = JSON.stringify(e.detail);
        console.log("----------> _selectedChanged = " + json);
        this._content = html`This is the ${json} tab`;
    }

}
customElements.define('qwc-arc-removed-components', QwcArcRemovedComponents);