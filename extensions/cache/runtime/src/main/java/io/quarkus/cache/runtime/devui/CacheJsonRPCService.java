package io.quarkus.cache.runtime.devui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.quarkus.cache.Cache;
import io.quarkus.cache.CaffeineCache;
import io.quarkus.cache.runtime.CaffeineCacheSupplier;
import io.quarkus.cache.runtime.caffeine.CaffeineCacheImpl;

/**
 * Provide Info on the cache
 */
public class CacheJsonRPCService {

    public List<CacheInfo> getCacheInfos() {
        Collection<String> cacheNames = CaffeineCacheSupplier.cacheManager().getCacheNames();
        List<CacheInfo> cacheInfos = new ArrayList<>();
        for (String name : cacheNames) {
            CacheInfo ci = getCacheInfo(name);
            if (ci != null) {
                cacheInfos.add(ci);
            }
        }
        return cacheInfos;
    }

    public CacheInfo getCacheInfo(String name) {
        Optional<Cache> cache = CaffeineCacheSupplier.cacheManager().getCache(name);
        if (cache.isPresent() && cache.get() instanceof CaffeineCache) {
            CaffeineCacheImpl caffeineCache = (CaffeineCacheImpl) cache.get();
            return new CacheInfo(caffeineCache.getName(), caffeineCache.getSize());
        }
        return null;
    }

    public CacheInfo clearCache(String name) {
        Optional<Cache> cache = CaffeineCacheSupplier.cacheManager().getCache(name);
        if (cache.isPresent() && cache.get() instanceof CaffeineCache) {
            CaffeineCacheImpl caffeineCache = (CaffeineCacheImpl) cache.get();
            caffeineCache.invalidateAll().subscribe().with(ignored -> {
            });
        }

        return getCacheInfo(name);
    }

}
