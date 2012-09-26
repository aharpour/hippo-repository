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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.hippoecm.repository.TestCase;
import org.hippoecm.repository.api.HippoWorkspace;
import org.hippoecm.repository.api.Workflow;
import org.hippoecm.repository.api.WorkflowException;
import org.hippoecm.repository.api.WorkflowManager;

public class FailingWorkflowTest extends TestCase {

    private String[] content = {
        "/test", "nt:unstructured",
        "/test/folder", "hippostd:folder",
        "jcr:mixinTypes", "hippo:harddocument",
        "/test/folder/document", "hippo:handle",
        "jcr:mixinTypes", "hippo:hardhandle",
        "/test/folder/document/document", "hippostdpubwf:test",
        "hippostdpubwf:createdBy", "admin",
        "hippostdpubwf:creationDate", "2010-02-04T16:32:28.068+02:00",
        "hippostdpubwf:lastModifiedBy", "admin",
        "hippostdpubwf:lastModificationDate", "2010-02-04T16:32:28.068+02:00",
        "jcr:mixinTypes", "hippo:harddocument",
        "hippostd:holder", "admin",
        "hippostd:state", "published"
    };

    @Before
    public void setUp() throws Exception {
        super.setUp(true);
        Node root = session.getRootNode();
        if(root.hasNode("test")) {
            root.getNode("test").remove();
            session.save();
        }

        build(session, content);
        session.save();

        for (NodeIterator iter = session.getRootNode().getNode("hippo:configuration/hippo:workflows").getNodes(); iter.hasNext(); ) {
            for (NodeIterator i = iter.nextNode().getNodes(); i.hasNext(); ) {
                Node workflowNode = i.nextNode();
                if (workflowNode.hasProperty("hipposys:privileges")) {
                    workflowNode.getProperty("hipposys:privileges").remove();
                }
            }
        }

        session.save();
    }

    @After
    public void tearDown() throws Exception {
        Node root = session.getRootNode();
        if(root.hasNode("test")) {
            root.getNode("test").remove();
        }
        super.tearDown();
    }

    @Test
    public void testFailAfterRename() throws Exception {
        WorkflowManager manager = ((HippoWorkspace) session.getWorkspace()).getWorkflowManager();
        Node handle = session.getRootNode().getNode("test/folder/document");
        Node document = handle.getNode(handle.getName());
        // WorkflowDescriptor workflowDescriptor = manager.getWorkflowDescriptor("default", document);
        // Workflow workflowInterface = manager.getWorkflow(workflowDescriptor);
        Workflow workflowInterface = manager.getWorkflow("default", document);
        assertTrue(workflowInterface instanceof FullReviewedActionsWorkflow);
        FullReviewedActionsWorkflow workflow = (FullReviewedActionsWorkflow) workflowInterface;
        try {
            workflow.rename("fail");
            fail("rename should have failed");
        } catch(WorkflowException ex) {
            // expected
        }

        document = handle.getNode(handle.getName());
        // workflowDescriptor = manager.getWorkflowDescriptor("default", document);
        // workflowInterface = manager.getWorkflow(workflowDescriptor);
        workflowInterface = manager.getWorkflow("default", document);
        assertTrue(workflowInterface instanceof FullReviewedActionsWorkflow);
        workflow = (FullReviewedActionsWorkflow) workflowInterface;

        workflow.depublish();

        document = handle.getNode(handle.getName());
        // workflowDescriptor = manager.getWorkflowDescriptor("default", document);
        // workflowInterface = manager.getWorkflow(workflowDescriptor);
        workflowInterface = manager.getWorkflow("default", document);
        assertTrue(workflowInterface instanceof FullReviewedActionsWorkflow);
        workflow = (FullReviewedActionsWorkflow) workflowInterface;
        workflow.rename("succeed");
    }
}