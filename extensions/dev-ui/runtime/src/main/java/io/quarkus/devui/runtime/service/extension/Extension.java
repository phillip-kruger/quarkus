package io.quarkus.devui.runtime.service.extension;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.quarkus.devui.deployment.spi.page.Page;

public class Extension {
    private static final String SPACE = " ";
    private static final String DASH = "-";

    private String namespace;
    private String artifact;
    private String name;
    private String shortName;
    private String description;
    private URL guide;
    private List<String> keywords;
    private String status;
    private List<String> configFilter;
    private List<String> categories;
    private boolean unlisted = false;
    private String builtWith;
    private List<String> providesCapabilities;
    private List<String> extensionDependencies;
    private Codestart codestart;
    private final List<Page> pages = new ArrayList<>();

    public Extension() {

    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getArtifact() {
        return artifact;
    }

    public void setArtifact(String artifact) {
        this.artifact = artifact;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPathName() {
        return name.toLowerCase().replaceAll(SPACE, DASH);
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public URL getGuide() {
        return guide;
    }

    public void setGuide(URL guide) {
        this.guide = guide;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getConfigFilter() {
        return configFilter;
    }

    public void setConfigFilter(List<String> configFilter) {
        this.configFilter = configFilter;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public boolean isUnlisted() {
        return unlisted;
    }

    public void setUnlisted(boolean unlisted) {
        this.unlisted = unlisted;
    }

    public String getBuiltWith() {
        return builtWith;
    }

    public void setBuiltWith(String builtWith) {
        this.builtWith = builtWith;
    }

    public List<String> getProvidesCapabilities() {
        return providesCapabilities;
    }

    public void setProvidesCapabilities(List<String> providesCapabilities) {
        this.providesCapabilities = providesCapabilities;
    }

    public List<String> getExtensionDependencies() {
        return extensionDependencies;
    }

    public void setExtensionDependencies(List<String> extensionDependencies) {
        this.extensionDependencies = extensionDependencies;
    }

    public Codestart getCodestart() {
        return codestart;
    }

    public void setCodestart(Codestart codestart) {
        this.codestart = codestart;
    }

    public void addPage(Page page) {
        this.pages.add(page);
    }

    public void addPages(List<Page> pages) {
        this.pages.addAll(pages);
    }

    public List<Page> getPages() {
        return pages;
    }

    @Override
    public String toString() {
        return "Extension{" + "namespace=" + namespace + ", artifact=" + artifact + ", name=" + name + ", shortName="
                + shortName + ", description=" + description + ", guide=" + guide + ", keywords=" + keywords + ", status="
                + status + ", configFilter=" + configFilter + ", categories=" + categories + ", unlisted=" + unlisted
                + ", builtWith=" + builtWith + ", providesCapabilities=" + providesCapabilities + ", extensionDependencies="
                + extensionDependencies + ", codestart=" + codestart + '}';
    }
}
