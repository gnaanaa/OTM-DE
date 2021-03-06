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
package org.opentravel.schemas.modelObject;

/**
 * Class to use when there is no TL model object.
 * 
 * XsdNode, NavNode, ImpliedNode, TypeNode, ModelNode, ProjectNode SimpleTypeNode ?? LibraryNode during constructor.
 * 
 * @author Dave Hollander
 * 
 */
public class EmptyMO extends ModelObject<TLEmpty> {
	// Object tlEmpty = null;

	public EmptyMO(final TLEmpty obj) {
		super(obj);
	}

	// @Override
	// public boolean isEmpty() {
	// return true;
	// }

	// @Override
	// public String getComponentType() {
	// return "Empty Model";
	// }

	// @Override
	// public String getName() {
	// return "empty";
	// }

	// @Override
	// public String getNamePrefix() {
	// return "";
	// }

	// @Override
	// public String getNamespace() {
	// // EmptyMO is used for built in libraries. Get the ns from the library.
	// return node instanceof LibraryNode ? ((LibraryNode) node).getTLaLib().getNamespace() : "";
	// }

	@Override
	public TLEmpty getTLModelObj() {
		return srcObj;
	}

	// @Override
	// public boolean setName(final String name) {
	// return false;
	// }

	@Override
	public void delete() {
		if (srcObj == null)
			return;
		srcObj.delete();
		srcObj = null;
	}

	// @Override
	// protected AbstractLibrary getLibrary(final TLEmpty obj) {
	// return null;
	// }

}
