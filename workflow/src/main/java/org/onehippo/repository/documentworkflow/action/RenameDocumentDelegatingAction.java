/*
 * Copyright 2013 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onehippo.repository.documentworkflow.action;

import java.util.Map;

import org.onehippo.repository.documentworkflow.DocumentHandle;
import org.onehippo.repository.documentworkflow.task.RenameDocumentWorkflowTask;
import org.onehippo.repository.scxml.AbstractWorkflowTaskDelegatingAction;

/**
 * RenameDocumentDelegatingAction delegating the execution to RenameDocumentWorkflowTask.
 * <P>
 * Note: All the setters must be redefined to delegate to the RenameDocumentWorkflowTask.
 * </P>
 */
public class RenameDocumentDelegatingAction extends AbstractWorkflowTaskDelegatingAction<RenameDocumentWorkflowTask> {

    private static final long serialVersionUID = 1L;

    public String getNewNameExpr() {
        return (String) getProperties().get("newName");
    }

    public void setNewNameExpr(String newNameExpr) {
        getProperties().put("newName", newNameExpr);
    }

    @Override
    protected RenameDocumentWorkflowTask createWorkflowTask() {
        return new RenameDocumentWorkflowTask();
    }

    @Override
    protected void initTaskBeforeEvaluation(Map<String, Object> properties) {
        super.initTaskBeforeEvaluation(properties);
        DocumentHandle dm = getContextAttribute("dm");
        getWorkflowTask().setDataModel(dm);
    }

}
