/*
 *  Copyright 2012 Hippo.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.onehippo.repository.update;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;

/**
 * Base {@link Updater} class adding support for logging.
 */
public abstract class BaseUpdater implements Updater {

    protected Logger log;

    public void setLogger(Logger log) {
        this.log = log;
    }

    @Override
    public void initialize(Session session) throws RepositoryException {
    }

    @Override
    public void destroy() {
    }

}