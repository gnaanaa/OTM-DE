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
package org.opentravel.schemas.node.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.modelObject.SimpleAttributeMO;
import org.opentravel.schemas.modelObject.TLnSimpleAttribute;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.ImpliedNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.properties.EqExOneValueHandler.ValueWithContextType;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.TypeProvider;

/**
 * A property node that represents a simple property of a core or value with attributes object. See
 * {@link NodeFactory#newMember(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */

public class SimpleAttributeNode extends PropertyNode {
	// private static final Logger LOGGER = LoggerFactory.getLogger(SimpleAttributeNode.class);

	public SimpleAttributeNode(TLnSimpleAttribute tlObj, INode parent) {
		super(tlObj, parent, PropertyNodeType.SIMPLE);

		if (parent != null) {
			TLModelElement tlOwner = ((Node) parent.getParent()).getTLModelObject();
			if ((tlOwner instanceof TLFacetOwner))
				tlObj.setParentObject(tlOwner);
		}

		assert (modelObject instanceof SimpleAttributeMO);

	}

	@Override
	public boolean canAssign(Node type) {
		return type instanceof TypeProvider ? ((TypeProvider) type).isAssignableToSimple() : false;
	}

	@Override
	public INode createProperty(Node type) {
		// Need for DND but can't actually create a property, just set the type.
		if (type instanceof TypeProvider)
			setAssignedType((TypeProvider) type);
		return this;
	}

	/**
	 * Simple Attribute Properties are new to a chain if their parent is new. Override the behavior in the property
	 * class.
	 */
	@Override
	public boolean isNewToChain() {
		if (getChain() == null || super.isNewToChain())
			return true; // the parent is new so must be its properties
		return false;
	}

	@Override
	public boolean isOnlySimpleTypeUser() {
		return true;
	}

	@Override
	public boolean isEnabled_AddProperties() {
		return false;
	}

	@Override
	public boolean isMandatory() {
		return getTLModelObject().isMandatory();
	}

	@Override
	public boolean isRenameable() {
		return false; // name must come from owning object
	}

	@Override
	public boolean hasNavChildren(boolean deep) {
		return deep && getNavType() != null;
	}

	@Override
	public boolean isNavChild(boolean deep) {
		return deep;
	}

	// Do not show implied types in tree views
	private Node getNavType() {
		Node type = getType();
		return type instanceof ImpliedNode ? null : type;
	}

	/**
	 * Return new array containing assigned type
	 */
	public List<Node> getNavChildren(boolean deep) {
		Node type = getNavType();
		ArrayList<Node> kids = new ArrayList<Node>();
		if (deep && type != null)
			kids.add(type);
		return kids;
	}

	@Override
	public String getName() {
		return emptyIfNull(getTLModelObject().getName());
	}

	@Override
	public TLnSimpleAttribute getTLModelObject() {
		return (TLnSimpleAttribute) (modelObject != null ? modelObject.getTLModelObj() : null);
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.SIMPLE_ATTRIBUTE;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.XSDAttribute);
	}

	@Override
	public TypeProvider getAssignedType() {
		return typeHandler.get();
	}

	@Override
	public void setName(String name) {
		// LOGGER.debug("Tried to set the name of a simple property.");
	}

	@Override
	public IValueWithContextHandler getEquivalentHandler() {
		if (equivalentHandler == null)
			equivalentHandler = new EqExOneValueHandler(this, ValueWithContextType.EQUIVALENT);
		return equivalentHandler;
	}

	@Override
	public String getEquivalent(String context) {
		return getEquivalentHandler().get(context);
	}

	@Override
	public IValueWithContextHandler setEquivalent(String example) {
		getEquivalentHandler().set(example, null);
		return equivalentHandler;
	}

	@Override
	public IValueWithContextHandler getExampleHandler() {
		if (exampleHandler == null)
			exampleHandler = new EqExOneValueHandler(this, ValueWithContextType.EXAMPLE);
		return exampleHandler;
	}

	@Override
	public String getExample(String context) {
		return getExampleHandler().get(context);
	}

	@Override
	public IValueWithContextHandler setExample(String example) {
		getExampleHandler().set(example, null);
		return exampleHandler;
	}

}
