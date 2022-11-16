import { LitElement, html, css} from 'lit';
import { until } from 'lit/directives/until.js';
import { JsonRpc } from 'jsonrpc';
import '@vaadin/icon';
import '@vaadin/button';
import '@vaadin/grid';
import '@vaadin/grid/vaadin-grid-sort-column.js';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';

/**
 * This component shows the current caches and allow clearing of cache
 */
export class QwcCacheCaches extends LitElement {
    jsonRpc = new JsonRpc("Cache");
    
    static styles = css`
        .button {
            background-color: transparent;
            cursor: pointer;
        }
        .clearIcon {
            color: orange;
        }
        `;
    
    static properties = {
        _caches: {state:true},
    };
    
    connectedCallback() {
        super.connectedCallback();
        this.jsonRpc.getCacheInfos().then(caches => {
            this._caches = new Map(); // map cache by name
            caches.result.forEach(c => {
                this._caches.set(c.name, c);
            });
        });
    }
    
    render() {
        return html`${until(this._renderCaches(), html`<span>Loading Caches...</span>`)}`;
    }
    
    _renderCaches(){
        if(this._caches){
            let caches = [...this._caches.values()];
            return html`<vaadin-grid .items="${caches}" class="datatable" theme="no-border">
                <vaadin-grid-sort-column auto-width
                    header="Name"
                    ${columnBodyRenderer(this._nameRenderer, [])}
                    resizable>
                </vaadin-grid-sort-column>
            
                <vaadin-grid-sort-column path="size" resizable></vaadin-grid-sort-column>
                <vaadin-grid-column
                    ${columnBodyRenderer(this._actionRenderer, [])}
                >
                </vaadin-grid-column>

            </vaadin-grid>`;
        }
    }

    _nameRenderer(cache) {
        return html`
            <vaadin-button theme="small" @click=${() => this._refresh(cache)} class="button">
                <vaadin-icon icon="font-awesome-solid:rotate"></vaadin-icon>
            </vaadin-button>
            ${cache.name}`;
    }

    _actionRenderer(cache) {
        return html`
            <vaadin-button theme="small" @click=${() => this._clear(cache)} class="button">
                <vaadin-icon class="clearIcon" icon="font-awesome-solid:broom"></vaadin-icon> Clear
            </vaadin-button>`;
    }
    
    _refresh(cache){
        let params = {
            name: cache.name,
        };
        this.jsonRpc.getCacheInfo(params).then(cache => {
            this._updateCache(cache.result);
        });
    }
    
    _clear(cache){
        let params = {
            name: cache.name,
        };
        this.jsonRpc.clearCache(params).then(cache => {
            this._updateCache(cache.result);
        });    
    }
    
    _updateCache(cache){
        if (this._caches.has(cache.name)) {
            this._caches.set(cache.name, cache);
            this.requestUpdate();
        }
    }
}
customElements.define('qwc-cache-caches', QwcCacheCaches);