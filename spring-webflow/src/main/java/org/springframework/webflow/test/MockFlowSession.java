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

import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.StateDefinition;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.State;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.execution.FlowSession;
import org.springframework.webflow.execution.FlowSessionStatus;

/**
 * Mock implementation of the {@link FlowSession} interface.
 * 
 * @see FlowSession
 * 
 * @author Erwin Vervaet
 */
public class MockFlowSession implements FlowSession {

	private Flow definition;

	private State state;

	private FlowSessionStatus status = FlowSessionStatus.CREATED;

	private MutableAttributeMap scope = new LocalAttributeMap();

	private MutableAttributeMap flashMap = new LocalAttributeMap();

	private FlowSession parent;

	/**
	 * Creates a new mock flow session that sets a flow with id "mockFlow" as the 'active flow' in state "mockState".
	 * This session marks itself active.
	 */
	public MockFlowSession() {
		setDefinition(new Flow("mockFlow"));
		State state = new ViewState(definition, "mockState");
		setStatus(FlowSessionStatus.ACTIVE);
		setState(state);
	}

	/**
	 * Creates a new mock session in a created state for the specified flow definition.
	 */
	public MockFlowSession(Flow flow) {
		setDefinition(flow);
	}

	/**
	 * Creates a new mock session in {@link FlowSessionStatus#CREATED} state for the specified flow definition.
	 * @param flow the flow definition for the session
	 * @param input initial contents of 'flow scope'
	 */
	public MockFlowSession(Flow flow, AttributeMap input) {
		setDefinition(flow);
		scope.putAll(input);
	}

	// implementing FlowSession

	public FlowDefinition getDefinition() {
		return definition;
	}

	public StateDefinition getState() {
		return state;
	}

	public FlowSessionStatus getStatus() {
		return status;
	}

	public MutableAttributeMap getScope() {
		return scope;
	}

	public MutableAttributeMap getFlashMap() {
		return flashMap;
	}

	public FlowSession getParent() {
		return parent;
	}

	public boolean isRoot() {
		return parent == null;
	}

	// mutators

	/**
	 * Set the flow associated with this flow session.
	 */
	public void setDefinition(Flow flow) {
		this.definition = flow;
	}

	/**
	 * Set the currently active state.
	 */
	public void setState(State state) {
		this.state = state;
	}

	/**
	 * Set the status of this flow session.
	 */
	public void setStatus(FlowSessionStatus status) {
		this.status = status;
	}

	/**
	 * Set the scope data maintained by this flow session. This will be the flow scope data of the ongoing flow
	 * execution.
	 */
	public void setScope(MutableAttributeMap scope) {
		this.scope = scope;
	}

	/**
	 * Set the parent flow session of this flow session in the ongoing flow execution.
	 */
	public void setParent(FlowSession parent) {
		this.parent = parent;
	}

	// conveniece accessors

	/**
	 * Returns the flow definition of this session.
	 */
	public Flow getDefinitionInternal() {
		return definition;
	}

	/**
	 * Returns the current state of this session.
	 */
	public State getStateInternal() {
		return state;
	}
}