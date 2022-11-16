package io.quarkus.cache.runtime.devui;

public class CacheInfo {
    private String name;
    private long size;

    public CacheInfo() {
    }

    public CacheInfo(String name, long size) {
        this.name = name;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "CacheInfo{" + "name=" + name + ", size=" + size + '}';
    }
}