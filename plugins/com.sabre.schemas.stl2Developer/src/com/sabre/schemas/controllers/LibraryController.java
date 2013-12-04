/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.controllers;

import java.util.Collection;
import java.util.List;

import com.sabre.schemacompiler.repository.ProjectItem;
import com.sabre.schemacompiler.repository.RepositoryItemState;
import com.sabre.schemas.node.INode;
import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.node.ModelNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.ProjectNode;

/**
 * Central place for all the library related actions. Note that the global model actions are
 * controlled by {@link ModelController}
 * 
 * @author Agnieszka Janowska
 * 
 */
public interface LibraryController {

    /**
     * Creates new library in the project currently selected in the navigator view.
     * 
     * @return
     * 
     */
    LibraryNode createLibrary();

    /**
     * Change namespace. If the namespace is shared, the user is asked if they want to change all,
     * one or none.
     */
    void changeNamespace(LibraryNode library, String namespace);

    void changeNamespaceExtension(LibraryNode library, String namespace);

    /**
     * @return list of open user (TLLibrary) libraries. Returns empty list if there are no user
     *         libraries.
     */
    List<LibraryNode> getUserLibraries();

    /**
     * Get all libraries assigned to a namespace.
     * 
     * @param namespace
     * @return list of libraries assigned to the namespace.
     */

    List<LibraryNode> getLibrariesWithNamespace(String namespace);

    /**
     * Opens already existing library using a file selection dialog and adds it to the model.
     * 
     * @param model
     *            {@link ModelNode} to which attach the opened library
     */
    void openLibrary(ProjectNode model);

    void openLibrary(INode model);

    // /**
    // * Opens the library returns a list of TL libraries representing the file and its dependents.
    // * Note, the list may contain libraries that are already in the model.
    // *
    // * @return list of Abstract Libraries related to the file and its imports.
    // * @throws LibraryLoaderException
    // */
    // public List<AbstractLibrary> open(final String filePath) throws LibraryLoaderException;

    /**
     * Saves, closes and removes a library from its model
     * 
     * @param library
     *            {@link LibraryNode} to be closed
     */
    void closeLibrary(LibraryNode library);

    /**
     * Saves, closes and removes the given libraries from their models
     * 
     * @param libraries
     *            list of {@link LibraryNode}s to be closed
     */
    void closeLibraries(List<LibraryNode> libraries);

    // /**
    // * Saves, closes and removes all the user defined libraries from the given model
    // *
    // * @param model
    // * {@link ModelNode} of the libraries to be closed
    // */
    // void closeAllLibraries(INode model);

    /**
     * Saves the given library to the physical file
     * 
     * @param library
     *            {@link LibraryNode} to be saved
     * @param quiet
     *            be quite, do not notify user of happy path
     * @return false if library was not saved successfully
     */
    boolean saveLibrary(LibraryNode library, boolean quiet);

    /**
     * Saves the given libraries to the physical files
     * 
     * @param libraries
     *            list of {@link LibraryNode}s to be saved
     * @param quiet
     *            be quite, do not notify user of happy path
     * @return false in case one if libraries was not saved successfully
     */
    boolean saveLibraries(List<LibraryNode> libraries, boolean quiet);

    /**
     * Saves all the user defined libraries in the given model
     * 
     * @param quiet
     *            be quite, do not notify user of happy path
     * @return false in case one if libraries was not saved successfully
     */
    boolean saveAllLibraries(boolean quiet);

    /**
     * Remove the library from the parent project.
     * 
     * @param libraries
     */
    void remove(Collection<? extends Node> libraries);

    /**
     * Update the editable status for all libraries.
     */
    void updateLibraryStatus();

    /**
     * @param libary
     *            with status and {@link ProjectItem} with {@link RepositoryItemState}
     * @return status base on {@link LibraryNode#getStatus()} and {@link ProjectItem#getState()}.
     */
    String getLibraryStatus(LibraryNode libary);

}