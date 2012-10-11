/*
 *  Copyright 2008 Hippo.
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
package org.hippoecm.repository.reviewedactions;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.hippoecm.repository.api.MappingException;
import org.hippoecm.repository.api.WorkflowException;
import org.hippoecm.repository.ext.WorkflowImpl;

@PersistenceCapable
public class UnlockWorkflowImpl extends WorkflowImpl implements UnlockWorkflow {

    @Persistent(column=".")
    protected PublishableDocument document;

    public UnlockWorkflowImpl() throws RemoteException {
    }

    @Override
    public Map<String, Serializable> hints() {
        Map<String, Serializable> info = super.hints();
        if (document == null || !"draft".equals(document.getState())) {
            info.put("unlock", false);
        }
        return info;
    }

    public void unlock() throws WorkflowException, RepositoryException, RemoteException {
        if (document == null) {
            throw new WorkflowException("No document to unlock");
        }
        document.setOwner(getWorkflowContext().getUserIdentity());
    }
}