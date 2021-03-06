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

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemas.modelObject.IndicatorMO;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.ImpliedNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.properties.Images;

/**
 * A property node that represents a boolean XML element with the semantics of "False unless present and true". See
 * {@link NodeFactory#newMember(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */

public class IndicatorElementNode extends PropertyNode {

	public IndicatorElementNode(PropertyOwnerInterface parent, String name) {
		super(new TLIndicator(), (Node) parent, name, PropertyNodeType.INDICATOR_ELEMENT);
		((TLIndicator) getTLModelObject()).setPublishAsElement(true);
	}

	public IndicatorElementNode(TLIndicator tlObj, PropertyOwnerInterface parent) {
		super(tlObj, (INode) parent, PropertyNodeType.INDICATOR_ELEMENT);
		((TLIndicator) tlObj).setPublishAsElement(true);

		assert (modelObject instanceof IndicatorMO);
	}

	@Override
	public boolean canAssign(Node type) {
		return (type == ModelNode.getIndicatorNode() || type == ModelNode.getUndefinedNode());
	}

	@Override
	public boolean isNavChild(boolean deep) {
		return false;
	}

	@Override
	public boolean isRenameable() {
		return isEditable() && !inherited;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.PropertyNode#createProperty(org.opentravel.schemas.node.Node)
	 */
	@Override
	public INode createProperty(Node type) {
		TLIndicator tlObj = (TLIndicator) cloneTLObj();
		int index = indexOfNode();
		((TLIndicator) getTLModelObject()).getOwner().addIndicator(index, tlObj);
		IndicatorElementNode n = new IndicatorElementNode(tlObj, null);
		n.setName(type.getName());
		getParent().linkChild(n, indexOfNode());
		((TLIndicator) getTLModelObject()).getOwner().addIndicator(index, tlObj);
		n.setDescription(type.getDescription());
		return n;
	}

	@Override
	public ImpliedNode getRequiredType() {
		return ModelNode.getIndicatorNode();
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.INDICATOR_ELEMENT;
	}

	@Override
	public TLIndicator getTLModelObject() {
		return (TLIndicator) getModelObject().getTLModelObj();
	}

	@Override
	public String getName() {
		return emptyIfNull(getTLModelObject().getName());
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.IndicatorElement);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#getLabel()
	 */
	@Override
	public String getLabel() {
		return getName();
		// return modelObject.getLabel() == null ? "" : modelObject.getLabel();
	}

	@Override
	public int indexOfTLProperty() {
		final TLIndicator thisProp = (TLIndicator) getTLModelObject();
		return thisProp.getOwner().getIndicators().indexOf(thisProp);
	}

	@Override
	public boolean isIndicator() {
		return true;
	}

	@Override
	public void setName(String name) {
		getTLModelObject().setName(NodeNameUtils.fixIndicatorElementName(name));
	}

}
