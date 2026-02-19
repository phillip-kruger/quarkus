import { Notification } from '@vaadin/notification';
import { html} from 'lit';
import { unsafeHTML } from 'lit/directives/unsafe-html.js';
import '@vaadin/icon';
import '@vaadin/horizontal-layout';
import '@vaadin/vertical-layout';
import '@vaadin/button';
import '@vaadin/details';

/**
 * Show toast messages with support for deduplication and expandable details.
 */
class Notifier {
    constructor() {
        // Track recent messages for deduplication
        this._recentMessages = new Map();
        this._deduplicationWindow = 2000; // 2 seconds
    }

    /**
     * Check if a message was recently shown (for deduplication)
     * @param {string} message - The message to check
     * @returns {boolean} - True if the message should be suppressed
     */
    _isDuplicate(message) {
        const now = Date.now();
        const messageKey = message.substring(0, 100); // Use first 100 chars as key

        // Clean up old entries
        this._recentMessages.forEach((timestamp, key) => {
            if (now - timestamp > this._deduplicationWindow) {
                this._recentMessages.delete(key);
            }
        });

        if (this._recentMessages.has(messageKey)) {
            return true;
        }

        this._recentMessages.set(messageKey, now);
        return false;
    }

    showPrimaryInfoMessage(message, position, duration = 5) {
        if(position === null)position = "bottom-start";
        let color = "var(--lumo-primary-contrast-color)";
        this.showMessage("font-awesome-solid:circle-info", "primary", message, color, duration, position);
    }

    showInfoMessage(message, position, duration = 5) {
        if(position === null)position = "bottom-start";
        let color = "var(--lumo-primary-text-color)";
        this.showMessage("font-awesome-solid:circle-info", "contrast", message, color, duration, position);
    }

    showPrimarySuccessMessage(message, position, duration = 5) {
        if(position === null)position = "bottom-start";
        let color = "var(--lumo-primary-contrast-color)";
        this.showMessage("font-awesome-solid:circle-check", "success", message, color, duration, position);
    }

    showSuccessMessage(message, position, duration = 5) {
        if(position === null)position = "bottom-start";
        let color = "var(--lumo-success-text-color)";
        this.showMessage("font-awesome-solid:circle-check", "contrast", message, color, duration, position);
    }

    showPrimaryWarningMessage(message, position, duration = 5) {
        if(position === null)position = "bottom-start";
        let color = "var(--lumo-primary-contrast-color)";
        this.showMessage("font-awesome-solid:triangle-exclamation", "contrast", message, color, duration, position);
    }

    showWarningMessage(message, position, duration = 5) {
        if(position === null)position = "bottom-start";
        let color = "var(--lumo-warning-text-color)";
        this.showMessage("font-awesome-solid:triangle-exclamation", "contrast", message, color, duration, position);
    }

    showPrimaryErrorMessage(message, position, duration = 5) {
        if(position === null)position = "bottom-start";
        let color = "var(--lumo-primary-contrast-color)";
        this.showMessage("font-awesome-solid:circle-exclamation", "error", message, color, duration, position, false);
    }

    showErrorMessage(message, position, duration = 5) {
        if(position === null)position = "bottom-start";
        // Apply deduplication for error messages
        if (this._isDuplicate(message)) {
            return;
        }
        let color = "var(--lumo-error-text-color)";
        this.showMessage("font-awesome-solid:circle-exclamation", "contrast", message, color, duration, position);
    }

    /**
     * Show an error message with expandable details (e.g., stack trace)
     * @param {string} message - The main error message
     * @param {string} details - Detailed information (e.g., stack trace)
     * @param {string} position - Notification position
     * @param {number} duration - Duration in seconds (0 for persistent)
     */
    showErrorWithDetails(message, details, position = "bottom-start", duration = 0) {
        // Apply deduplication
        if (this._isDuplicate(message)) {
            return;
        }

        let color = "var(--lumo-error-text-color)";
        let d = duration * 1000;

        let notification;
        const closeHandler = () => {
            if (notification) {
                notification.close();
            }
        };

        notification = Notification.show(html`
            <vaadin-vertical-layout style="color:${color}; max-width: 400px;">
                <vaadin-horizontal-layout theme="spacing" style="align-items: center; width: 100%;">
                    <vaadin-icon icon="font-awesome-solid:circle-exclamation"></vaadin-icon>
                    <span style="flex: 1;">${unsafeHTML(message)}</span>
                    <vaadin-button theme="tertiary-inline" @click=${closeHandler}>
                        <vaadin-icon icon="font-awesome-solid:xmark"></vaadin-icon>
                    </vaadin-button>
                </vaadin-horizontal-layout>
                ${details ? html`
                    <vaadin-details summary="Show details" style="width: 100%; margin-top: 8px;">
                        <pre style="font-size: 11px; max-height: 200px; overflow: auto; white-space: pre-wrap; word-break: break-word; margin: 0; padding: 8px; background: var(--lumo-contrast-5pct); border-radius: 4px;">${details}</pre>
                    </vaadin-details>
                ` : ''}
            </vaadin-vertical-layout>
        `, {
            position: position,
            duration: d,
            theme: "error",
        });
    }

    /**
     * Show a warning message with expandable details
     * @param {string} message - The main warning message
     * @param {string} details - Detailed information
     * @param {string} position - Notification position
     * @param {number} duration - Duration in seconds (0 for persistent)
     */
    showWarningWithDetails(message, details, position = "bottom-start", duration = 10) {
        let color = "var(--lumo-warning-text-color)";
        let d = duration * 1000;

        let notification;
        const closeHandler = () => {
            if (notification) {
                notification.close();
            }
        };

        notification = Notification.show(html`
            <vaadin-vertical-layout style="color:${color}; max-width: 400px;">
                <vaadin-horizontal-layout theme="spacing" style="align-items: center; width: 100%;">
                    <vaadin-icon icon="font-awesome-solid:triangle-exclamation"></vaadin-icon>
                    <span style="flex: 1;">${unsafeHTML(message)}</span>
                    <vaadin-button theme="tertiary-inline" @click=${closeHandler}>
                        <vaadin-icon icon="font-awesome-solid:xmark"></vaadin-icon>
                    </vaadin-button>
                </vaadin-horizontal-layout>
                ${details ? html`
                    <vaadin-details summary="Show details" style="width: 100%; margin-top: 8px;">
                        <pre style="font-size: 11px; max-height: 200px; overflow: auto; white-space: pre-wrap; word-break: break-word; margin: 0; padding: 8px; background: var(--lumo-contrast-5pct); border-radius: 4px;">${details}</pre>
                    </vaadin-details>
                ` : ''}
            </vaadin-vertical-layout>
        `, {
            position: position,
            duration: d,
            theme: "contrast",
        });
    }

    showMessage(icon, theme, message, color, duration, position = "bottom-start", deduplicate = true) {
        // Apply deduplication for repeated messages (optional)
        if (deduplicate && theme === "error" && this._isDuplicate(message)) {
            return;
        }

        let d = duration * 1000;

        const notification = Notification.show(html`<vaadin-horizontal-layout theme="spacing" style="align-items: center;color:${color};">
                                                        <vaadin-icon icon="${icon}"></vaadin-icon> <span>${unsafeHTML(message)}</span>
                                                    </vaadin-horizontal-layout>`, {
            position: position,
            duration: d,
            theme: theme,
        });
    }

    /**
     * Clear the deduplication cache (useful for testing or reset)
     */
    clearDeduplicationCache() {
        this._recentMessages.clear();
    }
}

export const notifier = new Notifier();
