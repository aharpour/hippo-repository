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
package org.hippoecm.repository.decorating.checked;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessControlException;

import javax.jcr.AccessDeniedException;
import javax.jcr.Credentials;
import javax.jcr.InvalidItemStateException;
import javax.jcr.InvalidSerializedDataException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.LoginException;
import javax.jcr.NamespaceException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.retention.RetentionManager;
import javax.jcr.security.AccessControlManager;
import javax.jcr.version.VersionException;
import javax.transaction.xa.XAResource;

import org.apache.jackrabbit.api.XASession;
import org.hippoecm.repository.api.HippoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 */
public class SessionDecorator implements XASession, HippoSession {
    private static Logger log = LoggerFactory.getLogger(SessionDecorator.class);

    @SuppressWarnings("unused")
    private static final String SVN_ID = "$Id$";

    protected final DecoratorFactory factory;
    protected final Repository repository;
    protected HippoSession session;
    private Credentials credentials;
    private String workspaceName;

    protected SessionDecorator(DecoratorFactory factory, Repository repository, HippoSession session, Credentials credentials, String workspaceName) {
        this.factory = factory;
        this.repository = repository;
        this.session = session;
        this.credentials = credentials;
        this.workspaceName = workspaceName;
    }

    public static Session unwrap(Session session) {
        if (session == null) {
            return null;
        } else if (session instanceof SessionDecorator) {
            try {
                ((SessionDecorator)session).check();
            } catch(RepositoryException ex) {
            }
            return ((SessionDecorator)session).session;
        } else {
            return session;
        }
    }

    public XAResource getXAResource() {
        try {
            check();
        } catch(RepositoryException ex) {
        }
        return ((XASession)session).getXAResource();
    }

    /** {@inheritDoc} */
    public Repository getRepository() {
        return repository;
    }

    /**
     * Forwards the method call to the underlying session.
     */
    public String getUserID() {
        try {
            check();
        } catch(RepositoryException ex) {
        }
        return session.getUserID();
    }

    /**
     * Forwards the method call to the underlying session.
     */
    public Object getAttribute(String name) {
        try {
            check();
        } catch(RepositoryException ex) {
        }
        return session.getAttribute(name);
    }

    /**
     * Forwards the method call to the underlying session.
     */
    public String[] getAttributeNames() {
        try {
            check();
        } catch(RepositoryException ex) {
        }
        return session.getAttributeNames();
    }

    /**
     * Forwards the method call to the underlying session. The returned
     * workspace is wrapped into a workspace decorator using the decorator
     * factory.
     *
     * @return decorated workspace
     */
    public Workspace getWorkspace() {
        try {
            check();
        } catch(RepositoryException ex) {
        }
        return factory.getWorkspaceDecorator(this, session.getWorkspace());
    }

    /**
     * Forwards the method call to the underlying session. The returned
     * session is wrapped into a session decorator using the decorator factory.
     *
     * @return decorated session
     */
    public Session impersonate(Credentials credentials) throws LoginException, RepositoryException {
        check();
        Session newSession = session.impersonate(credentials);
        return factory.getSessionDecorator(repository, newSession, credentials, workspaceName);
    }

    /**
     * Forwards the method call to the underlying session. The returned
     * node is wrapped into a node decorator using the decorator factory.
     *
     * @return decorated node
     */
    public Node getRootNode() throws RepositoryException {
        check();
        Node root = session.getRootNode();
        return factory.getNodeDecorator(this, root);
    }

    /**
     * Forwards the method call to the underlying session. The returned
     * node is wrapped into a node decorator using the decorator factory.
     *
     * @return decorated node
     */
    public Node getNodeByUUID(String uuid) throws ItemNotFoundException, RepositoryException {
        check();
        Node node = session.getNodeByUUID(uuid);
        return factory.getNodeDecorator(this, node);
    }

    /**
     * Forwards the method call to the underlying session. The returned
     * item is wrapped into a node, property, or item decorator using
     * the decorator factory. The decorator type depends on the type
     * of the underlying item.
     *
     * @return decorated item, property, or node
     */
    public Item getItem(String absPath) throws PathNotFoundException, RepositoryException {
        check();
        Item item = session.getItem(absPath);
        return factory.getItemDecorator(this, item);
    }

    /**
     * Forwards the method call to the underlying session.
     */
    public boolean itemExists(String path) throws RepositoryException {
        check();
        return session.itemExists(path);
    }

    /**
     * Forwards the method call to the underlying session.
     */
    public void move(String srcAbsPath, String destAbsPath) throws ItemExistsException, PathNotFoundException,
                                                                   VersionException, RepositoryException {
        check();
        session.move(srcAbsPath, destAbsPath);
    }

    /**
     * Forwards the method call to the underlying session.
     */
    public void save() throws AccessDeniedException, ConstraintViolationException, InvalidItemStateException,
                              VersionException, LockException, RepositoryException {
        check();
        session.save();
    }

    /**
     * Forwards the method call to the underlying session.
     */
    public void refresh(boolean keepChanges) throws RepositoryException {
        check();
        session.refresh(keepChanges);
    }

    /**
     * Forwards the method call to the underlying session.
     */
    public boolean hasPendingChanges() throws RepositoryException {
        check();
        return session.hasPendingChanges();
    }

    /**
     * Forwards the method call to the underlying session.
     */
    public void checkPermission(String absPath, String actions) throws AccessControlException, RepositoryException {
        check();
        session.checkPermission(absPath, actions);
    }

    /**
     * Forwards the method call to the underlying session.
     */
    public ContentHandler getImportContentHandler(String parentAbsPath, int uuidBehaviour)
            throws PathNotFoundException, ConstraintViolationException, VersionException, LockException,
                   RepositoryException {
        check();
        return session.getImportContentHandler(parentAbsPath, uuidBehaviour);
    }

    /**
     * Forwards the method call to the underlying session.
     */
    public void importXML(String parentAbsPath, InputStream in, int uuidBehaviour) throws IOException,
                                                                                          PathNotFoundException, ItemExistsException, ConstraintViolationException, VersionException,
                                                                                          InvalidSerializedDataException, LockException, RepositoryException {
        check();
        session.importXML(parentAbsPath, in, uuidBehaviour);
    }

    public void exportSystemView(String absPath, ContentHandler contentHandler, boolean binaryAsLink, boolean noRecurse)
            throws PathNotFoundException, SAXException, RepositoryException {
        check();
        session.exportSystemView(absPath, contentHandler, binaryAsLink, noRecurse);
    }

    public void exportSystemView(String absPath, OutputStream out, boolean binaryAsLink, boolean noRecurse)
            throws IOException, PathNotFoundException, RepositoryException {
        check();
        session.exportSystemView(absPath, out, binaryAsLink, noRecurse);
    }

    /**
     * Forwards the method call to the underlying session.
     */
    public void exportDocumentView(String absPath, ContentHandler contentHandler, boolean binaryAsLink,
                                   boolean noRecurse) throws PathNotFoundException, SAXException, RepositoryException {
        check();
        session.exportDocumentView(absPath, contentHandler, binaryAsLink, noRecurse);
    }

    /**
     * Forwards the method call to the underlying session.
     */
    public void exportDocumentView(String absPath, OutputStream out, boolean binaryAsLink, boolean noRecurse)
            throws IOException, PathNotFoundException, RepositoryException {
        check();
        session.exportDocumentView(absPath, out, binaryAsLink, noRecurse);
    }

    /**
     * Forwards the method call to the underlying session.
     */
    public void setNamespacePrefix(String prefix, String uri) throws NamespaceException, RepositoryException {
        check();
        session.setNamespacePrefix(prefix, uri);
    }

    /**
     * Forwards the method call to the underlying session.
     */
    public String[] getNamespacePrefixes() throws RepositoryException {
        check();
        return session.getNamespacePrefixes();
    }

    /**
     * Forwards the method call to the underlying session.
     */
    public String getNamespaceURI(String prefix) throws NamespaceException, RepositoryException {
        check();
        return session.getNamespaceURI(prefix);
    }

    /**
     * Forwards the method call to the underlying session.
     */
    public String getNamespacePrefix(String uri) throws NamespaceException, RepositoryException {
        check();
        return session.getNamespacePrefix(uri);
    }

    /**
     * Forwards the method call to the underlying session.
     */
    public void logout() {
        try {
            check();
        } catch(RepositoryException ex) {
        }
        session.logout();
    }

    /**
     * Forwards the method call to the underlying session.
     */
    public void addLockToken(String lt) {
        try {
            check();
        } catch(RepositoryException ex) {
        }
        session.addLockToken(lt);
    }

    /**
     * Forwards the method call to the underlying session.
     */
    public String[] getLockTokens() {
        try {
            check();
        } catch(RepositoryException ex) {
        }
        return session.getLockTokens();
    }

    /**
     * Forwards the method call to the underlying session.
     */
    public void removeLockToken(String lt) {
        try {
            check();
        } catch(RepositoryException ex) {
        }
        session.removeLockToken(lt);
    }

    public ValueFactory getValueFactory() throws UnsupportedRepositoryOperationException, RepositoryException {
        check();
        return factory.getValueFactoryDecorator(this, session.getValueFactory());
    }

    public boolean isLive() {
        // no check() on purpose
        return session.isLive();
    }

    /**
     * Convenience function to copy a node to a destination path in the same workspace
     *
     * @param srcNode the source path node to copy
     * @param destAbsNodePath the absolute path of the to be created target
     * node which will be a copy of srcNode
     * @returns the resulting copy
     */
    public Node copy(Node srcNode, String destAbsNodePath) throws PathNotFoundException, ItemExistsException,
                                                                  LockException, VersionException, RepositoryException {
        check();
        return factory.getNodeDecorator(this, session.copy(srcNode, destAbsNodePath));
    }

    public NodeIterator pendingChanges(Node node, String nodeType, boolean prune) throws NamespaceException,
                                                                                         NoSuchNodeTypeException, RepositoryException {
        check();
        return new NodeIteratorDecorator(factory, this, session.pendingChanges(NodeDecorator.unwrap(node), nodeType, prune));
    }

    public NodeIterator pendingChanges(Node node, String nodeType) throws NamespaceException, NoSuchNodeTypeException,
                                                                          RepositoryException {
        check();
        return new NodeIteratorDecorator(factory, this, session.pendingChanges(NodeDecorator.unwrap(node), nodeType));
    }

    public NodeIterator pendingChanges() throws RepositoryException {
        check();
        return new NodeIteratorDecorator(factory, this, session.pendingChanges());
    }

    public ClassLoader getSessionClassLoader() throws RepositoryException {
        return Thread.currentThread().getContextClassLoader();
    }

    public void exportDereferencedView(String absPath, OutputStream out, boolean binaryAsLink, boolean noRecurse)
            throws IOException, PathNotFoundException, RepositoryException {
        check();
        session.exportDereferencedView(absPath, out, binaryAsLink, noRecurse);
    }

    public void importDereferencedXML(String parentAbsPath, InputStream in, int uuidBehavior, int referenceBehavior,
                                      int mergeBehavior) throws IOException, PathNotFoundException, ItemExistsException,
                                                                ConstraintViolationException, VersionException, InvalidSerializedDataException, LockException,
                                                                RepositoryException {
        check();
        session.importDereferencedXML(parentAbsPath, in, uuidBehavior, referenceBehavior, mergeBehavior);
    }

    public void check() throws RepositoryException {
        if(!session.isLive()) {
            repair();
        }
    }

    private void repair() throws RepositoryException {
        // FIXME: this assumes that an impersonated() session can also login()
        session = (HippoSession) repository.login(credentials, workspaceName);
    }

    public Node getNodeByIdentifier(String id) throws ItemNotFoundException, RepositoryException {
        check();
        Node node = session.getNodeByIdentifier(id);
        return factory.getNodeDecorator(this, node);
    }

    public Node getNode(String absPath) throws PathNotFoundException, RepositoryException {
        check();
        Node node = session.getNode(absPath);
        return factory.getNodeDecorator(this, node);
    }

    public Property getProperty(String absPath) throws PathNotFoundException, RepositoryException {
        check();
        Property property = session.getProperty(absPath);
        return factory.getPropertyDecorator(this, property);
    }

    public boolean nodeExists(String absPath) throws RepositoryException {
        return session.nodeExists(absPath);
    }

    public boolean propertyExists(String absPath) throws RepositoryException {
        return session.propertyExists(absPath);
    }

    public void removeItem(String absPath) throws VersionException, LockException, ConstraintViolationException, AccessDeniedException, RepositoryException {
        session.removeItem(absPath);
    }

    public boolean hasPermission(String absPath, String actions) throws RepositoryException {
        return session.hasPermission(absPath, actions);
    }

    public boolean hasCapability(String methodName, Object target, Object[] arguments) throws RepositoryException {
        return session.hasCapability(methodName, target, arguments);
    }

    public AccessControlManager getAccessControlManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public RetentionManager getRetentionManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void registerSessionCloseCallback(CloseCallback callback) {
        session.registerSessionCloseCallback(callback);
    }
}