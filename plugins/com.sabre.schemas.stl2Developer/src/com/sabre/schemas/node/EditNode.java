/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * The EditNode class extends nodes to be used ONLY for editor construction of components. It is
 * never linked into the node tree.
 * 
 * @author Dave Hollander
 * 
 */
public class EditNode extends ComponentNode {
    static final Logger LOGGER = LoggerFactory.getLogger(EditNode.class);
    protected PropertyNodeType propertyType; // Attribute or Element or Indicator
    protected String nodeType; // Type of the node: Property, Navigation, Facet
                               // �
    protected String useType; // how it can be used: Element, Simple, Complex or
                              // Service
    private Node tlType;
    private String name;

    public EditNode() {
        super();
        nodeType = "";
        useType = "";
        propertyType = PropertyNodeType.UNKNOWN;
    }

    public EditNode(final String nam) {
        this();
        if (nam == null || nam.isEmpty()) {
            setName(UNDEFINED_PROPERTY_TXT);
        } else {
            setName(nam);
        }
    }

    public EditNode(LibraryNode ln) {
        this();
        setLibrary(ln);
    }

    public INode getAssignedNode() {
        // FIXME - use the type field and return the node where the type is
        // defined.
        return null;
    }

    /**
     * Get the node type which describes how the node can be used in the GUI
     * 
     * @return
     */
    public String getNodeType() {
        return nodeType == null ? "" : nodeType;
    }

    @Override
    public int getRepeat() {
        return modelObject.getRepeat();
    }

    /**
     * Get the use type which describes how the node can be used in the construction of other model
     * components
     * 
     * @return
     */
    public String getUseType() {
        return useType == null ? "" : useType;
    }

    @Override
    public boolean isSimpleType() {
        return false;
    }

    /**
     * Edit nodes may NOT be linked into the tree.
     */
    @Override
    public boolean linkChild(final Node child) {
        return false;
    }

    @Override
    public boolean linkChild(final Node child, final boolean doFamily) {
        return false;
    }

    @Override
    public boolean linkChild(final Node child, final int index) {
        return false;
    }

    /***********************************************************************
     * Source Object
     */

    @Override
    public boolean linkIfUnique(final Node child) {
        return false;
    }

    /**
     * Set the type elements of the node.
     * 
     * TODO - validate these are proper values
     * 
     * @param node
     *            - type of node
     * @param use
     *            - useType
     * @param property
     *            - property role
     * @return
     */
    public boolean loadNodeTypes(final String node, final String use,
            final PropertyNodeType property) {
        nodeType = node;
        useType = use;
        propertyType = property;
        return true;
    }

    /**
     * Set the type.
     * 
     * @param type
     * @return
     */
    public boolean setAssignedType(final String type) {
        return true;
    }

    /**
     * set the assigned type of <i>this</i> node. Does <b>not</b> set the type of the underlying
     * model object.
     * 
     * @param type
     * @param nsPrefix
     *            Warning: DOES NOT set value in base model.
     */
    public void setAssignedTypeName(final String type, final String nsPrefix) {
    }

    /**
     * Just set the node's name field and don't worry about family names.
     */
    @Override
    public void setName(final String n) {
        name = n;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * @param type
     */
    public void setNodeType(final String type) {
        nodeType = type;
    }

    public void setPropertyType(final PropertyNodeType role) {
        propertyType = role; // TODO - test to assure valid value
    }

    public PropertyNodeType getPropertyType() {
        return propertyType;
    }

    @Override
    public void setRepeat(final int cnt) {
        if (modelObject != null) {
            modelObject.setRepeat(cnt);
        }
    }

    public void setUseType(final String use) {
        useType = use;
    }

    /**
     * @return the tlType
     */
    public Node getTLType() {
        return tlType;
    }

    /**
     * @param tlType
     *            - the tlType to set TODO - should this really be setting the AssignedType?
     */
    public void setTLType(final Node tlType) {
        this.tlType = tlType;
    }

}