/*
 * Copyright 2012-2013 Hippo B.V. (http://www.onehippo.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onehippo.repository.impl;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import org.hippoecm.repository.HierarchyResolverImpl;
import org.hippoecm.repository.api.DocumentManager;
import org.hippoecm.repository.api.HierarchyResolver;
import org.hippoecm.repository.api.WorkflowManager;
import org.hippoecm.repository.impl.DocumentManagerImpl;
import org.hippoecm.repository.impl.WorkflowManagerImpl;
import org.onehippo.repository.ManagerService;

public class ManagerServiceImpl implements ManagerService {
    Session session, rootSession;
    DocumentManagerImpl documentManager = null;
    WorkflowManagerImpl workflowManager = null;
    HierarchyResolver hierarchyResolver = null;

    public ManagerServiceImpl(Session session) {
        this.session = session;
    }

    public DocumentManager getDocumentManager() throws RepositoryException {
        if (documentManager == null) {
            documentManager = new DocumentManagerImpl(session);
        }
        return documentManager;
    }

    public WorkflowManager getWorkflowManager() throws RepositoryException {
        try {
            if (workflowManager == null) {
                rootSession = session.impersonate(new SimpleCredentials("workflowuser", new char[] {}));
                workflowManager = new WorkflowManagerImpl(session, rootSession);
            }
            return workflowManager;
        } catch (LoginException ex) {
            throw ex;
        } catch (RepositoryException ex) {
            throw ex;
        }
    }

    @Override
    public HierarchyResolver getHierarchyResolver() throws RepositoryException {
        if (hierarchyResolver == null) {
            hierarchyResolver = new HierarchyResolverImpl();
        }
        return hierarchyResolver;
    }

    @Override
    public void close() {
        if (workflowManager != null) {
            workflowManager.close();
        }
        if (documentManager != null) {
            documentManager.close();
        }
        if (rootSession != null) {
            rootSession.logout();
        }
        session = rootSession = null;
        workflowManager = null;
        hierarchyResolver = null;
    }
}