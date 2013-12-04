/**
 * 
 */
package com.sabre.schemas.node;

import org.junit.Assert;
import org.junit.Test;

import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.testUtils.LoadFiles;
import com.sabre.schemas.testUtils.MockLibrary;

/**
 * @author Dave Hollander
 * 
 */
public class NamespaceHandler_Tests {
    ModelNode model = null;
    Node_Tests nt = new Node_Tests();
    LoadFiles lf = new LoadFiles();
    LibraryTests lt = new LibraryTests();
    MockLibrary mockLibrary = new MockLibrary();

    @Test
    public void mockLibTest() {
        LibraryNode ln = mockLibrary.createNewLibrary("http://foo.bar", "test", null);
        ln.setNSPrefix("aaa");
        Assert.assertFalse(ln.getNamespace().isEmpty());
        Assert.assertFalse(ln.getNamePrefix().isEmpty());

        // Test setting to a namespace not in the handler registry.
        ln.setNamespace("http://foo.bar/too");
        Assert.assertFalse(ln.getNamespace().isEmpty());
        Assert.assertFalse(ln.getNamePrefix().isEmpty());
    }

    @Test
    public void nsHandlerTest() throws Exception {
        MainController mc = new MainController();
        final String testNS = "http://www.sabre.com/ns/TEST";

        final String testExtension = "Test";
        final String testVersionError = "0.1"; // period is error
        final String testVerison = "0.1.0"; // as will be used
        final String versionID = "v0_1"; // as used in ns
        final String expectedFullNS = mc.getRepositoryController().getLocalRepository()
                .getNamespace()
                + "/Test/" + versionID;

        lf.loadTestGroupA(mc);
        for (LibraryNode ln : Node.getAllLibraries()) {
            ln.visitAllNodes(nt.new TestNode());

            // Make sure the handlers are assigned correctly.
            Assert.assertNotNull(NamespaceHandler.getNamespaceHandler(ln));
            NamespaceHandler handler = NamespaceHandler.getNamespaceHandler(ln);
            Assert.assertNotNull(ln.getNsHandler());
            Assert.assertTrue(handler == ln.getNsHandler());
        }

        for (LibraryNode ln : Node.getAllUserLibraries()) {
            NamespaceHandler handler = NamespaceHandler.getNamespaceHandler(ln);
            String namespace = ln.getNamespace();
            String prefix = ln.getNamePrefix();
            int nsCount = handler.getNamespaces().size();

            Assert.assertFalse(namespace.isEmpty());
            Assert.assertFalse(prefix.isEmpty());
            Assert.assertTrue(prefix.equals(handler.getPrefix(namespace)));

            // Just make sure these do not cause error
            handler.getNSBase();
            handler.getNSExtension(namespace);
            handler.getNSVersion(namespace);
            handler.createValidNamespace(namespace, testVersionError);
            handler.createValidNamespace(namespace, testExtension, testVersionError);

            // rename
            handler.renameInProject(namespace, testNS);
            handler.rename(namespace, testNS);

            // Set
            handler.setNamespacePrefix(testNS, "TST");
            handler.setLibraryNamespace(ln, namespace);

            // Originals plus the test one. On second loop, they are equal.
            int size = handler.getNamespaces().size();
            Assert.assertTrue(size >= nsCount);
        }

        for (ProjectNode pn : Node.getAllProjects()) {
            NamespaceHandler handler = NamespaceHandler.getNamespaceHandler(pn);
            Assert.assertNotNull(handler);
            Assert.assertTrue(handler.getNamespaces().size() > 0);
        }

        // Test the namespace string functions
        NamespaceHandler handler = NamespaceHandler.getNamespaceHandler(mc.getProjectController()
                .getDefaultProject());
        Assert.assertNotNull(handler);

        String nsb = handler.getNSBase();
        String ns = handler.createValidNamespace(nsb, testVersionError);
        ns = handler.createValidNamespace(nsb, testExtension, testVersionError);
        Assert.assertTrue(ns.equals(expectedFullNS));

        Assert.assertTrue(nsb.equals(handler.getNSBase()));
        Assert.assertTrue(testExtension.equals(handler.getNSExtension(ns)));
        String version = handler.getNSVersion(ns);
        Assert.assertTrue(testVerison.equals(handler.getNSVersion(ns)));

        Assert.assertTrue(handler.isValidNamespace(ns).isEmpty());
        Assert.assertFalse(handler.isValidNamespace("foo:urn:junk").isEmpty());
        Assert.assertFalse(handler.isValidNamespace("http://tempuri.org/ns").isEmpty());
    }
}