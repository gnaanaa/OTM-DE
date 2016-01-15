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
package org.opentravel.schemas.views.decoration;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.repository.impl.RemoteRepositoryClient;
import org.opentravel.schemas.controllers.MainController.IRefreshListener;
import org.opentravel.schemas.node.LibraryChainNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.NamespaceHandler;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
import org.opentravel.schemas.node.resources.ResourceNode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryChainNode;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryInstanceNode;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryItemNode;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryRootNsNode;

public class LibraryDecorator extends BaseLabelProvider implements ILightweightLabelDecorator, IRefreshListener {

	public LibraryDecorator() {
		OtmRegistry.getMainController().addRefreshListener(this);
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		if (element instanceof LibraryNode) {
			return true;
		}
		return false;
	}

	public static ImageDescriptor errorDesc() {
		return Images.getImageRegistry().getDescriptor(Images.ErrorDecoration);
	}

	public static ImageDescriptor warningDesc() {
		return Images.getImageRegistry().getDescriptor(Images.WarningDecoration);
	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof LibraryNode) {
			decoration.addSuffix(getLibraryDecoration((LibraryNode) element));
		} else if (element instanceof LibraryChainNode) {
			LibraryNode head = ((LibraryChainNode) element).getHead();
			decoration.addSuffix(getLibraryDecoration(head));
		} else if (element instanceof RepositoryInstanceNode) {
			decoration.addSuffix(getRepositoryNameDecoration((RepositoryInstanceNode) element));
		} else if (element instanceof RepositoryChainNode) {
			RepositoryChainNode node = (RepositoryChainNode) element;
			decoration.addSuffix(getNamespaceDecoration(node));
		} else if (element instanceof RepositoryItemNode) {
			RepositoryItemNode node = (RepositoryItemNode) element;
			decoration.addSuffix(getRepositoryItemDecoration(node.getItem()));
		} else if (element instanceof RepositoryRootNsNode) {
			decoration.addSuffix(getNamespaceDecoration((RepositoryRootNsNode) element));
		} else if (element instanceof ResourceNode) {
			if (!((ResourceMemberInterface) element).isValid())
				decoration.addOverlay(errorDesc(), IDecoration.BOTTOM_LEFT);
		} else if (element instanceof ResourceMemberInterface) {
			if (!((ResourceMemberInterface) element).isValid())
				decoration.addOverlay(errorDesc(), IDecoration.BOTTOM_LEFT);
			else if (!((ResourceMemberInterface) element).isValid_NoWarnings())
				decoration.addOverlay(warningDesc(), IDecoration.BOTTOM_LEFT);
			// } else if (element instanceof PropertyNode) {
			// if (((PropertyNode) element).getAssignedType() == ModelNode.getUnassignedNode())
			// decoration.addOverlay(warningDesc(), IDecoration.BOTTOM_LEFT);
		} else if (element instanceof LibraryMemberInterface)
			if (!((LibraryMemberInterface) element).isValid())
				decoration.addOverlay(errorDesc(), IDecoration.BOTTOM_LEFT);
	}

	private String getRepositoryNameDecoration(RepositoryInstanceNode element) {
		if (element.getRepository() instanceof RemoteRepositoryClient) {
			RemoteRepositoryClient rc = (RemoteRepositoryClient) element.getRepository();
			String userID = rc.getUserId();
			if (rc.getUserId() == null) {
				userID = Messages.getString("repository.user.anonymous");
			}
			return surround("User: " + userID);
		}
		return "";
	}

	private String getNamespaceDecoration(RepositoryChainNode node) {
		StringBuilder sb = new StringBuilder();
		sb.append(" ");
		sb.append(node.getChildren().size());
		sb.append(surround(node.getPermission()));
		return sb.toString();
	}

	private String getNamespaceDecoration(RepositoryRootNsNode node) {
		StringBuilder sb = new StringBuilder();
		sb.append(" ");
		sb.append(getChildCount(node));
		sb.append(surround(node.getPermission()));
		return sb.toString();
	}

	@Override
	public void refresh() {
		fireLabelProviderChanged(new LabelProviderChangedEvent(this));
	}

	@Override
	public void refresh(INode node) {
		fireLabelProviderChanged(new LabelProviderChangedEvent(this, node));
	}

	private String getRepositoryItemDecoration(RepositoryItem item) {
		String status = translateStatusState(item.getStatus(), item.getState(), item.getLockedByUser(), false);
		return getDecoration(status, item.getVersion());
	}

	public static String translateStatusState(TLLibraryStatus status, RepositoryItemState state, String lockedBy,
			boolean isEditableInGui) {
		if (state == RepositoryItemState.BUILT_IN) {
			return "Built-in";
		}
		if (TLLibraryStatus.FINAL.equals(status)) {
			return "Final";
		} else if (status == null) {
			return "NULL Status";
		} else {
			switch (state) {
			case MANAGED_LOCKED:
				return "Locked: " + lockedBy;
			case MANAGED_UNLOCKED:
				return "Draft";
			case MANAGED_WIP:
				if (isEditableInGui)
					return "Editable";
				else
					return "Locked: " + lockedBy;
			case UNMANAGED:
				return "";
			case BUILT_IN:
				// should be handled before checking if final
				return "Built-in";
			default:
				return "Unknown";
			}
		}

	}

	private String getChildCount(RepositoryRootNsNode node) {
		int count = node.getChildren().size();
		return "(" + count + ")";
	}

	private String getLibraryDecoration(LibraryNode element) {
		return getDecoration(getLibraryStatus(element), getLibraryVersion(element));
	}

	public String getLibraryStatus(LibraryNode lib) {
		return OtmRegistry.getMainController().getLibraryController().getLibraryStatus(lib);
	}

	public String getLibraryVersion(LibraryNode lib) {
		String version = "";
		NamespaceHandler nsHandler = null;
		if (lib != null) {
			nsHandler = lib.getNsHandler();
			ProjectItem projectItem = lib.getProjectItem();
			if (projectItem != null && nsHandler != null
					&& !RepositoryItemState.UNMANAGED.equals(projectItem.getState())) {
				version = nsHandler.getNSVersion(lib.getNamespace());
			}
		}
		return version;
	}

	private String getDecoration(String status, String version) {
		return surround(status) + surround(version);
	}

	private String surround(String txt) {
		if (txt != null && !txt.isEmpty()) {
			return " [" + txt + "]";
		}
		return "";
	}

	@Override
	public void dispose() {
	}

}
