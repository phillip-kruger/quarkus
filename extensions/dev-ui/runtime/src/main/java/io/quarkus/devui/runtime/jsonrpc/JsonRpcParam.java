package io.quarkus.devui.runtime.jsonrpc;

public final class JsonRpcParam {
    private String name;
    private Class type;

    public JsonRpcParam() {
    }

    public JsonRpcParam(String name, Class type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return name;
    }

}
