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
package org.opentravel.schemas.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemas.commands.ContextualFacetHandler;
import org.opentravel.schemas.commands.OtmAbstractHandler;
import org.opentravel.schemas.node.ChoiceObjectNode;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.facets.ChoiceFacetNode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;

/**
 * @author Dave Hollander
 * 
 */
public class AddChoiceFacetAction extends OtmAbstractAction {
	private static StringProperties propsDefault = new ExternalizedStringProperties("action.addChoice");

	OtmAbstractHandler handler = new OtmAbstractHandler() {
		@Override
		public Object execute(ExecutionEvent event) throws ExecutionException {
			return null;
		}
	};

	/**
	 *
	 */
	public AddChoiceFacetAction() {
		super(propsDefault);
	}

	public AddChoiceFacetAction(final StringProperties props) {
		super(props);
	}

	@Override
	public void run() {
		if (OTM16Upgrade.otm16Enabled)
			addContextualFacet(TLFacetType.CHOICE);
		else
			addChoiceFacet();
	}

	@Override
	public boolean isEnabled() {
		// Unmanaged or in the most current (head) library in version chain.
		Node n = mc.getCurrentNode_NavigatorView().getOwningComponent();
		return n instanceof ChoiceObjectNode ? n.isEditable_newToChain() : false;
		// use if we allow custom facets to be added as minor version change
		// return n instanceof BusinessObjectNode ? n.isEnabled_AddProperties() : false;

	}

	private void addContextualFacet(TLFacetType type) {
		// Verify the current node is editable business object
		ComponentNode current = (ComponentNode) mc.getSelectedNode_NavigatorView().getOwningComponent();
		if (current == null || !(current instanceof ChoiceObjectNode) || !current.isEditable_newToChain()) {
			DialogUserNotifier.openWarning("Add Choice Facet",
					"Choice Facets can only be added to non-versioned Choice objects.");
			return;
		}
		ChoiceObjectNode co = (ChoiceObjectNode) current;

		// Create the contextual facet
		ChoiceFacetNode cf = new ChoiceFacetNode();
		cf.setName("new");
		co.getLibrary().addMember(cf);
		co.getTLModelObject().addChoiceFacet(cf.getTLModelObject());

		// Create contributed facet
		NodeFactory.newMember(co, cf.getTLModelObject());
		mc.refresh(co);
	}

	/*
	 * Version 1.5 and older
	 */
	private void addChoiceFacet() {
		ContextualFacetHandler cfh = new ContextualFacetHandler();
		ComponentNode current = (ComponentNode) mc.getSelectedNode_NavigatorView().getOwningComponent();
		if (current != null && current instanceof ChoiceObjectNode)
			cfh.addContextualFacet((ChoiceObjectNode) current);

		// final TLFacetType facetType = TLFacetType.CHOICE;
		// ComponentNode current = (ComponentNode) mc.getSelectedNode_NavigatorView().getOwningComponent();
		// if (current == null || !(current instanceof ChoiceObjectNode) || !current.isEditable_newToChain()) {
		// DialogUserNotifier.openWarning("Add Choice Facet",
		// "Choice Facets can only be added to non-versioned Choice objects.");
		// return;
		// }
		//
		// final ChoiceObjectNode co = (ChoiceObjectNode) current;
		//
		// SimpleNameWizard wizard = new SimpleNameWizard("wizard.newOperation");
		// wizard.setValidator(new NewNodeNameValidator(co, wizard,
		// Messages.getString("wizard.newOperation.error.name")));
		// wizard.run(OtmRegistry.getActiveShell());
		// if (!wizard.wasCanceled()) {
		// co.addFacet(wizard.getText());
		// // new FacetNode(co, wizard.getText(), mc.getContextController().getDefaultContextId(), TLFacetType.CHOICE);
		// mc.refresh(co);
		// }
		//
		// mc.refresh(co);
	}

}
