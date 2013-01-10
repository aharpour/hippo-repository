/*
 *  Copyright 2012-2013 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.repository.mock;

import java.io.InputStream;
import java.io.OutputStream;

import javax.jcr.Credentials;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;
import javax.jcr.retention.RetentionManager;
import javax.jcr.security.AccessControlManager;

import org.xml.sax.ContentHandler;

/**
 * Mock version of a {@link Session}. It only returns the root node. Saving changes is ignored.
 * All methods that are not implemented throw an {@link UnsupportedOperationException}.
 */
public class MockSession implements Session {

    private final MockNode root;

    public MockSession(MockNode root) {
        this.root = root;
    }

    @Override
    public Node getRootNode() {
        return root;
    }

    @Override
    public void save() {
        // do nothing
    }

    @Override
    public Item getItem(final String absPath) throws RepositoryException {
        if (!absPath.startsWith("/")) {
            throw new IllegalArgumentException("Expected an absolute path");
        }
        Item item = getRootNode();
        for (String element : absPath.split("/")) {
            if (element.isEmpty()) {
                continue;
            }
            if (!item.isNode()) {
                throw new PathNotFoundException("No such item: " + absPath);
            }
            Node node = (Node) item;
            if (node.hasNode(element)) {
                item = node.getNode(element);
            } else if (node.hasProperty(element)) {
                item = node.getProperty(element);
            } else {
                throw new PathNotFoundException("No such item: " + absPath);
            }
        }
        return item;
    }

    @Override
    public Node getNode(final String absPath) throws RepositoryException {
        Item item = getItem(absPath);
        if (!item.isNode()) {
            throw new PathNotFoundException("No such node: " + absPath);
        }
        return (Node) item;
    }

    @Override
    public boolean itemExists(final String absPath) throws RepositoryException {
        try {
            getItem(absPath);
        } catch (PathNotFoundException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean nodeExists(final String absPath) throws RepositoryException {
        try {
            getNode(absPath);
        } catch (PathNotFoundException e) {
            return false;
        }
        return true;
    }

    // REMAINING METHODS ARE NOT IMPLEMENTED

    @Override
    public Repository getRepository() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getUserID() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getAttributeNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getAttribute(final String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Workspace getWorkspace() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Session impersonate(final Credentials credentials) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node getNodeByUUID(final String uuid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node getNodeByIdentifier(final String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Property getProperty(final String absPath) throws RepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean propertyExists(final String absPath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void move(final String srcAbsPath, final String destAbsPath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeItem(final String absPath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refresh(final boolean keepChanges) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasPendingChanges() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ValueFactory getValueFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasPermission(final String absPath, final String actions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void checkPermission(final String absPath, final String actions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasCapability(final String methodName, final Object target, final Object[] arguments) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ContentHandler getImportContentHandler(final String parentAbsPath, final int uuidBehavior) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void importXML(final String parentAbsPath, final InputStream in, final int uuidBehavior) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void exportSystemView(final String absPath, final ContentHandler contentHandler, final boolean skipBinary, final boolean noRecurse) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void exportSystemView(final String absPath, final OutputStream out, final boolean skipBinary, final boolean noRecurse) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void exportDocumentView(final String absPath, final ContentHandler contentHandler, final boolean skipBinary, final boolean noRecurse) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void exportDocumentView(final String absPath, final OutputStream out, final boolean skipBinary, final boolean noRecurse) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNamespacePrefix(final String prefix, final String uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getNamespacePrefixes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getNamespaceURI(final String prefix) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getNamespacePrefix(final String uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void logout() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLive() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addLockToken(final String lt) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getLockTokens() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeLockToken(final String lt) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AccessControlManager getAccessControlManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RetentionManager getRetentionManager() {
        throw new UnsupportedOperationException();
    }
}
