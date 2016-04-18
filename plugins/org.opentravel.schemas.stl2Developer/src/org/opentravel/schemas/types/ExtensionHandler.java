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
package org.opentravel.schemas.types;

import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.types.WhereExtendedHandler.WhereExtendedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles extension assignment. Extensions modify the structure of the base object. Note: ExtensionPointFacet extends
 * facets not ExtensionPointFacets.
 * 
 * @author Dave Hollander
 * 
 */
public class ExtensionHandler extends AbstractAssignmentHandler<ExtensionOwner> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionHandler.class);

	private ExtensionOwner owner = null;

	public ExtensionHandler(ExtensionOwner n) {
		super();
		owner = n;
	}

	@Override
	public Node getOwner() {
		return (Node) owner;
	}

	public Node get() {

		// Get the extension base from the TL Model Element
		NamedEntity tlObj = owner.getModelObject().getTLBase();
		return Node.GetNode((TLModelElement) tlObj);

		// From enum: works
		// From VWA : works
		// From choice works
	}

	/**
	 * Set the base type for owner node to the passed base object such that <i>this extends base</i>.
	 * 
	 * A WhereExtended listener will be added to the base before being set. The listener will add this node to the
	 * base's where extended list.
	 * 
	 * If null, remove assignment.
	 * 
	 * Note: extension point facets extend facets not themselves.
	 * 
	 * @param base
	 */
	public boolean set(Node base) {
		if (base == null)
			return remove();

		// LOGGER.debug("Start - set extension base of " + owner.getNameWithPrefix() + " to " +
		// base.getNameWithPrefix());

		// Save the old base object for after the assignment
		Node oldBase = owner.getExtensionBase();
		// If owner was extending a different base, remove the link and listener.
		if (oldBase != null) {
			oldBase.getWhereExtendedHandler().removeListener(owner);
			oldBase.getWhereExtendedHandler().remove((Node) owner);
			owner.getModelObject().setExtendsType(null); // allow ownership event to happen
		}
		// Add a where extended listener to the new base before making the assignment
		base.getWhereExtendedHandler().setListener(owner);

		// Do the assignment
		owner.getModelObject().setExtendsType(base.getModelObject());

		// update library where used
		// on initial load/type resolver old and base will be the same
		if (oldBase == base)
			if (base.getLibrary() != null)
				base.getLibrary().getWhereUsedHandler().add(owner);
		if (oldBase == null) {
			if (base.getLibrary() != null)
				base.getLibrary().getWhereUsedHandler().add(owner);
		} else if (oldBase.getLibrary() != base.getLibrary()) {
			if (base.getLibrary() != null)
				base.getLibrary().getWhereUsedHandler().add(owner);
			if (oldBase.getLibrary() != null)
				oldBase.getLibrary().getWhereUsedHandler().remove(owner);
		}

		// if (oldBase == null)
		// LOGGER.debug("END -" + owner + " changed assigment from null to " + base);
		// else
		// LOGGER.debug("END -" + owner + " changed assigment from " + oldBase.getNameWithPrefix() + " to "
		// + base.getNameWithPrefix());
		return false;
	}

	/**
	 * Remove the extension
	 * 
	 * @return
	 */
	private boolean remove() {
		// Let the model object clear it out.
		owner.getModelObject().setExtendsType(null);

		removeListener();
		return true;
	}

	public void removeListener() {
		// Remove the listener identifying the base type
		for (ModelElementListener l : ((Node) owner).getTLModelObject().getListeners())
			if (l instanceof WhereExtendedListener) {
				owner.getTLModelObject().removeListener(l);
				return;
			}
	}

	@Override
	public TLModelElement getTLModelElement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NamedEntity getTLNamedEntity() {
		// TODO Auto-generated method stub
		return null;
	}

}