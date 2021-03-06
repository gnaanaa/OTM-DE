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

import org.eclipse.swt.widgets.Event;
import org.opentravel.schemas.commands.AddNodeHandler2;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.properties.PropertyNodeType;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;

/**
 * Run the addNode command to add roles to a core object.
 * 
 * @author Dave Hollander
 * 
 */
public class AddRoleAction extends OtmAbstractAction {
	private static StringProperties propDefault = new ExternalizedStringProperties("action.addRole");

	public AddRoleAction(final MainWindow mainWindow) {
		super(mainWindow, propDefault);
	}

	public AddRoleAction(final MainWindow mainWindow, final StringProperties props) {
		super(mainWindow, props);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
	 */
	@Override
	public void runWithEvent(Event event) {
		event.data = PropertyNodeType.ROLE;
		// mc.runAddProperties(event);
		// IHandlerService handlerSvc = (IHandlerService)
		// mc.getMainWindow().getSite().getService(IHandlerService.class);
		// try {
		new AddNodeHandler2().execute(event);
		// handlerSvc.executeCommand(AddNodeHandler2.COMMAND_ID, event);
		// } catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
		// DialogUserNotifier.openWarning("Add Role Error", "Could not run command: " + e.getLocalizedMessage());
		// }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		Node n = mc.getCurrentNode_NavigatorView().getOwningComponent();
		return n instanceof CoreObjectNode ? n.isEnabled_AddProperties() : false;
		// return n instanceof CoreObjectNode ? n.isNewToChain() : false;
	}

}
