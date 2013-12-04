/*

 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.controllers;

import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sabre.schemacompiler.model.TLAdditionalDocumentationItem;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLDocumentationItem;
import com.sabre.schemas.node.ComponentNode;
import com.sabre.schemas.node.CoreObjectNode;
import com.sabre.schemas.node.ModelNode;
import com.sabre.schemas.node.NodeNameUtils;
import com.sabre.schemas.node.PropertyNodeType;
import com.sabre.schemas.node.VWA_Node;
import com.sabre.schemas.node.properties.AttributeNode;
import com.sabre.schemas.node.properties.ElementNode;
import com.sabre.schemas.node.properties.PropertyNode;
import com.sabre.schemas.node.properties.SimpleAttributeNode;
import com.sabre.schemas.utils.ComponentNodeBuilder;
import com.sabre.schemas.utils.PropertyNodeBuilder;

/**
 * @author Pawel Jedruch
 * 
 */
public class DefaultModelControllerTest {

    private static MainController mc;
    private static DefaultModelController dc;

    @BeforeClass
    public static void boforeTests() {
        mc = new MainController();
        dc = (DefaultModelController) mc.getModelController();
    }

    @Test
    public void changeToSimpleShouldChangeSimpleType() {
        ComponentNode coType = ComponentNodeBuilder.createCoreObject("Type").get();
        PropertyNode p = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).assign(coType)
                .build();
        CoreObjectNode co = ComponentNodeBuilder.createCoreObject("Core").addToSummaryFacet(p)
                .get();

        boolean res = dc.changeToSimple(p);

        Assert.assertTrue(res);
        Assert.assertSame(coType, co.getSimpleType());
        Assert.assertEquals(1, co.getSimpleFacet().getChildren().size());
    }

    @Test
    public void changeToSimpleShouldAlwaysBeAttribute() {
        ComponentNode coType = ComponentNodeBuilder.createCoreObject("Type").get();
        PropertyNode p = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).assign(coType)
                .build();
        CoreObjectNode co = ComponentNodeBuilder.createCoreObject("Core").addToSummaryFacet(p)
                .get();

        dc.changeToSimple(p);

        Assert.assertTrue(co.getSimpleFacet().getSimpleAttribute() instanceof SimpleAttributeNode);
    }

    @Test
    public void changeToSimpleShouldRemoveProperty() {
        ComponentNode coType = ComponentNodeBuilder.createCoreObject("Type").get();
        PropertyNode p = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).assign(coType)
                .build();
        CoreObjectNode co = ComponentNodeBuilder.createCoreObject("Core").addToSummaryFacet(p)
                .get();

        boolean res = dc.changeToSimple(p);

        Assert.assertTrue(res);
        Assert.assertTrue(co.getSummaryFacet().getChildren().isEmpty());
    }

    @Test
    public void changeToSimpleSimplePropertyShouldReturnFalse() {
        ComponentNode coType = ComponentNodeBuilder.createCoreObject("Type").get();
        PropertyNode p = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).assign(coType)
                .build();
        CoreObjectNode co = ComponentNodeBuilder.createCoreObject("Core").addToSummaryFacet(p)
                .get();

        boolean res = dc.changeToSimple((PropertyNode) co.getSimpleFacet().getSimpleAttribute());

        Assert.assertFalse(res);
    }

    @Test
    public void changeToSimpleShouldCopyDocumentation() {
        ComponentNode coType = ComponentNodeBuilder.createCoreObject("Type").get();
        PropertyNode p = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).assign(coType)
                .setDocumentation(createSampleDoc()).build();
        CoreObjectNode co = ComponentNodeBuilder.createCoreObject("Core").addToSummaryFacet(p)
                .get();

        dc.changeToSimple(p);
        assertDocumentationEquals(createSampleDoc(), co.getSimpleFacet().getSimpleAttribute()
                .getDocumentation());
    }

    @Test
    public void changeFromSimpleNotSimpleAttributeShouldReturnFalse() {
        PropertyNode p = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).build();
        CoreObjectNode co = ComponentNodeBuilder.createCoreObject("Core").addToSummaryFacet(p)
                .get();
        ComponentNode newProperty = dc.moveSimpleToFacet(p, co.getSummaryFacet());
        Assert.assertNull(newProperty);
    }

    @Test
    public void changeFromSimpleShouldCreateNewProperty() {
        ComponentNode coType = ComponentNodeBuilder.createCoreObject("Type").get();
        CoreObjectNode co = ComponentNodeBuilder.createCoreObject("Core").get();
        co.getSimpleFacet().getSimpleAttribute().setAssignedType(coType);

        // make sure that summary is empty
        Assert.assertEquals(0, co.getSummaryFacet().getChildren().size());
        ComponentNode newProperty = dc.moveSimpleToFacet(co.getSimpleFacet().getSimpleAttribute(),
                co.getSummaryFacet());

        Assert.assertNotNull(newProperty);
        Assert.assertEquals(1, co.getSummaryFacet().getChildren().size());
        Assert.assertSame(newProperty, co.getSummaryFacet().getChildren().get(0));
        Assert.assertSame(coType, newProperty.getType());
    }

    @Test
    public void changeFromSimpleShouldLeftSimpleAttributeWithEmpty() {
        ComponentNode vwaType = ComponentNodeBuilder.createVWA("Type").get();
        CoreObjectNode co = ComponentNodeBuilder.createCoreObject("Core").get();
        co.getSimpleFacet().getSimpleAttribute().setAssignedType(vwaType);

        dc.moveSimpleToFacet(co.getSimpleFacet().getSimpleAttribute(), co.getSummaryFacet());

        Assert.assertSame(ModelNode.getEmptyNode(), co.getSimpleFacet().getSimpleAttribute()
                .getType());

    }

    @Test
    public void changeFromSimpleShouldCreateAttributeForVWA() {
        ComponentNode vwaType = ComponentNodeBuilder.createVWA("Type").get();
        VWA_Node co = ComponentNodeBuilder.createVWA("VWA").get();

        co.getSimpleFacet().getSimpleAttribute().setAssignedType(vwaType);

        ComponentNode newProperty = dc.moveSimpleToFacet(co.getSimpleFacet().getSimpleAttribute(),
                co.getSummaryFacet());

        Assert.assertTrue(newProperty instanceof AttributeNode);
    }

    @Test
    public void changeFromSimpleShouldCreateElementForNotVWA() {
        ComponentNode vwaType = ComponentNodeBuilder.createVWA("Type").get();
        CoreObjectNode co = ComponentNodeBuilder.createCoreObject("Core").get();
        co.getSimpleFacet().getSimpleAttribute().setAssignedType(vwaType);

        ComponentNode newProperty = dc.moveSimpleToFacet(co.getSimpleFacet().getSimpleAttribute(),
                co.getSummaryFacet());

        Assert.assertTrue(newProperty instanceof ElementNode);
    }

    @Test
    public void changeFromSimpleShouldStripSimpleSuffix() {
        ComponentNode vwaType = ComponentNodeBuilder.createVWA("Type").get();
        String typeName = "NewCoreObject";
        CoreObjectNode co = ComponentNodeBuilder.createCoreObject(typeName).get();
        co.getSimpleFacet().getSimpleAttribute().setAssignedType(vwaType);

        ComponentNode newProperty = dc.moveSimpleToFacet(co.getSimpleFacet().getSimpleAttribute(),
                co.getSummaryFacet());

        Assert.assertEquals(typeName, newProperty.getName());
    }

    @Test
    public void changeFromSimpleShouldStripSimpleSuffixVWA() {
        ComponentNode vwaType = ComponentNodeBuilder.createVWA("Type").get();

        VWA_Node co = ComponentNodeBuilder.createVWA("VWA").get();
        co.getSimpleFacet().getSimpleAttribute().setAssignedType(vwaType);

        String name = co.getSimpleFacet().getSimpleAttribute().getName();
        ComponentNode newProperty = dc.moveSimpleToFacet(co.getSimpleFacet().getSimpleAttribute(),
                co.getSummaryFacet());

        Assert.assertEquals(NodeNameUtils.fixAttributeName(name), newProperty.getName());
    }

    @Test
    public void changeFromSimpleShouldCopyDocumentation() {
        VWA_Node vwaType = ComponentNodeBuilder.createVWA("Type").get();

        VWA_Node co = ComponentNodeBuilder.createVWA("VWA").get();
        SimpleAttributeNode san = (SimpleAttributeNode) co.getSimpleFacet().getSimpleAttribute();
        TLDocumentation doc = createSampleDoc();
        san.getModelObject().setDocumentation(doc);
        san.setAssignedType(vwaType);

        ComponentNode newProperty = dc.moveSimpleToFacet(co.getSimpleFacet().getSimpleAttribute(),
                co.getSummaryFacet());
        Assert.assertNotSame(doc, newProperty.getDocumentation());
        assertDocumentationEquals(doc, newProperty.getDocumentation());

    }

    private TLDocumentation createSampleDoc() {
        TLDocumentation doc = new TLDocumentation();
        doc.addDeprecation(createDocItem("deprecation"));
        doc.addImplementer(createDocItem("implementer"));
        doc.addMoreInfo(createDocItem("moreinfo"));
        doc.addReference(createDocItem("reference"));
        doc.addOtherDoc(createDocItem("otherdoc", "context"));
        doc.setDescription("Description");
        return doc;
    }

    private void assertDocumentationEquals(TLDocumentation expected, TLDocumentation actual) {
        Assert.assertEquals(expected.getDescription(), actual.getDescription());
        assertListemItemEquals(expected.getDeprecations(), actual.getDeprecations());
        assertListemItemEquals(expected.getImplementers(), actual.getImplementers());
        assertListemItemEquals(expected.getMoreInfos(), actual.getMoreInfos());
        assertListemItemEquals(expected.getReferences(), actual.getReferences());
        assertListemItemEquals(expected.getOtherDocs(), actual.getOtherDocs());

    }

    private void assertListemItemEquals(List<? extends TLDocumentationItem> expected,
            List<? extends TLDocumentationItem> actual) {
        Assert.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertDocumentationItemEquals(expected.get(i), actual.get(i));
        }
    }

    private void assertDocumentationItemEquals(TLDocumentationItem actual,
            TLDocumentationItem expected) {
        Assert.assertTrue(actual.getClass().isInstance(expected));
        if (actual instanceof TLAdditionalDocumentationItem) {
            ((TLAdditionalDocumentationItem) actual).getContext().equals(
                    ((TLAdditionalDocumentationItem) expected).getContext());
        }
        Assert.assertEquals(actual.getText(), expected.getText());
    }

    private TLDocumentationItem createDocItem(String text) {
        TLDocumentationItem item = new TLDocumentationItem();
        item.setText(text);
        return item;
    }

    private TLAdditionalDocumentationItem createDocItem(String text, String context) {
        TLAdditionalDocumentationItem item = new TLAdditionalDocumentationItem();
        item.setText(text);
        item.setContext(context);
        return item;
    }

}
