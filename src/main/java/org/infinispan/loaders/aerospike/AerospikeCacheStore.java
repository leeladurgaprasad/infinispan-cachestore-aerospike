package org.infinispan.loaders.aerospike;

import com.aerospike.client.AerospikeClient;
import org.infinispan.Cache;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.loaders.AbstractCacheStore;
import org.infinispan.loaders.CacheLoaderConfig;
import org.infinispan.loaders.CacheLoaderException;
import org.infinispan.marshall.StreamingMarshaller;
import org.infinispan.util.logging.LogFactory;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;


/**
 * @version 1.0
 * @author Leeladurga Prasad Gunti
 */

public class AerospikeCacheStore extends AbstractCacheStore {

    private static final Log logger = LogFactory.getLog(AerospikeCacheStore.class, Log.class);

    private String cacheName;

    private static AerospikeClient aerospikeClient;

    private static final String NAME_SPACE = "test";
    private static final String SET_NAME = "session";
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 3000;
    private static final int SESSION_EXP_TIME = 12000;
    private static final String AEROSPIKE_MASTER_BIN_NAME = "AMBN";


    @Override
    public void init(CacheLoaderConfig clc, Cache<?, ?> cache, StreamingMarshaller m)
            throws CacheLoaderException {
        super.init(clc, cache, m);
        this.cacheName = cache.getName();

    }

    @Override
    protected void purgeInternal() throws CacheLoaderException {
        logger.debug("purgeInternal");
    }

    @Override
    public void store(InternalCacheEntry entry) throws CacheLoaderException {
        logger.debug("store");
    }

    @Override
    public void fromStream(ObjectInput inputStream) throws CacheLoaderException {

    }

    @Override
    public void toStream(ObjectOutput outputStream) throws CacheLoaderException {

    }

    @Override
    public void clear() throws CacheLoaderException {

    }

    @Override
    public boolean remove(Object key) throws CacheLoaderException {
        return false;
    }

    @Override
    public InternalCacheEntry load(Object key) throws CacheLoaderException {
        return null;
    }

    @Override
    public Set<InternalCacheEntry> loadAll() throws CacheLoaderException {
        return null;
    }

    @Override
    public Set<InternalCacheEntry> load(int numEntries) throws CacheLoaderException {
        return null;
    }

    @Override
    public Set<Object> loadAllKeys(Set<Object> keysToExclude) throws CacheLoaderException {
        return null;
    }

    @Override
    public Class<? extends CacheLoaderConfig> getConfigurationClass() {
        return null;
    }
}
