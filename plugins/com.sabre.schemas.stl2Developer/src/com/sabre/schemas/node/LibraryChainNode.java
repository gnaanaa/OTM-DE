package com.sabre.schemas.node;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.repository.ProjectItem;
import com.sabre.schemacompiler.repository.RepositoryItem;
import com.sabre.schemacompiler.version.VersionSchemeException;
import com.sabre.schemas.controllers.ProjectController;
import com.sabre.schemas.node.AggregateNode.AggregateType;
import com.sabre.schemas.properties.Images;
import com.sabre.schemas.stl2developer.OtmRegistry;

/**
 * Library chains are all libraries based on the same major release. Their content is aggregated in
 * this node.
 * 
 * @author Dave Hollander
 * 
 */
public class LibraryChainNode extends Node {
    private static final Logger LOGGER = LoggerFactory.getLogger(LibraryChainNode.class);

    protected static final String LIBRARY_CHAIN = "Library Collection";

    // Library Chains collect content from all chain libraries organized by the nav-node.
    protected AggregateNode complexRoot;
    protected AggregateNode simpleRoot;
    protected AggregateNode serviceRoot;
    protected VersionAggregateNode versions;
    protected RepositoryItem repoItem;
    protected List<LibraryNode> chain;
    protected LibraryNode head;

    public LibraryNode getHead() {
        return head;
    }

    protected ProjectItem projectItem; // The TL Project Item wrapped around this library

    /**
     * Create a new chain and move the passed library from its parent to the chain. The library
     * parent will be used as the chain's parent. Children are the 4 aggregate nodes linked by their
     * constructors.
     * 
     * @param ln
     *            - library to add to new chain.
     */
    public LibraryChainNode(LibraryNode ln) {
        super();
        if (ln == null)
            return;
        if (ln.isInChain())
            return;

        ProjectNode pn = ln.getProject();
        if (pn == null) {
            LOGGER.error("Library Chains must be made from libraries in a project.");
            return;
        }
        setParent(pn);
        pn.getChildren().remove(ln);
        pn.getChildren().add(this);
        setIdentity(ln.getProjectItem().getBaseNamespace());

        setLibrary(head = ln);
        createAggregates();
        versions.add(ln);

        aggregateChildren(ln);
        ln.updateLibraryStatus();

        LOGGER.debug("Created library chain " + this.getLabel());
    }

    private void createAggregates() {
        versions = new VersionAggregateNode(AggregateType.Versions, this);
        complexRoot = new AggregateNode(AggregateType.ComplexTypes, this);
        simpleRoot = new AggregateNode(AggregateType.SimpleTypes, this);
        serviceRoot = new AggregateNode(AggregateType.Service, this);
    }

    /**
     * Create a new chain and add to passed project. Model the project item and add to the new
     * chain.
     * 
     * @param pi
     *            - project item to be modeled and added to chain
     * @param project
     *            - parent of the chain
     */
    public LibraryChainNode(ProjectItem pi, ProjectNode project) {
        super();
        if (pi == null || pi.getContent() == null) {
            LOGGER.debug("Null project item content!");
            return;
        }
        setIdentity(pi.getBaseNamespace());

        setParent(project);
        project.getChildren().add(this);

        head = null;

        chain = new ArrayList<LibraryNode>();
        createAggregates();

        List<ProjectItem> piChain = null;
        try {
            piChain = pi.getProjectManager().getVersionChain(pi);
        } catch (VersionSchemeException e1) {
            throw (new IllegalStateException("Could not get chain from project manager."));
        }

        for (ProjectItem item : piChain) {
            add(item);
        }
        setLibrary(head);
    }

    /**
     * Add this project item to the version chain.
     * 
     * @return the library node added to the chain or null if it already was in the chain.
     * @param pi
     */
    public LibraryNode add(ProjectItem pi) {
        // If the chain already has this PI, skip it.
        LibraryNode newLib = null;
        for (Node n : versions.getChildren()) {
            if ((n instanceof LibraryNode))
                if (pi.equals(((LibraryNode) n).getProjectItem())) {
                    newLib = (LibraryNode) n;
                    break;
                }
        }

        if (newLib == null) {
            LOGGER.debug("Adding pi " + pi.getFilename() + " to chain " + getLabel());
            newLib = new LibraryNode(pi, this);
            versions.add(newLib);
            newLib.updateLibraryStatus();
            aggregateChildren(newLib);
        }
        if (head == null || newLib.getTLaLib().isLaterVersion(head.getTLaLib()))
            head = newLib;

        return newLib;
    }

    /**
     * Return true if 1st node is from a later version that node2. For example: (v01:flight,
     * v00:flight) returns true.
     * 
     * @param node1
     * @param node2
     */
    public boolean isLaterVersion(Node node1, Node node2) {
        return node1.getLibrary().getTLaLib().isLaterVersion(node2.getLibrary().getTLaLib());
    }

    /**
     * Add the passed node to the appropriate chain aggregate. Wrap the node in a version node in
     * the library's children list.
     * 
     * @param node
     */
    protected void add(ComponentNode node) {
        if (node == null)
            return;
        // If not already wrapped in a version, add version wrapper
        if (!(node.getParent() instanceof VersionNode))
            new VersionNode(node);

        // Add to chain object aggregates.
        if ((node instanceof ComplexComponentInterface))
            complexRoot.add(node);
        else if ((node instanceof SimpleComponentInterface))
            simpleRoot.add(node);
        else if ((node instanceof ServiceNode || (node instanceof OperationNode)))
            serviceRoot.add(node);
        else
            LOGGER.warn("add skipped: " + node);
    }

    /**
     * Remove the node from the appropriate aggregate node. This does not delete the node, just
     * remove it from aggregate list and takes care of family if needed.
     * 
     * @param n
     */
    public void removeAggregate(ComponentNode node) {
        // Remove this version.
        if ((node instanceof ComplexComponentInterface))
            complexRoot.remove(node);
        else if ((node instanceof SimpleComponentInterface))
            simpleRoot.remove(node);
        else if ((node instanceof ServiceNode || (node instanceof OperationNode)))
            serviceRoot.remove(node);

        // Must remove first or add will ignore this later version.
        add(findPreviousVersion(node));
    }

    /**
     * Find the "latest" previous version of the node.
     * 
     * @param node
     */
    private ComponentNode findPreviousVersion(ComponentNode node) {
        // assume node is in this chain.
        ComponentNode n, vn = null;
        for (Node ln : versions.getChildren()) {
            n = (ComponentNode) ln.findNodeByName(node.getName());
            if (vn == null && n != node)
                vn = n;
            else if (n != null && n != node)
                if (isLaterVersion(n, vn))
                    vn = n;
        }
        return vn;
    }

    /**
     * Add each named-type descendant to the chain.
     * 
     * @param lib
     */
    private void aggregateChildren(LibraryNode lib) {
        if (lib.getServiceRoot() != null) {
            add((ComponentNode) lib.getServiceRoot());
        }
        for (Node n : lib.getDescendentsNamedTypes()) {
            add((ComponentNode) n);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.Node#close()
     */
    @Override
    public void close() {
        ProjectNode project = (ProjectNode) getParent();
        super.close();
        // Super will close all libraries in the chain. Save the project since it will have changed.
        ProjectController pc = OtmRegistry.getMainController().getProjectController();
        pc.save(project);
    }

    @Override
    public String getComponentType() {
        return LIBRARY_CHAIN;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.Node#getChain()
     */
    @Override
    public LibraryChainNode getChain() {
        return this;
    }

    @Override
    public Image getImage() {
        return Images.getImageRegistry().get(Images.libraryChain);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.Node#getLabel()
     */
    @Override
    public String getLabel() {
        String label = "Version Chain";
        if (head != null) {
            label = head.getLabel();
        }
        return label;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.Node#getLibraries()
     */
    @Override
    public List<LibraryNode> getLibraries() {
        ArrayList<LibraryNode> libs = new ArrayList<LibraryNode>();
        for (Node n : versions.getChildren())
            if (n instanceof LibraryNode)
                libs.add((LibraryNode) n);
        return libs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.Node#getName()
     */
    @Override
    public String getName() {
        String label = "Version Chain";
        if (head != null) {
            NamespaceHandler handler = head.getNsHandler();
            if (handler != null)
                label = head.getName() + "-" + handler.getNSVersion(head.getNamespace());
        }
        return label;
    }

    /**
     * All members of the chain must have the same name, base namespace and major version number.
     */
    @Override
    public String getIdentity() {
        String identity = "UNIDENTIFIED-CHAIN:someNS:9";
        if (head != null) {
            NamespaceHandler handler = head.getNsHandler();
            if (handler != null)
                identity = makeIdentity();
        }
        return identity;
    }

    /**
     * See also {@link ProjectNode#makeChainIdentity(ProjectItem)} 9/23/2013 - this method does not
     * use the repository for managed base namespaces. It matches the behavior or makeChainIdentity
     * in ProjectNode.
     */
    public String makeChainIdentity() {
        String name = head.getName();
        NamespaceHandler handler = head.getNsHandler();
        String baseNS = handler.removeVersion(head.getNamespace());
        return makeIdentity(name, baseNS, handler.getNS_Major(head.getNamespace()));
    }

    /**
     * See also {@link ProjectNode#makeChainIdentity(ProjectItem)}
     */
    // TODO - see if users of this method should be using chainIdentity()
    public String makeIdentity() {
        String name = head.getName();
        NamespaceHandler handler = head.getNsHandler();
        return makeIdentity(name, handler.getNSBase(head.getNamespace()),
                handler.getNS_Major(head.getNamespace()));
    }

    public static String makeIdentity(String name, String baseNS, String majorNS) {
        return name + ":" + baseNS + ":" + majorNS;
    }

    @Override
    public List<Node> getNavChildren() {
        return getChildren();
    }

    public Node getVersions() {
        return versions;
    }

    @Override
    public boolean hasNavChildren() {
        return getChildren().size() <= 0 ? false : true;
    }

    @Override
    public boolean isEditable() {
        // True if any library is editable.
        for (Node ln : versions.getChildren())
            if (ln.isEditable())
                return true;
        return false;
    }

    @Override
    public boolean isLibraryContainer() {
        return true;
    }

    public boolean isNewer(LibraryNode ref, LibraryNode test) {
        return ref == null || test.getTLaLib().isLaterVersion(ref.getTLaLib()) ? true : false;
    }

    public INode getSimpleAggregate() {
        return simpleRoot;
    }

    public boolean isPatch() {
        return head.isPatchVersion();
    }

    public boolean isMinor() {
        return head.isMinorVersion();
    }

    public boolean isMajor() {
        return head.isMajorVersion();
    }

    public INode getServiceAggregate() {
        return serviceRoot;
    }

    public INode getComplexAggregate() {
        return complexRoot;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.INode#hasChildren_TypeProviders()
     */
    @Override
    public boolean hasChildren_TypeProviders() {
        return versions.getChildren().size() > 0 ? true : false;
    }

    @Override
    public boolean isNavigation() {
        return true;
    }

    /**
     * Return the Simple/Complex/Service navNode in the latest library that matches the type of this
     * node. *
     * 
     * @param parent
     */
    public NavNode getLatestNavNode(ComponentNode node) {
        Node parent = node.getOwningNavNode();
        for (Node nav : head.getChildren()) {
            if (parent.getComponentType().equals(nav.getComponentType()))
                return (NavNode) nav;
        }
        return null;
    }

}