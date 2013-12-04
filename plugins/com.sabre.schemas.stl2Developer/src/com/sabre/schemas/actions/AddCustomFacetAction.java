/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import com.sabre.schemas.properties.ExternalizedStringProperties;
import com.sabre.schemas.properties.StringProperties;

/**
 * @author Agnieszka Janowska
 * 
 */
public class AddCustomFacetAction extends OtmAbstractAction {
    private static StringProperties propsDefault = new ExternalizedStringProperties(
            "action.addCustom");

    /**
	 *
	 */
    public AddCustomFacetAction() {
        super(propsDefault);
    }

    public AddCustomFacetAction(final StringProperties props) {
        super(props);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        mc.addCustomFacet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return (mc.getCurrentNode_NavigatorView().getOwningComponent().isBusinessObject()) ? true
                : false;
    }

}