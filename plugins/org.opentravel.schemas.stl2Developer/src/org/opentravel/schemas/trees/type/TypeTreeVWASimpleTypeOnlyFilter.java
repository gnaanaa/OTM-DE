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
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypeTreeVWASimpleTypeOnlyFilter extends TypeSelectionFilter {
	private static final Logger LOGGER = LoggerFactory.getLogger(Type.class);

	/**
	 * @see org.opentravel.schemas.typeTree.TypeSelectionFilter#isValidSelection(org.opentravel.schemas.node.Node)
	 */
	@Override
	public boolean isValidSelection(Node n) {
		if (n.isAssignable() && !n.isVWASimpleAssignable())
			LOGGER.debug("Error in tree filter.");
		return (n != null) && n.isAssignable() && n.isVWASimpleAssignable();
		// when isAssignable but not isVWASimpleAssignable?
	}

	/**
	 * Establish the filter to select only nodes that match the node.library.
	 */
	public TypeTreeVWASimpleTypeOnlyFilter() {
	}

	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
		if (element == null || !(element instanceof Node)) {
			return false;
		}
		final Node n = (Node) element;
		return (n.isNavigation()) ? true : n.isVWASimpleAssignable();
	}

}
