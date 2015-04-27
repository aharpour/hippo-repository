/*
 * Copyright 2015 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hippoecm.repository.impl;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.lock.LockManager;

import org.hippoecm.repository.util.JcrUtils;
import org.onehippo.repository.locking.HippoLockManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hippoecm.repository.api.HippoNodeType.HIPPO_TIMEOUT;
import static org.hippoecm.repository.api.HippoNodeType.NT_LOCKABLE;

public class LockManagerDecorator extends org.hippoecm.repository.decorating.LockManagerDecorator implements HippoLockManager {

    private static final Logger log = LoggerFactory.getLogger(LockManagerDecorator.class);
    private static final Calendar NO_TIMEOUT = Calendar.getInstance();
    static {
        NO_TIMEOUT.setTimeInMillis(Long.MAX_VALUE);
    }

    public LockManagerDecorator(final Session session, final LockManager lockManager) {
        super(session, lockManager);
    }

    public static LockManager unwrap(LockManager lockManager) {
        if (lockManager instanceof LockManagerDecorator) {
            return ((LockManagerDecorator) lockManager).lockManager;
        }
        return lockManager;
    }

    @Override
    public boolean expireLock(final String absPath) throws LockException, RepositoryException {
        final Node lockNode = session.getNode(absPath);
        final Calendar timeout = JcrUtils.getDateProperty(lockNode, HIPPO_TIMEOUT, NO_TIMEOUT);
        if (System.currentTimeMillis() < timeout.getTimeInMillis()) {
            return false;
        }
        try {
            unlock(absPath);
        } catch (LockException e) {
            return !isLocked(absPath);
        }
        return true;
    }

    @Override
    public Lock lock(final String absPath, final boolean isDeep, final boolean isSessionScoped, final long timeoutHint, final String ownerInfo)
            throws RepositoryException {
        if (isLocked(absPath)) {
            if (!expireLock(absPath)) {
                throw new LockException("Already locked: " + absPath);
            }
        }
        final Lock lock = super.lock(absPath, isDeep, isSessionScoped, timeoutHint, ownerInfo);
        setTimeout(lock, timeoutHint);
        return new LockDecorator(lock);
    }

    @Override
    public Lock getLock(final String absPath) throws RepositoryException {
        return new LockDecorator(super.getLock(absPath));
    }

    private void setTimeout(final Lock lock, final long timeoutHint) {
        try {
            final Node lockNode = lock.getNode();
            if (timeoutHint != Long.MAX_VALUE) {
                lockNode.addMixin(NT_LOCKABLE);
                final Calendar timeout = Calendar.getInstance();
                final long timeoutTime = System.currentTimeMillis() + timeoutHint * 1000;
                timeout.setTimeInMillis(timeoutTime);
                lockNode.setProperty(HIPPO_TIMEOUT, timeout);
            } else {
                if (lockNode.hasProperty(HIPPO_TIMEOUT)) {
                    lockNode.getProperty(HIPPO_TIMEOUT).remove();
                }
            }
            lockNode.getSession().save();
        } catch (RepositoryException e) {
            if (log.isDebugEnabled()) {
                log.error("Failed to set hippo:timeout on lock", e);
            } else {
                log.error("Failed to set hippo:timeout on lock: {}", e.toString());
            }
        }
    }

    public class LockDecorator implements Lock {

        private final Lock lock;

        private LockDecorator(final Lock lock) {
            this.lock = lock;
        }

        @Override
        public String getLockOwner() {
            return lock.getLockOwner();
        }

        @Override
        public boolean isDeep() {
            return lock.isDeep();
        }

        @Override
        public Node getNode() {
            return lock.getNode();
        }

        @Override
        public String getLockToken() {
            return lock.getLockToken();
        }

        @Override
        public long getSecondsRemaining() throws RepositoryException {
            return lock.getSecondsRemaining();
        }

        @Override
        public boolean isLive() throws RepositoryException {
            return lock.isLive();
        }

        @Override
        public boolean isSessionScoped() {
            return lock.isSessionScoped();
        }

        @Override
        public boolean isLockOwningSession() {
            return lock.isLockOwningSession();
        }

        @Override
        public void refresh() throws LockException, RepositoryException {
            lock.refresh();
            setTimeout(lock, getSecondsRemaining());
        }
    }
}
