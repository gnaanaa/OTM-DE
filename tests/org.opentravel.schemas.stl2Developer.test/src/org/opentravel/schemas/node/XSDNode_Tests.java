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
package org.opentravel.schemas.node;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.controllers.ProjectController;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class XSDNode_Tests {
	private final static Logger LOGGER = LoggerFactory.getLogger(XSDNode_Tests.class);

	// Lets make sure they are all unique
	// private Map<String, Node> providerMap = new HashMap<String, Node>(Node.getNodeCount());
	int dups = 0;
	int counter = 0;

	@Test
	public void checkXsdNodes() throws Exception {
		MainController mc = new MainController();
		ProjectController pc = mc.getProjectController();
		ProjectNode pn = pc.getDefaultProject();
		LoadFiles lf = new LoadFiles();

		int libCnt = 0; // from init
		int locals = 0; // locally defined nodes in the library
		for (LibraryNode ln : Node.getAllLibraries()) {
			// providerMap.clear();
			checkCounts(ln);
			if (ln.isXSDSchema()) {
				visitXsdNodes(ln);
			}
			libCnt++;
		}
		Assert.assertEquals(2, libCnt); // the default built-in libraries

		for (LibraryNode ln : Node.getAllLibraries()) {
			// providerMap.clear();
			checkCounts(ln);
			if (ln.isXSDSchema()) {
				visitXsdNodes(ln);
			}
			visitSimpleTypes(ln);
			libCnt++;
		}

	}

	private void checkCounts(LibraryNode lib) {
		int simpleCnt = 0;
		for (Node type : lib.getDescendants_LibraryMembers()) {
			if (type.isSimpleType()) {
				simpleCnt++;
			}
		}
		String libName = lib.getName();
		int libCnt = getDescendentsSimpleComponents(lib).size();
		Assert.assertEquals(simpleCnt, getDescendentsSimpleComponents(lib).size());
	}

	private void visitXsdNodes(INode node) {
		for (Node n : node.getChildren()) {
			if (n.isNavigation()) {
				visitXsdNodes(n);
			} else {
				checkName(n);
				if (n instanceof XsdNode) {
					visitXsdNode((XsdNode) n);
				} else {
					if (!n.isXsdType())
						Assert.assertFalse(n.isXsdType());
				}
			}
		}
	}

	private List<SimpleComponentNode> getDescendentsSimpleComponents(LibraryNode ln) {
		List<SimpleComponentNode> kids = new ArrayList<SimpleComponentNode>();
		for (Node n : ln.getSimpleRoot().getChildren())
			if (n instanceof SimpleComponentNode)
				kids.add((SimpleComponentNode) n);
		return kids;
	}

	private void checkName(Node n) {
		if (!(n.isNamedEntity()))
			return;

		// if (providerMap.put(n.getName(), n) != null)
		// dups++;
		// THERE is a bug in family processing that leaves two duplicates.
		// if (peerCount+1 != providerMap.size()) {
		// dups++;
		// }
		// Assert.assertEquals(0, dups);

	}

	private void visitXsdNode(XsdNode xn) {
		Assert.assertTrue(xn.isXsdType());
		Assert.assertTrue(xn.hasOtmModelChild());
		counter++;
	}

	private void visitSimpleTypes(LibraryNode ln) {
		for (SimpleComponentNode st : getDescendentsSimpleComponents(ln)) {
			Assert.assertNotNull(st.getLibrary());
			Assert.assertNotNull(st.getBaseType());

			// Check names
			Assert.assertFalse(st.getName().isEmpty());

			// Type Names
			String an = st.getTypeName();
			if (an.isEmpty())
				an = "Empty";
			// st.getAssignedType().getName();
			// String tn = st.getTypeClass().getTypeNode().getName();
			// if (!(st.getTypeClass().getTypeNode() instanceof ImpliedNode))
			// Assert.assertEquals(tn, an);
			// Assert.assertFalse(an.isEmpty());
			// // Check type namespace
			// String anp = st.getAssignedPrefix();
			// String tnp = st.getTypeClass().getTypeNode().getNamePrefix();
			// if (!(st.getTypeClass().getTypeNode() instanceof ImpliedNode))
			// Assert.assertEquals(tnp, anp);
			// Prefixes can be empty
		}
	}

}
