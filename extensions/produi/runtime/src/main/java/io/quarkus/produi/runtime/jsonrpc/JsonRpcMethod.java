package io.quarkus.produi.runtime.jsonrpc;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

/**
 * Metadata about a JSON-RPC method for Prod UI.
 */
public final class JsonRpcMethod {
    private Class<?> bean;
    private String methodName;
    private Method javaMethod;
    private Map<String, Parameter> parameters;

    private boolean isExplicitlyBlocking;
    private boolean isExplicitlyNonBlocking;

    public JsonRpcMethod() {
    }

    public Class<?> getBean() {
        return bean;
    }

    public void setBean(Class<?> bean) {
        this.bean = bean;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getJavaMethodName() {
        if (methodName.contains(UNDERSCORE)) {
            return methodName.substring(methodName.indexOf(UNDERSCORE) + 1);
        }
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Method getJavaMethod() {
        return javaMethod;
    }

    public void setJavaMethod(Method javaMethod) {
        this.javaMethod = javaMethod;
    }

    public Map<String, Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Parameter> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(String name, Class<?> type) {
        if (this.parameters == null)
            this.parameters = new LinkedHashMap<>();
        this.parameters.put(name, new Parameter(type));
    }

    public boolean hasParameters() {
        return this.parameters != null && !this.parameters.isEmpty();
    }

    public boolean isExplicitlyBlocking() {
        return isExplicitlyBlocking;
    }

    public void setExplicitlyBlocking(boolean explicitlyBlocking) {
        isExplicitlyBlocking = explicitlyBlocking;
    }

    public boolean isExplicitlyNonBlocking() {
        return isExplicitlyNonBlocking;
    }

    public void setExplicitlyNonBlocking(boolean explicitlyNonBlocking) {
        isExplicitlyNonBlocking = explicitlyNonBlocking;
    }

    public boolean isReturningMulti() {
        return javaMethod != null && javaMethod.getReturnType().getName().equals(Multi.class.getName());
    }

    public boolean isReturningUni() {
        return javaMethod != null && javaMethod.getReturnType().getName().equals(Uni.class.getName());
    }

    public boolean isReturningCompletionStage() {
        return javaMethod != null && javaMethod.getReturnType().getName().equals(CompletionStage.class.getName());
    }

    public boolean isReturningCompletableFuture() {
        return javaMethod != null && javaMethod.getReturnType().getName().equals(CompletableFuture.class.getName());
    }

    public static class Parameter {
        private Class<?> type;

        public Parameter() {
        }

        public Parameter(Class<?> type) {
            this.type = type;
        }

        public Class<?> getType() {
            return type;
        }

        public void setType(Class<?> type) {
            this.type = type;
        }
    }

    private static final String UNDERSCORE = "_";
}
