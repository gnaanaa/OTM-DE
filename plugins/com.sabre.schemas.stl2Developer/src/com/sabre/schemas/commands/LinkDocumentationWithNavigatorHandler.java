/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.commands;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.sabre.schemas.stl2developer.OtmRegistry;
import com.sabre.schemas.views.DocumentationView;

/**
 * 
 * @author Agnieszka Janowska
 * 
 */
public class LinkDocumentationWithNavigatorHandler extends OtmAbstractHandler {
    public static String COMMAND_ID = "com.sabre.schemas.commands.linkDocumentationWithNav";

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        Command command = event.getCommand();
        boolean oldValue = HandlerUtil.toggleCommandState(command);
        final DocumentationView view = OtmRegistry.getDocumentationView();
        if (view != null) {
            view.setListening(!oldValue);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.commands.OtmHandler#getID()
     */
    @Override
    public String getID() {
        return COMMAND_ID;
    }

}
