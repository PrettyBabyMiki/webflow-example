/*
 * Copyright 2004-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow.samples.sellitem;

import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.definition.StateDefinition;
import org.springframework.webflow.execution.EnterStateVetoException;
import org.springframework.webflow.execution.FlowExecutionListenerAdapter;
import org.springframework.webflow.execution.RequestContext;

public class SellItemFlowExecutionListener extends FlowExecutionListenerAdapter {

	public void stateEntering(RequestContext context, StateDefinition nextState) throws EnterStateVetoException {
		String role = nextState.getAttributes().getString("role");
		if (StringUtils.hasText(role)) {
			HttpServletRequest request = ((ServletExternalContext) context.getExternalContext()).getRequest();// todo
			// remove dependency on servlet api
			if (!request.isUserInRole(role)) {
				throw new EnterStateVetoException(context.getActiveFlow().getId(), context.getCurrentState().getId(),
						nextState.getId(), "State requires role '" + role
								+ "', but the authenticated user doesn't have it!");
			}
		}
	}
}
