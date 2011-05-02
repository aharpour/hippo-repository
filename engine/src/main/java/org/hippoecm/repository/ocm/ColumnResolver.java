package org.hippoecm.repository.ocm;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.PropertyDefinition;

public interface ColumnResolver {
    @SuppressWarnings("unused")
    final String SVN_ID = "$Id: ";
    
    public PropertyDefinition resolvePropertyDefinition(Node node, String column, int propertyType) throws RepositoryException;
    
    public Property resolveProperty(Node node, String column) throws RepositoryException;

    public Node resolveNode(Node node, String column) throws RepositoryException;

    public ColumnResolver.NodeLocation resolveNodeLocation(Node node, String column) throws RepositoryException;

    public JcrOID resolveClone(Cloneable cloned) throws RepositoryException;

    public Node copyClone(Node source, Cloneable cloned, Node target, String name, Node current) throws RepositoryException;

    public class NodeLocation {
        Node parent;
        Node child;
        String name;

        public NodeLocation(Node parent, Node child, String name) {
            this.parent = parent;
            this.child = child;
            this.name = name;
        }
    }
}
