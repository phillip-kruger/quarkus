package io.quarkus.devui.runtime.jsonrpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

/**
 * Route JsonRPC message to the correct method
 * TODO: Cache the mapping ?
 * TODO: Add params
 */
@ApplicationScoped
public class JsonRpcRouter {

    @Inject
    BeanManager beanManager;

    private Map<String, String> buildTimeResponses;

    public void buildTimeData(Map<String, String> jsonResponses) {
        this.buildTimeResponses = jsonResponses;
    }

    public String route(String message) {
        JsonRpcRequest jsonRpcRequest = toJsonRpcRequest(message);
        JsonRpcResponse jsonRpcResponse = route(jsonRpcRequest);
        return Json.encode(jsonRpcResponse);
    }

    public JsonRpcResponse route(JsonRpcRequest jsonRpcRequest) {
        System.out.println(">>>>>> jsonRpcRequest = " + jsonRpcRequest);

        if (isBuildTimeData(jsonRpcRequest.getMethod())) {
            return routeBuildtime(jsonRpcRequest);
        } else {
            return routeRuntime(jsonRpcRequest);
        }
    }

    private JsonRpcResponse routeBuildtime(JsonRpcRequest jsonRpcRequest) {
        String responseJson = buildTimeResponses.get(jsonRpcRequest.getMethod());
        Object result = Json.decodeValue(responseJson);
        return toJsonRpcResponse(jsonRpcRequest, result);
    }

    private JsonRpcResponse routeRuntime(JsonRpcRequest jsonRpcRequest) {
        try {
            String[] extensionMethod = jsonRpcRequest.getMethod().split("\\.");
            String extension = extensionMethod[0];
            String method = extensionMethod[1];

            Object provider = findProvider(extension, method);
            Method jsonRPCMethod = lookupMethod(provider.getClass(), method, List.of());
            Object result = jsonRPCMethod.invoke(provider);
            return toJsonRpcResponse(jsonRpcRequest, result);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | ClassNotFoundException
                | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isBuildTimeData(String method) {
        if (buildTimeResponses != null) {
            return buildTimeResponses.containsKey(method);
        }
        return false;
    }

    // TODO: Cache
    private Object findProvider(String extension, String method) {
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

}
