/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import com.sabre.schemas.properties.StringProperties;
import com.sabre.schemas.stl2developer.DialogUserNotifier;
import com.sabre.schemas.stl2developer.MainWindow;

/**
 * @author Agnieszka Janowska
 * 
 */
public class NewModelAction extends OtmAbstractAction {

    /**
	 *
	 */
    public NewModelAction(final MainWindow mainWindow, final StringProperties props) {
        super(mainWindow, props);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        boolean okey = true;
        okey = DialogUserNotifier.openConfirm("New Model",
                "Not implemented. Please create new project or library.");
        // "Are you sure you want to close existing model and create a new one? "
        // + "Closing will save and close all the currently open libraries");
        // if (okey) {
        // mc.getModelController().close();
        // }
    }

}