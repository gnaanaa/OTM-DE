/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.views;

import java.util.List;

import com.sabre.schemas.node.INode;
import com.sabre.schemas.node.Node;

/**
 * Base Interface for all controllers.
 * 
 * @author Dave Hollander
 * 
 */
public interface OtmView {

    /**
     * Attempt to activate this view.
     * 
     * @return true if successful.
     */
    public boolean activate();

    /**
     * Clear filter text
     */
    public void clearFilter();

    /**
     * Clear the view selection.
     */
    public void clearSelection();

    /**
     * Collapse the view fully.
     */
    public void collapse();

    /**
     * Expand the view fully.
     */
    public void expand();

    /**
     * @return the current node being displayed
     */
    public INode getCurrentNode();

    /**
     * @return the node that was displayed prior to the current one.
     */
    public INode getPreviousNode();

    /**
     * @return a new list of the currently selected nodes, possibly empty.
     */
    public List<Node> getSelectedNodes();

    /**
     * @return the string that identifies this view.
     */
    public String getViewID();

    /**
     * @return the state of the is inherited properties control
     */
    public boolean isShowInheritedProperties();

    /**
     * @return the state of the listening control
     */
    public boolean isListening();

    /**
     * Command the view to refresh its contents.
     */
    public void refresh();

    /**
     * Command the view to refresh and set its contents.
     */
    public void refresh(INode node);

    /**
     * Command the view to refresh and set its contents. If force, any user controls are
     * ignored/overriden.
     */
    public void refresh(INode node, boolean force);

    /**
     * Command to refresh all view contents.
     */
    public void refreshAllViews();

    /**
     * Command the view to refresh its contents and set the current view to <i>node</i>. By default,
     * the navigator view is set.
     */
    public void refreshAllViews(INode node);

    /**
     * Select the view node. Generates a selection event.
     * 
     * @param node
     *            to select.
     */
    public void select(final INode node);

    /**
     * Set the currently displayed node to the passed node.
     */
    public void setCurrentNode(INode node);

    /**
     * Set the exact matches only filter.
     */
    public void setExactMatchFiltering(final boolean state);

    /**
     * Set the inherited properties filter.
     */
    public void setInheritedPropertiesDisplayed(final boolean state);

    /**
     * Set the input data source for the view.
     */
    public void setInput(INode node);

    /**
     * Set the property type filter.
     */
    public void setDeepPropertyView(boolean state);

    /**
     * Move current node down
     */
    public void moveDown();

    /**
     * Move current node up
     */
    public void moveUp();

    /**
     * Enable or disable listening (linked behavior)
     */
    public void setListening(final boolean state);

    /**
     * Set the previous node to the current node.
     */
    public void restorePreviousNode();

    /**
     * Refresh the view. If regenerate is true, regenerate the contents first.
     */
    public void refresh(boolean regenerate);

    void remove(INode node);

}