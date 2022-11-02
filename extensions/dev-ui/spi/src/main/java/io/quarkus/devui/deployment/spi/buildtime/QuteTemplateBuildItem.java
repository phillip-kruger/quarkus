package io.quarkus.devui.deployment.spi.buildtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.quarkus.devui.deployment.spi.AbstractDevUIBuildItem;

/**
 * Contains info on the build time template used to build static content for Dev UI
 * All files are relative to dev-ui-templates/build-time/{extensionName} (in src/main/resources)
 *
 * This contain the fileName to the template, and the template data (variables)
 *
 * This allows extensions developers to add "static files" that they generate with Qute at build time.
 * From a runtime p.o.v this is file served from "disk"
 */
public final class QuteTemplateBuildItem extends AbstractDevUIBuildItem {
    private final List<TemplateData> templateDatas = new ArrayList<>();

    public QuteTemplateBuildItem(String extensionName) {
        super(extensionName);
    }

    public List<TemplateData> getTemplateDatas() {
        return templateDatas;
    }

    public void add(String templatename, Map<String, Object> data) {
        add(templatename, templatename, data); // By default the template is used for only one file.
    }

    public void add(String templatename, String fileName, Map<String, Object> data) {
        add(new TemplateData(templatename, fileName, data));
    }

    public void add(TemplateData templateData) {
        templateDatas.add(templateData);
    }

    public static class TemplateData {
        final String templateName;
        final String fileName;
        final Map<String, Object> data;

        public TemplateData(String templateName, String fileName, Map<String, Object> data) {
            this.templateName = templateName;
            this.fileName = fileName;
            this.data = data;
        }

        public String getTemplateName() {
            return templateName;
        }

        public String getFileName() {
            return fileName;
        }

        public Map<String, Object> getData() {
            return data;
        }
    }
}
