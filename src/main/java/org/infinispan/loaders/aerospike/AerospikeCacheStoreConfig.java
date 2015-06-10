package org.infinispan.loaders.aerospike;

import org.infinispan.loaders.LockSupportCacheStoreConfig;
import org.infinispan.loaders.aerospike.configuration.AerospikePoolProperties;
import org.infinispan.loaders.keymappers.DefaultTwoWayKey2StringMapper;

/**
 * @author Leeladurga Prasad Gunti
 */
public class AerospikeCacheStoreConfig extends LockSupportCacheStoreConfig {

    String keyMapper = DefaultTwoWayKey2StringMapper.class.getName();

    public static final String AEROSPIKE_MASTER_BIN_NAME = "AMBN";

    protected AerospikePoolProperties poolProperties;

    public AerospikeCacheStoreConfig() {
        setCacheLoaderClassName(AerospikeCacheStore.class.getName());
        poolProperties = new AerospikePoolProperties();
    }

    public String getHost() {
        return poolProperties.getHost();
    }

    public void setHost(String host) {
        poolProperties.setHost(host);
    }

    public int getPort() {
        return poolProperties.getPort();
    }

    public void setPort(int port) {
        poolProperties.setPort(port);
    }

    public String getNameSpace() {
        return poolProperties.getNameSpace();
    }

    public void setNameSpace(String nameSpace) {
        poolProperties.setNameSpace(nameSpace);
    }

    public String getSetName() {
        return poolProperties.getSetName();
    }

    public void setSetName(String setName) {
        poolProperties.setSetName(setName);
    }

    public String getUsername() {
        return poolProperties.getUsername();
    }

    public void setUsername(String username) {
        poolProperties.setUsername(username);
    }

    public String getPassword() {
        return poolProperties.getPassword();
    }

    public void setPassword(String password) {
        poolProperties.setPassword(password);
    }

    public int getTimeout() {
        return poolProperties.getTimeout();
    }

    public void setTimeout(int timeout) {
        poolProperties.setTimeout(timeout);
    }

    public AerospikePoolProperties getPoolProperties() {
        return poolProperties;
    }

    public void setPoolProperties(AerospikePoolProperties poolProperties) {
        this.poolProperties = poolProperties;
    }

    public String getKeyMapper() {
        return keyMapper;
    }

    public void setKeyMapper(String keyMapper) {
        this.keyMapper = keyMapper;
    }
}
