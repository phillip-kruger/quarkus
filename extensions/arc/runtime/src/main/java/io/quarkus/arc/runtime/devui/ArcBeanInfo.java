package io.quarkus.arc.runtime.devui;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ArcBeanInfo implements Comparable<ArcBeanInfo> {

    private final String id;
    private final String kind;
    private final boolean isApplicationBean;
    private final String providerType;
    private final String memberName;
    private final Set<String> types;
    private final Set<String> qualifiers;
    private final String scope;
    private final String declaringClass;
    private final List<String> interceptors;

    public ArcBeanInfo(String id, String kind, boolean isApplicationBean, String providerType, String memberName,
            Set<String> types,
            Set<String> qualifiers, String scope, String declaringClass, List<String> boundInterceptors) {
        this.id = id;
        this.kind = kind;
        this.isApplicationBean = isApplicationBean;
        this.providerType = providerType;
        this.memberName = memberName;
        this.types = types;
        this.qualifiers = qualifiers;
        this.scope = scope;
        this.declaringClass = declaringClass;
        this.interceptors = boundInterceptors;
    }

    public String getId() {
        return id;
    }

    public String getKind() {
        return kind;
    }

    public String getScope() {
        return scope;
    }

    public Set<String> getQualifiers() {
        return qualifiers;
    }

    public Set<String> getNonDefaultQualifiers() {
        Set<String> nonDefault = new HashSet<>();
        String atDefault = "@Default";
        String atAny = "@Any";
        for (String qualifier : qualifiers) {
            if (qualifier.toString().endsWith(atDefault) || qualifier.toString().endsWith(atAny)) {
                continue;
            }
            nonDefault.add(qualifier);
        }
        return nonDefault;
    }

    public Set<String> getTypes() {
        return types;
    }

    public String getProviderType() {
        return providerType;
    }

    public String getMemberName() {
        return memberName;
    }

    public boolean isApplicationBean() {
        return isApplicationBean;
    }

    public String getDeclaringClass() {
        return declaringClass;
    }

    public List<String> getInterceptors() {
        return interceptors;
    }

    public String getDescription() {
        return description(false);
    }

    public String getSimpleDescription() {
        return description(true);
    }

    private String description(boolean simple) {
        String typeInfo = typeInfo(simple);
        switch (kind) {
            case "Field":
                return typeInfo + "#" + memberName;
            case "Method":
                return typeInfo + "#" + memberName + "()";
            case "Synthetic":
                return "Synthetic: " + typeInfo;
            default:
                return typeInfo;
        }
    }

    public String typeInfo(boolean simple) {
        String type;
        switch (kind) {
            case "Field":
            case "Method":
                type = declaringClass;
                break;
            default:
                type = providerType;
                break;
        }
        if (simple) {
            int idx = type.lastIndexOf(".");
            return idx != -1 && type.length() > 1 ? type.substring(idx + 1) : type;
        }
        return type;
    }

    @Override
    public int compareTo(ArcBeanInfo o) {
        // Application beans should go first
        if (isApplicationBean == o.isApplicationBean) {
            return providerType.compareTo(o.providerType);
        }
        return isApplicationBean ? -1 : 1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ArcBeanInfo other = (ArcBeanInfo) obj;
        return Objects.equals(id, other.id);
    }

}
