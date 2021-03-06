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

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.properties.Images;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Navigation Nodes describe GUI model objects that are not part of the TL Model. They ease navigating the GUI and
 * <b>not</b> representing the OTM model.
 * <p>
 * NavNodes only contain objects (LibraryMembers). If the object is in a version chain, then all of the objects in that
 * chain will have a link to a single NavNode which is a child of the corresponding AggregateNode in the chain.
 * 
 * @author Dave Hollander
 * 
 */
public class NavNode extends Node {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(NavNode.class);

	private String name = "";

	/**
	 * Create a navigation node, get ns and prefix from parentNode node. link to parentNode node.
	 * 
	 * @param name
	 * @param parent
	 */
	public NavNode(final String name, final Node parent) {
		super();
		assert (parent != null) : "Parent is null.";
		setName(name);
		setLibrary(parent.getLibrary());
		// Don't break version node-component node bond.
		// if (parent instanceof VersionNode)
		// parent.getParent().linkChild(this);
		// else
		parent.linkChild(this); // link without doing family tests.
	}

	@Override
	public void linkLibrary(LibraryNode lib) {
		if (lib != null && !getChildren().contains(lib))
			getChildren().add(lib);
	}

	@Override
	public String getDecoration() {
		return "  (" + getChildren().size() + " Objects)";
	}

	@Override
	public Image getImage() {
		if (isResourceRoot())
			return Images.getImageRegistry().get(Images.Resources);
		return Images.getImageRegistry().get(Images.Folder);
	}

	@Override
	public boolean isLibraryContainer() {
		return false;
	}

	public boolean isComplexRoot() {
		return this == getLibrary().getComplexRoot() ? true : false;
	}

	public boolean isResourceRoot() {
		return getLibrary() != null ? this == getLibrary().getResourceRoot() : false;
	}

	public boolean isSimpleRoot() {
		return this == getLibrary().getSimpleRoot() ? true : false;
	}

	public boolean isServiceRoot() {
		return this == getLibrary().getServiceRoot() ? true : false;
	}

	@Override
	public boolean isNavigation() {
		return true;
	}

	@Override
	public boolean isNavChild(boolean deep) {
		return true;
	}

	@Override
	public String getComponentType() {
		return "Navigation";
	}

	/**
	 * For navigation nodes, return the default component type for the type of navigation node.
	 */
	@Override
	public ComponentNodeType getComponentNodeType() {
		if (isSimpleRoot())
			return ComponentNodeType.SIMPLE;
		else
			return ComponentNodeType.CORE;
	}

	/**
	 * Get a new list of library members in this Nav Node.
	 * 
	 * @return
	 */
	public List<LibraryMemberInterface> get_LibraryMembers() {
		List<LibraryMemberInterface> members = new ArrayList<LibraryMemberInterface>();
		for (Node n : getChildren()) {
			// if (n instanceof VersionNode && ((VersionNode) n).get() != null)
			// n = ((VersionNode) n).get();
			if (n instanceof LibraryMemberInterface)
				members.add((LibraryMemberInterface) n);
		}
		return members;
	}

	@Override
	public boolean hasChildren_TypeProviders() {
		return getChildren().size() > 0;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isDeleteable() {
		return false;
	}

	@Override
	public boolean isDeleted() {
		return deleted;
	}

	/**
	 * @return true if children array is null or empty
	 */
	public boolean isEmpty() {
		return getChildren() != null ? getChildren().isEmpty() : true;
	}

	/**
	 * @return true if this member is a child or if it has a version node that is a child.
	 */
	public boolean contains(Node member) {
		// Node thisNode = member;
		// if (member.getVersionNode() != null)
		// thisNode = member.getVersionNode();
		return getChildren().contains(member);
	}
}
