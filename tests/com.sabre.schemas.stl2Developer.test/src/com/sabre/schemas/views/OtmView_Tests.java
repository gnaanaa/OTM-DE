/**
 * 
 */
package com.sabre.schemas.views;

import junit.framework.Assert;

import org.junit.Test;

import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.node.INode;
import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.node.LibraryTests;
import com.sabre.schemas.node.ModelNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.NodeModelTestUtils;
import com.sabre.schemas.node.Node_Tests;
import com.sabre.schemas.stl2developer.OtmRegistry;
import com.sabre.schemas.testUtils.LoadFiles;
import com.sabre.schemas.views.example.ExampleView;

/**
 * Test the OTM View interface. Note: you can not test the individual views because they require the
 * workbench execution environment.
 * 
 * @author Dave Hollander
 * 
 */
public class OtmView_Tests {
    ModelNode model = null;
    Node_Tests nt = new Node_Tests();
    LoadFiles lf = new LoadFiles();
    LibraryTests lt = new LibraryTests();

    protected NavigatorView nv;

    public OtmView_Tests() {
    }

    @Test
    public void viewTests() throws Exception {
        MainController mc = new MainController();
        model = mc.getModelNode();

        lf.loadTestGroupA(mc);
        for (LibraryNode ln : Node.getAllLibraries()) {
            nt.visitAllNodes(ln);
        }
        NodeModelTestUtils.testNodeModel();

        OtmView view = mc.getDefaultView();
        Assert.assertNotNull(view);

        view = OtmRegistry.getNavigatorView();
        Assert.assertNotNull(view);
        checkViewMethods(view);

        TypeView tv = new TypeView();
        view = OtmRegistry.getTypeView();
        checkViewMethods(view);

        ExampleView ev = new ExampleView();
        view = OtmRegistry.getExampleView();
        checkViewMethods(view);
    }

    protected void checkViewMethods(OtmView view) {
        // just make sure these are implemented safely.
        view.activate();
        Assert.assertFalse(view.getViewID().isEmpty());
        view.clearFilter();
        view.clearSelection();
        view.collapse();
        view.expand();
        view.refresh();
        view.refreshAllViews();
        view.setDeepPropertyView(true);
        view.setDeepPropertyView(false);
        view.setExactMatchFiltering(true);
        view.setExactMatchFiltering(false);
        view.setInheritedPropertiesDisplayed(true);
        view.setInheritedPropertiesDisplayed(false);
        Assert.assertFalse(view.isShowInheritedProperties());
        view.setListening(false);
        view.setListening(true);

        // Make sure these are safe for all node types.
        for (INode node : model.getDescendants()) {
            view.refresh(node);
            if (!(view instanceof ExampleView)) {
                // do not generate examples this cause loading 3 additional built-in libraries
                view.refresh(node, true);
            }
            view.refresh(node, false);
            view.refreshAllViews(node);
            view.select(node);
            view.setCurrentNode(node);
            view.setInput(node);
        }

        // should not be empty after the input and current node is set above.
        Assert.assertNotNull(view.getCurrentNode()); // must be "listening"
        view.getPreviousNode();
        Assert.assertNotNull(view.getSelectedNodes());
    }

}