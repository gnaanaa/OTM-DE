/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.modelObject;

import java.util.ArrayList;
import java.util.List;

import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.TLAttribute;
import com.sabre.schemacompiler.model.TLEnumValue;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLModelElement;
import com.sabre.schemacompiler.model.TLOpenEnumeration;
import com.sabre.schemas.utils.StringComparator;

public class OpenEnumMO extends ModelObject<TLOpenEnumeration> {

    public OpenEnumMO(final TLOpenEnumeration obj) {
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
        // owning library may be deleted during a delete process.
        if (srcObj.getOwningLibrary() != null)
            srcObj.getOwningLibrary().removeNamedMember(srcObj);
    }

    @Override
    public List<TLEnumValue> getChildren() {
        return getTLModelObj().getValues();
    }

    @Override
    public String getComponentType() {
        return "Open Enumeration";
    }

    /**
     * @see com.sabre.schemas.modelObject.ModelObject#getInheritedChildren()
     */
    @Override
    public List<?> getInheritedChildren() {
        final List<TLModelElement> inheritedKids = new ArrayList<TLModelElement>();
        final TLOpenEnumeration openEnum = getTLModelObj();
        TLAttribute other = new TLAttribute();
        other.setName("Other_" + openEnum.getName());
        inheritedKids.add(other);
        return inheritedKids;
        // FIXME - this works, but does not create children seen in the display.
    }

    @Override
    public String getName() {
        return getTLModelObj().getName();
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
    protected AbstractLibrary getLibrary(final TLOpenEnumeration obj) {
        return obj.getOwningLibrary();
    }

    @Override
    public boolean isComplexAssignable() {
        return true;
    }

    @Override
    public boolean setExample(final String ex, final String context) {
        return false;
    }

    @Override
    public boolean setName(final String name) {
        getTLModelObj().setName(name);
        return true;
    }

    @Override
    public boolean isSimpleAssignable() {
        return true;
    }

    @Override
    public void sort() {
        TLOpenEnumeration eOpen = getTLModelObj();
        eOpen.sortValues(new StringComparator<TLEnumValue>() {

            @Override
            protected String getString(TLEnumValue object) {
                return object.getLiteral();
            }
        });
    }

}