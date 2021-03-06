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
package org.opentravel.schemas.node;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.controllers.LibraryModelManager;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.NodeModelEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model Node is a conceptual node that is never displayed. It is the parent of all nodes at the root of the navigator
 * tree.
 * 
 * It maintains contents of the model.
 * 
 * @author Dave Hollander
 * 
 */
public class ModelNode extends Node {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModelNode.class);

	private final static AtomicInteger counter = new AtomicInteger(0);
	private TLModel tlModel;
	private String name = "";
	// Just have one so we can skip checking for null MO and TLmodelObjects

	// Constants defined here because it is a singleton
	public static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
	public static final String Chameleon_NS = "http://chameleon.anonymous/ns";

	private TLModelElement tlModelEle = new TLModelElement() {

		@Override
		public String getValidationIdentity() {
			return "The_Model";
		}

		@Override
		public TLModel getOwningModel() {
			return getTLModel();
		}
	};

	// Statistics
	private int unresolvedTypes = 0;

	// These nodes are not in the TL model but used within the node model.
	// They allow all nodes to have a type and related properties.
	// private static TLLibrary impliedTLLib = makeImpliedLibrary();
	protected static ImpliedNode undefinedNode = new ImpliedNode(ImpliedNode.Undefined);
	protected static ImpliedNode indicatorNode = new ImpliedNode(ImpliedNodeType.Indicator);
	protected static ImpliedNode unassignedNode = new ImpliedNode(ImpliedNodeType.UnassignedType);
	protected static ImpliedNode defaultStringNode = new ImpliedNode(ImpliedNodeType.String);
	protected static ImpliedNode atomicTypeNode = new ImpliedNode(ImpliedNodeType.XSD_Atomic);
	protected static ImpliedNode unionTypeNode = new ImpliedNode(ImpliedNodeType.Union);
	// protected static ImpliedNode duplicateTypesNode = new ImpliedNode(ImpliedNodeType.Duplicate);
	// protected List<Node> duplicateTypes = new ArrayList<Node>();

	protected static Node emptyNode = null; // will be set to a built-in type.
	private static final QName OTA_EMPTY_QNAME = new QName("http://www.opentravel.org/OTM/Common/v0", "Empty");

	protected ModelContentsData mc = new ModelContentsData();

	private LibraryModelManager libMgr = null;

	/**
	 * Constructor
	 * 
	 * @param model
	 */
	public ModelNode(final TLModel model) {
		super();
		setParent(null);
		// duplicateTypesNode.initialize(this);
		undefinedNode.initialize(this);
		indicatorNode.initialize(this);
		unassignedNode.initialize(this);
		defaultStringNode.initialize(this);
		name = "Model_Root_" + counter.incrementAndGet();
		root = this;
		tlModel = model;
		libMgr = new LibraryModelManager(this);

		addListeners();

		// LOGGER.debug("ModelNode(TLModel) done.");
	}

	/**
	 * 
	 * @return new list of libraries managed by the library model manager
	 */
	public List<LibraryInterface> getManagedLibraries() {
		return libMgr.getLibraries();
	}

	public LibraryModelManager getLibraryManager() {
		return libMgr;
	}

	public void addProject(final ProjectNode project) {
		getChildren().add(project);
		project.setParent(this);
	}

	@Override
	public void close() {
		List<Node> kids = new ArrayList<Node>(getChildren());
		for (Node n : kids)
			n.close();
		libMgr = new LibraryModelManager(this);
		undefinedNode.initialize(this);
		indicatorNode.initialize(this);
		unassignedNode.initialize(this);
		defaultStringNode.initialize(this);
	}

	@Override
	public List<Node> getChildren() {
		if (!super.getChildren().contains(ModelNode.getUnassignedNode())) {
			super.getChildren().add(ModelNode.getUnassignedNode());
			// Enhancement - enable duplicate display. Needs controls to keep it up to
			// date with changes throughout the model before enabling.
			//
			// navChildren.add(ModelNode.getDuplicateTypesNode());
		}
		return super.getChildren();
	}

	@Override
	public String getComponentType() {
		return "Model";
	}

	public TLModel getTLModel() {
		return tlModel;
	}

	public void addListeners() {
		tlModel.addListener(new NodeModelEventListener());
	}

	@Override
	public TLModelElement getTLModelObject() {
		// Models do not have model elements, just TLModel.
		// TLModel is not an TLModelElement.
		// But return an empty just have one so we can skip checking for
		// null MO and TLmodelObjects
		return tlModelEle;
	}

	@Override
	public Node getParent() {
		return null; // top of the tree
	}

	@Override
	public boolean hasChildren_TypeProviders() {
		return getChildren().size() > 0 ? true : false;
	}

	/**
	 * @return true if namespace is managed by any of the child projects
	 */
	public boolean isInProjectNS(String namespace) {
		for (Node n : getChildren())
			if (n instanceof ProjectNode)
				if (namespace.startsWith(n.getNamespace())) // order is significant due to versions
					return true;
		return false;
	}

	@Override
	public boolean isNavigation() {
		return true;
	}

	@Override
	public boolean isUnique(final INode testNode) {
		return true;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public boolean isDeleted() {
		return deleted;
	}

	@Override
	public boolean isDeleteable() {
		return false;
	}

	@Override
	public LibraryNode getLibrary() {
		return null;
	}

	/**
	 * @return the empty node
	 */
	public static INode getEmptyNode() {
		setEmptyNode();
		return emptyNode;
	}

	public static TLAttributeType getEmptyType() {
		setEmptyNode();
		return emptyNode != null ? (TLAttributeType) emptyNode.getTLModelObject() : null;
	}

	private static void setEmptyNode() {
		// Find the built-in empty node.
		if (emptyNode == null)
			emptyNode = NodeFinders.findNodeByQName(OTA_EMPTY_QNAME);
		if (emptyNode == null)
			LOGGER.error("Empty Node could not be set. Be sure that library is loaded early.");
	}

	/**
	 * @return the indicatorNode
	 */
	public static ImpliedNode getIndicatorNode() {
		return indicatorNode;
	}

	/**
	 * @return the undefined node for use on nodes that have no type associated with them.
	 */
	public static ImpliedNode getUndefinedNode() {
		return undefinedNode;
	}

	/**
	 * @return the unassignedNode for use on nodes that should have types assigned but are <i>missing</i>
	 */
	public static ImpliedNode getUnassignedNode() {
		return unassignedNode;
	}

	/**
	 * @return the defaultStringNode
	 */
	public static ImpliedNode getDefaultStringNode() {
		return defaultStringNode;
	}

	/**
	 * @return the unresolvedTypes
	 */
	public int getUnresolvedTypeCount() {
		return unresolvedTypes;
	}

	@Override
	public boolean isLibraryContainer() {
		return true;
	}

}
