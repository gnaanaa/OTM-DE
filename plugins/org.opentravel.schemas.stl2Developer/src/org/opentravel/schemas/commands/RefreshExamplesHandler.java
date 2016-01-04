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
package org.opentravel.schemas.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.OtmView;

/**
 * 
 * @author Agnieszka Janowska
 * 
 *         9/1/2015 dmh - dead code
 */
public class RefreshExamplesHandler extends OtmAbstractHandler {
	public static String COMMAND_ID = "org.opentravel.schemas.commands.generateExamples";

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final OtmView view = OtmRegistry.getExampleView();
		if (view != null) {
			view.refresh(true);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.commands.OtmHandler#getID()
	 */
	@Override
	public String getID() {
		return COMMAND_ID;
	}

}
