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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.types.TestTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test all the is* tests in Node
 * 
 * @author Dave Hollander
 * 
 */
public class Node_IsTests {
	static final Logger LOGGER = LoggerFactory.getLogger(Node_IsTests.class);

	ModelNode model = null;
	TestTypes tt = new TestTypes();

	NodeTesters nt = new NodeTesters();
	LoadFiles lf = new LoadFiles();
	LibraryTests lt = new LibraryTests();
	MockLibrary ml = null;
	LibraryNode ln = null;
	MainController mc;
	DefaultProjectController pc;
	ProjectNode defaultProject;

	@Before
	public void beforeAllTests() {
		mc = new MainController();
		ml = new MockLibrary();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();

		ln = ml.createNewLibrary("http://example.com", "isTests", defaultProject);
		new LibraryChainNode(ln); // test in a chain
		ln.setEditable(true);
		assertTrue(ln.isEditable());
	}

	@Test
	public void isInServiceTest() {
		// Given - non service nodes
		ml.addOneOfEach(ln, "svcTest");
		// Then - all must be false
		for (Node n : ln.getDescendants()) {
			assertFalse(n.isInService());
			assertFalse(n.isEditable_inService());
		}

		// Given - service nodes
		BusinessObjectNode bo = null;
		for (Node n : ln.getDescendants_LibraryMembers())
			if (n instanceof BusinessObjectNode)
				bo = (BusinessObjectNode) n;
		ServiceNode sn = new ServiceNode(bo);

		// Then - all must be true
		assertTrue(sn.isInService());
		for (Node n : sn.getDescendants()) {
			Node owner = n.getOwningComponent();
			assertTrue(n.isInService());
			assertTrue(n.isEditable_inService());
		}
	}

	@Test
	public void isInstanceOfTests() {
		// Given
		// Then
	}
}
