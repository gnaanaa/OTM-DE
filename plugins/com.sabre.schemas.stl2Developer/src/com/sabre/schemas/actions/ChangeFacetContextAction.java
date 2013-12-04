/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.TLContextReferrer;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.properties.ExternalizedStringProperties;
import com.sabre.schemas.properties.StringProperties;
import com.sabre.schemas.stl2developer.MainWindow;
import com.sabre.schemas.stl2developer.OtmRegistry;
import com.sabre.schemas.views.ContextsView;

/**
 * @author Agnieszka Janowska
 * 
 */
public class ChangeFacetContextAction extends OtmAbstractAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeFacetContextAction.class);
    private static StringProperties propsDefault = new ExternalizedStringProperties(
            "action.setContext");

    /**
	 *
	 */
    public ChangeFacetContextAction(final MainWindow mainWindow) {
        super(mainWindow, propsDefault);
    }

    public ChangeFacetContextAction(final MainWindow mainWindow, final StringProperties props) {
        super(mainWindow, props);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        final ContextsView view = OtmRegistry.getContextsView();
        final Node selected = getMainController().getCurrentNode_NavigatorView();
        if (selected != null && selected.isFacet()) {
            final Object model = selected.getModelObject().getTLModelObj();
            if (model instanceof TLContextReferrer) {
                view.getContextController().changeContext((TLContextReferrer) model);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        LOGGER.debug("isEnabled for " + getMainController().getCurrentNode_NavigatorView());
        if (getMainController().getCurrentNode_NavigatorView().isQueryFacet())
            return true;
        return (getMainController().getCurrentNode_NavigatorView().isCustomFacet()) ? true : false;
    }
}