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
package org.springframework.webflow.test;

import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.StateDefinition;
import org.springframework.webflow.definition.TransitionDefinition;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.FlowExecutionContext;
import org.springframework.webflow.execution.FlowSession;
import org.springframework.webflow.execution.RequestContext;

/**
 * Mock implementation of the <code>RequestContext</code> interface to facilitate standalone flow artifact (e.g.
 * action) unit tests.
 * 
 * @see org.springframework.webflow.execution.RequestContext
 * @see org.springframework.webflow.execution.Action
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class MockRequestContext implements RequestContext {

	private FlowExecutionContext flowExecutionContext = new MockFlowExecutionContext();

	private ExternalContext externalContext = new MockExternalContext();

	private MutableAttributeMap requestScope = new LocalAttributeMap();

	private MutableAttributeMap attributes = new LocalAttributeMap();

	private Event lastEvent;

	private Transition lastTransition;

	/**
	 * Creates a new mock request context with the following defaults:
	 * <ul>
	 * <li>A flow execution context with a active session of flow "mockFlow" in state "mockState".
	 * <li>A mock external context with no request parameters set.
	 * </ul>
	 * To add request parameters to this request, use the {@link #putRequestParameter(String, String)} method.
	 */
	public MockRequestContext() {
	}

	/**
	 * Creates a new mock request context with the following defaults:
	 * <ul>
	 * <li>A flow execution context with an active session for the specified flow.
	 * <li>A mock external context with no request parameters set.
	 * </ul>
	 * To add request parameters to this request, use the {@link #putRequestParameter(String, String)} method.
	 */
	public MockRequestContext(Flow flow) {
		flowExecutionContext = new MockFlowExecutionContext(flow);
	}

	/**
	 * Creates a new mock request context with the following defaults:
	 * <ul>
	 * <li>A flow execution context with a active session of flow "mockFlow" in state "mockState".
	 * <li>A mock external context with the provided parameters set.
	 * </ul>
	 */
	public MockRequestContext(ParameterMap requestParameterMap) {
		externalContext = new MockExternalContext(requestParameterMap);
	}

	// implementing RequestContext

	public FlowDefinition getActiveFlow() {
		return getFlowExecutionContext().getActiveSession().getDefinition();
	}

	public StateDefinition getCurrentState() {
		return getFlowExecutionContext().getActiveSession().getState();
	}

	public MutableAttributeMap getRequestScope() {
		return requestScope;
	}

	public MutableAttributeMap getFlashScope() {
		return getMockFlowExecutionContext().getFlashScope();
	}

	public MutableAttributeMap getFlowScope() {
		return getFlowExecutionContext().getActiveSession().getScope();
	}

	public MutableAttributeMap getConversationScope() {
		return getMockFlowExecutionContext().getConversationScope();
	}

	public ParameterMap getRequestParameters() {
		return externalContext.getRequestParameterMap();
	}

	public ExternalContext getExternalContext() {
		return externalContext;
	}

	public FlowExecutionContext getFlowExecutionContext() {
		return flowExecutionContext;
	}

	public Event getLastEvent() {
		return lastEvent;
	}

	public TransitionDefinition getLastTransition() {
		return lastTransition;
	}

	public AttributeMap getAttributes() {
		return attributes;
	}

	public void setAttributes(AttributeMap attributes) {
		this.attributes.replaceWith(attributes);
	}

	public AttributeMap getModel() {
		return getConversationScope().union(getFlowScope()).union(getFlashScope()).union(getRequestScope());
	}

	// mutators

	/**
	 * Sets the active flow session of the executing flow associated with this request. This will influence
	 * {@link #getActiveFlow()} and {@link #getCurrentState()}, as well as {@link #getFlowScope()} and
	 * {@link #getFlashScope()}.
	 */
	public void setActiveSession(FlowSession flowSession) {
		getMockFlowExecutionContext().setActiveSession(flowSession);
	}

	/**
	 * Sets the external context.
	 */
	public void setExternalContext(ExternalContext externalContext) {
		this.externalContext = externalContext;
	}

	/**
	 * Sets the flow execution context.
	 */
	public void setFlowExecutionContext(FlowExecutionContext flowExecutionContext) {
		this.flowExecutionContext = flowExecutionContext;
	}

	/**
	 * Set the last event that occured in this request context.
	 * @param lastEvent the event to set
	 */
	public void setLastEvent(Event lastEvent) {
		this.lastEvent = lastEvent;
	}

	/**
	 * Set the last transition that executed in this request context.
	 * @param lastTransition the last transition to set
	 */
	public void setLastTransition(Transition lastTransition) {
		this.lastTransition = lastTransition;
	}

	/**
	 * Set a request context attribute.
	 * @param attributeName the attribute name
	 * @param attributeValue the attribute value
	 */
	public void setAttribute(String attributeName, Object attributeValue) {
		attributes.put(attributeName, attributeValue);
	}

	/**
	 * Remove a request context attribute.
	 * @param attributeName the attribute name
	 */
	public void removeAttribute(String attributeName) {
		attributes.remove(attributeName);
	}

	// convenience accessors

	/**
	 * Returns the contained mutable context {@link AttributeMap attribute map} allowing setting of mock context
	 * attributes.
	 * @return the attribute map
	 */
	public MutableAttributeMap getAttributeMap() {
		return attributes;
	}

	/**
	 * Returns the flow execution context as a {@link MockFlowExecutionContext}.
	 */
	public MockFlowExecutionContext getMockFlowExecutionContext() {
		return (MockFlowExecutionContext) flowExecutionContext;
	}

	/**
	 * Returns the external context as a {@link MockExternalContext}.
	 */
	public MockExternalContext getMockExternalContext() {
		return (MockExternalContext) externalContext;
	}

	/**
	 * Adds a request parameter to the configured external context.
	 * @param parameterName the parameter name
	 * @param parameterValue the parameter value
	 */
	public void putRequestParameter(String parameterName, String parameterValue) {
		getMockExternalContext().putRequestParameter(parameterName, parameterValue);
	}

	/**
	 * Adds a multi-valued request parameter to the configured external context.
	 * @param parameterName the parameter name
	 * @param parameterValues the parameter values
	 */
	public void putRequestParameter(String parameterName, String[] parameterValues) {
		getMockExternalContext().putRequestParameter(parameterName, parameterValues);
	}
}