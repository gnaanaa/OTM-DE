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

public class TypeTreeSimpleTypeOnlyFilter extends TypeSelectionFilter {

    /**
     * @see org.opentravel.schemas.trees.type.TypeSelectionFilter#isValidSelection(org.opentravel.schemas.node.Node)
     */
    @Override
    public boolean isValidSelection(Node n) {
        return (n != null) && n.isAssignable() && n.isSimpleAssignable();
    }

    /**
     * Establish the filter to select only nodes that match the node.library.
     */
    public TypeTreeSimpleTypeOnlyFilter() {
    }

    @Override
    public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
        if (element == null || !(element instanceof Node)) {
            return false;
        }
        final Node n = (Node) element;
        // System.out.println("TTSimpleTypeOnlyFilter:select() - is "+n.getName()+" Simple? "+n.isSimpleAssignable());
        return (n.isNavigation()) ? true : n.isSimpleAssignable();
    }
}
