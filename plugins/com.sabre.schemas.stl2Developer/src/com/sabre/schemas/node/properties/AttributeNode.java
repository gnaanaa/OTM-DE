package com.sabre.schemas.node.properties;

import org.eclipse.swt.graphics.Image;

import com.sabre.schemacompiler.model.TLAttribute;
import com.sabre.schemacompiler.model.TLModelElement;
import com.sabre.schemas.node.AliasNode;
import com.sabre.schemas.node.INode;
import com.sabre.schemas.node.ModelNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.NodeFactory;
import com.sabre.schemas.node.NodeNameUtils;
import com.sabre.schemas.node.PropertyNodeType;
import com.sabre.schemas.node.VWA_Node;
import com.sabre.schemas.properties.Images;
import com.sabre.schemas.types.TypeProvider;
import com.sabre.schemas.types.TypeUser;

/**
 * A property node that represents an XML attribute. See
 * {@link NodeFactory#newComponentMember(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */
public class AttributeNode extends PropertyNode implements TypeUser {

    public AttributeNode(Node parent, String name) {
        super(new TLAttribute(), parent, name, PropertyNodeType.ATTRIBUTE);
        setAssignedType(ModelNode.getUnassignedNode());
    }

    public AttributeNode(TLModelElement tlObj, INode parent) {
        super(tlObj, parent, PropertyNodeType.ATTRIBUTE);

        if (!(tlObj instanceof TLAttribute))
            throw new IllegalArgumentException("Invalid object for an attribute.");
    }

    @Override
    public boolean canAssign(Node type) {
        if (super.canAssign(type)) {
            TypeProvider provider = (TypeProvider) type;

            // GUI assist: aliases stand in for their base type on attributes.
            if (type instanceof AliasNode)
                provider = (TypeProvider) type.getOwningComponent();

            if (getOwningComponent() instanceof VWA_Node)
                return provider.isAssignableToVWA();
            else
                return provider.isAssignableToSimple();
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.PropertyNode#createProperty(com.sabre.schemas.node.Node)
     */
    @Override
    public INode createProperty(Node type) {
        if (!(getTLModelObject() instanceof TLAttribute))
            throw new IllegalArgumentException("Invalid object for an attribute.");

        TLAttribute tlObj = (TLAttribute) cloneTLObj();
        int index = indexOfNode();
        Node n = new AttributeNode(tlObj, null);
        ((TLAttribute) getTLModelObject()).getAttributeOwner().addAttribute(index, tlObj);
        n.setName(type.getName(), false);
        getParent().linkChild(n, index);
        n.setDescription(type.getDescription());
        n.setAssignedType(type);
        return n;
    }

    @Override
    public Image getImage() {
        return Images.getImageRegistry().get(Images.XSDAttribute);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.INode#getLabel()
     */
    @Override
    public String getLabel() {
        String label = modelObject.getLabel();
        if (getType() != null)
            label = getName() + " [" + getTypeNameWithPrefix() + "]";
        return label;
    }

    // /*
    // * (non-Javadoc)
    // *
    // * @see com.sabre.schemas.node.PropertyNode#isAttributeProperty()
    // */
    // @Override
    // public boolean isAttributeProperty() {
    // return true;
    // }

    @Override
    public int indexOfTLProperty() {
        final TLAttribute thisProp = (TLAttribute) getTLModelObject();
        return thisProp.getAttributeOwner().getAttributes().indexOf(thisProp);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.Node#isSimpleTypeUser()
     */
    @Override
    public boolean isOnlySimpleTypeUser() {
        // allow VWAs to be assigned to VWA Attributes.
        return parent != null && parent.isVWA_AttributeFacet() ? false : true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.Node#isTypeUser()
     */
    @Override
    public boolean isTypeUser() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.PropertyNode#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        modelObject.setName(NodeNameUtils.fixAttributeName(name));
    }

}