/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.TLBusinessObject;
import com.sabre.schemas.modelObject.BusinessObjMO;
import com.sabre.schemas.modelObject.ModelObject;
import com.sabre.schemas.node.ComponentNodeType;
import com.sabre.schemas.node.EditNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.properties.Images;
import com.sabre.schemas.properties.Messages;
import com.sabre.schemas.stl2developer.NavigatorMenus;
import com.sabre.schemas.trees.type.ExtensionTreeContentProvider;
import com.sabre.schemas.trees.type.TypeTreeExtensionSelectionFilter;

/*
 * Good references:
 * http://www.eclipse.org/articles/article.php?file=Article-JFaceWizards/index.html
 */
public class NewComponentWizard extends Wizard implements IDoubleClickListener {
    protected Node targetNode = null;

    public NewComponentWizardPage ncPage1;
    private TypeSelectionPage serviceSubjectSelectionPage;
    private WizardDialog dialog;
    public NavigatorMenus libraryTreeView;
    private EditNode editNode = new EditNode();

    private final static Logger LOGGER = LoggerFactory.getLogger(NewComponentWizard.class);

    public NewComponentWizard(Node n) {
        super();
        targetNode = n;
        LOGGER.debug("New Component Wizard created, set targetnode to: " + targetNode.getName());
    }

    @Override
    public void addPages() {
        ImageDescriptor imageDesc = Images.getImageRegistry().getDescriptor("AddComponent");
        ncPage1 = new NewComponentWizardPage("CreateComponent",
                Messages.getString("wizard.newObject.title"), imageDesc, targetNode);
        addPage(ncPage1);

        serviceSubjectSelectionPage = new TypeSelectionPage(
                "Service Subject Selection",
                "Select Service Subject",
                "Select an subject for the service messages. Or go back and finish without creating operations and messages.",
                null, targetNode);
        serviceSubjectSelectionPage.addDoubleClickListener(this);
        // Set the filter to only business objects.
        TLBusinessObject tlbo = new TLBusinessObject();
        ModelObject<?> tlmo = new BusinessObjMO(tlbo);
        serviceSubjectSelectionPage.setTypeSelectionFilter(new TypeTreeExtensionSelectionFilter(
                tlmo));
        serviceSubjectSelectionPage.setTypeTreeContentProvider(new ExtensionTreeContentProvider());
        addPage(serviceSubjectSelectionPage);

    }

    @Override
    public boolean canFinish() {
        // We can finish if an extension facet is selected AND ???
        if (ncPage1.getComponentType().equals(ComponentNodeType.EXTENSION_POINT.getDescription()))
            return true;
        if ((ncPage1.getComponentType().isEmpty()) || ncPage1.getName().isEmpty())
            return false;
        return true;
    }

    @Override
    public boolean performFinish() {
        editNode = createNode();
        return true;
    }

    /**
     * Launch the new component wizard.
     * 
     * @param parentNode
     * @return
     */
    // invoker must instantiate the class first: NewComponent wizard = new NewComponent();
    public EditNode postNewComponentWizard(final Shell shell) {
        dialog = new WizardDialog(shell, this);
        dialog.create();
        dialog.open();
        if (editNode == null || editNode.getName() == null || editNode.getName().isEmpty()) {
            return null;
        }
        return editNode;
    }

    /**
     * Create a node by reading data from the wizard pages.
     * 
     * @return
     */
    private EditNode createNode() {
        if (ncPage1.getComponentType().equals(ComponentNodeType.EXTENSION_POINT.getDescription())) {
            // for Extension Point Facet set name as Undefined regardless of what it was set to.
            // Even if the name was set empty in the dialog, set it again to Undefined.
            editNode.setName("Undefined");
        } else {
            editNode.setName(ncPage1.getName());
        }
        editNode.setDescription(ncPage1.getDescription());
        editNode.setUseType(ncPage1.getComponentType());
        editNode.setLibrary(targetNode.getLibrary());
        editNode.setParent(targetNode);
        editNode.setTLType(serviceSubjectSelectionPage.getSelectedNode());
        return editNode;
    }

    @Override
    public void doubleClick(final DoubleClickEvent event) {
        if (canFinish()) {
            performFinish();
            dialog.close();
        }
    }

}