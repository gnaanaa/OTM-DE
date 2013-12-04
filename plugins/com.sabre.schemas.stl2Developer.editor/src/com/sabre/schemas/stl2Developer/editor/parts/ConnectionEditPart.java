/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.stl2Developer.editor.parts;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.TextUtilities;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.jface.resource.JFaceResources;

import com.sabre.schemas.stl2Developer.editor.internal.Features;
import com.sabre.schemas.stl2Developer.editor.model.Connection;

/**
 * @author Pawel Jedruch
 * 
 */
public class ConnectionEditPart extends AbstractConnectionEditPart {

    /**
     * @param model
     */
    public ConnectionEditPart(Connection model) {
        setModel(model);
    }

    @Override
    protected void createEditPolicies() {

    }

    @Override
    public Connection getModel() {
        return (Connection) super.getModel();
    }

    @Override
    protected ConnectionAnchor getSourceConnectionAnchor() {
        if (getSource() != null) {

            return new MyAnchor(((GraphicalEditPart) getSource()).getFigure());
        }
        return super.getSourceConnectionAnchor();
    }

    @Override
    protected ConnectionAnchor getTargetConnectionAnchor() {
        if (getTarget() != null) {
            int topOffset = -1;
            if (Features.fixedTargetAnchor()) {
                topOffset = TextUtilities.INSTANCE.getStringExtents("A",
                        JFaceResources.getDefaultFont()).height / 2;
            }
            return new MyAnchor(((GraphicalEditPart) getTarget()).getFigure(), topOffset);
        }
        return super.getTargetConnectionAnchor();
    }

    class MyAnchor extends ChopboxAnchor {

        private int topOffset;

        public MyAnchor(IFigure owner) {
            this(owner, -1);
        }

        public MyAnchor(IFigure owner, int topOffset) {
            super(owner);
            this.topOffset = topOffset;
        }

        @Override
        public Point getLocation(Point reference) {
            IFigure owner = getOwner();
            if (!owner.isShowing()) {
                owner = findVisibleParent(getOwner());
            }
            Rectangle ownerBounds = owner.getBounds().getCopy();
            getOwner().translateToAbsolute(ownerBounds);
            Point ownerCenter = ownerBounds.getCenter();
            Point location = null;
            if (isExtension(getModel())) {
                if (ownerIsOnTop(ownerCenter, reference)) {
                    location = ownerBounds.getBottom();
                } else {
                    location = ownerBounds.getTop();
                }
            } else {
                if (ownerIsOnLeft(ownerCenter, reference)) {
                    location = ownerBounds.getRight();
                } else {
                    location = ownerBounds.getLeft();
                }
                if (topOffset >= 0) {
                    location.y = ownerBounds.getTop().y + topOffset;
                }
            }
            return location;
        }

        private boolean ownerIsOnTop(Point myLocation, Point reference) {
            return myLocation.y < reference.y;
        }

        private boolean ownerIsOnLeft(Point myLocation, Point reference) {
            return myLocation.x < reference.x;
        }

        private IFigure findVisibleParent(IFigure owner) {
            IFigure parent = owner.getParent();
            while (parent != null && !parent.isShowing()) {
                parent = parent.getParent();
            }
            return parent;
        }

    }

    @Override
    protected IFigure createFigure() {
        PolylineConnection fig = new PolylineConnection();
        return fig;
    }

    @Override
    protected void refreshVisuals() {
        PolylineConnection con = (PolylineConnection) getFigure();
        if (Features.customLinesForNotVisbile()) {
            if (!isVisible(getSource()) || !isVisible(getTarget())) {
                con.setLineStyle(Graphics.LINE_CUSTOM);
                con.setLineDash(new float[] { 6, 3 });
                con.setForegroundColor(ColorConstants.gray);
            } else {
                con.setLineStyle(Graphics.LINE_SOLID);
                con.setForegroundColor(null);
            }
        }
        PolygonDecoration decoration = new PolygonDecoration();
        decoration.setTemplate(PolygonDecoration.TRIANGLE_TIP);
        con.setTargetDecoration(decoration);
        if (isExtension(getModel())) {
            decoration.setBackgroundColor(ColorConstants.white);
        }
    }

    private boolean isExtension(Connection model) {
        return model.source.getNode().isExtendedBy(model.target.getNode());
    }

    private boolean isVisible(EditPart target) {
        if (target instanceof GraphicalEditPart) {
            IFigure f = ((GraphicalEditPart) target).getFigure();
            return f.isShowing();
        }
        return false;
    }
}