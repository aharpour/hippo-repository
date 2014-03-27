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
package org.onehippo.repository.scxml;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.env.SimpleDispatcher;

/**
 * RepositorySCXMLExecutorFactory
 */
public class RepositorySCXMLExecutorFactory implements SCXMLExecutorFactory {

    void initialize() {
    }

    @Override
    public SCXMLExecutor createSCXMLExecutor(SCXMLDefinition scxmlDef) throws SCXMLException {

        SCXMLExecutor executor = new SCXMLExecutor(scxmlDef.getEvaluator(), new SimpleDispatcher(), new SCXMLStrictErrorReporter(scxmlDef));
        executor.setRootContext(scxmlDef.getEvaluator().newContext(null));
        executor.setStateMachine(scxmlDef.getSCXML());
        return executor;
    }

    void destroy() {
    }
}