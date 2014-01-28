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

import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.model.ModelException;
import org.onehippo.repository.documentworkflow.DocumentVariant;
import org.onehippo.repository.documentworkflow.task.ConfigVariantTask;

/**
 * ConfigVariantAction delegating the execution to ConfigVariantTask.
 */
public class ConfigVariantAction extends AbstractDocumentTaskAction<ConfigVariantTask> {

    private static final long serialVersionUID = 1L;

    public String getVariant() {
        return getParameter("variantExpr");
    }

    public void setVariant(String variant) {
        setParameter("variantExpr", variant);
    }

    public String getAvailabilities() {
        return getParameter("availabilities");
    }

    public void setAvailabilities(String availabilities) {
        setParameter("availabilities", availabilities);
    }

    public boolean isApplyModified() {
        return Boolean.parseBoolean(getParameter("applyModified"));
    }

    public void setApplyModified(String applyModified) {
        setParameter("applyModified", applyModified);
    }

    public boolean isVersionable() {
        return Boolean.parseBoolean(getParameter("versionable"));
    }

    public void setVersionable(String versionable) {
        setParameter("versionable", versionable);
    }

    public boolean isSetHolder() {
        return Boolean.parseBoolean(getParameter("setHolder"));
    }

    public void setSetHolder(String setHolder) {
        setParameter("setHolder", setHolder);
    }

    @Override
    protected ConfigVariantTask createWorkflowTask() {
        return new ConfigVariantTask();
    }

    @Override
    protected void initTask(ConfigVariantTask task) throws ModelException, SCXMLExpressionException {
        super.initTask(task);
        task.setVariant((DocumentVariant)eval(getVariant()));
        task.setAvailabilities(getAvailabilities());
        task.setApplyModified(isApplyModified());
        task.setVersionable(isVersionable());
        task.setSetHolder(isSetHolder());
    }
}
