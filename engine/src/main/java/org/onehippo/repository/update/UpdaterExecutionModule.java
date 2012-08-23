package org.onehippo.repository.update;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.lock.LockManager;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.codehaus.groovy.control.CompilationFailedException;
import org.hippoecm.repository.api.HippoNodeType;
import org.hippoecm.repository.ext.DaemonModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdaterExecutionModule implements DaemonModule, EventListener {

    private static final Logger log = LoggerFactory.getLogger(UpdaterExecutionModule.class);

    private static final String UPDATE_PATH = "/" + HippoNodeType.CONFIGURATION_PATH + "/hippo:update";
    private static final String UPDATE_QUEUE_PATH = UPDATE_PATH + "/hippo:queue";
    private static final String UPDATE_HISTORY_PATH = UPDATE_PATH + "/hippo:history";
    private static final long TWO_MINUTES = 1000 * 60 * 2;
    private static final String DEFAULT_CLUSTER_NODE_ID = "default";

    private final ExecutorService updaterExecutor = Executors.newSingleThreadExecutor();
    private Session session;

    @Override
    public void initialize(final Session session) throws RepositoryException {
        this.session = session;
        session.getWorkspace().getObservationManager().addEventListener(this, Event.NODE_ADDED, UPDATE_QUEUE_PATH, false, null, null, false);
        // check if any updaters are queued and execute them on startup
        executeUpdaterTask();
    }

    @Override
    public void shutdown() {
        try {
            session.getWorkspace().getObservationManager().removeEventListener(this);
        } catch (RepositoryException e) {
            log.error(e.getClass().getName() + ": " + e.getMessage(), e);
        }
        updaterExecutor.shutdown();
    }

    @Override
    public void onEvent(final EventIterator events) {
        executeUpdaterTask();
    }

    private void executeUpdaterTask() {
        updaterExecutor.execute(new UpdaterTask());
    }

    private final class UpdaterTask implements Runnable {

        private Lock lock;
        private ScheduledExecutorService keepAliveExecutor;

        private UpdaterTask() {}

        @Override
        public void run() {
            try {
                if (lock()) {
                    execute();
                }
            } catch (RepositoryException e) {
                log.error("Failed to obtain lock", e);
            } finally {
                unlock();
            }
        }

        private boolean lock() throws RepositoryException {
            log.debug("Trying to obtain lock");
            final LockManager lockManager = session.getWorkspace().getLockManager();
            if (!lockManager.isLocked(UPDATE_PATH)) {
                try {
                    lock = lockManager.lock(UPDATE_PATH, false, false, TWO_MINUTES, getClusterNodeId());
                    log.debug("Lock successfully obtained");
                    scheduleKeepAlive();
                    return true;
                } catch (LockException e) {
                    // happens when other cluster node beat us to it
                    log.debug("Failed to set lock: " + e.getMessage());
                }
            }
            return false;
        }

        private void execute() {
            NodeIterator nodes;
            try {
                nodes = session.getNode(UPDATE_QUEUE_PATH).getNodes();
                if (!nodes.hasNext()) {
                    log.debug("Updater queue is empty. Nothing to execute");
                    return;
                }
            } catch (RepositoryException e) {
                log.error("Failed to get nodes in the updater queue", e);
                return;
            }
            final Node node = nodes.nextNode();
            if (node != null) {
                UpdaterExecutor updaterExecutor = null;
                try {
                    updaterExecutor = new UpdaterExecutor(node, session);
                    updaterExecutor.execute();
                } catch (RepositoryException e) {
                    log.error(e.getClass().getName() + ": " + e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    log.error(e.getClass().getName() + ": " + e.getMessage(), e);
                } catch (InstantiationException e) {
                    log.error(e.getClass().getName() + ": " + e.getMessage(), e);
                } catch (ClassNotFoundException e) {
                    log.error(e.getClass().getName() + ": " + e.getMessage(), e);
                } catch (IllegalArgumentException e) {
                    log.error(e.getClass().getName() + ": " + e.getMessage(), e);
                } catch (CompilationFailedException e) {
                    log.error(e.getClass().getName() + ": " + e.getMessage(), e);
                } catch (IOException e) {
                    log.error("Could not execute updater: log initialization failed", e);
                } finally {
                    if (updaterExecutor != null) {
                        updaterExecutor.destroy();
                    }
                    moveToHistory(node);
                }
            }
            // check for more queued updaters
            execute();
        }

        private void unlock() {
            log.debug("Trying to release lock");
            try {
                final LockManager lockManager = session.getWorkspace().getLockManager();
                if (lockManager.isLocked(UPDATE_PATH)) {
                    final Lock lock = lockManager.getLock(UPDATE_PATH);
                    if (lock.isLockOwningSession()) {
                        lockManager.unlock(UPDATE_PATH);
                        log.debug("Lock successfully released");
                    }
                }
            } catch (RepositoryException e) {
                log.error("Failed to release lock", e);
            } finally {
                if (keepAliveExecutor != null) {
                    keepAliveExecutor.shutdown();
                }
            }
        }

        private void moveToHistory(final Node node) {
            try {

                final String srcPath = node.getPath();
                long index = session.getNode(UPDATE_HISTORY_PATH).getNodes(node.getName() + "*").getSize();
                final String destPath = UPDATE_HISTORY_PATH + "/" + node.getName() + "-" + index;
                session.move(srcPath, destPath);
                session.save();
            } catch (RepositoryException e) {
                log.error("Failed to remove updater from queue", e);
            }
        }

        private String getClusterNodeId() {
            String clusteNodeId = session.getRepository().getDescriptor("jackrabbit.cluster.id");
            if (clusteNodeId == null) {
                clusteNodeId = DEFAULT_CLUSTER_NODE_ID;
            }
            return clusteNodeId;
        }

        private void scheduleKeepAlive() {
            keepAliveExecutor = Executors.newSingleThreadScheduledExecutor();
            keepAliveExecutor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    refreshLock();
                }
            }, 1l, 1l, TimeUnit.MINUTES);
        }

        private void refreshLock() {
            try {
                lock.refresh();
                log.debug("Lock successfully refreshed");
            } catch (RepositoryException e) {
                log.error("Failed to refresh lock", e);
            }
        }
    }

}
