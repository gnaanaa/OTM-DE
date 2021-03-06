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
package org.opentravel.schemas.node.libraries;

import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.interfaces.LibraryInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Library Navigation Node
 * 
 * The LibNavNode is used to provide a link between a library a specific project. Libraries exist only once in the model
 * but may be rendered under multiple projects. This node provides access to the library and from the library to a
 * specific project.
 * 
 * Methods in this class must have no knowledge of the contents of the library or chain.
 */

public class LibraryNavNode extends Node {
	private static final Logger LOGGER = LoggerFactory.getLogger(LibraryNavNode.class);

	protected static final String DEFAULT_LIBRARY_TYPE = "Library Nav";

	/**
	 */
	public LibraryNavNode(LibraryNode library, ProjectNode project) {
		assert (library != null);
		assert (project != null);

		// Save the project and child chain relationship
		getChildren().add(library);
		parent = project;

		// Make sure the library is not in project's children list.
		if (library.getParent() != null && library.getParent() instanceof ProjectNode)
			library.getParent().getChildren().remove(library);

		// Insert between library and project
		library.setParent(this);
		project.getChildren().add(this);

		// LOGGER.debug("Created library nav node for library " + library + " in project " + project);
	}

	/**
	 * Create library navigation node.
	 * 
	 * @param chain
	 *            - chain to associate with this project
	 * @param project
	 *            - set to this node's parent
	 */
	public LibraryNavNode(LibraryChainNode chain, ProjectNode project) {
		assert (chain != null);
		assert (project != null);

		// Save the project and child chain relationship
		getChildren().add(chain);
		parent = project;

		// Make sure the chain is not in project children list.
		if (chain.getParent() != null && chain.getParent() instanceof ProjectNode)
			chain.getParent().getChildren().remove(chain);

		// Insert between library chain and project
		chain.setParent(this);
		project.getChildren().add(this);

		// LOGGER.debug("Created library nav node for chain " + chain + " in project " + project);
	}

	public LibraryInterface getThisLib() {
		if (getChildren().isEmpty())
			return null;
		return (LibraryInterface) getChildren().get(0);
	}

	@Override
	public boolean isEditable() {
		return getThisLib() != null ? ((Node) getThisLib()).isEditable() : false;
	}

	@Override
	public String getComponentType() {
		return DEFAULT_LIBRARY_TYPE;
	}

	@Override
	public String getName() {
		// return getThisLib() != null ? getThisLib().getName() + "  " + nodeID + "/" + ((Node)
		// getThisLib()).getNodeID()
		// : "(*)";
		return getThisLib() != null ? getThisLib().getName() : "";
	}

	@Override
	public String getLabel() {
		// return getThisLib().getLabel() + "  " + nodeID + "/" + ((Node) getThisLib()).getNodeID();
		return getThisLib() != null ? getThisLib().getLabel() : "";
	}

	@Override
	public Image getImage() {
		return getThisLib() != null ? getThisLib().getImage() : null;
	}

	@Override
	public ProjectNode getParent() {
		return (ProjectNode) parent;
	}

	public ProjectNode getProject() {
		return (ProjectNode) parent;
	}

	@Override
	public boolean isLibraryContainer() {
		return true;
	}

	@Override
	public void close() {
		getModelNode().getLibraryManager().close(getThisLib(), getProject());
		unlinkNode();
		deleted = true;
		getChildren().clear();
	}

	@Override
	public LibraryNode getLibrary() {
		return getThisLib() != null ? getThisLib().getLibrary() : null;
	}

	@Override
	public boolean isNavChild(boolean deep) {
		return true;
	}

	@Override
	public boolean hasNavChildren(boolean deep) {
		return getThisLib() != null ? getThisLib().hasNavChildren(deep) : false;
	}

	@Override
	public List<Node> getNavChildren(boolean deep) {
		return getThisLib() != null ? getThisLib().getNavChildren(deep) : null;
	}

	@Override
	public boolean hasTreeChildren(boolean deep) {
		return getThisLib() != null ? getThisLib().hasTreeChildren(deep) : false;
	}

	@Override
	public boolean hasChildren_TypeProviders() {
		return getThisLib() != null ? ((Node) getThisLib()).hasChildren_TypeProviders() : false;
	}

	@Override
	public List<Node> getChildren_TypeProviders() {
		return getThisLib() != null ? ((Node) getThisLib()).getChildren_TypeProviders() : null;
	}

	@Override
	public List<Node> getTreeChildren(boolean deep) {
		return getThisLib() != null ? getThisLib().getTreeChildren(deep) : null;
	}

	public void setThisLib(LibraryInterface library) {
		getChildren().clear();
		getChildren().add((Node) library);
	}

	/**
	 * Return true if the passed library or chain is contained in this nav node. Members of chains are tested as
	 * required.
	 */
	public boolean contains(LibraryInterface li) {
		if (getThisLib() == null)
			return false;
		if (getThisLib() == li)
			return true;
		if (li instanceof LibraryNode && getThisLib() instanceof LibraryChainNode)
			return ((LibraryChainNode) getThisLib()).contains((Node) li);
		return false;
	}

	@Override
	public boolean isNavigation() {
		return true;
	}

}
