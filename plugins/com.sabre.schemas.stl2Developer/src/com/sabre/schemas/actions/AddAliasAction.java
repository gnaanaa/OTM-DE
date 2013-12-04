/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import com.sabre.schemas.node.AliasNode;
import com.sabre.schemas.node.ComponentNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.properties.ExternalizedStringProperties;
import com.sabre.schemas.properties.Messages;
import com.sabre.schemas.properties.StringProperties;
import com.sabre.schemas.stl2developer.DialogUserNotifier;
import com.sabre.schemas.stl2developer.MainWindow;
import com.sabre.schemas.stl2developer.OtmRegistry;
import com.sabre.schemas.wizards.NewNodeNameValidator;
import com.sabre.schemas.wizards.SimpleNameWizard;

/**
 * @author Agnieszka Janowska
 * 
 */
public class AddAliasAction extends OtmAbstractAction {
    private static StringProperties propsDefault = new ExternalizedStringProperties(
            "action.addAlias");

    /**
	 *
	 */
    public AddAliasAction(final MainWindow mainWindow) {
        super(mainWindow, propsDefault);
    }

    public AddAliasAction(final MainWindow mainWindow, final StringProperties props) {
        super(mainWindow, props);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        addAlias();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        Node cur = getMainController().getCurrentNode_NavigatorView().getOwningComponent();
        if (cur.isBusinessObject() || cur.isCoreObject())
            if (cur.getChain() == null)
                return true;
            else if (cur.getChain().getHead().isMajorVersion())
                return true;
            else if (cur.getChain().getHead().isMinorVersion())
                return true;
        return false;
        // return (cur.isBusinessObject() || cur.isCoreObject()) ? true : false;
    }

    // TODO - move this to a command
    public void addAlias() {
        Node current = mc.getCurrentNode_NavigatorView();
        current = current.getOwningComponent();
        if (current != null && (current.isBusinessObject() || current.isCoreObject())) {
            final SimpleNameWizard wizard = new SimpleNameWizard(new ExternalizedStringProperties(
                    "wizard.aliasName"));
            final ComponentNode cn = (ComponentNode) current;
            wizard.setValidator(new NewNodeNameValidator(cn, wizard, Messages
                    .getString("error.aliasName")));
            wizard.run(OtmRegistry.getActiveShell());
            if (!wizard.wasCanceled()) {
                new AliasNode(current, wizard.getText());
                mc.refresh(current);
            }
        } else {
            DialogUserNotifier
                    .openWarning("Warning",
                            "New alias cannot be added because aliases are only for Business and Core Objects");
        }
    }

}