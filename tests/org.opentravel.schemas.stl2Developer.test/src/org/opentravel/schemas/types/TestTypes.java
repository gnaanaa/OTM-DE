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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.codegen.example.ExampleBuilder;
import org.opentravel.schemacompiler.codegen.example.ExampleDocumentBuilder;
import org.opentravel.schemacompiler.codegen.example.ExampleGeneratorOptions;
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemas.modelObject.ListFacetMO;
import org.opentravel.schemas.node.AliasNode;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.LibraryTests;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.Node.NodeVisitor;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.SimpleComponentNode;
import org.opentravel.schemas.node.SimpleTypeNode;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.NodeIdentityListener;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.ElementReferenceNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.types.WhereAssignedHandler.WhereAssignedListener;
import org.opentravel.schemas.utils.BaseProjectTest;
import org.opentravel.schemas.utils.ComponentNodeBuilder;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * @author Dave Hollander
 * 
 */
public class TestTypes extends BaseProjectTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestTypes.class);

	NodeTesters nt = new NodeTesters();
	LoadFiles lf = new LoadFiles();
	LibraryTests lt = new LibraryTests();
	MockLibrary ml = null;
	LibraryNode ln = null;
	ProjectNode defaultProject;
	int nodeCount;

	// From baseProjecTest
	// rc, mc, pc, testProject
	// MainController mc;
	// DefaultProjectController pc;

	@Before
	public void beforeAllTests() {
		LOGGER.debug("Initializing Test Setup.");
		// mc = new MainController();
		ml = new MockLibrary();
		// pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();
	}

	/**
	 * Test where used handler
	 */
	@Test
	public void whereUsedTests() {
		ln = ml.createNewLibrary("http://opentravel.org/test", "test", defaultProject);
		LibraryChainNode lcn = ml.createNewManagedLibrary("inChain", defaultProject);
		ln.setEditable(true);

		TypeProvider unAssigned = ModelNode.getUnassignedNode();
		int wuc = unAssigned.getWhereUsedAndDescendantsCount();

		VWA_Node vwa = new VWA_Node(new TLValueWithAttributes());
		vwa.setName("Vwa_Provider1");
		assert vwa.getWhereUsedAndDescendantsCount() == 0;

		BusinessObjectNode bo = new BusinessObjectNode(new TLBusinessObject());
		ElementNode e1 = new ElementNode(bo.getFacet_Summary(), "E1");
		wuc = unAssigned.getWhereUsedAndDescendantsCount();
		int listeners = e1.getTLModelObject().getListeners().size();
		e1.setAssignedType(vwa);
		assert vwa.getWhereUsedAndDescendantsCount() == 1;
		assert listeners == e1.getTLModelObject().getListeners().size(); // should be equal

		ElementNode e2 = new ElementNode(bo.getFacet_Summary(), "E2");
		e2.setAssignedType(vwa);
		assert vwa.getWhereUsedAndDescendantsCount() == 2;
		int afterWuc = unAssigned.getWhereUsedAndDescendantsCount();
		// wuc inlcudes e1, after should not inlcude either e1 or e2
		assert wuc - 1 == afterWuc; // make sure the 2 new properties were removed from unused count

		// Make sure the listeners are added and removed
		assert listeners == e1.getTLModelObject().getListeners().size();
		e2.setAssignedType(); // listener replaced with Missing listener
		e1.setAssignedType();
		assert listeners == e1.getTLModelObject().getListeners().size();
		assert afterWuc + 2 == unAssigned.getWhereUsedAndDescendantsCount(); // two new unassigned types

		// run test against simple types and complex types of all property types
		TypeProvider aType = (TypeProvider) NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		TypeProvider bType = (TypeProvider) NodeFinders.findNodeByName("int", ModelNode.XSD_NAMESPACE);
		int aTypeCount = aType.getWhereUsedAndDescendantsCount();
		checkListeners(aType);

		AttributeNode a1 = new AttributeNode(bo.getFacet_Summary(), "a1");
		a1.setAssignedType(aType);
		Assert.assertEquals(aType, a1.getAssignedType());
		Assert.assertEquals(aTypeCount + 1, aType.getWhereUsedAndDescendantsCount());
		checkListeners(aType);

		// Setting simple types
		vwa.getFacet_Simple().getSimpleAttribute().setAssignedType(bType);
		TypeProvider b = vwa.getFacet_Simple().getSimpleAttribute().getAssignedType();
		Assert.assertEquals("VWA Assignment via simple", bType, b);
		checkListeners(bType);
		assert bType.getWhereAssigned().contains(vwa.getFacet_Simple().getSimpleAttribute());

		vwa.setSimpleType(aType);
		TypeProvider a = vwa.getSimpleType();
		Assert.assertEquals("VWA Assignment via simple", aType, a);
		checkListeners(aType);
		assert aType.getWhereAssigned().contains(vwa.getSimpleAttribute());

		CoreObjectNode core = new CoreObjectNode(new TLCoreObject());
		core.setName("Core1");
		core.setSimpleType(aType);
		assert core.getSimpleType() == aType;
		checkListeners(aType);
		assert aType.getWhereAssigned().contains(core.getSimpleAttribute());

	}

	public void checkListeners(TypeProvider provider) {
		Collection<ModelElementListener> listeners = provider.getTLModelObject().getListeners();

		// There should be only one named type listener
		int identity = 0;
		for (ModelElementListener l : listeners)
			if (l instanceof NodeIdentityListener)
				identity++;
		Assert.assertEquals(1, identity);

		// Go to all the where used nodes and make sure there is one and only one where used listener for this provider
		for (TypeUser user : provider.getWhereAssigned()) {
			int myListeners = 0;
			for (ModelElementListener l : user.getTLModelObject().getListeners())
				if (l instanceof WhereAssignedListener)
					if (((WhereAssignedListener) l).getNode() == provider)
						myListeners++;
			Assert.assertEquals(user + " where used listeners error:", 1, myListeners);
		}

		// // If it is a type user, there should be only one where used
		// if (provider instanceof TypeUser) {
		// int whereUsed = 0;
		// for (ModelElementListener l : listeners)
		// if (l instanceof WhereAssignedListener)
		// whereUsed++;
		// Assert.assertEquals(1, whereUsed);
		// }
	}

	public int getIdentityListenerCount(Node n) {
		int i = 0;
		for (ModelElementListener listener : n.getTLModelObject().getListeners())
			if (listener instanceof NodeIdentityListener)
				i++;
		return i;
	}

	public int getWhereUsedListenerCount(Node n) {
		int i = 0;
		for (ModelElementListener listener : n.getTLModelObject().getListeners())
			if (listener instanceof WhereAssignedListener)
				i++;
		return i;
	}

	/**
	 * This test checks some identity listener counts and uses type assignment for test since type assignment depends on
	 * listeners to work properly.
	 * 
	 * Only uses mock libraries
	 */
	@Test
	public void listenerTest() {
		// Given - 2 libraries - one not in a chain and an editable library chain
		ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);
		LibraryNode ln_inChain = ml.createNewLibrary("http://www.test.com/test1c", "test1c", defaultProject);
		LibraryChainNode lcn = new LibraryChainNode(ln_inChain);
		ln_inChain.setEditable(true);

		// Given - types from the xsd namespace
		TypeProvider type1 = (TypeProvider) NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE);
		assertNotNull(type1);
		TLModelElement tlType1 = type1.getTLModelObject();
		assertNotNull(tlType1);
		assertTrue(tlType1 instanceof TLAttributeType);
		TypeProvider type2 = (TypeProvider) NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		Node type3 = NodeFinders.findNodeByName("decimal", ModelNode.XSD_NAMESPACE);
		assertNotNull(type2);
		assertNotNull(type3);

		// Given - simple types in library not in a chain
		SimpleTypeNode s1 = ml.addSimpleTypeToLibrary(ln, "s1");
		assertNotNull(s1);
		SimpleTypeNode s2 = ml.addSimpleTypeToLibrary(ln, "s2");
		assertNotNull(s2);
		// Given - simple type in library chain
		SimpleTypeNode v2 = ml.addSimpleTypeToLibrary(ln_inChain, "s2");
		assertNotNull(v2);
		// Then - make sure the have one and only one identity listener
		assertEquals(1, getIdentityListenerCount(s2));
		assertEquals(1, getIdentityListenerCount(s1));
		assertEquals(1, getIdentityListenerCount(v2));

		// When - type assigned via tlModel to simple Not in a chain
		TLModelElement tlS1 = s1.getTLModelObject();
		if (tlS1 instanceof TLSimple)
			((TLSimple) tlS1).setParentType((TLAttributeType) tlType1);
		// Then - should be changed to string by listener
		assertEquals(type1, s1.getAssignedType());

		// When - type assigned
		s2.setAssignedType(type1);
		// Then - assignment worked
		assertEquals(type1, s2.getAssignedType());

		// When - type assigned
		v2.setAssignedType(type1);
		// Then - assignment worked
		assertEquals(type1, v2.getAssignedType());

		// Given - a VWA to assign types to
		VWA_Node vwa = ml.addVWA_ToLibrary(ln, "Vwa1");
		// When - Simple facet assigned type
		vwa.getFacet_Simple().getSimpleAttribute().setAssignedType(type1);
		// Then - assignment worked
		TypeProvider at = vwa.getFacet_Simple().getSimpleAttribute().getAssignedType();
		assertEquals(type1, at);
		// When - simple facet assigned a different type
		vwa.getFacet_Simple().getSimpleAttribute().setAssignedType(type2);
		// Then - assignment worked
		assertEquals(type2, vwa.getFacet_Simple().getSimpleAttribute().getAssignedType());
		assertEquals(1, getIdentityListenerCount(vwa));
		assertEquals(1, getIdentityListenerCount(vwa.getFacet_Simple()));
		// Given - an attribute of the vwa
		AttributeNode attr = (AttributeNode) vwa.getAttributeFacet().getChildren().get(0);
		assertNotNull(attr);
		assertEquals(1, getIdentityListenerCount(attr));
		// When - attribute assigned a type
		attr.setAssignedType(s2);
		// Then - assignment worked
		assertEquals(s2, attr.getAssignedType());

		// Given - a core object
		CoreObjectNode core = ml.addCoreObjectToLibrary(ln, "Core1");
		assertNotNull(core);
		assertEquals(1, getIdentityListenerCount(core));
		// When - type assigned to simple facet of a core object
		core.getFacet_Simple().getSimpleAttribute().setAssignedType(s1);
		// Then - assignment worked
		assertEquals(s1, core.getSimpleType());
		core.setSimpleType(s2);
		// Then - assignment worked
		assertEquals(s2, core.getSimpleType());
		assertEquals(1, getIdentityListenerCount(core.getFacet_Simple()));
		// When - type assigned to a property of the core summary facet
		PropertyNode p1 = (PropertyNode) core.getFacet_Summary().getChildren().get(0);
		p1.setAssignedType(s1);
		// Then - assignment worked
		assertEquals(s1, p1.getAssignedType());
		p1.setAssignedType(v2); // a versioned node
		// Then - assignment worked
		assertEquals(v2, p1.getAssignedType());

		// Test with both property and type in versioned libraries.
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln_inChain, "Bo1");
		p1 = null;
		for (Node t : bo.getFacet_Summary().getChildren())
			if (t instanceof ElementNode) {
				p1 = (PropertyNode) t;
			}
		assertTrue("Element property was found.", p1 != null);
		p1.setAssignedType(s1); // a simple type
		assertEquals(s1, p1.getAssignedType());
		p1.setAssignedType(v2); // a versioned node
		assertEquals(v2, p1.getAssignedType()); // a simple type

		// Test with aliases as types
		AliasNode a1 = new AliasNode(bo, "A4bo1");
		p1.setAssignedType(a1);
		assertEquals(a1, p1.getAssignedType());

		ElementReferenceNode newProp = new ElementReferenceNode(bo.getFacet_Summary(), "TestSum");
		newProp.setAssignedType(bo);

		// TODO - add Tests known bad assignments
		// assertFalse(attr.setAssignedType(bo));
		// FIXME - assertFalse(vwa.setAssignedType(bo));
		// FIXME - assertFalse(core.setAssignedType(bo));
	}

	@Test
	public void checkTypes() throws Exception {
		// String NS = "http://opentravel.org/local/dave";
		// defaultProject.setNamespace(NS);

		// lf.loadFile2(mc);
		// lf.loadFile4(mc);

		ln = lf.loadFile5(mc);
		lf.loadFile1(mc);

		for (Node n : Node.getAllUserLibraries()) {
			visitAllNodes(n);
		}
		testSettingType();

		// Assert.assertEquals(17, testSimples(ln));

		lf.loadFile3(mc);
		lf.loadFile4(mc);
		lf.loadFile2(mc);

		for (Node n : Node.getAllLibraries()) {
			visitAllNodes(n);
		}
		LOGGER.debug("End checkTypes test.");
	}

	@Test
	public void testsetAssignedTypeForThisNode() {
		String ns = "http://example.com/test";
		boolean ret = false;
		Node prop;
		LOGGER.debug("TEST being run -- testsetAssignedTypeForThisNode");

		ln = ml.createNewLibrary(ns, "LIB", pc.getDefaultProject());
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "BO");

		// FIXME
		//
		// ret = bo.getTypeClass().setAssignedTypeForThisNode(bo);
		// Assert.assertFalse(ret); // should fail since BO is not type user.
		//
		// // Test using the finders.
		// prop = bo.getIDFacet().getChildren().get(0);
		// // ret = prop.getTypeClass().setAssignedTypeForThisNode();
		// // Assert.assertTrue(ret);
		//
		// // Test using TypeResolver map.
		// TypeResolver tr = new TypeResolver();
		// prop = bo.getSummaryFacet().getChildren().get(0);
		// Assert.assertNotNull(prop);
		// ret = prop.getTypeClass().setAssignedTypeForThisNode(prop, tr.getProviderMap());
		// Assert.assertTrue(ret);
		//
		// String simpleName = "simple";
		// prop = ml.addSimpleTypeToLibrary(ln, simpleName);
		// ret = prop.getTypeClass().setAssignedTypeForThisNode(prop, tr.getProviderMap());
		// Assert.assertTrue(ret);
		//
		// // Make sure GUI can access types
		// Assert.assertFalse(prop.getTypeName().equals(simpleName)); // properties view
		// Assert.assertTrue(prop.getTypeName().equals("int")); // properties view
		// Assert.assertFalse(prop.getTypeNameWithPrefix().isEmpty()); // facet view
		// Assert.assertFalse(((TypeUser) prop).getAssignedType() instanceof ImpliedNode);

	}

	// FIXME
	// @Test
	public void typeResolver_ImportUseCase() throws LibrarySaveException {
		// Import clones TL objects into a new library then runs the resolver.

		// Given - a library with 2 cores with types and extensions assigned
		LibraryNode moveFrom = LibraryNodeBuilder.create("MoveFrom", defaultProject.getNamespace() + "/Test/One", "o1",
				new Version(1, 0, 0)).build(defaultProject, pc);
		TypeProvider type1 = (TypeProvider) NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE);
		assertNotNull(type1);
		SimpleTypeNode simple = ComponentNodeBuilder.createSimpleObject("simple").assignType((Node) type1)
				.get(moveFrom);
		CoreObjectNode coBase = ComponentNodeBuilder.createCoreObject("COBase").get(moveFrom);
		CoreObjectNode coExt = ComponentNodeBuilder.createCoreObject("COExt").extend(coBase).get(moveFrom);
		assertTrue(coExt.isInstanceOf(coBase));
		coBase.setSimpleType(simple);
		ElementNode e1 = new ElementNode(coBase.getFacet_Summary(), "E1", simple);
		ElementNode e2 = new ElementNode(coExt.getFacet_Summary(), "E2", simple);
		assertTrue(simple.getWhereAssignedCount() == 3);
		assertTrue("Core object must be extension owner.", coExt instanceof ExtensionOwner);

		// When - visited as done in type resolver
		nodeCount = 0;
		moveFrom.visitAllTypeUsers(new CountVisits());
		int typeUsers = nodeCount;
		nodeCount = 0;
		moveFrom.visitAllExtensionOwners(new CountVisits());
		int baseUsers = nodeCount;
		// Then - check visit counts
		assertTrue("Must have visited 5 type users.", typeUsers == 5);
		assertTrue("Must have visited 2 extension owners.", baseUsers == 2);

		LibraryNode moveTo = LibraryNodeBuilder.create("MoveTo", defaultProject.getNamespace() + "/Test/TO", "to",
				new Version(1, 0, 0)).build(defaultProject, pc);

		// When
		// each member is imported to moveTo library (cloned and library assigned, not typed)
		for (Node n : moveFrom.getDescendants_LibraryMembers())
			moveTo.importNode(n);
		assertTrue("moveTo must have 3 members.", moveTo.getDescendants_LibraryMembers().size() == 3);
		// and Type resolver run
		new TypeResolver().resolveTypes(moveTo);

		// Find the imported objects
		CoreObjectNode newBase = null, newExt = null;
		SimpleTypeNode newSimple = null;
		for (Node n : moveTo.getDescendants_LibraryMembers())
			if (n.getName().equals("COBase"))
				newBase = (CoreObjectNode) n;
			else if (n.getName().equals("COExt"))
				newExt = (CoreObjectNode) n;
			else
				newSimple = (SimpleTypeNode) n;

		// then
		assertTrue("newBase must have been found.", newBase != null);
		assertTrue("Simple must have base type.", type1.getWhereAssigned().contains(newSimple));
		assertTrue("Simple must be used by original and new objects", simple.getWhereAssignedCount() == 6);
		assertTrue("coBase must have orginal extension.",
				coBase.getWhereExtendedHandler().getWhereExtended().contains(coExt));
		assertTrue("newExt must have extension base for visitor to work.",
				((ExtensionOwner) newExt).getExtensionBase() != null);
		assertTrue("coBase must be extended.", coBase.getWhereExtendedHandler().getWhereExtended().contains(newExt));

		assertTrue("coExt must still be instanceof coBase.", coExt.isInstanceOf(coBase));
		assertTrue("newExt must be instanceof coBase.", newExt.isInstanceOf(coBase));
	}

	private class CountVisits implements NodeVisitor {
		@Override
		public void visit(INode in) {
			nodeCount++;
		}
	}

	/**
	 * Examine all objects under SimpleRoot and return count.
	 * 
	 * @param ln
	 * @return
	 */
	public int testSimples(LibraryNode ln) {
		int simpleCnt = 0;
		for (Node sn : ln.getSimpleRoot().getChildren()) {
			assertTrue("Must be simple type.", sn instanceof SimpleComponentNode);
			assertTrue("Must be a type user.", sn instanceof TypeUser);
			TypeProvider at = ((TypeUser) sn).getAssignedType();
			assertTrue("Must have assigned type.", ((TypeUser) sn).getAssignedType() != null);
			// assertTrue("Must not be assigned implied type.",
			// !(((TypeUser) sn).getAssignedType() instanceof ImpliedNode));
			simpleCnt++;
		}
		return simpleCnt;
	}

	private void testSettingType() {
		// 1/15/2017 dmh - find something more reliable. test libraries have changed and this
		// supposed to test setting types not test files and methods.
		//

		// final String testNS = "http://www.sabre.com/ns/OTA2/Demo/Profile/v01";
		// final String testNS = "http://opentravel.org/local/dave/t4";
		// final String testNS = "http://www.opentravel.org/Sandbox/junits/v1";
		// TypeProvider typeToAssign = (TypeProvider) NodeFinders.findNodeByName("String_Long", testNS);
		// Assert.assertNotNull(typeToAssign);
		//
		// // EmploymentZZZ is a core object with a simple type, 8 summary, 1 detail property, 2 roles and an alias
		// // Repeat for a business object (profile) and VWA (location)
		// testAssignmentToObjectProperties(getNode("EmploymentZZZ", testNS), typeToAssign);
		// testAssignmentToObjectProperties(getNode("Profile", testNS), typeToAssign);
		// testAssignmentToObjectProperties(getNode("Location", testNS), typeToAssign);
	}

	// private Node getNode(String name, String testNS) {
	// Node tn = NodeFinders.findNodeByName(name, testNS);
	// Assert.assertNotNull(tn);
	// tn.getLibrary().setEditable(true);
	// return tn;
	// }

	/**
	 * Test assigning typeToAssign to all properties in all facets of the object.
	 */
	public void testAssignmentToObjectProperties(Node object, TypeProvider typeToAssign) {
		for (INode facet : object.getChildren()) {
			if (facet instanceof FacetNode)
				testFacetAssignments((FacetNode) facet, typeToAssign);
		}
	}

	/**
	 * Assign typeToAssign to all of the facet's children and assure that the assignment worked.
	 */
	public void testFacetAssignments(FacetNode facet, TypeProvider typeToAssign) {
		int usrCnt = typeToAssign.getWhereAssignedCount();
		for (Node property : facet.getChildren()) {

			// Given TypeUser property that does not have fixed type assignment.
			if (property instanceof TypeUser && ((TypeUser) property).getRequiredType() == null) {

				// Assure set worked (returned true)
				Assert.assertTrue(((TypeUser) property).setAssignedType((TypeProvider) typeToAssign));

				// Then assure type node and tl elements are set, where used set and counts adjusted.
				Assert.assertEquals(typeToAssign, ((TypeUser) property).getAssignedType());
				Assert.assertEquals(typeToAssign.getTLModelObject(), ((TypeUser) property).getAssignedTLObject());
				Assert.assertTrue(typeToAssign.getWhereAssigned().contains(property));
				Assert.assertEquals("User count after assignment.", typeToAssign.getWhereAssignedCount(), ++usrCnt);
			}
		}
	}

	/**
	 * Test the type providers and assure where used and owner. Test type users and assure getType returns valid node.
	 * 
	 * @param n
	 */
	public void visitAllNodes(Node n) {
		visitTypeNode(n);
		for (Node c : n.getChildren())
			visitAllNodes(c);
	}

	public void GenExampleAndValidate(Node cn) {
		ValidationFindings findings = null;
		final ExampleBuilder<Document> exampleBuilder = new ExampleDocumentBuilder(new ExampleGeneratorOptions());
		String xml = "";
		int errorCount = 0;

		// Make sure we can create findings.
		findings = cn.validate();
		errorCount = findings.getFindingsAsList(FindingType.ERROR).size();
		// LOGGER.debug("Validation Error count for " + n + " = " +
		// errorCount);

		// Make sure we can create examples for types without errors.
		xml = cn.compileExampleXML(errorCount > 0);
		if (errorCount < 1)
			Assert.assertFalse(xml.endsWith("ERROR"));
		else
			Assert.assertTrue(xml.endsWith("ERROR"));

	}

	/**
	 * JUnit test to validate the contents of the Type assignments within the node. TypeProvider and TypeUser specific
	 * <b>assertions</b> are made. Generates Examples and runs compiler validation. <b>Very cpu intensive!</b>
	 * 
	 * {@link NodeTesters#visit(Node)} node based test.
	 * 
	 * @param n
	 */
	public void visitTypeNode(Node n) {
		ValidationFindings findings = null;
		final ExampleBuilder<Document> exampleBuilder = new ExampleDocumentBuilder(new ExampleGeneratorOptions());
		String xml = "";
		int errorCount = 0;
		if (n == null)
			return;

		if (n.getParent() == null)
			n.getParent();
		Assert.assertNotNull(n.getParent());
		if (!n.isLibraryContainer()) {
			if (n.getLibrary() == null)
				LOGGER.debug("Null library in " + n);
			Assert.assertNotNull(n.getLibrary());
		}
		Assert.assertNotNull(n.getComponentType());
		Assert.assertFalse(n.getComponentType().isEmpty());

		if (n instanceof TypeProvider)
			assert ((TypeProvider) n).getWhereAssigned() != null;

		if (n instanceof TypeUser) {
			ComponentNode cn = (ComponentNode) n;
			if (cn.getType() == null)
				LOGGER.debug("FIXME - Null type: " + cn);
			else {
				// Why do you get null types? Maybe the library is not editable.
				// LOGGER.debug("Testing " + cn.getLibrary() + "-" + cn + " of type \t"
				// + cn.getType().getClass().getSimpleName() + ":" + cn.getType());
				TypeProvider type = ((TypeUser) cn).getAssignedType();
				Assert.assertNotNull(type);
				Assert.assertFalse(type.getName().isEmpty());
				if (((Node) type).getNamespace().isEmpty())
					LOGGER.warn("Namespace is empty for " + type + " assigned to " + cn);
				if (!(((Node) type).getModelObject() instanceof ListFacetMO))
					Assert.assertFalse(cn.getType().getNamespace().isEmpty());
			}
		}
	}

}
