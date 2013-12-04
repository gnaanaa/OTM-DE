/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Shell;

import com.sabre.schemas.properties.Messages;
import com.sabre.schemas.properties.PropertyType;
import com.sabre.schemas.properties.StringProperties;

/**
 * Allows setting the name. used for aliases, enums. Use to be used for context -- being changed
 * (4/20/12 dmh)
 * 
 * @author Agnieszka Janowska
 * 
 */
public class SimpleNameWizard extends ValidatingWizard implements ModifyListener, Cancelable {

    private String fieldLabel = "Name:";
    private String fieldToolTip = "Name of the new property";
    private SimpleNameWizardPage page;
    private String text;
    private String[] names;
    private final List<String> availableValues = new ArrayList<String>();
    private boolean canceled;
    private String defaultValue;
    private int nameCount = 1; // number of names to allow them to enter
    private String pageName = "Name";
    private String title = "Enter new names";

    public SimpleNameWizard() {
    }

    public SimpleNameWizard(String messageProperties) {
        pageName = Messages.getString(messageProperties + ".pageName");
        title = Messages.getString(messageProperties + ".title");
    }

    /**
     * Wizard to enter count names.
     * 
     * @param cnt
     */
    public SimpleNameWizard(final StringProperties props) {
        this.setFieldLabel(props.get(PropertyType.TEXT));
        this.setFieldToolTip(props.get(PropertyType.TOOLTIP));
    }

    /**
     * Initialize wizard allowing numFields entries. Use getNames() to retrieve list of entries.
     * 
     * @param props
     * @param numFields
     */
    public SimpleNameWizard(final StringProperties props, int numFields) {
        this.setFieldLabel(props.get(PropertyType.TEXT));
        this.setFieldToolTip(props.get(PropertyType.TOOLTIP));
        if (numFields > nameCount)
            nameCount = numFields;
        title = "Enter new names";
    }

    public SimpleNameWizard(final StringProperties props, final List<String> availableValues) {
        this(props);
        this.availableValues.addAll(availableValues);
    }

    @Override
    public void addPages() {
        if (availableValues.isEmpty()) {
            if (nameCount > 1)
                page = new SimpleNameWizardPage(pageName, title, nameCount, getValidator());
            else
                page = new SimpleNameWizardPage(pageName, title, getValidator());
        } else {
            page = new SimpleNameComboWizardPage("Edit property",
                    "Choose value of the new property", getValidator(), availableValues);
        }
        page.setFieldLabel(fieldLabel);
        page.setFieldToolTip(fieldToolTip);
        page.setDefault(defaultValue);
        page.addModifyListener(this);
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        canceled = false;
        names = page.getNames();
        return true;
    }

    @Override
    public boolean performCancel() {
        canceled = true;
        return true;
    }

    public void run(final Shell shell) {
        final WizardDialog dialog = new WizardDialog(shell, this);
        dialog.setPageSize(SWT.DEFAULT, 40 + 25 * nameCount);
        dialog.create();
        dialog.open();
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @return the fieldLabel
     */
    public String getFieldLabel() {
        return fieldLabel;
    }

    /**
     * @param fieldLabel
     *            the fieldLabel to set
     */
    public void setFieldLabel(final String fieldLabel) {
        this.fieldLabel = fieldLabel;
    }

    /**
     * @return the fieldToolTip
     */
    public String getFieldToolTip() {
        return fieldToolTip;
    }

    /**
     * @param fieldToolTip
     *            the fieldToolTip to set
     */
    public void setFieldToolTip(final String fieldToolTip) {
        this.fieldToolTip = fieldToolTip;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
     */
    @Override
    public void modifyText(final ModifyEvent e) {
        text = page.getText();
    }

    /**
     * @return
     */
    @Override
    public boolean wasCanceled() {
        return canceled;
    }

    public void setDefault(final String def) {
        defaultValue = def;
    }

    public String[] getNames() {
        return names;
    }
}