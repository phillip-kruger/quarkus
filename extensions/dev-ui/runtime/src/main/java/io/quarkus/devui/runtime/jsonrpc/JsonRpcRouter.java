package io.quarkus.devui.runtime.jsonrpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

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
        JsonRpcMessage jsonRpcResponse = route(jsonRpcRequest);
        return Json.encode(jsonRpcResponse);
    }

    @SuppressWarnings("unchecked")
    public JsonRpcMessage route(JsonRpcRequest jsonRpcRequest) {
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

                log.info("<<<<<< result = " + result);

                JsonRpcResponse jsonRpcResponse = toJsonRpcResponse(jsonRpcRequest, result);
                log.info("<<<<<< jsonRpcResponse = " + jsonRpcResponse);
                return jsonRpcResponse;
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }

        // Method not found
        return JsonRpcErrorResponse.methodNotFound("Method [" + jsonRpcMethodName + "] not found");

    }

    private Object[] getArgsAsObjects(Map<String, Class> params, JsonRpcRequest jsonRpcRequest) {
        List<Object> objects = new ArrayList<>();
        for (Map.Entry<String, Class> expectedParams : params.entrySet()) {
            String paramName = expectedParams.getKey();
            Class paramType = expectedParams.getValue();
            Object param = jsonRpcRequest.getParam(paramName);
            Object casted = paramType.cast(param);
            objects.add(casted);
        }
        return objects.toArray(Object[]::new);
    }

    private JsonRpcRequest toJsonRpcRequest(String message) {
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
