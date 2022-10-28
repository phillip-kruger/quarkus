package io.quarkus.devui.deployment.spi;

import java.util.HashMap;
import java.util.Map;

/**
 * Content that is made available in the DEV UI
 */
public class DevUIContent {
    private final String fileName;
    private final byte[] template;
    private final String mimeType;
    private final Map<String, Object> data;

    private DevUIContent(DevUIContent.Builder builder) {
        this.fileName = builder.fileName;
        this.template = builder.template;
        this.mimeType = builder.mimeType;
        this.data = builder.data;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getTemplate() {
        return template;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public static interface FileExtension {
        public static final String HTML = "html";
        public static final String HTM = "htm";
        public static final String JS = "js";
        public static final String JSON = "json";
        public static final String CSS = "css";
    }

    public static interface MimeType {
        public static final String HTML = "text/html";
        public static final String JS = "text/javascript";
        public static final String JSON = "application/json";
        public static final String CSS = "text/css";
    }

    public static class Builder {
        private String fileName;
        private byte[] template;
        private String mimeType;
        private Map<String, Object> data = new HashMap<>();

        public Builder() {
        }

        public Builder fileName(String fileName) {
            if (fileName == null || fileName.isEmpty()) {
                throw new RuntimeException("Invalid fileName");
            }
            this.fileName = fileName;
            return this;
        }

        public Builder template(byte[] template) {
            if (template == null || template.length == 0) {
                throw new RuntimeException("Invalid template");
            }

            this.template = template;
            return this;
        }

        public Builder mimeType(String mimeType) {
            if (mimeType == null || mimeType.isEmpty()) {
                throw new RuntimeException("Invalid mimeType");
            }
            this.mimeType = mimeType;
            return this;
        }

        public Builder addData(Map<String, Object> data) {
            this.data.putAll(data);
            return this;
        }

        public Builder addData(String key, Object value) {
            this.data.put(key, value);
            return this;
        }

        public DevUIContent build() {
            if (fileName == null) {
                throw new RuntimeException(
                        ERROR + " FileName is mandatory, for example 'index.html'");
            }

            if (template == null) {
                template = DEFAULT_TEMPLATE;
            }

            if (mimeType == null && fileName.contains(DOT)) {
                // Detect the mimeType from the file extension
                int dotIndex = fileName.lastIndexOf(DOT) + 1; // remove do too
                String ext = fileName.substring(dotIndex);
                if (!ext.isEmpty()) {
                    if (ext.equalsIgnoreCase(FileExtension.HTML) || ext.equalsIgnoreCase(FileExtension.HTM)) {
                        mimeType = MimeType.HTML;
                    } else if (ext.equalsIgnoreCase(FileExtension.JS)) {
                        mimeType = MimeType.JS;
                    } else if (ext.equalsIgnoreCase(FileExtension.CSS)) {
                        mimeType = MimeType.CSS;
                    } else if (ext.equalsIgnoreCase(FileExtension.JSON)) {
                        mimeType = MimeType.JSON;
                    }
                }

            }

            if (mimeType == null) {
                throw new RuntimeException(ERROR
                        + " Please provide the mimeType or add a file extension for " + fileName
                        + " with a know mimetype (html, js, css, json)");
            }

            return new DevUIContent(this);
        }

        private static final String ERROR = "Not enough information to create Dev UI content.";
        private static final String DOT = ".";
        private static final byte[] DEFAULT_TEMPLATE = "Here the template of your page. Set your own by providing the template() in the DevUIContent"
                .getBytes();
    }
}
