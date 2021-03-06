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
package org.opentravel.schemas.actions;

import org.opentravel.schemas.controllers.RepositoryController;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;

/**
 * @author Dave Hollander
 * 
 */
// TODO - collapse the major/minor/patch into one
@Deprecated
public class VersionMajorAction extends OtmAbstractAction {
	private static StringProperties propsDefault = new ExternalizedStringProperties("action.library.version.major");

	public VersionMajorAction() {
		super(propsDefault);
	}

	public VersionMajorAction(final StringProperties props) {
		super(props);
	}

	/**
	 * run repository controller create major version
	 */
	@Override
	public void run() {
		for (Node node : mc.getSelectedNodes_NavigatorView()) {
			mc.postStatus("Major Version " + node);
			RepositoryController rc = mc.getRepositoryController();
			node = node.getLibrary();
			if (node instanceof LibraryNode)
				rc.createMajorVersion((LibraryNode) node);
		}
	}

	@Override
	public boolean isEnabled() {
		LibraryNode ln = null;
		Node n = mc.getSelectedNode_NavigatorView();
		if (n != null)
			ln = n.getLibrary();
		// Don't allow lock unless library is in a project with managing namespace
		if (!ln.isInProjectNS())
			return false;
		return ln != null && ln.isManaged();
	}
}
