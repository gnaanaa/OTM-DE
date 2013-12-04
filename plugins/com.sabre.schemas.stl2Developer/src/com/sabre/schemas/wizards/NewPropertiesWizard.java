/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.PropertyNodeType;
import com.sabre.schemas.node.properties.PropertyNode;

/**
 * Wizard to allow the user to add element, attribute or indicator properties. Returns via getter
 * the new properties.
 * 
 * @author Agnieszka Janowska
 * 
 */
public class NewPropertiesWizard extends ValidatingWizard implements Cancelable {
    private final static Logger LOGGER = LoggerFactory.getLogger(NewPropertiesWizard.class);

    private NewPropertiesWizardPage page;
    private boolean canceled;
    private final List<PropertyNodeType> enabledTypes = new ArrayList<PropertyNodeType>();
    private static List<PropertyNodeType> enabledTypesDefault = new ArrayList<PropertyNodeType>(
            Arrays.asList(PropertyNodeType.ELEMENT, PropertyNodeType.ATTRIBUTE,
                    PropertyNodeType.INDICATOR));
    private final Node model;
    private ViewerFilter propertyFilter;
    private LibraryNode library; // new nodes need a library to be editable and type assignable.

    /**
     * @param model
     *            - root of copy from tree
     * @param enabledTypes
     *            0 or more Property Node Types to enable.
     */
    public NewPropertiesWizard(final LibraryNode library, final Node model,
            final List<PropertyNodeType> enabledTypes) {
        this.enabledTypes.addAll(enabledTypes);
        this.model = model;
        this.library = library;
        LOGGER.debug("Wizard enabled for: " + enabledTypes);
    }

    @Override
    public void addPages() {
        page = new NewPropertiesWizardPage("Add property", "Copy or create new properties.",
                getValidator(), enabledTypes, library, model);
        page.setPropertyFilter(propertyFilter);
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        canceled = false;
        return true;
    }

    @Override
    public boolean performCancel() {
        canceled = true;
        return true;
    }

    public void run(final Shell shell) {
        final WizardDialog dialog = new WizardDialog(shell, this);
        dialog.setPageSize(SWT.DEFAULT, 350);
        dialog.create();
        dialog.open();
    }

    @Override
    public boolean wasCanceled() {
        return canceled;
    }

    /**
     * @return the last node selected from the property tree.
     */
    public PropertyNode getSelectedNode() {
        return page.getSelectedNode();
    }

    /**
     * @return the list of newly created properties. NOTE: these are not attached to any specific
     *         object or library.
     */
    public List<PropertyNode> getNewProperties() {
        return page.getNewProperties();
    }

    public void setPropertyFilter(final ViewerFilter filter) {
        propertyFilter = filter;

    }

    /**
     * @return the library
     */
    public LibraryNode getLibrary() {
        return library;
    }
}