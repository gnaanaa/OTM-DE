/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.wizards;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemas.node.EditNode;
import com.sabre.schemas.node.Node;

/**
 * @author Agnieszka Janowska
 * 
 */
public class NewNodeNameValidator implements FormValidator {
    private final static Logger LOGGER = LoggerFactory.getLogger(NewNodeNameValidator.class);

    private final SimpleNameWizard wizard;
    private final Node componentNode;
    private final String error;

    /**
     * @param roleFacet
     * @param wizard
     */
    public NewNodeNameValidator(final Node roleFacet, final SimpleNameWizard wizard,
            final String error) {

        componentNode = roleFacet;
        this.wizard = wizard;
        this.error = error;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.wizards.FormValidator#validate()
     */
    // FIXME - does not work on multi-field implementation of wizard.
    @Override
    public void validate() throws ValidationException {
        // LOGGER.debug("Validator ready to run on: " + componentNode.getName() + " for value: "
        // + wizard.getText());
        final String value = wizard.getText();
        final EditNode n = new EditNode(value);
        n.setLibrary(componentNode.getLibrary());
        if (!componentNode.isFacetUnique(n)) {
            throw new ValidationException(error);
        }
    }

}