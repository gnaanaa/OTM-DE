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
package org.opentravel.schemas.node.resources;


/**
 * Manages access to data for the Action Example URLs.
 * 
 * An example: GET http://example.com/basePath/{PathParam}?QueryParam=xxx&Q2=yyy <BO>...</BO>
 * 
 * @author Dave
 *
 */
public class ActionExample {
	// private static final Logger LOGGER = LoggerFactory.getLogger(ActionExample.class);

	private final static String SYSTEM = "http://example.com";
	private ActionNode action;

	public ActionExample(ActionNode action) {
		this.action = action;
	}

	public String getBasePath() {
		String basePath = "";
		if (action.tlObj.getOwner() != null) {
			basePath = action.tlObj.getOwner().getBasePath();
			if (basePath != null && basePath.endsWith("/"))
				basePath = basePath.substring(0, basePath.length() - 1);
		}
		return basePath;
	}

	public String getMethod() {
		if (action.tlObj.getRequest() == null)
			return "";
		return action.tlObj.getRequest().getHttpMethod() != null ? action.tlObj.getRequest().getHttpMethod().toString()
				: "";
	}

	public String getTemplate() {
		return action.tlObj.getRequest() != null ? action.tlObj.getRequest().getPathTemplate() : "";
	}

	public String getQueryTemplate() {
		return action.getQueryTemplate();
	}

	public String getPayloadExample() {
		String payload = "";
		if (action.tlObj.getRequest() != null)
			payload = action.tlObj.getRequest().getPayloadTypeName();
		if (payload == null)
			payload = "";
		return !payload.isEmpty() ? "<" + payload + ">...</" + payload + ">" : "";
	}

	public String getLabel() {
		return action.tlObj.getActionId();
	}

	public String getURL() {
		if (getMethod().isEmpty())
			return "";
		return getMethod() + " " + SYSTEM + getBasePath() + getTemplate() + getQueryTemplate() + " "
				+ getPayloadExample();
	}

	@Override
	public String toString() {
		return getLabel() + ": " + getURL();
	}
}