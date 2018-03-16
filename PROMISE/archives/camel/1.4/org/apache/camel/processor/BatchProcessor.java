package org.apache.camel.processor;

import java.util.Collection;
import java.util.Iterator;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.PollingConsumer;
import org.apache.camel.Processor;
import org.apache.camel.impl.LoggingExceptionHandler;
import org.apache.camel.impl.ServiceSupport;
import org.apache.camel.spi.ExceptionHandler;
import org.apache.camel.util.ServiceHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A base class for any kind of {@link Processor} which implements some kind of
 * batch processing.
 * 
 * @version $Revision: 669756 $
 */
public class BatchProcessor extends ServiceSupport implements Runnable, Processor {
    public static final long DEFAULT_BATCH_TIMEOUT = 1000L;
    public static final int DEFAULT_BATCH_SIZE = 100;

    private static final transient Log LOG = LogFactory.getLog(BatchProcessor.class);
    private Endpoint endpoint;
    private Processor processor;
    private Collection<Exchange> collection;
    private long batchTimeout = DEFAULT_BATCH_TIMEOUT;
    private int batchSize = DEFAULT_BATCH_SIZE;
    private PollingConsumer consumer;
    private ExceptionHandler exceptionHandler;

    public BatchProcessor(Endpoint endpoint, Processor processor, Collection<Exchange> collection) {
        this.endpoint = endpoint;
        this.processor = processor;
        this.collection = collection;
    }

    @Override
    public String toString() {
        return "BatchProcessor[to: " + processor + "]";
    }

    public void run() {
        LOG.debug("Starting thread for " + this);
        while (isRunAllowed()) {
            try {
                processBatch();
            } catch (Exception e) {
                getExceptionHandler().handleException(e);
            }
        }
        collection.clear();
    }

    public ExceptionHandler getExceptionHandler() {
        if (exceptionHandler == null) {
            exceptionHandler = new LoggingExceptionHandler(getClass());
        }
        return exceptionHandler;
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public long getBatchTimeout() {
        return batchTimeout;
    }

    public void setBatchTimeout(long batchTimeout) {
        this.batchTimeout = batchTimeout;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public Processor getProcessor() {
        return processor;
    }

    /**
     * A transactional method to process a batch of messages up to a timeout
     * period or number of messages reached.
     */
    protected synchronized void processBatch() throws Exception {
        long start = System.currentTimeMillis();
        long end = start + batchTimeout;
        for (int i = 0; !isBatchCompleted(i); i++) {
            long timeout = end - System.currentTimeMillis();
            if (timeout < 0L) {                
                LOG.debug("batch timeout expired at batch index:"  + i);
                break;
            }
            Exchange exchange = consumer.receive(timeout);
            if (exchange == null) {
                LOG.debug("receive with timeout: " + timeout + " expired at batch index:"  + i);
                break;
            }
            collection.add(exchange);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Finished batch size: " + batchSize + " timeout: " + batchTimeout + " so sending set: "
                      + collection);
        }

        Iterator<Exchange> iter = collection.iterator();
        while (iter.hasNext()) {
            Exchange exchange = iter.next();
            iter.remove();
            processExchange(exchange);
        }
    }

    /**
     * A strategy method to decide if the batch is completed the resulting exchanges should be sent
     */
    protected boolean isBatchCompleted(int index) {
        return index >= batchSize;
    }

    /**
     * Strategy Method to process an exchange in the batch. This method allows
     * derived classes to perform custom processing before or after an
     * individual exchange is processed
     */
    protected void processExchange(Exchange exchange) throws Exception {
        processor.process(exchange);
    }

    protected void doStart() throws Exception {
        consumer = endpoint.createPollingConsumer();

        ServiceHelper.startServices(processor, consumer);

        Thread thread = new Thread(this, this + " Polling Thread");
        thread.start();
    }

    protected void doStop() throws Exception {
        ServiceHelper.stopServices(consumer, processor);
        collection.clear();
    }

    protected Collection<Exchange> getCollection() {
        return collection;
    }

    public void process(Exchange exchange) throws Exception {
    }
}