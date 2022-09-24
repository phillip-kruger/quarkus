package io.quarkus.arc.runtime.devui;

import java.util.List;
import java.util.Set;

public class ArcDevUIService {

    public List<ArcBeanInfo> getAllBeans() {
        return List.of(new ArcBeanInfo("1", "kind", true, "providerType", "memberName", Set.of("types"),
                Set.of("qualifiers"), "scope", "declaringClass", List.of("boundInterceptors")));
    }
    
    
    
}
