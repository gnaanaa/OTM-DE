/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opentravel.schemas.node;

import java.util.List;

import org.opentravel.schemacompiler.model.TLAdditionalDocumentationItem;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationItem;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;

/**
 * Handles access to the values in a {@TLDocumentation} object.
 * 
 * @author Dave Hollander
 * 
 */
// TODO - replace users of ModelObject documentation methods
// TODO - consider delete
// TODO - assure documentationNode controller uses these.
// TODO - node expose a getDocumentation method that is a facade to getOrNewTL()
//
public class DocumentationHandler {
	// private final static Logger LOGGER = LoggerFactory.getLogger(DocumentationHandler.class);

	protected static String emptyIfNull(final String string) {
		return string == null ? "" : string;
	}

	protected Node owner;

	/**
	 * Create a documentation handler for the owning node.
	 * 
	 * @param owner
	 *            <b>must</b> not be null and <b>must</b> be a documentation owner
	 */
	public DocumentationHandler(Node owner) {
		assert owner != null;
		assert owner.isDocumentationOwner();
		this.owner = owner;
	}

	public void addDeprecation(String string) {
		getOrNewTL().addDeprecation(newDI(string));
	}

	/**
	 * Add (append) to the description string
	 * 
	 * @param string
	 */
	public void addDescription(final String string) {
		TLDocumentation tld = getOrNewTL();
		if (tld.getDescription() == null || tld.getDescription().isEmpty())
			tld.setDescription(string);
		else
			tld.setDescription(tld.getDescription() + " " + string);
	}

	public void addImplementer(String string) {
		getOrNewTL().addImplementer(newDI(string));
	}

	public void addMoreInfo(String string) {
		getOrNewTL().addMoreInfo(newDI(string));
	}

	/**
	 * Add (append) to the other documentation. Forced to only be one item using the owner's library's default context
	 * id
	 * 
	 * @param string
	 */
	public void addOther(String string) {
		TLAdditionalDocumentationItem adi = getOrNewTL().getOtherDoc(owner.getLibrary().getDefaultContextId());
		if (adi == null)
			getOrNewTL().addOtherDoc(newADI(string));
		else {
			if (adi.getText().isEmpty())
				adi.setText(string);
			else
				adi.setText(adi.getText() + " " + string);
		}
	}

	public void addReference(String string) {
		getOrNewTL().addReference(newDI(string));
	}

	/**
	 * @return deprecation string or null
	 */
	public String getDeprecation(final int i) {
		return getTL() != null ? get(getTL().getDeprecations(), i) : null;
	}

	/**
	 * @return Description string or empty string
	 */
	public String getDescription() {
		final TLDocumentation tld = getTL();
		return (tld == null || tld.getDescription() == null) ? "" : tld.getDescription();
	}

	/**
	 * @return Developers documentation (implementers) string or null
	 */
	public String getImplementer(final int i) {
		return getTL() != null ? get(getTL().getImplementers(), i) : null;
	}

	/**
	 * @return Developers documentation (implementers) string or null
	 */
	public List<TLDocumentationItem> getImplementers() {
		return getTL() != null ? getTL().getImplementers() : null;
	}

	/**
	 * @return MoreInfo documentation string or null
	 */
	public String getMoreInfo(final int i) {
		return getTL() != null ? get(getTL().getMoreInfos(), i) : null;
	}

	/**
	 * @return Other documentation string for the library's default context or null
	 */
	public String getOther() {
		if (getTL() == null)
			return null;
		TLAdditionalDocumentationItem adi = getTL().getOtherDoc(owner.getLibrary().getDefaultContextId());
		if (adi == null)
			return null;
		return adi.getText();
	}

	/**
	 * @return Reference documentation string or null
	 */
	public String getReference(final int i) {
		return getTL() != null ? get(getTL().getReferences(), i) : null;
	}

	/**
	 * Save string as description, replacing existing string if any.
	 * 
	 * @param string
	 */
	public void setDescription(final String string) {
		getOrNewTL().setDescription(string);
	}

	/**
	 * Set value if it exists, create new one if it does not.
	 */
	public void setImplementer(final String string, final int index) {
		setDocItem(getOrNewTL().getImplementers(), string, index);
	}

	/**
	 * Set value if it exists, create new one if it does not.
	 */
	public void setDeprecation(final String string, final int index) {
		setDocItem(getOrNewTL().getDeprecations(), string, index);
	}

	/**
	 * Set value if it exists, create new one if it does not.
	 */
	public void setMoreInfo(final String string, final int index) {
		setDocItem(getOrNewTL().getMoreInfos(), string, index);
	}

	/**
	 * Set value if it exists, create new one if it does not.
	 */
	public void setOther(final String value) {
		TLAdditionalDocumentationItem adi = getOrNewTL().getOtherDoc(owner.getLibrary().getDefaultContextId());
		if (adi == null)
			getOrNewTL().addOtherDoc(newADI(value));
		else
			adi.setText(value);
	}

	/**
	 * Set value if it exists, create new one if it does not.
	 */
	public void setReference(final String string, final int index) {
		setDocItem(getOrNewTL().getReferences(), string, index);
	}

	/**
	 */
	private String get(List<TLDocumentationItem> docs, final int index) {
		return (docs == null || docs.size() <= index) ? null : docs.get(index).getText();
	}

	/**
	 * Get the TLDocumentation object. Creates one if it didn't exist.
	 * 
	 * @return new or pre-existing TLDocumentation object
	 */
	protected TLDocumentation getOrNewTL() {
		TLDocumentation tld = getTL();
		if (getTL() == null) {
			tld = new TLDocumentation();
			((TLDocumentationOwner) owner.getTLModelObject()).setDocumentation(tld);
			// tld.setOwner((TLDocumentationOwner) owner.getTLModelObject());
		}
		return tld;
	}

	private TLDocumentation getTL() {
		if (owner.isDocumentationOwner())
			return ((TLDocumentationOwner) owner.getTLModelObject()).getDocumentation();
		return null;
	}

	/**
	 * @return newly created additional documentation item with text set to passed string.
	 *         <p>
	 *         <b>Must</b> be added to TLDocumentation item by caller.
	 */
	private TLAdditionalDocumentationItem newADI(String string) {
		TLAdditionalDocumentationItem adi = new TLAdditionalDocumentationItem();
		adi.setContext(owner.getLibrary().getDefaultContextId());
		adi.setText(string);
		return adi;
	}

	/**
	 * @return newly created documentation item with text set to passed string.
	 *         <p>
	 *         <b>Must</b> be added to TLDocumentation item by caller.
	 */
	private TLDocumentationItem newDI(String string) {
		TLDocumentationItem di = new TLDocumentationItem();
		di.setText(string);
		return di;
	}

	private void setDocItem(List<TLDocumentationItem> docs, final String string, final int index) {
		if (docs == null || docs.size() <= index)
			docs.add(index, newDI(string));
		else
			docs.get(index).setText(string);
	}

}
