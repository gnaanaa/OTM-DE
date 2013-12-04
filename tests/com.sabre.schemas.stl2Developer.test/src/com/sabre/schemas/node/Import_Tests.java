/**
 * 
 */
package com.sabre.schemas.node;

import java.io.File;
import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.controllers.ProjectController;
import com.sabre.schemas.node.Node_Tests.TestNode;
import com.sabre.schemas.node.properties.ElementNode;
import com.sabre.schemas.testUtils.LoadFiles;
import com.sabre.schemas.testUtils.MockLibrary;

/**
 * @author Dave Hollander
 * 
 */
public class Import_Tests {
    private static final Logger LOGGER = LoggerFactory.getLogger(Import_Tests.class);

    TestNode nt = new Node_Tests().new TestNode();
    ModelNode model = null;
    TestNode tn = new Node_Tests().new TestNode();
    LoadFiles lf = new LoadFiles();
    LibraryTests lt = new LibraryTests();
    MockLibrary ml = null;
    LibraryNode ln = null;
    MainController mc = new MainController();
    ProjectController pc = mc.getProjectController();
    ProjectNode defaultProject = pc.getDefaultProject();

    @Test
    public void ImportTest() throws Exception {
        LoadFiles lf = new LoadFiles();
        Node_Tests nt = new Node_Tests();
        List<Node> imported;

        LibraryNode sourceLib = lf.loadFile5Clean(mc);
        LibraryNode destLib = lf.loadEmpty(mc);
        LibraryNode xSourceLib = lf.loadXfileDsse(mc);

        // Make sure they loaded OK.
        sourceLib.visitAllNodes(nt.new TestNode());
        destLib.visitAllNodes(nt.new TestNode());
        xSourceLib.visitAllNodes(nt.new TestNode());

        // LOGGER.debug("\n");
        LOGGER.debug("Start Import ***************************");
        int destTypes = destLib.getDescendants_NamedTypes().size();

        // make sure that destLib is editable (move to project with correct ns)
        String projectFile = MockLibrary.createTempFile("TempProject", ".otp");
        ProjectNode project = pc.create(new File(projectFile), destLib.getNamespace(), "Name", "");
        destLib = pc.add(project, destLib.getTLaLib());
        Assert.assertTrue(destLib.isEditable());

        imported = destLib.importNodes(xSourceLib.getDescendants_NamedTypes(), true);
        int afterImportTypes = destLib.getDescendants_NamedTypes().size();
        Assert.assertEquals(destTypes + imported.size(), afterImportTypes);
        Assert.assertFalse(imported.isEmpty());

        // Make sure the source is still OK
        sourceLib.visitAllNodes(nt.new TestNode());

        // Make sure the imported nodes are OK.
        destLib.visitAllNodes(nt.new TestNode());
        checkContents(destLib);
    }

    private void checkContents(LibraryNode lib) {
        String ns = lib.getNamespace();
        LOGGER.debug("Testing library with namespace " + ns);
        if (!lib.getName().equals("EmptyOTM")) {
            LOGGER.debug("Wrong library, skipping " + lib);
            return;
        }

        // Find some DSSE imported types.
        Node ALG = NodeFinders.findTypeProviderByQName(new QName(ns, "ALG")); // vwa
        Node ASG = NodeFinders.findTypeProviderByQName(new QName(ns, "ASG")); // a core
        Node DIA = NodeFinders.findTypeProviderByQName(new QName(ns, "DIA")); // a simple
        // ODMsgType_TYP is a duplicate in a family node. Make sure 1 came through.
        Node ODMsgType_TYP = NodeFinders.findNodeByName("ODMsgType_TYP", ns); // an enum

        Assert.assertNotNull(ALG);
        Assert.assertNotNull(ASG);
        Assert.assertNotNull(ODMsgType_TYP);

        Assert.assertTrue(ALG instanceof VWA_Node);
        Assert.assertTrue(ASG instanceof CoreObjectNode);
        Assert.assertTrue(DIA instanceof SimpleTypeNode);
        Assert.assertTrue(ODMsgType_TYP instanceof SimpleTypeNode);

        Assert.assertTrue(ALG.getLibrary().getName().equals("EmptyOTM"));
        Assert.assertTrue(ASG.getLibrary().getName().equals("EmptyOTM"));
        Assert.assertTrue(DIA.getLibrary().getName().equals("EmptyOTM"));
        Assert.assertTrue(ODMsgType_TYP.getLibrary().getName().equals("EmptyOTM"));

        Assert.assertTrue(DIA.getTypeName().equals("string"));
        Assert.assertEquals(19, ALG.getDescendants_TypeUsers().size());
    }

    @Test
    public void importNode() {
        LoadFiles lf = new LoadFiles();
        ml = new MockLibrary();

        LibraryNode target = ml
                .createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary(target, "testBO");
        CoreObjectNode core = ml.addCoreObjectToLibrary(target, "testCore");
        int beforeImportFamilies = familyCount(target);

        target.importNode(bo);
        target.importNode(core);
        Assert.assertEquals(3, target.getDescendants_NamedTypes().size());
        Assert.assertEquals(beforeImportFamilies, familyCount(target));
    }

    private int familyCount(LibraryNode ln) {
        int count = 0;
        for (Node n : ln.getDescendants())
            if (n instanceof FamilyNode)
                count++;
        return count;
    }

    @Test
    public void createAliases() {
        LoadFiles lf = new LoadFiles();
        ml = new MockLibrary();

        ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "testBO");
        CoreObjectNode core = ml.addCoreObjectToLibrary(ln, "testCore");
        FacetNode summary = bo.getSummaryFacet();
        int coreKids = core.getChildren().size();

        // Add 3 core objects as property types to see the aliases get made.
        ElementNode prop1, prop2, prop3 = null;
        prop1 = new ElementNode(summary, "P1");
        prop1.setAssignedType(core);
        prop2 = new ElementNode(summary, "P2");
        prop2.setAssignedType(core);
        prop3 = new ElementNode(summary, "P3");
        prop3.setAssignedType(core);

        bo.createAliasesForProperties();
        Assert.assertTrue(summary.getChildren().size() == 4);
        Assert.assertEquals(coreKids + 3, core.getChildren().size());
        Assert.assertEquals("P1_testCore", prop1.getName());
        Assert.assertEquals("P1_testCore", prop1.getTypeName());
        Assert.assertEquals("P1_testCore", prop1.getTLTypeObject().getLocalName());

    }
}