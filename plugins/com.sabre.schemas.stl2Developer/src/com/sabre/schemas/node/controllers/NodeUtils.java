/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.node.controllers;

import java.util.LinkedList;
import java.util.List;

import com.sabre.schemacompiler.model.TLExampleOwner;
import com.sabre.schemacompiler.repository.impl.BuiltInProject;
import com.sabre.schemas.modelObject.FacetMO;
import com.sabre.schemas.node.ComponentNode;
import com.sabre.schemas.node.ComponentNodeType;
import com.sabre.schemas.node.EnumerationClosedNode;
import com.sabre.schemas.node.EnumerationOpenNode;
import com.sabre.schemas.node.LibraryChainNode;
import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.ProjectNode;
import com.sabre.schemas.node.PropertyNodeType;
import com.sabre.schemas.node.RenamableFacet;
import com.sabre.schemas.node.properties.PropertyNode;
import com.sabre.schemas.stl2developer.OtmRegistry;

/**
 * Purpose of this class is to having fluent interface to check against different model structure.
 * 
 * Usage Example:
 * 
 * <pre>
 * // check if node is VWA's simple property
 * NodeUtils.checker(node).ownerIs(ComponentNodeType.VWA).is(PropertyNodeType.SIMPLE).get()
 * 
 * <pre>
 * 
 * @author Pawel Jedruch
 */
public class NodeUtils {

    public static NodeChecker checker(Node node) {
        return new NodeChecker(node);
    }

    private static interface Matcher {
        public boolean match();
    }

    private static class ComponentMatcher implements Matcher {

        private Node node;
        private ComponentNodeType type;

        public ComponentMatcher(Node node, ComponentNodeType type) {
            this.node = node;
            this.type = type;
        }

        @Override
        public boolean match() {
            return (node instanceof ComponentNode) && isComponent(node, type);
        }

        private Boolean isComponent(Node node, ComponentNodeType type) {
            switch (type) {
                case ALIAS:
                    return node.isAlias();
                case BUSINESS:
                    return node.isBusinessObject();
                case CLOSED_ENUM:
                    return node instanceof EnumerationClosedNode;
                case OPEN_ENUM:
                    return node instanceof EnumerationOpenNode;
                case CORE:
                    return node.isCoreObject();
                case EXTENSION_POINT:
                    return node.isExtensionPointFacet();
                case MESSAGE:
                    return node.isMessage();
                case OPERATION:
                    return node.isOperation();
                case REQUEST:
                    return false; // TODO: how to check this ?
                case RESPONSE:
                    return false; // TODO: how to check this ?
                case NOTIFICATION:
                    return false; // TODO: how to check this ?
                case SERVICE:
                    return node.isService();
                case SIMPLE:
                    return node.isSimpleType();
                case VWA:
                    return node.isValueWithAttributes();
                default:
                    return false;

            }
        }

    }

    private static class PropertyMatcher implements Matcher {

        private Node node;
        private PropertyNodeType type;

        public PropertyMatcher(Node node, PropertyNodeType type) {
            this.node = node;
            this.type = type;
        }

        @Override
        public boolean match() {
            if (node instanceof PropertyNode) {
                PropertyNode pn = (PropertyNode) node;
                return type.equals(pn.getPropertyType());
            }
            return false;
        }

    }

    public static class NodeChecker {
        private final Node node;
        private final List<Matcher> matches = new LinkedList<Matcher>();

        public NodeChecker(Node node) {
            this.node = node;
        }

        public boolean get() {
            for (Matcher m : matches) {
                if (!m.match())
                    return false;
            }
            return true;
        }

        protected List<Matcher> getMatches() {
            return matches;
        }

        public NodeChecker is(ComponentNodeType type) {
            getMatches().add(new ComponentMatcher(node, type));
            return this;
        }

        public NodeChecker ownerIs(ComponentNodeType type) {
            getMatches().add(new ComponentMatcher(node.getOwningComponent(), type));
            return this;
        }

        public NodeChecker is(PropertyNodeType type) {
            getMatches().add(new PropertyMatcher(node, type));
            return this;
        }

        public NodeChecker isExampleSupported() {
            getMatches().add(new Matcher() {

                @Override
                public boolean match() {
                    return node.getTLModelObject() instanceof TLExampleOwner;
                }
            });
            return this;
        }

        /**
         * Check if given node is facet that is inherited from extension.
         */
        public NodeChecker isInheritedFacet() {
            getMatches().add(new Matcher() {

                @Override
                public boolean match() {
                    if (node instanceof RenamableFacet) {
                        RenamableFacet f = (RenamableFacet) node;
                        if (f.getModelObject() instanceof FacetMO) {
                            FacetMO ff = (FacetMO) f.getModelObject();
                            return ff.isInherited();
                        } else {
                            // DEBUG
                        }
                    }
                    return false;
                }

            });
            return this;
        }

        public NodeChecker isPatch() {
            getMatches().add(new Matcher() {

                @Override
                public boolean match() {
                    if (node.getLibrary() == null)
                        return false;
                    if (node.getLibrary().isInChain()) {
                        LibraryChainNode chain = node.getLibrary().getChain();
                        return chain.isPatch();
                    }
                    return node.getLibrary().isPatchVersion();
                }

            });
            return this;
        }

        public NodeChecker existInPreviousVersions() {
            getMatches().add(new Matcher() {

                @Override
                public boolean match() {
                    if (node.getLibrary().isInChain()) {
                        LibraryNode head = node.getLibrary().getChain().getHead();
                        return head != node.getLibrary();
                    }
                    return false;
                }

            });
            return this;
        }

    }

    // TODO: find better place for this method

    public static boolean isBuildInProject(ProjectNode n) {
        return BuiltInProject.BUILTIN_PROJECT_ID.equals(n.getProject().getProjectId());
    }

    public static boolean isProject(Node n) {
        return n instanceof ProjectNode;
    }

    public static boolean isDefaultProject(Node n) {
        return n == OtmRegistry.getMainController().getProjectController().getDefaultProject();
    }

}
