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

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLSimple;

/**
 * Class for top level page1 types.
 * 
 * @author Dave Hollander
 * 
 */
public class SimpleMO extends ModelObject<TLSimple> {

    public SimpleMO(final TLSimple obj) {
        super(obj);
        if (obj.getParentType() != null) {
            setTLType(obj.getParentType());
        }
    }

    @Override
    public void delete() {
        if (getTLModelObj().getOwningLibrary() != null) {
            getTLModelObj().getOwningLibrary().removeNamedMember(getTLModelObj());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.modelObject.ModelObject#clearTLType()
     */
    @Override
    public void clearTLType() {
        // this.type = null;
        this.srcObj.setParentType(null);
    }

    @Override
    public void setTLType(final ModelObject<?> mo) {
        Object tlObj = null;
        if (mo != null)
            tlObj = mo.getTLModelObj();
        if (tlObj instanceof TLAttributeType)
            getTLModelObj().setParentType((TLAttributeType) mo.getTLModelObj());
    }

    @Override
    public void setTLType(final NamedEntity tlObj) {
        getTLModelObj().setParentType((TLAttributeType) tlObj);
    }

    @Override
    protected AbstractLibrary getLibrary(final TLSimple obj) {
        return obj.getOwningLibrary();
    }

    @Override
    public List<Object> getChildren() {
        return new ArrayList<Object>();
    }

    @Override
    public String getComponentType() {
        return "Simple Type";
    }

    @Override
    public String getName() {
        if (getTLModelObj().getLocalName() == null) {
            return "";
        }
        return getTLModelObj().getLocalName().isEmpty() ? "" : getTLModelObj().getLocalName();
    }

    @Override
    public String getNamespace() {
        return getTLModelObj().getNamespace();
    }

    @Override
    public String getNamePrefix() {
        final TLLibrary lib = (TLLibrary) getLibrary(getTLModelObj());
        return lib == null ? "" : lib.getPrefix();
    }

    @Override
    public int getMaxLength() {
        return getTLModelObj().getMaxLength();
    }

    @Override
    public int getMinLength() {
        return getTLModelObj().getMinLength();
    }

    @Override
    public String getPattern() {
        return getTLModelObj().getPattern();
    }

    @Override
    public int getFractionDigits() {
        return getTLModelObj().getFractionDigits();
    }

    @Override
    public NamedEntity getTLType() {
        return srcObj.getParentType();
    }

    @Override
    public int getTotalDigits() {
        return getTLModelObj().getTotalDigits();
    }

    @Override
    public String getMinInclusive() {
        return getTLModelObj().getMinInclusive();
    }

    @Override
    public String getMaxInclusive() {
        return getTLModelObj().getMaxInclusive();
    }

    @Override
    public String getMinExclusive() {
        return getTLModelObj().getMinExclusive();
    }

    @Override
    public String getMaxExclusive() {
        return getTLModelObj().getMaxExclusive();
    }

    @Override
    public boolean isSimpleAssignable() {
        return true;
    }

    // @Override
    // public boolean isSimpleType() {
    // return true;
    // }

    @Override
    public boolean isSimpleList() {
        return srcObj.isListTypeInd();
    }

    @Override
    public void setList(final boolean selected) {
        getTLModelObj().setPattern("");
        getTLModelObj().setListTypeInd(selected);
    }

    @Override
    public boolean setMinLength(final int length) {
        getTLModelObj().setMinLength(length);
        return true;
    }

    @Override
    public boolean setMaxLength(final int length) {
        getTLModelObj().setMaxLength(length);
        return true;
    }

    @Override
    public boolean setFractionDigits(int digits) {
        getTLModelObj().setFractionDigits(digits);
        return true;
    }

    @Override
    public boolean setTotalDigits(int digits) {
        getTLModelObj().setTotalDigits(digits);
        return true;
    }

    @Override
    public boolean setMinInclusive(String value) {
        getTLModelObj().setMinInclusive(value);
        return true;
    }

    @Override
    public boolean setMaxInclusive(String value) {
        getTLModelObj().setMaxInclusive(value);
        return true;
    }

    @Override
    public boolean setMinExclusive(String value) {
        getTLModelObj().setMinExclusive(value);
        return true;
    }

    @Override
    public boolean setMaxExclusive(String value) {
        getTLModelObj().setMaxExclusive(value);
        return true;
    }

    @Override
    public boolean setName(final String name) {
        getTLModelObj().setName(name);
        return true;
    }

    @Override
    public boolean setPattern(final String pattern) {
        getTLModelObj().setPattern(pattern);
        return true;
    }

}