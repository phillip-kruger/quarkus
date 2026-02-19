import { LitState } from 'lit-element-state';

/**
 * This keeps state of the JsonRPC Connection
 */
class ConnectionState extends LitState {

    constructor() {
        super();
        this._previousState = null;
    }

    static get stateVars() {
        return {
            current: {}
        };
    }

    _dispatchStateChange(newState) {
        const previousStateName = this._previousState?.name || null;
        this._previousState = newState;

        const event = new CustomEvent('connection-state-changed', {
            detail: {
                state: newState.name,
                previousState: previousStateName,
                serverUri: newState.serverUri,
                isConnected: newState.isConnected,
                isDisconnected: newState.isDisconnected,
                isConnecting: newState.isConnecting,
                isHotreloading: newState.isHotreloading
            },
            bubbles: true,
            composed: true
        });
        document.dispatchEvent(event);
    }

    disconnected(serverUri){
        const newState = new Object();
        newState.name = "disconnected";
        newState.icon = "plug-circle-exclamation";
        newState.color = "var(--lumo-error-color)";
        newState.message = "Disconnected from";
        newState.serverUri = serverUri;
        newState.isConnected = false;
        newState.isDisconnected = true;
        newState.isConnecting = false;
        newState.isHotreloading = false;
        connectionState.current = newState;
        document.body.style.cursor = 'wait';
        this._dispatchStateChange(newState);
    }

    connecting(serverUri){
        const newState = new Object();
        newState.name = "connecting";
        newState.icon = "plug-circle-bolt";
        newState.color = "var(--lumo-warning-color)";
        newState.message = "Connecting to";
        newState.serverUri = serverUri;
        newState.isConnected = false;
        newState.isDisconnected = true;
        newState.isConnecting = true;
        newState.isHotreloading = false;
        connectionState.current = newState;
        document.body.style.cursor = 'progress';
        this._dispatchStateChange(newState);
    }

    hotreload(serverUri){
        const newState = new Object();
        newState.name = "hotreload";
        newState.icon = "plug-circle-bolt";
        newState.color = "var(--lumo-primary-color)";
        newState.message = "Hot reloading";
        newState.serverUri = serverUri;
        newState.isConnected = false;
        newState.isDisconnected = false;
        newState.isConnecting = false;
        newState.isHotreloading = true;
        connectionState.current = newState;
        document.body.style.cursor = 'progress';
        this._dispatchStateChange(newState);
    }

    connected(serverUri){
        const newState = new Object();
        newState.name = "connected";
        newState.icon = "plug-circle-check";
        newState.color = "var(--lumo-success-color)";
        newState.message = "Connected to";
        newState.serverUri = serverUri;
        newState.isConnected = true;
        newState.isDisconnected = false;
        newState.isConnecting = false;
        newState.isHotreloading = false;
        connectionState.current = newState;
        document.body.style.cursor = 'default';
        this._dispatchStateChange(newState);
    }
}

export const connectionState = new ConnectionState();