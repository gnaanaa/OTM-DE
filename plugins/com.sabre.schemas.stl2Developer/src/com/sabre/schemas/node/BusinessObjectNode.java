/**
 * 
 */
package com.sabre.schemas.node;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.graphics.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.codegen.util.FacetCodegenUtils;
import com.sabre.schemacompiler.model.LibraryMember;
import com.sabre.schemacompiler.model.TLBusinessObject;
import com.sabre.schemacompiler.model.TLComplexTypeBase;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLFacetOwner;
import com.sabre.schemacompiler.model.TLFacetType;
import com.sabre.schemas.modelObject.BusinessObjMO;
import com.sabre.schemas.modelObject.BusinessObjMO.Events;
import com.sabre.schemas.modelObject.FacetMO;
import com.sabre.schemas.modelObject.ModelObject;
import com.sabre.schemas.node.controllers.NodeUtils;
import com.sabre.schemas.node.properties.PropertyNode;
import com.sabre.schemas.properties.Images;

/**
 * @author Dave Hollander
 * 
 */
public class BusinessObjectNode extends ComponentNode implements ComplexComponentInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessObjectNode.class);

    public BusinessObjectNode(LibraryMember mbr) {
        super(mbr);
        addMOChildren();
        getModelObject().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (Events.FACET_ADDED.toString().equals(evt.getPropertyName())) {
                    createNewFacet(BusinessObjectNode.this, evt.getNewValue());
                } else if (Events.FACET_UPDATED.toString().equals(evt.getPropertyName())) {
                    TLFacet ff = (TLFacet) evt.getNewValue();
                    FacetNode node = findFacet(ff.getLabel(), ff.getContext());
                    if (node == null) {
                        LOGGER.warn("Couldnt find inhertied facet. Will recreate.");
                        createNewFacet(BusinessObjectNode.this, evt.getNewValue());
                    } else if (node.getModelObject() instanceof FacetMO) {
                        ((FacetMO) node.getModelObject()).attachInheritanceListener();
                    }
                } else if (Events.FACET_REMOVED.toString().equals(evt.getPropertyName())) {
                    TLFacet ff = (TLFacet) evt.getOldValue();
                    FacetNode node = findFacet(ff.getLabel(), ff.getContext());
                    node.delete();
                }
            }

            private void createNewFacet(BusinessObjectNode businessObjectNode, Object newValue) {
                NodeFactory.newComponentMember(businessObjectNode, newValue);
            }

        });
    }

    public BusinessObjectNode(CoreObjectNode core) {
        this(new TLBusinessObject());

        setLibrary(core.getLibrary());
        setName(core.getName());
        setDocumentation(core.getDocumentation());

        getSummaryFacet().copyFacet((FacetNode) core.getSummaryFacet());
        ((FacetNode) getDetailFacet()).copyFacet((FacetNode) core.getDetailFacet());
    }

    public BusinessObjectNode(VWA_Node vwa) {
        this(new TLBusinessObject());

        setLibrary(vwa.getLibrary());
        setName(vwa.getName());
        setDocumentation(vwa.getDocumentation());

        getSummaryFacet().copyFacet((FacetNode) vwa.getAttributeFacet());
    }

    @Override
    public boolean isExtensible() {
        return getTLModelObject() != null ? !((TLComplexTypeBase) getTLModelObject())
                .isNotExtendable() : false;
    }

    @Override
    public boolean isExtensibleObject() {
        return true;
    }

    @Override
    public Node setExtensible(boolean extensible) {
        if (getTLModelObject() instanceof TLComplexTypeBase)
            ((TLComplexTypeBase) getTLModelObject()).setNotExtendable(!extensible);
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.types.TypeProvider#getTypeNode()
     */
    @Override
    public Node getTypeNode() {
        return getTypeClass().getTypeNode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.INode#hasChildren_TypeProviders()
     */
    @Override
    public boolean hasChildren_TypeProviders() {
        return isXsdType() ? false : true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.Node#isNamedType()
     */
    @Override
    public boolean isNamedType() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.Node#isAssignedByReference()
     */
    @Override
    public boolean isAssignedByReference() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.Node#getChildren_TypeUsers()
     */
    @Override
    public List<Node> getChildren_TypeUsers() {
        ArrayList<Node> users = new ArrayList<Node>();
        users.addAll(getIDFacet().getChildren());
        users.addAll(getSummaryFacet().getChildren());
        users.addAll(getDetailFacet().getChildren());
        for (INode facet : getCustomFacets())
            users.addAll(facet.getChildren());
        for (INode facet : getQueryFacets())
            users.addAll(facet.getChildren());
        return users;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.ComplexComponentInterface#getSimpleType()
     */
    @Override
    public ComponentNode getSimpleType() {
        return null;
    }

    @Override
    public boolean setSimpleType(Node type) {
        return false;
    }

    @Override
    public SimpleFacetNode getSimpleFacet() {
        return null;
    }

    // FacetMO
    @Override
    public FacetNode getSummaryFacet() {
        for (INode f : getChildren())
            if (((FacetNode) f).getFacetType().equals(TLFacetType.SUMMARY))
                return (FacetNode) f;
        return null;
    }

    // FacetMO
    @Override
    public ComponentNode getDetailFacet() {
        for (INode f : getChildren())
            if (((FacetNode) f).getFacetType().equals(TLFacetType.DETAIL))
                return (ComponentNode) f;
        return null;
    }

    @Override
    public String getLabel() {
        if (getExtendsType() == null)
            return super.getLabel();
        else
            return super.getLabel() + " (E> " + getExtendsType().getNameWithPrefix() + ")";
    }

    @Override
    public Node getExtendsType() {
        return getTypeClass().getTypeNode();
    }

    @Override
    public Image getImage() {
        return Images.getImageRegistry().get(Images.BusinessObject);
    }

    public FacetNode addFacet(String name, String context, TLFacetType type) {
        if (getLibrary().isMajorVersion() || versionNode == null) {
            TLFacet newTlFacet = getModelObject().addFacet(name, context, type);
            FacetNode ff = (FacetNode) NodeFactory.newComponentMember(this, newTlFacet);
            return ff;
        }
        // New facets can only be added in unmanaged or major versions.
        // TODO - consider allowing them in minor and use createMinorVersionOfComponent()
        LOGGER.debug("Tried to add facet to a minor or patch version.");
        return null;
    }

    @Override
    public BusinessObjMO getModelObject() {
        ModelObject<?> obj = super.getModelObject();
        return (BusinessObjMO) (obj instanceof BusinessObjMO ? obj : null);
    }

    // Custom Facets
    public List<ComponentNode> getCustomFacets() {
        ArrayList<ComponentNode> ret = new ArrayList<ComponentNode>();
        for (INode f : getChildren()) {
            if (((Node) f).isCustomFacet()) {
                ret.add((ComponentNode) f);
            }
        }
        return ret;
    }

    public List<ComponentNode> getQueryFacets() {
        ArrayList<ComponentNode> ret = new ArrayList<ComponentNode>();
        for (INode f : getChildren()) {
            if (((Node) f).isQueryFacet()) {
                ret.add((ComponentNode) f);
            }
        }
        return ret;
    }

    private FacetNode findFacet(String label, String context) {
        label = emptyIfNull(label);
        context = emptyIfNull(context);
        for (Node c : getChildren()) {
            if (c instanceof FacetNode) {
                TLFacet tlFacet = (TLFacet) c.getTLModelObject();
                if (label.equals(emptyIfNull(tlFacet.getLabel()))
                        && context.equals(emptyIfNull(tlFacet.getContext())))
                    return (FacetNode) c;
            }
        }
        return null;
    }

    private String emptyIfNull(String str) {
        if (str == null)
            return "";
        return str;
    }

    /**
     * It is copy of {@link FacetCodegenUtils#findGhostFacets(TLFacetOwner, TLFacetType)} but with
     * this difference that it returns all facet with given facet type from all extension hierarchy
     * of facetOwner.
     * 
     * @param facetOwner
     *            the facet owner for which to return "ghost facets"
     * @param facetType
     *            the type of ghost facets to retrieve
     * @return List<TLFacet>
     */
    public List<TLFacet> findInheritedFacets(TLFacetOwner facetOwner, TLFacetType facetType) {
        Set<String> inheritedFacetNames = new HashSet<String>();
        List<TLFacet> inheritedFacets = new ArrayList<TLFacet>();
        TLFacetOwner extendedOwner = FacetCodegenUtils.getFacetOwnerExtension(facetOwner);
        Set<TLFacetOwner> visitedOwners = new HashSet<TLFacetOwner>();

        // Find all of the inherited facets of the specified facet type
        while (extendedOwner != null) {
            List<TLFacet> facetList = FacetCodegenUtils
                    .getAllFacetsOfType(extendedOwner, facetType);

            for (TLFacet facet : facetList) {
                String facetKey = facetType.getIdentityName(facet.getContext(), facet.getLabel());

                if (!inheritedFacetNames.contains(facetKey)) {
                    inheritedFacetNames.add(facetKey);
                    inheritedFacets.add(facet);
                }
            }
            visitedOwners.add(extendedOwner);
            extendedOwner = FacetCodegenUtils.getFacetOwnerExtension(extendedOwner);

            if (visitedOwners.contains(extendedOwner)) {
                break; // exit if we encounter a circular reference
            }
        }

        List<TLFacet> ghostFacets = new ArrayList<TLFacet>();

        for (TLFacet inheritedFacet : inheritedFacets) {
            TLFacet ghostFacet = new TLFacet();
            ghostFacet.setFacetType(facetType);
            ghostFacet.setContext(inheritedFacet.getContext());
            ghostFacet.setLabel(inheritedFacet.getLabel());
            ghostFacet.setOwningEntity(facetOwner);
            ghostFacets.add(ghostFacet);
        }
        return ghostFacets;
    }

    @Override
    public ComponentNode getAttributeFacet() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.Node#setName(java.lang.String)
     */
    @Override
    public void setName(String n) {
        this.setName(n, true);
        for (Node child : getChildren()) {
            for (Node users : child.getTypeUsers())
                NodeNameUtils.fixName(users);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.Node#setName(java.lang.String, boolean)
     */
    @Override
    public void setName(String n, boolean doFamily) {
        super.setName(n, doFamily);
        for (Node user : getTypeUsers()) {
            if (user instanceof PropertyNode)
                user.setName(n);
        }
    }

    @Override
    public void sort() {
        getSummaryFacet().sort();
        ((FacetNode) getDetailFacet()).sort();
        for (ComponentNode f : getCustomFacets())
            ((FacetNode) f).sort();
        for (ComponentNode f : getQueryFacets())
            ((FacetNode) f).sort();
    }

    @Override
    public void merge(Node source) {
        if (!(source instanceof BusinessObjectNode)) {
            throw new IllegalStateException("Can only merge objects with the same type");
        }
        BusinessObjectNode business = (BusinessObjectNode) source;
        getIDFacet().addProperties(business.getIDFacet().getChildren(), true);
        getSummaryFacet().addProperties(business.getSummaryFacet().getChildren(), true);
        getDetailFacet().addProperties(business.getDetailFacet().getChildren(), true);
        copyFacet(business.getCustomFacets());
        copyFacet(business.getQueryFacets());
    }

    private void copyFacet(List<ComponentNode> facets) {
        for (ComponentNode f : facets) {
            FacetNode facet = (FacetNode) f;
            if (!NodeUtils.checker(facet).isInheritedFacet().get()) {
                TLFacet tlFacet = (TLFacet) facet.getTLModelObject();
                FacetNode newFacet = addFacet(tlFacet.getLabel(), tlFacet.getContext(),
                        tlFacet.getFacetType());
                newFacet.addProperties(facet.getChildren(), true);
            }
        }
    }

    @Override
    public boolean isMergeSupported() {
        return true;
    }

}