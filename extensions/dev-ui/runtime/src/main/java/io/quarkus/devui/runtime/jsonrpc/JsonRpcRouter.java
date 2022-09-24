package io.quarkus.devui.runtime.jsonrpc;

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
 */
@ApplicationScoped
public class JsonRpcRouter {

    @Inject
    BeanManager beanManager;

    public String route(String message) {
        JsonRpcRequest jsonRpcRequest = toJsonRpcRequest(message);
        JsonRpcResponse jsonRpcResponse = route(jsonRpcRequest);
        return Json.encode(jsonRpcResponse);
    }

    public JsonRpcResponse route(JsonRpcRequest jsonRpcRequest) {
        System.out.println(">>>>>> jsonRpcRequest = " + jsonRpcRequest);
        JsonRPCMethodProvider provider = findProvider(jsonRpcRequest.getMethod());
        return toJsonRpcResponse(jsonRpcRequest, provider.request(jsonRpcRequest));
    }

    private JsonRPCMethodProvider findProvider(String methodName) {

        String[] extensionMethod = methodName.split("\\.");
        String extension = extensionMethod[0];
        String method = extensionMethod[1];

        String beanName = JsonRPCMethodProvider.createBeanName(extension);
        Set<Bean<?>> beans = beanManager.getBeans(beanName);
        if (beans != null && !beans.isEmpty() && beans.size() == 1) {
            Bean bean = beans.iterator().next();
            @SuppressWarnings("unchecked")
            CreationalContext ctx = beanManager.createCreationalContext(bean);
            return (JsonRPCMethodProvider) beanManager.getReference(bean, bean.getClass(), ctx);
        } else {
            throw new RuntimeException("Could not find bean " + beanName + " for extension " + extension);
        }
    }

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
