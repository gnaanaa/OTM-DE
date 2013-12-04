/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemas.modelObject.XSDComplexMO;
import com.sabre.schemas.node.ComponentNode;
import com.sabre.schemas.node.EnumerationClosedNode;
import com.sabre.schemas.node.EnumerationOpenNode;
import com.sabre.schemas.node.FacetNode;
import com.sabre.schemas.node.ImpliedNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.XsdNode;
import com.sabre.schemas.node.controllers.NodeUtils;
import com.sabre.schemas.node.properties.AttributeNode;
import com.sabre.schemas.node.properties.ElementNode;
import com.sabre.schemas.node.properties.ElementReferenceNode;
import com.sabre.schemas.node.properties.IndicatorElementNode;
import com.sabre.schemas.node.properties.IndicatorNode;
import com.sabre.schemas.properties.Fonts;
import com.sabre.schemas.properties.Images;
import com.sabre.schemas.stl2developer.ColorProvider;
import com.sabre.schemas.stl2developer.OtmRegistry;
import com.sabre.schemas.trees.library.LibrarySorter;

/**
 * @author Agnieszka Janowska
 * 
 */
// TODO: replace this with JfaceTable (content provider, sorter, and label
// provider)
public class LibraryTablePoster {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(LibraryTablePoster.class);

    private final Table table;
    private final ColorProvider colorProvider;

    /**
	 *
	 */
    public LibraryTablePoster(final Table table, final ColorProvider colorProvider) {
        this.table = table;
        this.colorProvider = colorProvider;

        this.table.setLinesVisible(true);
        this.table.setHeaderVisible(true);

        final String[] titles = { "Name", "Role", "Type", "Description" };
        final int[] widths = { 155, 65, 100, 30 };
        for (int i = 0; i < titles.length; i++) {
            final TableColumn column = new TableColumn(this.table, SWT.BOLD);
            column.setText(titles[i]);
            column.setWidth(widths[i]);
        }

        packTable();

    }

    /**
     * Post the node table and label to the display. This is the primary table display method.
     * Remembers what node is posted. Posts the name and related properties adjacent to the table.
     * 
     * @param curNode
     *            is the ComponentNode to display
     */
    public void postTable(Node curNode) {
        clearTable();

        if (curNode == null) {
            return;
        }

        // restore focus after posting
        final int[] selectionIndices = table.getSelectionIndices();

        List<Node> sortedChildren = new ArrayList<Node>(curNode.getChildren());
        sortedChildren = sort(sortedChildren);
        if (curNode.isService()) {
            for (final Node kid : sortedChildren) {
                postTableRows(kid, kid.getLabel());
            }
        } else if (curNode.isComponent()) {
            // If the node is an XSD node, display its xChild node
            // representation.
            if (curNode instanceof XsdNode && curNode.getModelObject() instanceof XSDComplexMO) {
                XsdNode xn = (XsdNode) curNode;// new XsdNode((LibraryMember)
                // curNode.getModelObject().getTLModelObj(), curNode);
                curNode = xn.getOtmModelChild();
            }

            if (curNode.isTopLevelObject()) {
                // Put the aliases at the top of the table.
                for (final Node kid : sortedChildren) {
                    if (kid.isAlias()) {
                        postTableRow(kid);
                    }
                }
            }

            if (curNode instanceof EnumerationClosedNode) {
                postTableRows(curNode, "Closed: " + curNode.getName());
            } else if (curNode.isAlias()) {
                postTableRows(curNode, "");
            } else if (curNode instanceof EnumerationOpenNode) {
                postTableRows(curNode, "Open: " + curNode.getName());
            } else if (curNode.isExtensionPointFacet()) {
                postTableRows(curNode, "Extension Point: " + curNode.getName());
            } else if (curNode.isFacet()) {
                postTableRows(curNode, curNode.getLabel());
            } else if (curNode.isCoreObject() || curNode.isBusinessObject()
                    || curNode.isValueWithAttributes() || curNode.isOperation()) {
                for (final Node child : sortedChildren) {
                    if (!child.isAlias()) {
                        postTableRows(child, child.getLabel());
                    }
                }
            }
            if (table.getSelectionIndices() != selectionIndices) {
                table.select(selectionIndices);
            }
        }
        packTable();
        table.deselectAll();
    }

    private List<Node> sort(List<Node> children) {
        ArrayList<Node> sorted = new ArrayList<Node>(children);
        Collections.sort(sorted, LibrarySorter.createComparator());
        return sorted;
    }

    /**
     * Post the contents of the node into the table, row by row.
     * 
     * @param componentNode
     * @param separator
     */
    protected void postTableRows(final Node n, final String separator) {
        if (n != null) {
            if (n.isListFacet()) {
                return;
            }

            // Post the separator if not empty.
            TableItem item = null;
            if (!separator.isEmpty()) {
                item = new TableItem(table, SWT.BOLD | SWT.FILL);
                item.setText(separator);
                item.setBackground(colorProvider.getColor(SWT.COLOR_GRAY));
                item.setData(n);
                if (n.isInheritedProperty() || NodeUtils.checker(n).isInheritedFacet().get()) {
                    decorateInheritedItem(item);
                } else {
                    item.setForeground(colorProvider.getColor(SWT.COLOR_DARK_BLUE));
                }
            }

            // Sort the table rows

            List<Node> children = new ArrayList<Node>(n.getChildren());
            boolean showInherited = OtmRegistry.getNavigatorView().isShowInheritedProperties();
            if (showInherited) {
                children.addAll(n.getInheritedChildren());
            }
            children = sort(children);
            for (final Node cn : children) {
                if (cn instanceof ElementReferenceNode) {
                    postTableRow(cn);
                } else if (cn instanceof IndicatorNode) {
                    postTableRow(cn);
                } else if (cn instanceof AttributeNode) {
                    postTableRow(cn);
                } else if ((cn instanceof ElementNode) || (cn instanceof IndicatorElementNode)) {
                    postTableRow(cn);
                } else if (cn instanceof FacetNode) {
                    postTableRows(cn, cn.getLabel());
                } else if (!cn.isFacet() && !cn.isAlias()) {
                    postTableRow(cn); // what falls through to here?
                }
            }

        }
    }

    private void decorateInheritedItem(TableItem item) {
        item.setFont(Fonts.getFontRegistry().get(Fonts.inheritedItem));
        item.setForeground(colorProvider.getColor(SWT.COLOR_DARK_BLUE));
        item.setGrayed(true);
    }

    private void decorateReadonlyItem(TableItem item) {
        item.setFont(Fonts.getFontRegistry().get(Fonts.readOnlyItem));
        item.setForeground(colorProvider.getColor(SWT.COLOR_DARK_GRAY));
        item.setGrayed(true);
    }

    /**
     * Add rows to the global table. Expects a Facet node with leaves and optional text as a
     * separator before the facet properties are posted. post one row at location i if > 0 or else
     * at bottom of table
     * 
     * @param componentNode
     * @param i
     */
    protected TableItem postTableRow(final Node n) {
        // LOGGER.debug("postTableRow( "+n.getName()+" ) - editable? "+n.isEditable());

        final TableItem item = new TableItem(table, SWT.NONE);
        item.setData(n); // link the node to the row item widget

        if (n instanceof ComponentNode) {
            final ComponentNode cn = (ComponentNode) n;
            // post the icons
            item.setImage(0, n.getImage());

            if (n.isInheritedProperty() || NodeUtils.checker(n).isInheritedFacet().get()) {
                decorateInheritedItem(item);
            } else if (!n.isEditable()) {
                decorateReadonlyItem(item);

            }

            item.setText(0, n.getName());
            item.setText(1, cn.getPropertyRole());
            item.setText(2, cn.getTypeNameWithPrefix());

            item.setText(3, n.getDescription());

            // Warn the user
            if (n.isUnAssigned()) {
                item.setFont(Fonts.getFontRegistry().get(Fonts.inheritedItem));
                item.setBackground(2, colorProvider.getColor(SWT.COLOR_YELLOW));
            }
            if (cn.getDefaultType() instanceof ImpliedNode)
                item.setText(2, "-------");

            // flag duplicates
            if (!cn.isUnique() && !cn.isInheritedProperty()) {
                item.setImage(Images.getImageRegistry().get(Images.ErrorDecoration));
            }
        }
        return item;
    }

    public void clearTable() {
        if (table == null || table.isDisposed()) {
            return;
        }
        table.removeAll();
    }

    private void packTable() {
        table.getColumn(3).pack();
    }

    /**
     * Set which row is selected.
     * 
     * @param n
     */
    public void select(final Node n) {
        for (int i = 0; i < table.getItemCount(); i++) {
            final Object data = table.getItem(i).getData();
            if (data != null && data.equals(n)) {
                table.select(i);
                table.showItem(table.getItem(i));
                return;
            }
        }
    }
}