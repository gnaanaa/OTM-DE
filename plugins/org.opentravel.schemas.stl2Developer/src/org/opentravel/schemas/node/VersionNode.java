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

import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.node.listeners.ListenerFactory;
import org.opentravel.schemas.properties.Images;

/**
 * 
 * Version nodes are used in the Versions aggregate to isolate actual component nodes from their parent library. For
 * libraries that are part of a chain, all links to component nodes will be through a version node. For the non-version
 * aggregate nodes, the links are directly to the most current component node.
 * 
 * @author Dave Hollander
 * 
 */
public class VersionNode extends ComponentNode {

	protected ComponentNode head; // link to the latest/newest version of this object
	protected ComponentNode prevVersion; // link to the preceding version. If null, it is new to the
											// chain.

	/**
	 * Creates the version node and inserts into the library before the passed node. This does NOT place this node into
	 * the Aggregates. Set previous version to null (new to chain).
	 */
	public VersionNode(ComponentNode node) {
		super(node.getTLModelObject());
		if (node.getParent() == null)
			throw new IllegalStateException("Version node - " + node + " parent is null.");
		if (node.getLibrary() == null)
			throw new IllegalStateException("Version Head library is null.");

		// Fail if in the list more than once.
		assert (node.getParent().getChildren().indexOf(node) == node.getParent().getChildren().lastIndexOf(node));

		getChildren().add(node);
		head = node;
		prevVersion = null;
		node.setVersionNode(this);
		setLibrary(node.getLibrary());
		family = node.getFamily(); // needed for family processing in node.

		// Insert this between parent and node.
		setParent(node.getParent());
		node.getParent().getChildren().remove(node);
		// Fixed 1/12/15 - family safe
		// un-fixed 1/19/15 -- see VersionNode_Tests
		node.getParent().getChildren().add(this);
		// node.getParent().linkChild(this);
		node.setParent(this);

		if (getParent().getChildren().contains(node)) {
			node = node;
		}

		// Replace listener on the head node's tl Model Element
		ListenerFactory.setListner(head);

		assert (getParent() != null);
		assert (!getParent().getChildren().contains(node)) : "Parent still contains node.";
		assert (getChildren().contains(node)) : "Version node does not contain node.";
		assert (node.getParent() == this) : "Node is not linked to version node.";
	}

	/**
	 * Return the actual node wrapped by this version node.
	 * 
	 * @return node or null
	 */
	public Node getVersionedObject() {
		return getChildren().isEmpty() ? null : getChildren().get(0);
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.libraryChain);
		// return head.getImage();
	}

	// TESTING ONLY
	// @Override
	// public List<Node> getChildren() {
	// return super.getChildren();
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.Node#hasChildren_TypeProviders()
	 */
	@Override
	public boolean hasChildren_TypeProviders() {
		// Type providers are delivered from their version nodes.
		return head != null;
	}

	// NOTE - this gives the desired result.
	// Version nodes that are also the latest do not have children while clicking on one that has
	// been extended will show the base type.
	@Override
	public boolean hasNavChildrenWithProperties() {
		return false;
	}

	@Override
	public List<Node> getNavChildren() {
		// this simplifies links from validation, user experience and showing families in the other aggregates.
		return getNewestVersion().getNavChildren();
		// return Collections.emptyList();
	}

	@Override
	public boolean hasNavChildren() {
		return false;
	}

	@Override
	public boolean isTypeProvider() {
		return false;
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	/**
	 * @return the newest version of the object (version head).
	 */
	public Node getNewestVersion() {
		return head;
	}

	public void setNewestVersion(ComponentNode head) {
		this.head = head;
	}

	/**
	 * @return the newest version of the object (version head).
	 */
	public ComponentNode getPreviousVersion() {
		return prevVersion;
	}

	public void setPreviousVersion(ComponentNode previous) {
		this.prevVersion = previous;
	}
}
