package io.quarkus.devui.runtime.jsonrpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import io.quarkus.arc.Arc;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

/**
 * Route JsonRPC message to the correct method
 */
@ApplicationScoped
public class JsonRpcRouter {

    private static final Logger log = Logger.getLogger(JsonRpcRouter.class);

    @Inject
    BeanManager beanManager;

    // Map json-rpc method to java
    private Map<String, ReflectionInfo> jsonRpcToJava = new HashMap<>();

    static class ReflectionInfo {
        public Object instance;
        public Method method;
        public Map<String, Class> params;

        public ReflectionInfo(Object instance, Method method, Map<String, Class> params) {
            this.instance = instance;
            this.method = method;
            this.params = params;
        }
    }

    public void setExtensionMethodsMap(Map<String, Map<JsonRpcMethodName, JsonRpcMethod>> extensionMethodsMap) {
        for (Map.Entry<String, Map<JsonRpcMethodName, JsonRpcMethod>> extension : extensionMethodsMap.entrySet()) {
            String extensionName = extension.getKey();
            Map<JsonRpcMethodName, JsonRpcMethod> jsonRpcMethods = extension.getValue();
            Map<JsonRpcMethodName, ReflectionInfo> javaMethods = new HashMap<>();
            for (Map.Entry<JsonRpcMethodName, JsonRpcMethod> method : jsonRpcMethods.entrySet()) {
                JsonRpcMethodName methodName = method.getKey();

                JsonRpcMethod jsonRpcMethod = method.getValue();

                @SuppressWarnings("unchecked")
                Object providerInstance = Arc.container().select(jsonRpcMethod.getClazz()).get();

                try {
                    Method javaMethod = null;
                    Map<String, Class> params = null;
                    if (jsonRpcMethod.hasParams()) {
                        params = jsonRpcMethod.getParams();
                        javaMethod = providerInstance.getClass().getMethod(jsonRpcMethod.getMethodName(),
                                params.values().toArray(new Class[] {}));
                    } else {
                        javaMethod = providerInstance.getClass().getMethod(jsonRpcMethod.getMethodName());
                    }
                    ReflectionInfo reflectionInfo = new ReflectionInfo(providerInstance, javaMethod, params);
                    jsonRpcToJava.put(extensionName + DOT + methodName, reflectionInfo);
                } catch (NoSuchMethodException | SecurityException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    public String route(String message) {
        JsonRpcRequest jsonRpcRequest = toJsonRpcRequest(message);
        JsonRpcResponse jsonRpcResponse = route(jsonRpcRequest);
        return Json.encode(jsonRpcResponse);
    }

    @SuppressWarnings("unchecked")
    public JsonRpcResponse route(JsonRpcRequest jsonRpcRequest) {
        log.info(">>>>>> jsonRpcRequest = " + jsonRpcRequest);

        String jsonRpcMethodName = jsonRpcRequest.getMethod();

        if (this.jsonRpcToJava.containsKey(jsonRpcMethodName)) {
            ReflectionInfo reflectionInfo = this.jsonRpcToJava.get(jsonRpcMethodName);
            try {
                Object result = null;
                if (jsonRpcRequest.hasParams()) {
                    Object[] args = getArgsAsObjects(reflectionInfo.params, jsonRpcRequest);
                    result = reflectionInfo.method.invoke(reflectionInfo.instance, args);
                } else {
                    result = reflectionInfo.method.invoke(reflectionInfo.instance);
                }
                return toJsonRpcResponse(jsonRpcRequest, result);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }

        } else {
            // TODO: Delete below . (Move internal to BuildTimeData)
            try {
                String[] extensionMethod = jsonRpcMethodName.split("\\.");
                String extension = extensionMethod[0];
                String method = extensionMethod[1];

                Object provider = findProvider(extension);
                Method jsonRPCMethod = lookupMethod(provider.getClass(), method, List.of());
                Object result = jsonRPCMethod.invoke(provider);
                return toJsonRpcResponse(jsonRpcRequest, result);

            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | ClassNotFoundException
                    | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Object[] getArgsAsObjects(Map<String, Class> params, JsonRpcRequest jsonRpcRequest) {

        List<Object> objects = new ArrayList<>();
        for (Map.Entry<String, Class> expectedParams : params.entrySet()) {
            String paramName = expectedParams.getKey();
            Class paramType = expectedParams.getValue();
            // TODO: Might need json mapping here ?
            Object param = jsonRpcRequest.getParam(paramName);
            Object casted = paramType.cast(param);
            objects.add(casted);
        }

        return objects.toArray(new Object[] {});
    }

    //    private JsonRpcMethod getJsonRpcMethod(String extension, String method) {
    //        if (extensionMethodsMap.containsKey(extension)) {
    //            Map<JsonRpcMethodName, JsonRpcMethod> jsonRpcMethods = extensionMethodsMap.get(extension);
    //            JsonRpcMethodName jsonRpcMethodName = new JsonRpcMethodName(method);
    //
    //            if (jsonRpcMethods.containsKey(jsonRpcMethodName)) {
    //                return jsonRpcMethods.get(jsonRpcMethodName);
    //            } else {
    //                if (!extension.equalsIgnoreCase("internal")) {
    //                    log.warn("Extension " + extension + " does not have a JsonRPC method called " + jsonRpcMethodName);
    //                }
    //            }
    //        } else {
    //            if (!extension.equalsIgnoreCase("internal")) {
    //                log.warn(">>>>>> Extension " + extension + " does not have any JsonRPC methods");
    //            }
    //        }
    //        return null;
    //    }

    // TODO: Cache
    private Object findProvider(String extension) {
        String beanName = DevUIJsonRPCProviderNamer.createBeanName(extension);
        Set<Bean<?>> beans = beanManager.getBeans(beanName);
        if (beans != null && !beans.isEmpty() && beans.size() == 1) {
            Bean bean = beans.iterator().next();
            @SuppressWarnings("unchecked")
            CreationalContext ctx = beanManager.createCreationalContext(bean);
            return beanManager.getReference(bean, bean.getClass(), ctx);
        } else {
            throw new RuntimeException("Could not find bean " + beanName + " for extension " + extension);
        }
    }

    // TODO: Cache
    private Method lookupMethod(Class<?> providerClass, String methodName, List<String> parameterClasses)
            throws ClassNotFoundException, NoSuchMethodException {
        return providerClass.getMethod(methodName, getParameterClasses(parameterClasses));
    }

    private Class<?>[] getParameterClasses(List<String> parameterClasses) throws ClassNotFoundException {
        if (parameterClasses != null && !parameterClasses.isEmpty()) {
            List<Class<?>> cl = new LinkedList<>();
            int cnt = 0;
            for (String className : parameterClasses) {
                Class<?> c = Class.forName(className, false, Thread.currentThread().getContextClassLoader());
                cl.add(c);
                cnt++;
            }

            return cl.toArray(new Class[] {});
        }
        return null;
    }

    // TODO: Cache
    //    private List<Method> getJsonRPCMethods(Class jsonRPCMethodProviderClass) {
    //        List<Method> jsonRPCMethods = new ArrayList<>();
    //        Method[] methods = jsonRPCMethodProviderClass.getMethods();
    //        for (Method method : methods) {
    //            if (Modifier.isPublic(method.getModifiers())
    //                    && !Modifier.isFinal(method.getModifiers())
    //                    && !Modifier.isStatic(method.getModifiers())
    //                    && !Modifier.isAbstract(method.getModifiers())
    //                    && !method.getDeclaringClass().equals(Object.class)) {
    //
    //                jsonRPCMethods.add(method);
    //            }
    //        }
    //        return jsonRPCMethods;
    //    }

    private JsonRpcRequest toJsonRpcRequest(String message) {
        // TODO: Handle parsing error ?
        JsonObject jsonObject = (JsonObject) Json.decodeValue(message);
        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest();
        jsonRpcRequest.setJsonrpc(jsonObject.getString(JSONRPC, VERSION));
        jsonRpcRequest.setMethod(jsonObject.getString(METHOD));
        jsonRpcRequest.setParams(jsonObject.getJsonObject(PARAMS).getMap());
        jsonRpcRequest.setId(jsonObject.getInteger(ID));

        return jsonRpcRequest;
    }

    private <T> JsonRpcResponse toJsonRpcResponse(JsonRpcRequest message, T t) {
        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse();
        jsonRpcResponse.setId(message.getId());
        jsonRpcResponse.setJsonrpc(message.getJsonrpc());
        jsonRpcResponse.setResult(t);
        return jsonRpcResponse;
    }

    private static final String JSONRPC = "jsonrpc";
    private static final String VERSION = "2.0";
    private static final String METHOD = "method";
    private static final String PARAMS = "params";
    private static final String ID = "id";
    private static final String DOT = ".";

}
