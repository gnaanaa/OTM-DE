/*
 * Copyright (c) 2013, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemas.controllers.repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.Mockito;

import com.sabre.schemacompiler.index.FreeTextSearchService;
import com.sabre.schemacompiler.repository.Project;
import com.sabre.schemacompiler.repository.ProjectItem;
import com.sabre.schemacompiler.repository.RemoteRepository;
import com.sabre.schemacompiler.repository.RepositoryException;
import com.sabre.schemacompiler.repository.RepositoryItem;
import com.sabre.schemacompiler.repository.RepositoryManager;
import com.sabre.schemacompiler.repository.impl.RemoteRepositoryClient;
import com.sabre.schemas.controllers.DefaultRepositoryController;
import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.controllers.ProjectController;
import com.sabre.schemas.node.LibraryChainNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.ProjectNode;
import com.sabre.schemas.stl2Developer.reposvc.JettyTestServer;
import com.sabre.schemas.stl2Developer.reposvc.RepositoryTestUtils;
import com.sabre.schemas.stl2developer.OtmRegistry;
import com.sabre.schemas.trees.repository.RepositoryNode;
import com.sabre.schemas.trees.repository.RepositoryNode.RepositoryItemNode;
import com.sabre.schemas.views.RepositoryView;

/**
 * Abstract base class that defines common methods used during live repository testing.
 * 
 * @author Pawel Jedruch
 */
public abstract class RepositoryIntegrationTestBase {

    protected static RepositoryManager repositoryManager;
    protected static RemoteRepository remoteRepository;
    protected static JettyTestServer jettyServer;
    protected static File tmpWorkspace;

    protected static DefaultRepositoryController rc;
    protected static MainController mc;
    protected static ProjectController pc;
    protected static ProjectNode defaultProject;
    private static List<ProjectNode> projectsToClean = new ArrayList<ProjectNode>();

    public abstract RepositoryNode getRepositoryForTest();

    @BeforeClass
    public final static void beforeTests() throws Exception {
        OtmRegistry.registerRepositoryView(Mockito.mock(RepositoryView.class));
        tmpWorkspace = new File(System.getProperty("user.dir"), "/target/test-workspace/");
        RepositoryTestUtils.deleteContents(tmpWorkspace);
        tmpWorkspace.deleteOnExit();
        startEmptyServer();
        mc = new MainController(repositoryManager);
        rc = (DefaultRepositoryController) mc.getRepositoryController();
        pc = mc.getProjectController();
        readdRemoteRepository();

    }

    @Before
    public void beforeEachTest() throws RepositoryException {
        defaultProject = createProject("Otm-Test-DefaultProject", rc.getLocalRepository(), "IT");
    }

    @After
    public void afterEachTest() throws RepositoryException, IOException {
        pc.closeAll();
        for (ProjectNode pn : projectsToClean) {
            RepositoryTestUtils.deleteContents(pn.getProject().getProjectFile().getParentFile());
        }
        projectsToClean.clear();
        reinitializeRepositories();
    }

    public void removeProject(ProjectNode pn) {
        if (projectsToClean.remove(pn)) {
            RepositoryTestUtils.deleteContents(pn.getProject().getProjectFile().getParentFile());
        }
    }

    public static ProjectNode createProject(String name, RepositoryNode nodeForNamespace,
            String extension) {
        return createProject(name, nodeForNamespace.getNamespace(), extension);
    }

    public static ProjectNode createProject(String name, String namespace, String extension) {
        File projectDir = new File(tmpWorkspace, name);
        File projectFile = new File(projectDir, name + ".otp");
        if (extension != null && !extension.isEmpty()) {
            namespace = namespace + "/" + extension;
        }
        ProjectNode project = pc.create(projectFile, namespace, name, "");
        if (project != null)
            projectsToClean.add(project);
        return project;
    }

    public RepositoryItemNode findRepositoryItem(LibraryChainNode chainNode, RepositoryNode parent) {
        for (RepositoryItemNode ri : getItems(parent)) {
            RepositoryItem item = ri.getItem();
            if (item.getNamespace().equals(chainNode.getHead().getNamespace())
                    && item.getLibraryName().equals(chainNode.getHead().getName())) {
                return ri;
            }
        }
        return null;
    }

    private List<RepositoryItemNode> getItems(Node parent) {
        List<RepositoryItemNode> nodes = new ArrayList<RepositoryItemNode>();
        if (parent instanceof RepositoryItemNode) {
            return Collections.singletonList((RepositoryItemNode) parent);
        } else {
            for (Node child : parent.getChildren()) {
                nodes.addAll(getItems(child));
            }
        }
        return nodes;
    }

    @AfterClass
    public final static void afterTests() throws Exception {
        shutdownTestServer();
        RepositoryTestUtils.deleteContents(tmpWorkspace);
    }

    public final static void reinitializeRepositories() throws IOException, RepositoryException {
        FreeTextSearchService searchSerfice = FreeTextSearchService.getInstance();
        searchSerfice.stopService();
        jettyServer.initializeRuntimeRepository();
        searchSerfice.startService();
        RepositoryTestUtils.deleteContents(repositoryManager.getRepositoryLocation());
        recreateLocalRepository(repositoryManager);
        // sync root
        rc.sync(null);
    }

    private static void recreateLocalRepository(RepositoryManager repositoryManager2)
            throws RepositoryException {
        new RepositoryManager(repositoryManager2.getRepositoryLocation());
        RemoteRepositoryClient remote = (RemoteRepositoryClient) remoteRepository;
        repositoryManager.addRemoteRepository(remote.getEndpointUrl());
        repositoryManager.setCredentials(remoteRepository, "testuser", "password");
    }

    protected static void startEmptyServer() throws Exception {
        System.setProperty("ota2.repository.realTimeIndexing", "true");
        File emptySnapshot = new File(FileLocator.resolve(
                RepositoryIntegrationTestBase.class
                        .getResource("/Resources/repo-snapshots/empty-repository")).toURI());
        File ota2config = new File(FileLocator.resolve(
                RepositoryIntegrationTestBase.class
                        .getResource("/Resources/repo-snapshots/ota2.xml")).toURI());
        File tmpRepository = createFolder(tmpWorkspace, "ota-test-repository");
        File localRepository = createFolder(tmpWorkspace, "local-repository");
        repositoryManager = new RepositoryManager(localRepository);

        int port = getIntProperty("com.sabre.schemas.test.repository.port", 19191);
        jettyServer = new JettyTestServer(port, emptySnapshot, tmpRepository, ota2config);
        jettyServer.start();

    }

    private static int getIntProperty(String key, int def) {
        try {
            String value = System.getProperty(key);
            return Integer.valueOf(value).intValue();
        } catch (NumberFormatException ex) {
            return def;
        }

    }

    public static final String getUserID() {
        return "testuser";
    }

    public static final String getUserPassword() {
        return "password";
    }

    protected static void readdRemoteRepository() throws RepositoryException {
        remoteRepository = jettyServer.configureRepositoryManager(repositoryManager);
        repositoryManager.setCredentials(remoteRepository, getUserID(), getUserPassword());
        rc.getRoot().addRepository(remoteRepository);
    }

    protected static void shutdownTestServer() throws Exception {
        jettyServer.stop();
    }

    protected ProjectItem findProjectItem(Project project, String filename) {
        ProjectItem result = null;

        for (ProjectItem item : project.getProjectItems()) {
            if (item.getFilename().equals(filename)) {
                result = item;
                break;
            }
        }
        return result;
    }

    protected RepositoryItem findRepositoryItem(List<RepositoryItem> itemList, String filename) {
        RepositoryItem result = null;

        for (RepositoryItem item : itemList) {
            if (item.getFilename().equals(filename)) {
                result = item;
                break;
            }
        }
        return result;
    }

    public static File createTempDirectory(String name) throws IOException {
        final File temp;

        temp = File.createTempFile(name, Long.toString(System.nanoTime()));

        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        return (temp);
    }

    public static File createFolder(File parent, String folder) {
        File file = new File(parent, folder);
        file.mkdir();
        return file;
    }

}