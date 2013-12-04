/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.wizards;

import com.sabre.schemacompiler.model.TLFacetType;
import com.sabre.schemas.node.EditNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.properties.Messages;

/**
 * @author Agnieszka Janowska
 * 
 */
public class NewFacetValidator implements FormValidator {

    private final Node componentNode;
    private final NewFacetWizard wizard;
    private final TLFacetType facetType;

    public NewFacetValidator(final Node componentNode, final TLFacetType facetType,
            final NewFacetWizard wizard) {
        this.componentNode = componentNode;
        this.facetType = facetType;
        this.wizard = wizard;
    }

    @Override
    public void validate() throws ValidationException {
        final EditNode n = new EditNode(wizard.getName());
        n.setLibrary(componentNode.getLibrary());
        if (!componentNode.isFacetUnique(n)) {
            throw new ValidationException(Messages.getString("error.newFacet"));
        }
        if (TLFacetType.CUSTOM.equals(facetType) && isEmpty(wizard.getName())) {
            throw new ValidationException(Messages.getString("error.newFacet.custom.empty"));
        }
    }

    private boolean isEmpty(String name) {
        if (name != null)
            return name.isEmpty();
        return true;
    }

}
