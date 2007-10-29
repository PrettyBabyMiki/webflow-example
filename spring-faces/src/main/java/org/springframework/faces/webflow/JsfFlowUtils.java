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
package org.springframework.faces.webflow;

import javax.faces.FactoryFinder;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.faces.lifecycle.Lifecycle;

import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

/**
 * Common support for the JSF integration with Spring Web Flow.
 * 
 * @author Jeremy Grelle
 */
class JsfFlowUtils {

	public static FacesContext getFacesContext(Lifecycle lifecycle) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (facesContext == null) {
			RequestContext requestContext = RequestContextHolder.getRequestContext();
			FacesContextFactory facesContextFactory = (FacesContextFactory) FactoryFinder
					.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
			facesContext = facesContextFactory.getFacesContext(requestContext.getExternalContext().getContext(),
					requestContext.getExternalContext().getRequest(),
					requestContext.getExternalContext().getResponse(), lifecycle);
		}
		return facesContext;
	}

	public static void notifyAfterListeners(PhaseId phaseId, Lifecycle lifecycle) {
		PhaseEvent afterPhaseEvent = new PhaseEvent(getFacesContext(lifecycle), phaseId, lifecycle);
		for (int i = 0; i < lifecycle.getPhaseListeners().length; i++) {
			PhaseListener listener = lifecycle.getPhaseListeners()[i];
			if (listener.getPhaseId() == phaseId || listener.getPhaseId() == PhaseId.ANY_PHASE) {
				listener.afterPhase(afterPhaseEvent);
			}
		}
	}

	public static void notifyBeforeListeners(PhaseId phaseId, Lifecycle lifecycle) {
		PhaseEvent beforePhaseEvent = new PhaseEvent(getFacesContext(lifecycle), phaseId, lifecycle);
		for (int i = 0; i < lifecycle.getPhaseListeners().length; i++) {
			PhaseListener listener = lifecycle.getPhaseListeners()[i];
			if (listener.getPhaseId() == phaseId || listener.getPhaseId() == PhaseId.ANY_PHASE) {
				listener.beforePhase(beforePhaseEvent);
			}
		}
	}
}
