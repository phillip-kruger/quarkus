package io.quarkus.produi.spi;

import org.jboss.jandex.DotName;

/**
 * Registers a class that provides JSON-RPC methods for Prod UI.
 * <p>
 * Unlike Dev UI, Prod UI only supports runtime methods - no deployment classpath access.
 * All methods must be production-safe and read-only by default.
 */
public final class ProdUIJsonRpcProvidersBuildItem extends AbstractProdUIBuildItem {

    private final Class<?> jsonRpcMethodProviderClass;
    private final DotName defaultBeanScope;

    public ProdUIJsonRpcProvidersBuildItem(Class<?> jsonRpcMethodProviderClass) {
        super();
        this.jsonRpcMethodProviderClass = jsonRpcMethodProviderClass;
        this.defaultBeanScope = null;
    }

    public ProdUIJsonRpcProvidersBuildItem(Class<?> jsonRpcMethodProviderClass, DotName defaultBeanScope) {
        super();
        this.jsonRpcMethodProviderClass = jsonRpcMethodProviderClass;
        this.defaultBeanScope = defaultBeanScope;
    }

    public ProdUIJsonRpcProvidersBuildItem(String customIdentifier, Class<?> jsonRpcMethodProviderClass) {
        super(customIdentifier);
        this.jsonRpcMethodProviderClass = jsonRpcMethodProviderClass;
        this.defaultBeanScope = null;
    }

    public Class<?> getJsonRpcMethodProviderClass() {
        return jsonRpcMethodProviderClass;
    }

    public DotName getDefaultBeanScope() {
        return defaultBeanScope;
    }
}
