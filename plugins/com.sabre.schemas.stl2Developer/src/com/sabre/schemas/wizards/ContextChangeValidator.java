/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.wizards;

import com.sabre.schemacompiler.model.TLAdditionalDocumentationItem;
import com.sabre.schemacompiler.model.TLBusinessObject;
import com.sabre.schemacompiler.model.TLContextReferrer;
import com.sabre.schemacompiler.model.TLEquivalent;
import com.sabre.schemacompiler.model.TLExample;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLFacetOwner;
import com.sabre.schemacompiler.model.TLFacetType;
import com.sabre.schemas.properties.Messages;

/**
 * @author Agnieszka Janowska
 * 
 */
public class ContextChangeValidator implements FormValidator {

    private final SimpleNameWizard wizard;
    private final TLContextReferrer referrer;

    public ContextChangeValidator(final TLContextReferrer referrer, final SimpleNameWizard wizard) {
        this.wizard = wizard;
        this.referrer = referrer;
    }

    @Override
    public void validate() throws ValidationException {
        final String context = wizard.getText();
        // final TLContext context = ((TLLibrary)
        // referrer.getOwningLibrary()).getContext(appContext);
        final String contextId = context != null ? context : "";
        if (!canBeChanged(referrer, contextId)) {
            throw new ValidationException(Messages.getString("error.changeContext"));
        }
    }

    private boolean canBeChanged(final TLContextReferrer referrer, final String context) {
        if (referrer.getContext() == null || referrer.getContext().equals(context)) {
            return true;
        }
        if (referrer instanceof TLEquivalent) {
            final TLEquivalent eq = ((TLEquivalent) referrer).getOwningEntity().getEquivalent(
                    context);
            return eq == null;
        }
        if (referrer instanceof TLExample) {
            final TLExample ex = ((TLExample) referrer).getOwningEntity().getExample(context);
            return ex == null;
        }
        if (referrer instanceof TLAdditionalDocumentationItem) {
            final TLAdditionalDocumentationItem doc = ((TLAdditionalDocumentationItem) referrer)
                    .getOwningDocumentation().getOtherDoc(context);
            return doc == null;
        }
        if (referrer instanceof TLFacet) {
            final TLFacet facet = (TLFacet) referrer;
            final TLFacetOwner object = facet.getOwningEntity();
            if (object instanceof TLBusinessObject) {
                final TLBusinessObject tlBo = (TLBusinessObject) object;
                if (facet.getFacetType().equals(TLFacetType.CUSTOM)) {
                    return tlBo.getCustomFacet(context) == null;
                } else if (facet.getFacetType().equals(TLFacetType.QUERY)) {
                    return tlBo.getQueryFacet(context) == null;
                }
            }
        }
        return true;
    }

}