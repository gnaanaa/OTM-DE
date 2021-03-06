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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.resources.ActionNode;
import org.opentravel.schemas.node.resources.ParentRef;
import org.opentravel.schemas.node.resources.ResourceBuilder;
import org.opentravel.schemas.node.resources.ResourceNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class ResourceObjectTests {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceObjectTests.class);

	ModelNode model = null;
	MockLibrary ml = null;
	LibraryNode ln = null;
	MainController mc;
	DefaultProjectController pc;
	ProjectNode defaultProject;
	TestNode tn = new NodeTesters().new TestNode();

	@Before
	public void beforeEachTest() {
		mc = new MainController();
		ml = new MockLibrary();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();
	}

	@Test
	public void constructorTests() {
		// Given - a library and the objects used in constructors
		LibraryNode ln = ml.createNewLibrary("http://example.com/resource", "RT", pc.getDefaultProject());
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "MyBo");
		Node node = bo;
		TLResource mbr = new TLResource();
		mbr.setName("MyTlResource");
		mbr.setBusinessObjectRef(bo.getTLModelObject());

		// When - used in LibraryNode.generateLibrary()
		TLResource tlr = new ResourceBuilder().buildTL(); // get a populated tl resource
		tlr.setBusinessObjectRef(bo.getTLModelObject());
		ResourceNode rn1 = new ResourceNode(tlr, ln);

		// When - used in tests
		ResourceNode rn2 = ml.addResource(bo);

		// When - used in NodeFactory
		ResourceNode rn3 = new ResourceNode(mbr);
		ln.addMember(rn3);

		// When - used in ResourceCommandHandler to launch wizard
		ResourceNode rn4 = new ResourceNode(node.getLibrary(), bo);
		// When - builder used as in ResourceCommandHandler
		new ResourceBuilder().build(rn4, bo);

		// Then - must be complete
		check(rn1);
		check(rn2);
		check(rn3);
		check(rn4);
	}

	@Test
	public void fileReadTest() throws Exception {
		LibraryNode testLib = new LoadFiles().loadFile6(mc);
		new LibraryChainNode(testLib); // Test in a chain

		for (Node n : testLib.getDescendants_LibraryMembers()) {
			if (n instanceof ResourceNode)
				check((ResourceNode) n);
		}
	}

	@Test
	public void deleteResourceTest() {
		// Given - a library and resource
		LibraryNode ln = ml.createNewLibrary("http://example.com/resource", "RT", pc.getDefaultProject());
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "MyBo");
		ResourceNode rn = ml.addResource(bo);
		check(rn);

		// Given
		Collection<TypeUser> l1 = bo.getWhereAssigned();
		Collection<TypeUser> l2 = bo.getWhereUsedAndDescendants();
		assertTrue("Resource must be in subject's where assigned list.", bo.getWhereAssigned().contains(rn));
		assertTrue("Resource must have a subject.", rn.getSubject() == bo);
		assertTrue("Resource must be in subject's where-used list.", bo.getWhereUsedAndDescendants().contains(rn));

		// When - the resource is deleted
		rn.delete();

		// Then
		assertTrue("Resource must be deleted.", rn.isDeleted());
		assertTrue("Resource must NOT be in subject's where-used list.", !bo.getWhereUsedAndDescendants().contains(rn));
	}

	@Test
	public void resource_CopyTests() {

		// Given - a destination library in a different namespace from the source
		LibraryNode destLib = ml.createNewLibrary_Empty("http://opentravel.org/Test/tx", "TL2", pc.getDefaultProject());
		// Given - a valid resource using mock library provided business object
		LibraryNode srcLib = ml.createNewLibrary(pc, "ResourceTestLib");
		BusinessObjectNode bo = null;
		for (Node n : srcLib.getDescendants_LibraryMembers())
			if (n instanceof BusinessObjectNode) {
				bo = (BusinessObjectNode) n;
				break;
			}
		ResourceNode resource = ml.addResource(bo);
		assertTrue("Resource created must not be null.", resource != null);
		ml.check(resource);
		ml.check(srcLib);

		// When - copied to destination library
		destLib.copyMember(resource);
		// Then - source lib is not changed
		assertTrue(srcLib.contains(resource));
		ml.check(srcLib);
		ml.check(resource);
		// Then - it is copied and is valid
		ResourceNode newResource = null;
		for (Node r : destLib.getDescendants_LibraryMembers())
			if (r.getName().equals(resource.getName()))
				newResource = (ResourceNode) r;
		assertTrue(destLib.contains(newResource));
		BusinessObjectNode subject = newResource.getSubject();
		ml.check(newResource);
		ml.check(destLib);
	}

	@Test
	public void resource_MoveTests() {

		// Given - a valid resource using mock library provided business object
		LibraryNode srcLib = ml.createNewLibrary(pc, "ResourceTestLib");
		LibraryNode destLib = ml.createNewLibrary(pc, "ResourceTestLib2");
		BusinessObjectNode bo = null;
		for (Node n : srcLib.getDescendants_LibraryMembers())
			if (n instanceof BusinessObjectNode) {
				bo = (BusinessObjectNode) n;
				break;
			}
		ResourceNode resource = ml.addResource(bo);
		assertTrue("Resource created must not be null.", resource != null);
		ml.check(resource);
		ml.check(srcLib);

		// When - moved to destination library
		srcLib.moveMember(resource, destLib);
		// Then - it is moved and is valid
		assertTrue(!srcLib.contains(resource));
		assertTrue(destLib.contains(resource));
		ml.check(resource);
		ml.check(srcLib);
		ml.check(destLib);
	}

	@Test
	public void actionExample_Tests() {

		// Given - a valid resource using mock library provided business object
		LibraryNode ln = ml.createNewLibrary(pc, "ResourceTestLib");
		BusinessObjectNode bo = null;
		for (Node n : ln.getDescendants_LibraryMembers())
			if (n instanceof BusinessObjectNode) {
				bo = (BusinessObjectNode) n;
				break;
			}
		ResourceNode resource = ml.addResource(bo);
		assertTrue("Resource created must not be null.", resource != null);

		// When
		// Then - examples are created
		for (ActionNode action : resource.getActions()) {
			String url = action.getRequest().getURL();
			LOGGER.debug("Example: " + url + ".");
			assertTrue("Action has example.", !url.isEmpty());
			if (action.getName().startsWith("Get"))
				assertTrue(
						"Get example must be correct.",
						url.startsWith("GET http://example.com/ResourceTestLibInitialBOs/{testIdResourceTestLibInitialBO}/{ResourceTestLibInitialBOID}"));
		}
	}

	/**
	 * Emulate Resource model to implement <br>
	 * /Reservations/{ResID}/Orders/{OrderID}/Products/{ProductID}
	 */
	@Test
	public void actionExampleWithBaseResource_Tests() {

		// Given - a valid resource using mock library provided business object
		LibraryNode ln = ml.createNewLibrary(pc, "ResourceTestLib");
		BusinessObjectNode resBO = ml.addBusinessObject_ResourceSubject(ln, "Reservations");
		BusinessObjectNode orderBO = ml.addBusinessObject_ResourceSubject(ln, "Orders");
		BusinessObjectNode productBO = ml.addBusinessObject_ResourceSubject(ln, "Products");
		BusinessObjectNode descBO = ml.addBusinessObject_ResourceSubject(ln, "Descriptions");

		ResourceNode resR = ml.addResource(resBO);
		ResourceNode orderR = ml.addResource(orderBO);
		ResourceNode productR = ml.addResource(productBO);
		ResourceNode descR = ml.addResource(descBO);
		// Given tests
		assertTrue("Resource was created.", productR != null);
		assertTrue("Resource was created.", orderR != null);
		assertTrue("Resource was created.", resR != null);
		ml.check(ln);

		checkActionURLs(resR, "Reservation");

		// NOTE - library will be invalid because the parent params are not correct.
		// When - reservation is set as parent on Order as done in the GUI
		orderR.toggleParent(resR.getName());
		ParentRef parentRef = orderR.getParentRef();
		parentRef.setParamGroup("ID");
		parentRef.setPathTemplate("/Reservations/{reservationId}");

		// Then - orders has reservation as parent
		assertTrue("Parent reference is OK.", orderR.getParentRef().getParentResource() == resR);
		String resContribution = orderR.getParentRef().getUrlContribution();
		assertTrue("Parent has URL path contribution.", !resContribution.isEmpty());
		checkActionURLs(orderR, "Reservation");

		// When - order is set as parent to product
		productR.setParentRef(orderR.getName(), "ID");
		productR.getParentRef().setPathTemplate("/Orders/{orderId}");

		// Then - product has orders as parent
		assertTrue("Parent reference is OK.", productR.getParentRef().getParentResource() == orderR);
		String orderContribution = productR.getParentRef().getUrlContribution();
		assertTrue("Parent has URL path contribution.", !orderContribution.isEmpty());
		checkActionURLs(productR, "Reservation");

		// When - product is set as parent to description
		descR.setParentRef(productR.getName(), "ID");
		descR.getParentRef().setPathTemplate("/Products/{productId}");

		// Then - description has product as parent
		assertTrue("Parent reference is OK.", descR.getParentRef().getParentResource() == productR);
		checkActionURLs(descR, "Reservation");
	}

	/**
	 * Emulate Resource model to implement <br>
	 * /Reservations/{ResID}/Orders/{OrderID}/ /Archive/{ArchiveID}/Reservations/{ResID}/Orders/{OrderID}/
	 * /Interactions/(InteractionID}/Reservations/{ResID}/Orders/{OrderID}/
	 */
	@Test
	public void actionExampleWithMultipleParents_Tests() {

		// Given - a valid resource using mock library provided business object
		LibraryNode ln = ml.createNewLibrary(pc, "ResourceTestLib");
		BusinessObjectNode resBO = ml.addBusinessObject_ResourceSubject(ln, "Reservations");
		BusinessObjectNode orderBO = ml.addBusinessObject_ResourceSubject(ln, "Orders");
		BusinessObjectNode interactionBO = ml.addBusinessObject_ResourceSubject(ln, "Interactions");
		BusinessObjectNode archiveBO = ml.addBusinessObject_ResourceSubject(ln, "Archives");

		ResourceNode resR = ml.addResource(resBO);
		ResourceNode orderR = ml.addResource(orderBO);
		ResourceNode interactionR = ml.addResource(interactionBO);
		ResourceNode archiveR = ml.addResource(archiveBO);

		orderR.setParentRef(resR.getName(), "ID");
		orderR.getParentRef().setPathTemplate("/Reservations/{resId}");
		resR.setParentRef(interactionR.getName(), "ID");
		resR.getParentRef().setPathTemplate("/Interactions/{interactionId}");
		resR.setParentRef(archiveR.getName(), "ID");
		resR.getParentRef().setPathTemplate("/Archives/{archiveId}");

		// FIXME
		checkActionURLs(orderR, "Reservation");

	}

	/**
	 * Assure each action has an URL. If the resource has a parent, assure it contributes to the URL.
	 * 
	 * @param rn
	 */
	private void checkActionURLs(ResourceNode rn, String stringToFind) {
		// LOGGER.debug("");
		LOGGER.debug("Printing action URLs for " + rn);
		for (ActionNode action : rn.getActions()) {
			// Action request uses private pathTemplate for URLs
			assert (action.getRequest() != null);
			assert (action.getPathTemplate() != null); // pass thorugh to request
			if (rn.getParentRef() != null)
				assert (!action.getParentContribution().isEmpty());

			// URL combines parent contribution with template params and payload
			String url = action.getRequest().getURL();
			// LOGGER.debug("Parent contribution: " + action.getParentContribution());
			LOGGER.debug("Action URL: " + url);
			assertTrue("Action has an URL.", !url.isEmpty());

			if (stringToFind != null)
				assertTrue("Example must contain " + stringToFind, url.contains(stringToFind));

		}

	}

	@Test
	public void deleteParentResource_Tests() {

		// Given - a valid resource using mock library provided business object
		LibraryNode ln = ml.createNewLibrary(pc, "ResourceTestLib");
		BusinessObjectNode bo = null;
		for (Node n : ln.getDescendants_LibraryMembers())
			if (n instanceof BusinessObjectNode) {
				bo = (BusinessObjectNode) n;
				break;
			}
		bo.setName("InnerObject");
		ResourceNode resource = ml.addResource(bo);
		assertTrue("Resource was created.", resource != null);
		ml.check(ln);

		// Given a second resource
		BusinessObjectNode parentBO = ml.addBusinessObjectToLibrary(ln, "ParentBO");
		ResourceNode parentResource = ml.addResource(parentBO);

		// When - parent resource is set on resource with paramGroup
		ParentRef parentRef = resource.setParentRef(parentResource.getName(), "ID");

		// Then - there is a parent contribution
		assertTrue("Parent makes URL contribution.", !parentRef.getUrlContribution().isEmpty());

		// When - parent resource is deleted
		parentRef.delete();

		// Then - the node, tlRef and contribution are gone
		assertTrue("Parent has empty URL contribution.", parentRef.getUrlContribution().isEmpty());
		assertTrue("TLResource does not have parentRefs", resource.getTLModelObject().getParentRefs().isEmpty());
		assertTrue("Resource does not have ParentRef child.", !resource.getChildren().contains(parentRef));
	}

	private void check(ResourceNode resource) {
		LOGGER.debug("Checking resource: " + resource);

		Assert.assertTrue(resource instanceof ResourceNode);

		// Validate model and tl object
		assertTrue(resource.getTLModelObject() instanceof TLResource);
		assertNotNull(resource.getTLModelObject().getListeners());
		TLResource tlr = (TLResource) resource.getTLModelObject();

		// Validate that the resource is in the where used list for its subject
		assertTrue("Must have a subject.", resource.getSubject() != null);
		assertTrue("Subject must have resource in its where assigned list.", resource.getSubject().getWhereAssigned()
				.contains(resource));
		// LOGGER.debug("Subject must have resource in its where assigned list: "
		// + resource.getSubject().getWhereAssigned().contains(resource));

		// Make sure it is in the library
		assertTrue("Must have library set.", resource.getLibrary() != null);
		List<TypeUser> users = resource.getLibrary().getDescendants_TypeUsers();
		assertTrue("Must be in library.", users.contains(resource));
		if (tlr.getOwningLibrary() != null)
			Assert.assertNotNull(resource.getLibrary());

		Object o;
		for (ResourceMemberInterface rmi : resource.getActionFacets())
			check(rmi);
		for (ResourceMemberInterface rmi : resource.getActions()) {
			check(rmi);
			for (Node child : rmi.getChildren())
				check((ResourceMemberInterface) child);
		}
		for (ResourceMemberInterface rmi : resource.getParameterGroups(false)) {
			check(rmi);
			for (Node child : rmi.getChildren())
				check((ResourceMemberInterface) child);
		}

		o = tlr.getBusinessObjectRef();
		o = tlr.getBusinessObjectRefName();
		o = tlr.getBaseNamespace();
		o = tlr.getBasePath();
		o = tlr.getExtension();
		o = tlr.getListeners();
		o = tlr.getLocalName();
		o = tlr.getName();
		o = tlr.getNamespace();
		o = tlr.getParentRefs();
		o = tlr.getVersion();

	}

	public void check(ResourceMemberInterface resource) {
		// LOGGER.debug("Checking " + resource + " " + resource.getClass().getSimpleName());
		assert resource.getParent() != null;
		assert resource.getName() != null;
		assert resource.getLabel() != null;
		assert resource.getTLModelObject() != null;
		assert resource.getTLModelObject().getListeners() != null;
		assert !resource.getTLModelObject().getListeners().isEmpty();
		assert Node.GetNode(resource.getTLModelObject()) == resource;
		resource.getFields(); // don't crash
	}
}
