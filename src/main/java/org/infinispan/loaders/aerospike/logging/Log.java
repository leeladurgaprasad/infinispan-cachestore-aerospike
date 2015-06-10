package org.infinispan.loaders.aerospike.logging;

import org.jboss.logging.Cause;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;

import static org.jboss.logging.Logger.Level.ERROR;

/**
 *  *
 * @author Leeladurga Prasad Gunti
 */

@MessageLogger(projectCode = "ppp")
public interface Log extends org.infinispan.util.logging.Log {

    @LogMessage(level = ERROR)
    @Message(value = "Error removing key %s", id = 72000)
    void errorRemovingKey(Object key, @Cause Exception e);
}
