package com.sabre.schemas.controllers.repository;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Version;

import com.sabre.schemacompiler.repository.RepositoryException;
import com.sabre.schemacompiler.repository.RepositoryItem;
import com.sabre.schemacompiler.repository.RepositoryItemState;
import com.sabre.schemacompiler.saver.LibrarySaveException;
import com.sabre.schemas.controllers.DefaultProjectController;
import com.sabre.schemas.node.LibraryChainNode;
import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.node.ProjectNode;
import com.sabre.schemas.trees.repository.RepositoryNode;
import com.sabre.schemas.utils.ComponentNodeBuilder;
import com.sabre.schemas.utils.LibraryNodeBuilder;

public class RemoteRepositoryTest extends RepositoryControllerTest {

    @Override
    public RepositoryNode getRepositoryForTest() {
        for (RepositoryNode rn : rc.getAll()) {
            if (rn.isRemote()) {
                return rn;
            }
        }
        throw new IllegalStateException("Missing remote repository. Check your configuration.");
    }

    /**
     * Search
     */
    @Test
    public void searchDraftLibrary() throws RepositoryException, LibrarySaveException {
        ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "");
        LibraryNode testLibary = LibraryNodeBuilder.create("TestLibrary",
                getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
                .build(uploadProject, pc);
        testLibary.addMember(ComponentNodeBuilder.createSimpleCore("test").get());
        rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary));
        List<RepositoryItem> results = rc.search("tes*");
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(testLibary.getName(), results.get(0).getLibraryName());

    }

    @Test
    public void searchFinalLibrary() throws RepositoryException, LibrarySaveException {
        ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "");
        LibraryNode testLibary = LibraryNodeBuilder
                .create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix",
                        new Version(1, 0, 0)).makeFinal().build(uploadProject, pc);
        testLibary.addMember(ComponentNodeBuilder.createSimpleCore("test").get());
        rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary));
        List<RepositoryItem> results = rc.search("tes*");
        Assert.assertEquals(1, results.size());
        Assert.assertEquals(testLibary.getName(), results.get(0).getLibraryName());
    }

    @Test
    public void searchEarlyVersionLibrary() throws RepositoryException, LibrarySaveException {
        ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "");
        LibraryNode testLibary = LibraryNodeBuilder
                .create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix",
                        new Version(1, 0, 0)).makeFinal().build(uploadProject, pc);
        testLibary.addMember(ComponentNodeBuilder.createSimpleCore("test").get());
        LibraryChainNode chain = rc.manage(getRepositoryForTest(),
                Collections.singletonList(testLibary)).get(0);
        LibraryNode newMajor = rc.createMajorVersion(chain.getHead());
        List<RepositoryItem> results = rc.search("tes*");
        Assert.assertEquals(2, results.size());
        Assert.assertEquals(newMajor.getName(), results.get(0).getLibraryName());
    }

    /**
     * <pre>
     * 1. Manage Library in repository 
     * 2. Lock Library 
     * 3. Remove repository from step 1. 
     * 4. Reopen project with library
     * </pre>
     * 
     * @throws LibrarySaveException
     * @throws RepositoryException
     */
    @Test
    public void openLibraryWithMissingRepository() throws LibrarySaveException, RepositoryException {
        ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "test");
        LibraryNode testLibary = LibraryNodeBuilder.create("TestLibrary",
                getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
                .build(uploadProject, pc);
        LibraryChainNode chain = rc.manage(getRepositoryForTest(),
                Collections.singletonList(testLibary)).get(0);
        boolean locked = rc.lock(chain.getHead());
        Assert.assertTrue(locked);
        Assert.assertEquals(RepositoryItemState.MANAGED_WIP, chain.getHead().getProjectItem()
                .getState());
        String namespace = getRepositoryForTest().getNamespace();
        try {
            rc.removeRemoteRepository(getRepositoryForTest());
            pc.close(uploadProject);

            DefaultProjectController dc = (DefaultProjectController) pc;
            // RepositoryUtils.checkItemState( item, this ); will throw NPE
            ProjectNode reopenedProject = dc.open(uploadProject.getProject().getProjectFile()
                    .toString());

            Assert.assertNotNull("Project couldn't be created. "
                    + "Reason of this is that this project is already opened but"
                    + " with incosistent state becouse of NPE", reopenedProject);
        } finally {
            readdRemoteRepository();
        }

    }
}