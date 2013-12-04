/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.wizards;

import java.util.ArrayList;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.TLBusinessObject;
import com.sabre.schemas.modelObject.BusinessObjMO;
import com.sabre.schemas.node.INode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.ServiceNode;
import com.sabre.schemas.node.VWA_Node;
import com.sabre.schemas.properties.Messages;
import com.sabre.schemas.trees.type.TypeTreeExtensionSelectionFilter;
import com.sabre.schemas.trees.type.TypeTreeIdReferenceTypeOnlyFilter;
import com.sabre.schemas.trees.type.TypeTreeSimpleTypeOnlyFilter;
import com.sabre.schemas.trees.type.TypeTreeVWASimpleTypeOnlyFilter;

/**
 * Wizard to allow user to select a type for the passed node objects.
 * 
 * Uses the passed node or first node of the list to set filters for simple/complex/vwa types.
 * 
 * @author Dave Hollander, Agnieszka Janowska
 * 
 */
public class TypeSelectionWizard extends Wizard implements IDoubleClickListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(TypeSelectionWizard.class);

    private Node curNode = null;
    private ArrayList<Node> curNodeList = null;
    private ArrayList<Node> setNodeList = new ArrayList<Node>();

    private TypeSelectionPage selectionPage;
    private WizardDialog dialog;
    private boolean dontFinish = false; // use only as converting to action class.

    /**
     * Type selection wizard to select a node to assign as a type.
     * 
     * @param nodeList
     *            is a list of nodes to assign the selected type to. The nodes are examined to
     *            select tree view filters.
     */
    public TypeSelectionWizard(ArrayList<Node> nodeList) {
        // this((Node) null);
        super();
        curNodeList = nodeList;
        // LOGGER.debug("Type Selection Wizard initialized for nodelist.");
    }

    /**
     * Type selection wizard to select a node to assign as a type.
     * 
     * @param n
     *            the node to assign to. Type of node selects the filter to use on tree view.
     */
    public TypeSelectionWizard(final Node n) {
        super();
        curNodeList = new ArrayList<Node>();
        if (n != null && n.isEditable())
            curNodeList.add(n);
        // LOGGER.debug("Type Selection Wizard initialized for node.");
    }

    @Override
    public void addPages() {
        // LOGGER.debug("Adding Selection Page.");

        // Make sure all the nodes are non-null and editable
        // and set lowest common denominator: simple, vwa and complex.
        boolean service = false;
        boolean simple = false;
        boolean vwa = false;
        boolean idReference = false;

        if (curNodeList != null) {
            for (Node n : curNodeList) {
                if (n != null && n.isEditable()) {
                    setNodeList.add(0, n); // why in front of list?
                    if (n.getOwningComponent() instanceof VWA_Node)
                        vwa = true;
                    else if (n.isOnlySimpleTypeUser())
                        simple = true;
                    else if (n instanceof ServiceNode)
                        service = true;
                    else if (n.isID_Reference())
                        idReference = true;
                }
            }
        }
        // Exit when selected nodes are all read-only
        if (setNodeList.size() <= 0)
            return; // TODO - How to exit wizard?

        // Create a type selection page
        String pageName = Messages.getString("wizard.typeSelection.pageName.component");
        String title = Messages.getString("wizard.typeSelection.title.component");
        String description = Messages.getString("wizard.typeSelection.description.component");
        if (service) {
            pageName = Messages.getString("wizard.typeSelection.pageName.service");
            title = Messages.getString("wizard.typeSelection.title.service");
            description = Messages.getString("wizard.typeSelection.description.service");
        }
        selectionPage = new TypeSelectionPage(pageName, title, description, null, setNodeList);

        // Set the filters based on type of passed node.
        if (simple)
            selectionPage.setTypeSelectionFilter(new TypeTreeSimpleTypeOnlyFilter());
        else if (vwa)
            selectionPage.setTypeSelectionFilter(new TypeTreeVWASimpleTypeOnlyFilter());
        else if (service)
            selectionPage.setTypeSelectionFilter(new TypeTreeExtensionSelectionFilter(
                    new BusinessObjMO(new TLBusinessObject())));
        else if (idReference)
            selectionPage.setTypeSelectionFilter(new TypeTreeIdReferenceTypeOnlyFilter());

        selectionPage.addDoubleClickListener(this);
        addPage(selectionPage);
    }

    // According to the link, this belongs in the wizard not page
    // http://dev.eclipse.org/viewcvs/viewvc.cgi/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/wizard/Snippet047WizardWithLongRunningOperation.java?view=markup
    @Override
    public boolean canFinish() {
        return selectionPage.getSelectedNode() == null ? false : true;
    }

    @Override
    public void doubleClick(final DoubleClickEvent event) {
        if (canFinish()) {
            performFinish();
            dialog.close();
        }
    }

    /**
     * @return the setNodeList which is the filtered copy of the source list
     */
    public ArrayList<Node> getList() {
        return setNodeList;
    }

    /**
     * @return the setNodeList which is the filtered copy of the source list
     */
    public Node getSelection() {
        return selectionPage == null ? null : selectionPage.getSelectedNode();
    }

    // This code is in the AssignTypeAction.execute().
    // TODO - eliminate after refactoring other invokers.
    @Override
    public boolean performFinish() {
        if (getSelection() == null)
            return false;
        if (dontFinish)
            return true;

        INode sn = selectionPage.getSelectedNode();
        if (setNodeList != null) {
            for (INode cn : selectionPage.getCurNodeList()) {
                LOGGER.debug("Assigning " + sn.getName() + " to list node " + cn.getName());
                cn.setAssignedType((Node) sn);
            }
        } else if (curNode != null) {
            LOGGER.debug("Assigning " + selectionPage.getSelectedNode() + " to node "
                    + curNode.getName());
            curNode.setAssignedType(selectionPage.getSelectedNode());
        } else
            return false;
        return true;
    }

    /**
     * Run the wizard but DO NOT assign the types. Usage if
     * (wizard.run(OtmRegistry.getActiveShell())) { AssignTypeAction.execute(wizard.getList(),
     * wizard.getSelection()); }
     * 
     * @return
     */
    public boolean run(final Shell shell) {
        if (curNode == null && curNodeList == null) {
            LOGGER.warn("Early Exit - no node(s) to post.");
            return false; // DO Nothing
        }

        dontFinish = true;
        dialog = new WizardDialog(shell, this);
        dialog.setPageSize(700, 600);
        dialog.create();
        dialog.open();
        return true;
    }

    /**
     * Run the wizard AND do assign the type.
     * 
     * @param assign
     *            - if true assign the type to the node on finish
     * @return
     */
    public boolean run(final Shell shell, boolean assign) {
        if (curNode == null && curNodeList == null) {
            LOGGER.warn("Early Exit - no node(s) to post.");
            return false; // DO Nothing
        }

        dontFinish = true;
        if (assign)
            dontFinish = false;
        dialog = new WizardDialog(shell, this);
        dialog.setPageSize(700, 600);
        dialog.create();
        dialog.open();
        return true;
    }

}