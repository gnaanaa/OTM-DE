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
package org.opentravel.schemas.trees.type;

import org.eclipse.jface.viewers.Viewer;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.facets.ContextualFacetNode;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide only objects that are ContextualFacetOwners to the type selection wizard.
 * 
 * @author Dave
 *
 */
public class ContextualFacetOwnersTypeFilter extends TypeSelectionFilter {
	private static final Logger LOGGER = LoggerFactory.getLogger(ContextualFacetOwnersTypeFilter.class);

	private TLFacetType type = null;

	/**
	 * Filter to select only contextual facet owners.
	 * 
	 * @param contextualFacet
	 *            facet to match. Candidate owners must return true for canOwn(node)
	 */
	public ContextualFacetOwnersTypeFilter(ContextualFacetNode contextualFacet) {
		this.type = contextualFacet.getTLModelObject().getFacetType();
	}

	/**
	 * Filter to select only contextual facet owners.
	 * 
	 * @param node
	 *            Type of facet to match. Candidate owners must return true for canOwn(type)
	 */
	public ContextualFacetOwnersTypeFilter(TLFacetType type) {
		this.type = type;
	}

	@Override
	public boolean isValidSelection(Node n) {
		if (n instanceof ContextualFacetOwnerInterface)
			if (n instanceof ContextualFacetOwnerInterface)
				return ((ContextualFacetOwnerInterface) n).canOwn(type);
		return false;
	}

	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
		if (element instanceof Node) {
			if (((Node) element).isNavigation())
				return true;
			if (element instanceof ContextualFacetOwnerInterface)
				return ((ContextualFacetOwnerInterface) element).canOwn(type);
		}
		return false;
	}
}
