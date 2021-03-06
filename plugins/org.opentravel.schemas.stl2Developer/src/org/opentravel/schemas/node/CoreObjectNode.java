/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
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
/**
 * 
 */
package org.opentravel.schemas.node;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLComplexTypeBase;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemas.modelObject.CoreObjectMO;
import org.opentravel.schemas.modelObject.ListFacetMO;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.facets.ListFacetNode;
import org.opentravel.schemas.node.facets.RoleFacetNode;
import org.opentravel.schemas.node.facets.SimpleFacetNode;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
import org.opentravel.schemas.node.properties.SimpleAttributeNode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.ExtensionHandler;
import org.opentravel.schemas.types.SimpleAttributeOwner;
import org.opentravel.schemas.types.TypeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core Object. This object has many facets: simple, summary, detail, roles and two lists. It implements the complex
 * component interface.
 * 
 * @author Dave Hollander
 * 
 */
public class CoreObjectNode extends LibraryMemberBase implements ComplexComponentInterface, ExtensionOwner,
		VersionedObjectInterface, LibraryMemberInterface, TypeProvider, SimpleAttributeOwner {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CoreObjectNode.class);
	private ExtensionHandler extensionHandler = null;

	public CoreObjectNode(TLCoreObject mbr) {
		super(mbr);
		addMOChildren();
		extensionHandler = new ExtensionHandler(this);

		assert (modelObject instanceof CoreObjectMO);
		// If the mbr was not null but simple type is, set the simple type
		if (getTLModelObject().getSimpleFacet().getSimpleType() == null)
			setSimpleType((TypeProvider) ModelNode.getEmptyNode());

	}

	/**
	 * Create new core with same name and documentation as the business object. Copy all ID and summary facet properties
	 * in to the summary facet. Copy all detail properties into detail facet.
	 * 
	 * Note: this core is added to the same library as the business object creating a validation error.
	 * 
	 * @param bo
	 */
	public CoreObjectNode(BusinessObjectNode bo) {
		this(new TLCoreObject());

		addAliases(bo.getAliases());

		setName(bo.getName());
		bo.getLibrary().addMember(this);
		setDocumentation(bo.getDocumentation());

		((FacetNode) getFacet_Summary()).copyFacet((FacetNode) bo.getIDFacet());
		((FacetNode) getFacet_Summary()).copyFacet(bo.getFacet_Summary());
		((FacetNode) getFacet_Detail()).copyFacet((FacetNode) bo.getFacet_Detail());
		setSimpleType((TypeProvider) ModelNode.getEmptyNode());
	}

	/**
	 * Add to VWA's library a new core with a copy of all the VWA attributes in the summary facet.
	 * 
	 * @param vwa
	 */
	public CoreObjectNode(VWA_Node vwa) {
		this(new TLCoreObject());

		setName(vwa.getName());
		vwa.getLibrary().addMember(this);
		setDocumentation(vwa.getDocumentation());

		getFacet_Summary().copyFacet(vwa.getAttributeFacet());
		setSimpleType(vwa.getSimpleType());

		// User assist - create an attribute for the VWA base type
		AttributeNode attr = new AttributeNode(new TLAttribute(), getFacet_Summary());
		attr.setName(vwa.getName());
		attr.setAssignedType(vwa.getSimpleType());
	}

	public AliasNode addAlias(String name) {
		AliasNode alias = null;
		if (this.isEditable_newToChain())
			alias = new AliasNode(this, NodeNameUtils.fixCoreObjectName(name));
		return alias;
	}

	public void addAliases(List<AliasNode> aliases) {
		for (AliasNode a : aliases)
			addAlias(a.getName());
	}

	@Override
	public ComponentNode createMinorVersionComponent() {
		return super.createMinorVersionComponent(new CoreObjectNode((TLCoreObject) createMinorTLVersion(this)));
	}

	// @Override
	// public boolean isExtensible() {
	// return getTLModelObject() != null ? !((TLComplexTypeBase) getTLModelObject()).isNotExtendable() : false;
	// }

	@Override
	public boolean isExtensibleObject() {
		return true;
	}

	@Override
	public Node setExtensible(boolean extensible) {
		if (isEditable_newToChain())
			if (getTLModelObject() instanceof TLComplexTypeBase)
				((TLComplexTypeBase) getTLModelObject()).setNotExtendable(!extensible);
		return this;
	}

	@Override
	public String getName() {
		return getTLModelObject() == null || getTLModelObject().getName() == null ? "" : getTLModelObject().getName();
	}

	@Override
	public TLCoreObject getTLModelObject() {
		return (TLCoreObject) (modelObject != null ? modelObject.getTLModelObj() : null);
	}

	// @Override
	// public boolean isNamedType() {
	// return true;
	// }

	@Override
	public List<Node> getChildren_TypeUsers() {
		ArrayList<Node> users = new ArrayList<Node>();
		users.add((Node) getSimpleType());
		users.addAll(getFacet_Summary().getChildren());
		users.addAll(getFacet_Detail().getChildren());
		return users;
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.CORE;
	}

	// /////////////////////////////////////////////////////////////////
	//
	// Simple Attribute Owner implementations
	//
	@Override
	public TypeProvider getSimpleType() {
		return getSimpleAttribute().getAssignedType();
	}

	@Override
	public boolean setSimpleType(TypeProvider type) {
		return getSimpleAttribute().setAssignedType(type);
	}

	@Override
	public SimpleAttributeNode getSimpleAttribute() {
		return getFacet_Simple().getSimpleAttribute();
	}

	@Override
	public SimpleFacetNode getFacet_Simple() {
		for (INode f : getChildren())
			if (f instanceof SimpleFacetNode)
				return (SimpleFacetNode) f;
		return null;
	}

	@Override
	public Node getSimpleProperty() {
		return getFacet_Simple().getChildren().get(0);
	}

	@Override
	public FacetNode getFacet_Summary() {
		for (INode f : getChildren())
			if (f instanceof FacetNode && ((FacetNode) f).isSummaryFacet())
				return (FacetNode) f;
		return null;
	}

	@Override
	public PropertyOwnerInterface getFacet_Default() {
		return getFacet_Summary();
	}

	@Override
	public FacetNode getFacet_Detail() {
		for (INode f : getChildren())
			if (f instanceof FacetNode && ((FacetNode) f).isDetailFacet())
				return (FacetNode) f;
		return null;
	}

	// Role w/model object RoleEnumerationMO
	// @Override
	public RoleFacetNode getRoleFacet() {
		for (Node f : getChildren())
			if (f instanceof RoleFacetNode)
				return (RoleFacetNode) f;
		return null;
	}

	// List w/model object ListFacetMO - Simple_List
	public ComponentNode getSimpleListFacet() {
		for (Node f : getChildren())
			if (f instanceof ListFacetNode && ((ListFacetNode) f).isSimpleListFacet())
				return (ComponentNode) f;
		return null;
	}

	// List w/model object ListFacetMO - Detail_List
	public ComponentNode getDetailListFacet() {
		for (Node f : getChildren())
			if (f.modelObject instanceof ListFacetMO)
				if (((ListFacetMO) f.modelObject).isDetailList())
					return (ComponentNode) f;

		return null;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.CoreObject);
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.PROPERTY;
	}

	public List<AliasNode> getAliases() {
		List<AliasNode> aliases = new ArrayList<AliasNode>();
		for (Node c : getChildren())
			if (c instanceof AliasNode)
				aliases.add((AliasNode) c);
		return aliases;
	}

	@Override
	public PropertyOwnerInterface getAttributeFacet() {
		return null;
	}

	@Override
	public boolean hasChildren_TypeProviders() {
		return isXsdType() ? false : true;
	}

	@Override
	public boolean isAssignableToSimple() {
		return true;
	}

	@Override
	public boolean isAssignableToVWA() {
		return true;
	}

	@Override
	public boolean isAssignedByReference() {
		// Note, core can also be assigned by type.
		return true;
	}

	@Override
	public void setName(String n) {
		getTLModelObject().setName(NodeNameUtils.fixCoreObjectName(n));
		updateNames(NodeNameUtils.fixCoreObjectName(n));
	}

	@Override
	public void sort() {
		((FacetNode) getFacet_Summary()).sort();
		((FacetNode) getFacet_Detail()).sort();
	}

	@Override
	public void merge(Node source) {
		if (!(source instanceof CoreObjectNode)) {
			throw new IllegalStateException("Can only merge objects with the same type");
		}
		CoreObjectNode core = (CoreObjectNode) source;
		getFacet_Summary().addProperties(core.getFacet_Summary().getChildren(), true);
		getFacet_Detail().addProperties(core.getFacet_Detail().getChildren(), true);
		getRoleFacet().addProperties(core.getRoleFacet().getChildren(), true);
	}

	@Override
	public boolean isMergeSupported() {
		return true;
	}

	@Override
	public boolean isAliasable() {
		return isEditable_newToChain();
	}

	@Override
	public boolean isAssignableToElementRef() {
		return false;
	}

	// /////////////////////////////////////////////////////////////////
	//
	// Extension Owner implementations
	//
	@Override
	public Node getExtensionBase() {
		return extensionHandler != null ? extensionHandler.get() : null;
	}

	public String getExtendsTypeNS() {
		return modelObject.getExtendsTypeNS();
	}

	@Override
	public void setExtension(final Node base) {
		if (extensionHandler == null)
			extensionHandler = new ExtensionHandler(this);
		extensionHandler.set(base);
	}

	@Override
	public ExtensionHandler getExtensionHandler() {
		return extensionHandler;
	}

}
