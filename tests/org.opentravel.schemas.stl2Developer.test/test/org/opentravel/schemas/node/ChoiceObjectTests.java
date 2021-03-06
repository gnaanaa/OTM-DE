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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.facets.ContextualFacetNode;
import org.opentravel.schemas.node.facets.ContributedFacetNode;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;

/**
 * @author Dave Hollander
 * 
 */
public class ChoiceObjectTests {
	ModelNode model = null;
	MockLibrary ml = new MockLibrary(); // define here to allow other classes to check objects
	LibraryNode ln = null;
	MainController mc;
	DefaultProjectController pc;
	ProjectNode defaultProject;
	TestNode tn = new NodeTesters().new TestNode();

	@Before
	public void beforeEachTest() {
		mc = new MainController();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();
	}

	@Test
	public void CH_ConstructorTests() {
		LibraryNode ln = ml.createNewLibrary("http://example.com/choice", "CT", pc.getDefaultProject());
		ChoiceObjectNode cn = ml.addChoice(ln, "ChoiceTest1");

		check(cn, true);
	}

	@Test
	public void CH_GetFacetsTests() {
		LibraryNode ln2 = ml.createNewLibrary("http://example.com/choice", "CT", pc.getDefaultProject());
		ChoiceObjectNode c1 = ml.addChoice(ln2, "Choice");

		assertTrue("Must have shared facet.", c1.getSharedFacet() != null);

		int cfCnt = c1.getChoiceFacets().size();
		c1.addFacet("cf1");
		c1.addFacet("cf2");
		assertTrue("Must have two more choice facets.", c1.getChoiceFacets().size() == cfCnt + 2);
	}

	@Test
	public void CH_FileReadTest() throws Exception {
		LibraryNode testLib = new LoadFiles().loadFile6(mc);
		LibraryNode ln2 = ml.createNewLibrary("http://example.com/choice", "CT", pc.getDefaultProject());
		ChoiceObjectNode extendedChoice = ml.addChoice(ln2, "ExtendedChoice");

		new LibraryChainNode(testLib); // Test in a chain

		for (Node choice : testLib.getDescendants_LibraryMembers()) {
			if (choice instanceof ChoiceObjectNode) {
				check((ChoiceObjectNode) choice, true);

				extendedChoice.setExtension(choice);
				check((ChoiceObjectNode) choice, true);
				check(extendedChoice, true);
			}
		}
	}

	@Test
	public void CH_ExtensionTests() {
		// Given the choice test file with 2 choice objects
		LibraryNode ln = new LoadFiles().loadFile_Choice(defaultProject);
		// new LibraryChainNode(ln); // Test in a chain

		ChoiceObjectNode choice = null;
		ChoiceObjectNode extChoice = null;
		for (Node n : ln.getDescendants_LibraryMembers())
			if (n instanceof ChoiceObjectNode) {
				if (((ChoiceObjectNode) n).getExtensionBase() == null)
					choice = (ChoiceObjectNode) n;
				else
					extChoice = (ChoiceObjectNode) n;
			}
		assertTrue("Must have base choice object.", choice != null);
		assertTrue("Choice must have 2 contextual facets.", getContextualFacets(choice).size() == 2);
		assertTrue("Must have extended choice object.", extChoice != null);
		assertTrue("Extended choice must have 2 contextual facets.", getContextualFacets(extChoice).size() == 2);
		assertTrue("Extended choice must have 2 inherited facets.", extChoice.getInheritedChildren().size() == 2);

		// Given - the choice extension should work exactly like business object.
		BusinessObjectNode bo = null;
		BusinessObjectNode exBo = null;
		for (Node n : ln.getDescendants_LibraryMembers())
			if (n instanceof BusinessObjectNode) {
				if (((BusinessObjectNode) n).getExtensionBase() == null)
					bo = (BusinessObjectNode) n;
				else
					exBo = (BusinessObjectNode) n;
			}
		assertTrue("Must have base business object.", bo != null);
		assertTrue("BO must have 2 contextual facets.", getContextualFacets(bo).size() == 2);
		assertTrue("Must have extended business object.", exBo != null);
		assertTrue("Extended BO must have 2 contextual facets.", getContextualFacets(exBo).size() == 2);
		assertTrue("Extended BO must have 2 inherited facets.", exBo.getInheritedChildren().size() == 2);
	}

	@Test
	public void ChoiceFacetsTests() {
		ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
		// new LibraryChainNode(ln); // Test in a chain

		// Given 3 choice groups
		ChoiceObjectNode ch1 = ml.addChoice(ln, "Ch1");
		int baseCount = ch1.getChoiceFacets().size();
		ChoiceObjectNode ch2 = new ChoiceObjectNode(new TLChoiceObject());
		ch2.setName("Ch2");
		ln.addMember(ch2);
		ch2.addFacet("Ch2CF1");
		ChoiceObjectNode ch3 = new ChoiceObjectNode(new TLChoiceObject());
		ch3.setName("Ch3");
		ln.addMember(ch3);

		// When extended
		ch2.setExtension(ch1);
		ch3.setExtension(ch2);

		// Then
		assertTrue("Ch1 must be extended by ch2.", ch1.getWhereExtendedHandler().getWhereExtended().contains(ch2));
		assertTrue("Ch2 must extend ch1.", ch2.getExtensionBase() == ch1);
		assertTrue("Ch2 must have 2 children.", ch2.getChildren().size() == 2);
		assertTrue("Ch2 shared facet must NOT have any children.", ch2.getSharedFacet().getChildren().isEmpty());
		assertTrue("Ch3 must extend ch2.", ch3.getExtensionBase() == ch2);
		assertTrue("Ch3 must have 1 child.", ch3.getChildren().size() == 1);
		assertTrue("Ch3 shared facet must NOT have any children.", ch3.getSharedFacet().getChildren().isEmpty());

		// Then - look for ghost facets from TL Model
		List<TLContextualFacet> inf2 = FacetCodegenUtils.findGhostFacets(ch2.getTLModelObject(), TLFacetType.CHOICE);
		List<TLContextualFacet> inf3 = FacetCodegenUtils.findGhostFacets(ch3.getTLModelObject(), TLFacetType.CHOICE);
		assertTrue("Ch2 must have 2 ghosts.", inf2.size() == 2);
		assertTrue("Ch3 must have 3 ghosts.", inf3.size() == 3);

		// When - the inherited children are initialized
		ch2.initInheritedChildren(); // not needed - inherited children are initialized on get()
		ch3.initInheritedChildren();
		// Then - children remain unchanged.
		assertTrue("Ch2 must have 2 children.", ch2.getChildren().size() == 2);
		assertTrue("Ch3 must have 1 child.", ch3.getChildren().size() == 1);
		// Then - inherited children are present.
		assertTrue("Ch2 must inherit base choice facets.", ch2.getInheritedChildren().size() == baseCount);
		assertTrue("Ch3 must inherit base and c2 choice facets.", ch3.getInheritedChildren().size() == baseCount
				+ ch2.getChoiceFacets().size());
		// Then - the inherited tree filter depends on isInherited.
		for (Node n : ch3.getInheritedChildren())
			assertTrue("Must be inherited.", n.isInherited());

		//
		// When - adding and deleting facets to base types
		//
		// Given starting inherited count.
		int ch3Count = ch3.getInheritedChildren().size();
		// When
		ContextualFacetNode ch2cf2 = ch2.addFacet("Ch2CF2");
		ch3Count++;
		// Then
		List<Node> ch3Inherited = ch3.getInheritedChildren();
		assertTrue("Ch3 must have 1 more inherited child.", ch3Inherited.size() == ch3Count);
		// NO - inherited children are new nodes.
		// assertTrue("Ch3 must have ch2cf2 as inherited child.", ch3Inherited.contains(ch2cf2));

		// When
		ContextualFacetNode ch1cf3 = ch1.addFacet("Ch1CF3");
		ch3Count++;
		// Then
		assertTrue("Ch3 must have 1 more inherited child.", ch3.getInheritedChildren().size() == ch3Count);

		// When deleted
		ch2cf2.delete();
		ch3Count--;
		assertTrue("Ch2 must not have deleted facet.", !ch2.getChoiceFacets().contains(ch2cf2));
		assertTrue("Ch3 must have 1 less inherited child.", ch3.getInheritedChildren().size() == ch3Count);

		ch1cf3.delete();
		ch3Count--;
		assertTrue("Ch3 must have 1 less inherited child.", ch3.getInheritedChildren().size() == ch3Count);

	}

	@Test
	public void CH_ImportAndCopyTests() {

		// Given - 2 versioned libraries in different namespaces
		OTM16Upgrade.otm16Enabled = true;
		LibraryChainNode srcLCN = ml.createNewManagedLibrary_Empty(defaultProject.getNSRoot() + "src", "SrcLib",
				defaultProject);
		LibraryNode srcLib = srcLCN.getHead();
		LibraryChainNode destLCN = ml.createNewManagedLibrary_Empty(defaultProject.getNSRoot() + "dest" + "/Dest",
				"DestLib", defaultProject);
		LibraryNode destLib = destLCN.getHead();
		assertTrue(destLib != srcLib);
		assertTrue(destLib.isEditable());

		// Given choice objects
		ChoiceObjectNode ch0 = ml.addChoice(srcLib, "Ch0");
		ChoiceObjectNode ch1 = ml.addChoice(srcLib, "Ch1");
		ChoiceObjectNode ch2 = ml.addChoice(srcLib, "Ch2");
		ch2.addFacet("Ch2CF3");
		ChoiceObjectNode ch3 = ml.addChoice(srcLib, "Ch3");
		ChoiceObjectNode ch4 = ml.addChoice(srcLib, "Ch4");
		ChoiceObjectNode ch5 = ml.addChoice(srcLib, "Ch5");
		ml.check(ch0);
		ml.check(srcLib); // checks all members

		// TODO - test case for just importing contextual facets

		//
		// When - cloned as used within LibraryNode.importNode()
		LibraryElement tlResult = ch0.cloneTLObj();
		ChoiceObjectNode newNode = (ChoiceObjectNode) NodeFactory.newComponent_UnTyped((LibraryMember) tlResult);
		destLib.addMember(newNode);

		// Then - there must be same number of contributed facets
		assertTrue(ch0.getChoiceFacets().size() == newNode.getChoiceFacets().size());

		// Then - result will not validate due to duplicate contextual facets
		ml.check(newNode, false);
		// Then - the contextual facets must be in the source library - importNode will move them
		for (ContextualFacetNode cf : newNode.getContextualFacets()) {
			assertTrue("Facet must NOT be in destLib.", !destLib.contains(cf));
			assertTrue("Facet must be in srcLib.", srcLib.contains(cf));
			assertTrue("Facet must NOT be in source choice.", !ch0.getContextualFacets().contains(cf));

			// Then - move facet to keep destLib valid
			cf.getLibrary().removeMember(cf);
			destLib.addMember(cf);
		}

		// Then - result will validate
		ml.check(destLib, true);

		//
		// When - LibraryNode.importNode() is run
		newNode = (ChoiceObjectNode) destLib.importNode(ch4);

		// Then - there must be same number of contributed facets
		assertTrue(ch4.getChoiceFacets().size() == newNode.getChoiceFacets().size());

		// Then - the contextual facets must be in the source library - importNode will move them
		for (ContextualFacetNode cf : newNode.getContextualFacets()) {
			assertTrue("Facet must be in destLib.", destLib.contains(cf));
			assertTrue("Facet must be in destination choice.", newNode.getContextualFacets().contains(cf));
			assertTrue("Facet must NOT be in srcLib.", !srcLib.contains(cf));
			assertTrue("Facet must NOT be in source choice.", !ch4.getContextualFacets().contains(cf));
		}
		// Then - result will validate
		ml.check(newNode, true);

		// When - imported but the source lib is not editable
		srcLib.setEditable(false);
		newNode = (ChoiceObjectNode) destLib.importNode(ch5);
		// Then - imported choice valid
		check(newNode, true);
		ml.check(srcLib);

		List<Node> nodeList = new ArrayList<Node>();

		// When - ImportObjectToLibraryAction - case 1
		nodeList.add(ch1);
		destLib.importNodes(nodeList);
		// When - ImportObjectToLibraryAction - case 2
		nodeList.clear();
		nodeList.add(ch2);
		destLib.importNodes(nodeList, true);
		// When - ImportObjectToLibraryAction - case 3
		nodeList.clear();
		nodeList.add(ch3);
		destLib.importNodes(nodeList, false);

		ml.check(destLib);
		ml.check(srcLib);

		OTM16Upgrade.otm16Enabled = false;
	}

	private List<ContextualFacetNode> getContextualFacets(Node container) {
		ArrayList<ContextualFacetNode> facets = new ArrayList<ContextualFacetNode>();
		for (Node n : container.getDescendants())
			if (n instanceof ContextualFacetNode)
				facets.add((ContextualFacetNode) n);
		return facets;
	}

	public void check(ChoiceObjectNode choice, boolean validate) {
		assertTrue(choice instanceof ChoiceObjectNode);

		// Validate model and tl object
		assertTrue(choice.getTLModelObject() instanceof TLChoiceObject);
		assertNotNull(choice.getTLModelObject().getListeners());
		TLChoiceObject tlChoice = (TLChoiceObject) choice.getTLModelObject();

		if (tlChoice.getOwningLibrary() != null)
			Assert.assertNotNull(choice.getLibrary());
		String s = tlChoice.getName();

		// must have shared facet
		assertNotNull(choice.getSharedFacet());
		s = ((FacetNode) choice.getSharedFacet()).getName();
		s = ((FacetNode) choice.getSharedFacet()).getLabel();

		// make sure this does not NPE
		List<PropertyOwnerInterface> choices = choice.getChoiceFacets();
		// can be empty - assertTrue(!choices.isEmpty());

		// For choice facets the Name and label should be not empty
		for (PropertyOwnerInterface poi : choice.getChoiceFacets()) {
			assertTrue(poi instanceof FacetNode);
			FacetNode f = (FacetNode) poi;
			String name = f.getName();
			assertFalse(name.isEmpty());
			String label = f.getLabel();
			assertFalse(f.getLabel().isEmpty());
			if (poi instanceof ContributedFacetNode)
				assertTrue(((Node) poi).getParent() == choice);
		}

		// Does this extend another choice? If so, examine inherited children
		boolean hasBaseClass = choice.getExtensionBase() != null;
		if (hasBaseClass) {
			Node baseClass = choice.getExtensionBase();
			// Test File 6 has an extended choice - make sure it inherits correctly.
			if (choice.getName().equals("ExtendedChoice")) {
				for (Node n : choice.getChildren())
					if (n instanceof FacetNode) {
						assertTrue(((Node) n).getParent() != null);

						List<TLAttribute> tlAttrs = PropertyCodegenUtils.getInheritedFacetAttributes((TLFacet) n
								.getTLModelObject());
						List<Node> inheritedList = n.getInheritedChildren();
						if (inheritedList.isEmpty()) {
							List<Node> x = n.getInheritedChildren();
						}

						// assert !inheritedList.isEmpty();
						// assert inheritedList.size() == 3;
					}
			}
			assertNotNull(baseClass);
		}

		// Check all the children
		for (Node n : choice.getChildren()) {
			ml.check(n, validate);
			assertTrue(!(n instanceof VersionNode));
			if (!OTM16Upgrade.otm16Enabled)
				assertTrue("Contributed facets are only supported in version 1.6 and later.",
						!(n instanceof ContributedFacetNode));
		}

		//
		// TODO - add test case where a new facet is added to the extension
		//

		// Get Equivalent
		// Get Aliases
		assertNotNull(choice.getAliases());

	}
}
