package org.infinispan.loaders.aerospike;

import com.aerospike.client.*;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.client.policy.RecordExistsAction;
import com.aerospike.client.policy.WritePolicy;
import org.infinispan.Cache;
import org.infinispan.config.ConfigurationException;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.container.entries.InternalCacheValue;
import org.infinispan.loaders.AbstractCacheStore;
import org.infinispan.loaders.CacheLoaderConfig;
import org.infinispan.loaders.CacheLoaderException;
import org.infinispan.loaders.CacheLoaderMetadata;
import org.infinispan.loaders.aerospike.configuration.AerospikePoolProperties;
import org.infinispan.loaders.aerospike.logging.Log;
import org.infinispan.loaders.keymappers.TwoWayKey2StringMapper;
import org.infinispan.loaders.keymappers.UnsupportedKeyTypeException;
import org.infinispan.loaders.modifications.Modification;
import org.infinispan.loaders.modifications.Store;
import org.infinispan.marshall.StreamingMarshaller;
import org.infinispan.util.Util;
import org.infinispan.util.logging.LogFactory;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.charset.Charset;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.infinispan.loaders.aerospike.AerospikeCacheStoreConfig.AEROSPIKE_MASTER_BIN_NAME;

/**
 *
 * @author Leeladurga Prasad Gunti
 */
@CacheLoaderMetadata(configurationClass = AerospikeCacheStoreConfig.class)
public class AerospikeCacheStore extends AbstractCacheStore {

    private static final String ENTRY_KEY_PREFIX = "entry_";
    private static final String ENTRY_COLUMN_NAME = "entry";
    private static final String EXPIRATION_KEY = "expiration";
    private static final int SLICE_SIZE = 100;
    private static final Log log = LogFactory.getLog(AerospikeCacheStore.class, Log.class);
    private static final boolean trace = log.isTraceEnabled();
    private static final boolean debug = log.isDebugEnabled();

    private AerospikeCacheStoreConfig config;
    private String cacheName;
    private TwoWayKey2StringMapper keyMapper;

    static private Charset UTF8Charset = Charset.forName("UTF-8");
    private static AerospikeClient aerospikeClient;
    private AerospikePoolProperties poolProperties;


    @Override
    public Class<? extends CacheLoaderConfig> getConfigurationClass() {
        return AerospikeCacheStoreConfig.class;
    }

    @Override
    public void init(CacheLoaderConfig clc, Cache<?, ?> cache, StreamingMarshaller m)
            throws CacheLoaderException {

        if(debug) {
            log.debug("Start :: init");
        }

        super.init(clc, cache, m);
        this.cacheName = cache.getName();
        this.config = (AerospikeCacheStoreConfig) clc;
        poolProperties = config.getPoolProperties();

        if(debug) {
            log.debug("End :: init");
        }
    }

    @Override
    public void start() throws CacheLoaderException {

        if(debug) {
            log.debug("Starting Aerospike..");
        }

        try {

            ClientPolicy policy = new ClientPolicy();
            policy.maxThreads = 50;
            if(null == aerospikeClient) {
                synchronized (AerospikeCacheStore.class) {
                    if(null == aerospikeClient) {
                        aerospikeClient = new AerospikeClient(policy, poolProperties.getHost(), poolProperties.getPort());
                    }
                }
            }
            System.out.println("Aerospike Start : " + aerospikeClient);
            keyMapper = (TwoWayKey2StringMapper) Util.getInstance(config.getKeyMapper(), config.getClassLoader());
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }

        if(debug) {
            log.debug("cleaning up expired entries...");
        }
        purgeInternal();

        if(debug) {
            log.debug("Started Cache.");
        }
        super.start();
    }

    @Override
    public InternalCacheEntry load(Object key) throws CacheLoaderException {

        System.out.println("load :: key "+key);

        if(debug) {
            log.debug("load :: key "+key);
        }
        Object masterObj = null;

        try {
            // Aerospike load
            Key sessionIdKey = null;
            Record sessionRecord = null;
            InternalCacheEntry ice = null;
            Map<String, Object> sessionMap = new Hashtable<String, Object>();
            if(null != aerospikeClient) {
                sessionIdKey = new Key(poolProperties.getNameSpace(), poolProperties.getSetName(), key.toString());
                sessionRecord = aerospikeClient.get(null, sessionIdKey);
                if(null != sessionRecord) {
                    System.out.println("got records from Aerospike size : "+sessionRecord.bins.size());
                    sessionMap = sessionRecord.bins;
                    masterObj = sessionMap.get(AEROSPIKE_MASTER_BIN_NAME);
                    if(null != masterObj) {
                        ice = unmarshall(masterObj, key);

                        if (ice != null && ice.isExpired(System.currentTimeMillis())) {
                            remove(key);
                            return null;
                        }
                    }
                } else {
                    if(debug) {
                        log.debug("No recodes found with key : "+ key.toString());
                    }
                    System.out.println("No recodes found with key : "+ key.toString());
                }
            }

            return ice;

        } catch (AerospikeException e) {
            log.error("Exception occurred getting data from Aerospike :  "+e.getMessage());
            System.out.println("Exception occurred getting data from Aerospike :  "+e.getMessage());
            //throw new CacheLoaderException(e);
            return null;
        } catch (Exception e) {
            throw new CacheLoaderException(e);
        }

    }

    @Override
    public Set<InternalCacheEntry> loadAll() throws CacheLoaderException {

       /*try {
           System.out.println("Load All");
           Statement statement = new Statement();
           statement.setNamespace(NAME_SPACE);
           statement.setSetName(SET_NAME);
           statement.setFilters(Filter.range(AEROSPIKE_MASTER_BIN_NAME, 0, Integer.MAX_VALUE));

           RecordSet rs = aerospikeClient.query(null, statement);
               System.out.println("got results");
               while (rs.next()) {
                   Key key = rs.getKey();
                   Record record = rs.getRecord();
                   System.out.println("Key : "+key.toString());
                   System.out.println("Record : "+record.bins);
               }

               rs.close();

            // TODO :: do it later
       } catch (Exception e) {
           System.out.println("Unable to get recodes .."+e.getMessage());
       }*/

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

    /**
     * Closes all databases, ignoring exceptions, and nulls references to all database related
     * information.
     */
    @Override
    public void stop() throws CacheLoaderException {
        super.stop();
    }

    @Override
    public void clear() throws CacheLoaderException {

    }

    @Override
    public boolean remove(Object key) throws CacheLoaderException {
        return false;
    }

    private byte[] marshall(InternalCacheEntry entry) throws IOException, InterruptedException {
        return getMarshaller().objectToByteBuffer(entry.toInternalCacheValue());
    }

    private InternalCacheEntry unmarshall(Object o, Object key) throws IOException,
            ClassNotFoundException {
        if (o == null)
            return null;
        byte b[] = (byte[]) o;
        InternalCacheValue v = (InternalCacheValue) getMarshaller().objectFromByteBuffer(b);
        return v.toInternalCacheEntry(key);
    }

    @Override
    public void store(InternalCacheEntry entry) throws CacheLoaderException {
        System.out.println("Store entry :  "+entry);
        try {
            store0(entry);
        } catch (Exception e) {
            throw new CacheLoaderException(e);
        }
    }

    private void store0(InternalCacheEntry entry) throws IOException,
            UnsupportedKeyTypeException {
        Object key = entry.getKey();
        if(debug) {
            log.debug("Store key :: "+ key.toString());
        }

        Key sessionIdKey = null;
        WritePolicy writePolicy = new WritePolicy();
        writePolicy.recordExistsAction = RecordExistsAction.UPDATE;
        writePolicy.expiration = poolProperties.getTimeout();
        System.out.println("Calling Aerospike to store");
        try {
            if (null != aerospikeClient) {
                sessionIdKey = new Key(poolProperties.getNameSpace(), poolProperties.getSetName(), key.toString());
                byte[] bytes = marshall(entry);
                Bin maserBin = new Bin(AEROSPIKE_MASTER_BIN_NAME, bytes);
                aerospikeClient.put(writePolicy, sessionIdKey, maserBin);

                if(debug) {
                    log.debug("Entry Saved in to Aerospike with key : "+ key);
                }
                System.out.println("Entry Saved in to Aerospike..!!!");
            }
        } catch (Exception e) {
            log.error("Unable to save data into Aerospike", e);
            System.out.println("Unable to save data into Aerospike"+e.getMessage());
        }

    }


    /**
     * Writes to a stream the number of entries (long) then the entries themselves.
     */
    @Override
    public void toStream(ObjectOutput out) throws CacheLoaderException {
        try {
            Set<InternalCacheEntry> loadAll = loadAll();
            int count = 0;
            for (InternalCacheEntry entry : loadAll) {
                getMarshaller().objectToObjectStream(entry, out);
                count++;
            }
            getMarshaller().objectToObjectStream(null, out);
        } catch (IOException e) {
            throw new CacheLoaderException(e);
        }
    }

    /**
     * Reads from a stream the number of entries (long) then the entries themselves.
     */
    @Override
    public void fromStream(ObjectInput in) throws CacheLoaderException {
        try {
            int count = 0;
            while (true) {
                count++;
                InternalCacheEntry entry = (InternalCacheEntry) getMarshaller().objectFromObjectStream(
                        in);
                if (entry == null)
                    break;
                store(entry);
            }
        } catch (IOException e) {
            throw new CacheLoaderException(e);
        } catch (ClassNotFoundException e) {
            throw new CacheLoaderException(e);
        } catch (InterruptedException ie) {
            if (log.isTraceEnabled())
                log.trace("Interrupted while reading from stream");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Purge expired entries. Expiration entries are stored in a single key (expirationKey) within a
     * specific ColumnFamily (set by configuration). The entries are grouped by expiration timestamp
     * in SuperColumns within which each entry's key is mapped to a column
     */
    @Override
    protected void purgeInternal() throws CacheLoaderException {
        if(debug) {
            log.debug("purgeInternal");
        }
        System.out.println("purgeInternal");


    }

    @Override
    protected void applyModifications(List<? extends Modification> mods) throws CacheLoaderException {
        System.out.println("Apply Modifications");
        try {
            for (Modification m : mods) {
                switch (m.getType()) {
                    case STORE:
                        store0(((Store) m).getStoredEntry());
                        break;
                    case CLEAR:
                        clear();
                        break;
                    case REMOVE:
                        break;
                    default:
                        throw new AssertionError();
                }
            }
        } catch (Exception e) {
            throw new CacheLoaderException(e);
        }
    }

    @Override
    public String toString() {
        return "AerospikeCacheStore";
    }

}
