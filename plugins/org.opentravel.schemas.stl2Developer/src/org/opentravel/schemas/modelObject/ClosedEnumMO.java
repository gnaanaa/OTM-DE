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
package org.opentravel.schemas.modelObject;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAbstractEnumeration;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.utils.StringComparator;

public class ClosedEnumMO extends ModelObject<TLClosedEnumeration> {

	public ClosedEnumMO(final TLClosedEnumeration obj) {
		super(obj);
	}

	@Override
	public boolean addChild(TLModelElement value) {
		if (value instanceof TLEnumValue)
			addLiteral((TLEnumValue) value);
		else
			return false;
		return true;
	}

	public void addLiteral(final TLEnumValue value) {
		getTLModelObj().addValue(value);
	}

	public void addLiteral(final TLEnumValue value, int index) {
		getTLModelObj().addValue(index, value);
	}

	@Override
	public void delete() {
		if (srcObj.getOwningLibrary() != null)
			srcObj.getOwningLibrary().removeNamedMember(srcObj);
	}

	@Override
	public List<TLEnumValue> getChildren() {
		return getTLModelObj().getValues();
	}

	/**
	 * @return the TLClosedEnumeration that extends the passed enum if any
	 */
	public TLAbstractEnumeration getExtension(TLAbstractEnumeration ce) {
		return ce.getExtension() != null ? ce = (TLAbstractEnumeration) ce.getExtension().getExtendsEntity() : null;
	}

	// /**
	// * @see org.opentravel.schemas.modelObject.ModelObject#getExtendsType()
	// */
	// @Override
	// public String getExtendsType() {
	// TLExtension tlExtension = getTLModelObj().getExtension();
	// String extendsTypeName = "";
	//
	// if (tlExtension != null) {
	// if (tlExtension.getExtendsEntity() != null)
	// extendsTypeName = tlExtension.getExtendsEntity().getLocalName();
	// else
	// extendsTypeName = "--base type can not be found--";
	// }
	// return extendsTypeName;
	// }

	@Override
	public String getExtendsTypeNS() {
		TLExtension tlExtension = getTLModelObj().getExtension();
		return tlExtension == null || tlExtension.getExtendsEntity() == null ? "" : tlExtension.getExtendsEntity()
				.getNamespace();
	}

	/**
	 * @see org.opentravel.schemas.modelObject.ModelObject#getInheritedChildren()
	 */
	@Override
	public List<?> getInheritedChildren() {
		final List<TLModelElement> inheritedKids = new ArrayList<TLModelElement>();
		inheritedKids.addAll(getInheritedValues());
		// The Codegen utils also insert non-inherited values
		// inheritedKids.addAll(EnumCodegenUtils.getInheritedValues(getTLModelObj()));
		return inheritedKids;
	}

	/**
	 * @return list of values found in previous versions of this open enum
	 */
	private List<TLEnumValue> getInheritedValues() {
		List<TLEnumValue> valueList = new ArrayList<TLEnumValue>();
		// TLClosedEnumeration tlOE = getTLModelObj();
		TLAbstractEnumeration oe = getExtension(getTLModelObj());
		while (oe != null) {
			valueList.addAll(oe.getValues());
			if (oe.getExtension() != null)
				oe = getExtension(oe);
			else
				oe = null;
		}
		return valueList;
	}

	@Override
	public TLClosedEnumeration getTLModelObj() {
		return srcObj;
	}

	@Override
	public NamedEntity getTLBase() {
		return srcObj.getExtension() != null ? srcObj.getExtension().getExtendsEntity() : null;
	}

	// /**
	// * Is this Enum extended by <i>extension</i>? VWA does not use an TL extension handler. Use the parentType
	// */
	// @Override
	// public boolean isExtendedBy(NamedEntity extension) {
	// if (extension == null || !(extension instanceof TLClosedEnumeration))
	// return false;
	// if (extension.getValidationIdentity() == null)
	// return false;
	//
	// if (getTLModelObj() != null)
	// if (getTLModelObj().getExtension() != null)
	// if (getTLModelObj().getExtension().getValidationIdentity() != null)
	// return getTLModelObj().getExtension().getExtendsEntity() == extension;
	// return false;
	// }

	@Override
	public boolean isSimpleAssignable() {
		return true;
	}

	/**
	 * @see org.opentravel.schemas.modelObject.ModelObject#setExtendsType(org.opentravel.schemas.modelObject.ModelObject)
	 */
	@Override
	public void setExtendsType(ModelObject<?> mo) {
		if (mo == null) {
			getTLModelObj().setExtension(null);

		} else {
			TLExtension tlExtension = getTLModelObj().getExtension();

			if (tlExtension == null) {
				tlExtension = new TLExtension();
				getTLModelObj().setExtension(tlExtension);
			}
			tlExtension.setExtendsEntity((NamedEntity) mo.getTLModelObj());
		}
	}

	// @Override
	// public boolean setName(final String name) {
	// getTLModelObj().setName(name);
	// return true;
	// }

	@Override
	public void sort() {
		TLClosedEnumeration eClosed = getTLModelObj();
		eClosed.sortValues(new StringComparator<TLEnumValue>() {

			@Override
			protected String getString(TLEnumValue object) {
				return object.getLiteral();
			}
		});
	}

}
