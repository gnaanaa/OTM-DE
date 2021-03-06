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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryNamespaceUtils;
import org.opentravel.schemacompiler.repository.impl.BuiltInProject;
import org.opentravel.schemas.controllers.LibraryModelManager;
import org.opentravel.schemas.controllers.ProjectController;
import org.opentravel.schemas.modelObject.EmptyMO;
import org.opentravel.schemas.modelObject.TLEmpty;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class ProjectNode extends Node implements INode {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectNode.class);

	private final Project project; // underlying TL model object

	/**
	 * Can be used as a marker for a null project.
	 */
	public ProjectNode() {
		this.project = null;
		parent = Node.getModelNode();
		// EmptyMO - TLEmpty
	}

	/**
	 * Create a project node from the TL project model. Read all libraries in the project and create library nodes and
	 * their children. Link into the model node.
	 * 
	 * @param tlProject
	 *            - tl project to be represented
	 */
	public ProjectNode(Project tlProject) {
		super(tlProject.getName());
		this.project = tlProject;
		setName(tlProject.getName());
		Node.getModelNode().addProject(this);

		load(tlProject.getProjectItems());

		assert (parent instanceof ModelNode);
		assert (modelObject instanceof EmptyMO);
	}

	/**
	 * Link the library as a child to the project node. Does NOT add to the underlying model. Does not resolve types.
	 * 
	 * @param lib
	 */
	@Override
	// FIXME - only used in library node constructors
	public void linkLibrary(LibraryNode lib) {
		if (lib != null && !getChildren().contains(lib))
			getChildren().add(lib);
	}

	/**
	 * Create a library nav node for the chain then add the nav node.
	 * 
	 * @param lcn
	 */
	public void linkLibrary(LibraryChainNode lcn) {
		LibraryNavNode lnn = new LibraryNavNode(lcn, this);
		if (!getChildren().contains(lnn))
			getChildren().add(lnn);
	}

	public LibraryNavNode load(final List<ProjectItem> piList) {
		LibraryModelManager manager = getParent().getLibraryManager();
		LibraryNavNode lnn = null;

		if (piList.isEmpty())
			LOGGER.warn(this + " project item list is empty.");

		for (ProjectItem pi : piList) {
			lnn = manager.add(pi, this);
			if (lnn != null) // can fail to create chain
				assert (getChildren().contains(lnn));

		}
		return lnn;
	}

	/**
	 * Add the files to the project and models all new project items in the GUI model. NOTE - for performance reasons,
	 * always try to add multiple files at once.
	 * {@link org.opentravel.schemas.controllers.DefaultLibraryController#openLibrary(ProjectNode)}
	 */
	public void add(List<File> libraryFiles) {
		ProjectController pc = OtmRegistry.getMainController().getProjectController();
		pc.addLibrariesToTLProject(this.project, libraryFiles);
		load(this.project.getProjectItems());
	}

	/**
	 * Close each library or chain using the library model manager. Does <b>not</b> close the TL Project.
	 */
	@Override
	public void close() {
		// LOGGER.debug("Closing " + getName());
		List<Node> libs = new ArrayList<Node>(getChildren());
		for (Node n : libs)
			n.close();
		unlinkNode();
	}

	/**
	 * Remove this library from this project then use library manager to attempt to close members
	 * 
	 * <b>Note:</b> use project controller to remove libraries from TL Project.
	 * 
	 * @param lib
	 *            - navigator node identifying which library or chain to close
	 */
	public void close(LibraryInterface lib) {
		// Find the affected LibraryNavNode
		for (Node n : getChildren())
			if (n instanceof LibraryNavNode && ((LibraryNavNode) n).getThisLib() == lib) {
				close((LibraryNavNode) n);
				return;
			}
	}

	/**
	 * Remove the associated library from this project. Using library manager, attempt to close members if this is the
	 * last project to use this library.
	 * 
	 * <b>Note:</b> use project controller to remove libraries from TL Project.
	 * 
	 * @param lib
	 *            - navigator node identifying which library or chain to close
	 */
	public void close(LibraryNavNode lnn) {
		getChildren().remove(lnn);
		getParent().getLibraryManager().close(lnn.getThisLib(), this);
		lnn.setParent(null);
		lnn.deleted = true;
	}

	public void unlinkNode(LibraryInterface lib) {
		// Find the child and unlink it
		// List<Node> kids = new ArrayList<Node>(getChildren());
		for (Node child : getChildren())
			if (child == lib) {
				child.unlinkNode();
				return;
			} else if (child instanceof LibraryNavNode)
				if (((LibraryNavNode) child).getThisLib() == lib) {
					child.unlinkNode();
					return;
				}
	}

	@Override
	public String getComponentType() {
		return "Project: " + getName();
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.Project);
	}

	@Override
	public String getLabel() {
		if (OtmRegistry.getMainController() != null && OtmRegistry.getMainController().getProjectController() != null) {
			ProjectController pc = OtmRegistry.getMainController().getProjectController();
			if (pc.getDefaultProject() == this)
				return getName();
			if (pc.getBuiltInProject() == this)
				return getName();
		}

		NamespaceHandler nsHandler = NamespaceHandler.getNamespaceHandler(this);
		String prefix = "";
		if (nsHandler != null && nsHandler.getPrefix(project.getProjectId()) != null)
			prefix = nsHandler.getPrefix(project.getProjectId()) + " = ";
		return getName() + " [" + prefix + project.getProjectId() + "]";
	}

	@Override
	public String getName() {
		return project.getName();
	}

	/**
	 * Try to find the project item for the library. If not found, try to add it to the project.
	 * 
	 * @return the project item associated with this library
	 */
	public ProjectItem add(AbstractLibrary tlLib) {
		ProjectItem pi = getProjectItem(tlLib);
		if (pi == null)
			try {
				pi = getTLProject().getProjectManager().addUnmanagedProjectItem(tlLib, getTLProject());
			} catch (RepositoryException e1) {
				LOGGER.error("Error adding " + tlLib.getName() + " to project. " + e1.getLocalizedMessage());
			}
		return pi;
	}

	/**
	 * @return the project item associated with the passed TL AbstractLibrary or null
	 */
	public ProjectItem getProjectItem(AbstractLibrary tlLib) {
		for (ProjectItem item : getTLProject().getProjectItems())
			if (item.getContent() == tlLib)
				return item;
		return null;
	}

	@Override
	public String getPrefix() {
		return "";
	}

	@Override
	public String getNamespace() {
		return project.getProjectId();
	}

	/**
	 * @return the portion of the project namespace that is managed/governed by a repository. Empty string if not
	 *         managed.
	 */
	public String getNSRoot() {
		String ns = getNamespace();
		for (String managedRoot : OtmRegistry.getMainController().getRepositoryController().getRootNamespaces())
			if (ns.startsWith(managedRoot)) {
				return managedRoot;
			}
		return "";
	}

	/**
	 * @return the portion of the project namespace that is not managed/governed by a repository. Returns the entire
	 *         namespace if unmanaged.
	 */
	public String getNSExtension() {
		if (getNSRoot().isEmpty())
			return getNamespace();
		String extension = getNamespace().substring(getNSRoot().length());
		if (extension.startsWith("/"))
			extension = extension.substring(1);
		return extension;
	}

	@Override
	public String getNameWithPrefix() {
		return project.getProjectId() + "/" + project.getName();
	}

	@Override
	public ModelNode getParent() {
		return (ModelNode) parent;
	}

	@Override
	public boolean hasChildren() {
		return !getChildren().isEmpty();
	}

	@Override
	public boolean isDeprecated() {
		// return project.isDeprecated();
		return false;
	}

	public boolean isDefaultProject() {
		return this == OtmRegistry.getMainController().getProjectController().getDefaultProject();
	}

	@Override
	public boolean isDeleteable() {
		return false;
	}

	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public boolean isLibraryContainer() {
		return true;
	}

	@Override
	public boolean isNavChild(boolean deep) {
		return true;
	}

	@Override
	public boolean isNavigation() {
		return true;
	}

	public void setNamespace(String namespace) {
		if (namespace != null && !namespace.isEmpty())
			project.setProjectId(namespace);
	}

	@Override
	public boolean isBuiltIn() {
		return project.getProjectId().equals(BuiltInProject.BUILTIN_PROJECT_ID);

	}

	/** Not Applicable for Project Interface *********************/

	@Override
	public Node getType() {
		return null;
	}

	@Override
	public TLEmpty getTLModelObject() {
		return (TLEmpty) getModelObject().getTLModelObj();
	}

	@Override
	public boolean hasChildren_TypeProviders() {
		return getChildren().size() > 0 ? true : false;
	}

	@Override
	public void removeFromLibrary() {
	}

	/**
	 * Remove all project items from this project. NO save, tests or checks.
	 */
	public void removeAllFromTLProject() {
		for (Node lib : getChildren())
			// get past LibraryNavNode and VersionNodes
			if (lib.getLibrary() != null)
				project.remove(lib.getLibrary().getProjectItem());
	}

	@Override
	public boolean isNamedEntity() {
		return false;
	}

	/**
	 * @return the compiler model repository project
	 */
	public Project getTLProject() {
		return project;
	}

	@Override
	public String toString() {
		return project.getName();
	}

	public String getPath() {
		return project.getProjectFile() != null ? project.getProjectFile().toString() : "";
	}

	@Override
	public boolean isEnabled_AddProperties() {
		return false;
	}

	/**
	 * Get the chain that the project item should belong to. Null if no chains with same base namespace are open.
	 * 
	 * @param pi
	 * @return
	 */
	public LibraryChainNode getChain(ProjectItem pi) {
		return getChain(makeChainIdentity(pi));
	}

	public LibraryChainNode getChain(String identity) {
		for (Node n : getChildren())
			if (n instanceof LibraryChainNode) {
				// LOGGER.debug("Does " + ((LibraryChainNode) n).makeChainIdentity() + " = " +
				// identity);
				if (((LibraryChainNode) n).makeChainIdentity().equals(identity))
					return (LibraryChainNode) n;
			}
		return null;
	}

	/**
	 * Also see {@link LibraryChainNode#makeIdentity(String, String)}
	 */
	public String makeChainIdentity(ProjectItem pi) {
		NamespaceHandler nsHandler = NamespaceHandler.getNamespaceHandler(this);
		return LibraryChainNode.makeIdentity(pi.getContent().getName(), pi.getBaseNamespace(),
				nsHandler.getNS_Major(pi.getNamespace()));
	}

	public static String appendExtension(String base, String extension) {
		String ns = base;
		if (extension != null && !extension.isEmpty()) {
			if (base.endsWith("/"))
				ns = base + extension;
			else
				ns = base + "/" + extension;
		}
		return RepositoryNamespaceUtils.normalizeUri(ns);
	}

	public void closeAll() {
		for (Node n : getChildren_New()) {
			n.close();
			getChildren().remove(n);
		}
	}

	/**
	 * @return true if this project contains the library or chain, false otherwise
	 */
	public boolean contains(LibraryInterface li) {
		if (li == null)
			return false;
		for (Node n : getChildren())
			if (n instanceof LibraryNavNode)
				if (((LibraryNavNode) n).contains(li))
					return true;
		return false;
	}

}
