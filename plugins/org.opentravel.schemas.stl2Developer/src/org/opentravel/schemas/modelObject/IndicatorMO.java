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

import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLIndicatorOwner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndicatorMO extends ModelObject<TLIndicator> {
	static final Logger LOGGER = LoggerFactory.getLogger(IndicatorMO.class);

	public IndicatorMO(final TLIndicator obj) {
		super(obj);
	}

	@Override
	public void delete() {
		if (getTLModelObj().getOwner() != null) {
			getTLModelObj().getOwner().removeIndicator(getTLModelObj());
		}
	}

	@Override
	public void addToTLParent(ModelObject<?> parentMO, int index) {
		if (parentMO.getTLModelObj() instanceof TLIndicatorOwner) {
			((TLIndicatorOwner) parentMO.getTLModelObj()).addIndicator(index, getTLModelObj());
		}
	}

	@Override
	public void addToTLParent(final ModelObject<?> parentMO) {
		if (parentMO.getTLModelObj() instanceof TLIndicatorOwner) {
			((TLIndicatorOwner) parentMO.getTLModelObj()).addIndicator(getTLModelObj());
		}
	}

	@Override
	public void removeFromTLParent() {
		if (getTLModelObj().getOwner() != null) {
			getTLModelObj().getOwner().removeIndicator(getTLModelObj());
		}
	}

	@Override
	public TLIndicator getTLModelObj() {
		return srcObj;
	}

	/**
	 * NOTE: as of 5/9/2012 this count includes inherited properties!
	 */
	protected int indexOf() {
		final TLIndicator thisProp = getTLModelObj();
		return thisProp.getOwner().getIndicators().indexOf(thisProp);
	}

	/**
	 * Move if you can, return false if you can not.
	 * 
	 * @return
	 */
	@Override
	public boolean moveUp() {
		if (indexOf() > 0) {
			getTLModelObj().moveUp();
			return true;
		}
		return false;
	}

	@Override
	public boolean moveDown() {
		if (indexOf() + 1 < getTLModelObj().getOwner().getIndicators().size()) {
			getTLModelObj().moveDown();
			return true;
		}
		return false;
	}

	public void setToElement(boolean state) {
		srcObj.setPublishAsElement(state);
	}

}
