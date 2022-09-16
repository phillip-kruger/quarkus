package io.quarkus.devui.runtime.service.extension;

import java.net.URL;
import java.util.List;

public class Extension {
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
    private List<Link> links;

    public Extension() {

    }

    public Extension(String namespace, String artifact, String name, String shortName, String description, URL guide,
            List<String> keywords, String status, List<String> configFilter, List<String> categories, String builtWith,
            List<String> providesCapabilities, List<String> extensionDependencies, Codestart codestart) {
        this.namespace = namespace;
        this.artifact = artifact;
        this.name = name;
        this.shortName = shortName;
        this.description = description;
        this.guide = guide;
        this.keywords = keywords;
        this.status = status;
        this.configFilter = configFilter;
        this.categories = categories;
        this.builtWith = builtWith;
        this.providesCapabilities = providesCapabilities;
        this.extensionDependencies = extensionDependencies;
        this.codestart = codestart;
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

    public List<Link> getLinks() {
        return this.links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
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
