package org.apache.camel.bam.processor;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;

import org.apache.camel.bam.model.ActivityState;
import org.apache.camel.bam.rules.ProcessRules;
import org.apache.camel.impl.ServiceSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.orm.jpa.JpaCallback;
import org.springframework.orm.jpa.JpaTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * A timer engine to monitor for expired activities and perform whatever actions
 * are required.
 * 
 * @version $Revision: 630591 $
 */
public class ActivityMonitorEngine extends ServiceSupport implements Runnable {
    private static final Log LOG = LogFactory.getLog(ActivityMonitorEngine.class);
    private JpaTemplate template;
    private TransactionTemplate transactionTemplate;
    private ProcessRules rules;
    private int escalateLevel;
    private long windowMillis = 1000L;
    private Thread thread;
    private boolean useLocking;

    public ActivityMonitorEngine(JpaTemplate template, TransactionTemplate transactionTemplate, ProcessRules rules) {
        this.template = template;
        this.transactionTemplate = transactionTemplate;
        this.rules = rules;
    }

    public boolean isUseLocking() {
        return useLocking;
    }

    public void setUseLocking(boolean useLocking) {
        this.useLocking = useLocking;
    }

    public void run() {
        LOG.debug("Starting to poll for timeout events");

        while (!isStopped()) {
            try {
                long now = System.currentTimeMillis();
                long nextPoll = now + windowMillis;
                final Date timeNow = new Date(now);

                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        List<ActivityState> list = template.find("select x from " + ActivityState.class.getName() + " x where x.timeOverdue < ?1", timeNow);
                        for (ActivityState activityState : list) {
                            fireExpiredEvent(activityState);
                        }
                    }
                });

                long timeToSleep = nextPoll - System.currentTimeMillis();
                if (timeToSleep > 0) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Sleeping for " + timeToSleep + " millis");
                    }
                    try {
                        Thread.sleep(timeToSleep);
                    } catch (InterruptedException e) {
                        LOG.debug("Caught: " + e, e);
                    }
                }
            } catch (Exception e) {
                LOG.error("Caught: " + e, e);
            }
        }
    }

    protected void fireExpiredEvent(final ActivityState activityState) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Trying to fire expiration of: " + activityState);
        }

        template.execute(new JpaCallback() {
            public Object doInJpa(EntityManager entityManager) throws PersistenceException {
                if (isUseLocking()) {
                    LOG.info("Attempting to lock: " + activityState);
                    entityManager.lock(activityState, LockModeType.WRITE);
                    LOG.info("Grabbed lock: " + activityState);
                }

                try {
                    rules.processExpired(activityState);
                } catch (Exception e) {
                    LOG.error("Failed to process expiration of: " + activityState + ". Reason: " + e, e);
                }
                activityState.setTimeOverdue(null);
                return null;
            }
        });
    }

    protected void doStart() throws Exception {
        rules.start();
        thread = new Thread(this, "ActivityMonitorEngine");
        thread.start();
    }

    protected void doStop() throws Exception {
        if (thread != null) {
            thread = null;
        }
        rules.stop();
    }
}
