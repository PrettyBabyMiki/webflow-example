/*
 * Copyright 2002-2007 the original author or authors.
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
package org.springframework.webflow.engine;

import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.FlowExecutionContext;
import org.springframework.webflow.execution.FlowExecutionException;
import org.springframework.webflow.execution.FlowSession;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.ViewSelection;

/**
 * Mutable control interface used to manipulate an ongoing flow execution in the
 * context of one client request. Primarily used internally by the various flow
 * artifacts when they are invoked.
 * <p>
 * This interface acts as a facade for core definition constructs such as the
 * central <code>Flow</code> and <code>State</code> classes, abstracting
 * away details about the runtime execution machine defined in the
 * {@link org.springframework.webflow.engine.impl execution engine implementation}
 * package.
 * <p>
 * Note this type is not the same as the {@link FlowExecutionContext}. Objects
 * of this type are <i>request specific</i>: they provide a control interface
 * for manipulating exactly one flow execution locally from exactly one request.
 * A <code>FlowExecutionContext</code> provides information about a single
 * flow execution (conversation), and it's scope is not local to a specific
 * request (or thread).
 * 
 * @see org.springframework.webflow.engine.Flow
 * @see org.springframework.webflow.engine.State
 * @see org.springframework.webflow.execution.FlowExecution
 * @see FlowExecutionContext
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface RequestControlContext extends RequestContext {

	/**
	 * Record the last event signaled in the executing flow. This method will be
	 * called as part of signaling an event in a flow to indicate the
	 * 'lastEvent' that was signaled.
	 * @param lastEvent the last event signaled
	 * @see Flow#onEvent(RequestControlContext)
	 */
	public void setLastEvent(Event lastEvent);

	/**
	 * Record the last transition that executed in the executing flow. This
	 * method will be called as part of executing a transition from one state to
	 * another.
	 * @param lastTransition the last transition that executed
	 * @see Transition#execute(State, RequestControlContext)
	 */
	public void setLastTransition(Transition lastTransition);

	/**
	 * Record the current state that has entered in the executing flow. This
	 * method will be called as part of entering a new state by the State type
	 * itself.
	 * @param state the current state
	 * @see State#enter(RequestControlContext)
	 */
	public void setCurrentState(State state);

	/**
	 * Spawn a new flow session and activate it in the currently executing flow.
	 * Also transitions the spawned flow to its start state. This method should
	 * be called by clients that wish to spawn new flows, such as subflow
	 * states.
	 * <p>
	 * This will start a new flow session in the current flow execution, which
	 * is already active.
	 * @param flow the flow to start, its <code>start()</code> method will be
	 * called
	 * @param input initial contents of the newly created flow session (may be
	 * <code>null</code>, e.g. empty)
	 * @return the selected starting view, which returns control to the client
	 * and requests that a view be rendered with model data
	 * @throws FlowExecutionException if an exception was thrown within a state
	 * of the flow during execution of this start operation
	 * @see Flow#start(RequestControlContext, MutableAttributeMap)
	 */
	public ViewSelection start(Flow flow, MutableAttributeMap input) throws FlowExecutionException;

	/**
	 * Signals the occurence of an event in the current state of this flow
	 * execution request context. This method should be called by clients that
	 * report internal event occurences, such as action states. The
	 * <code>onEvent()</code> method of the flow involved in the flow
	 * execution will be called.
	 * @param event the event that occured
	 * @return the next selected view, which returns control to the client and
	 * requests that a view be rendered with model data
	 * @throws FlowExecutionException if an exception was thrown within a state
	 * of the flow during execution of this signalEvent operation
	 * @see Flow#onEvent(RequestControlContext)
	 */
	public ViewSelection signalEvent(Event event) throws FlowExecutionException;

	/**
	 * End the active flow session of the current flow execution. This method
	 * should be called by clients that terminate flows, such as end states. The
	 * <code>end()</code> method of the flow involved in the flow execution
	 * will be called.
	 * @param output output produced by the session that is eligible for mapping
	 * by a resuming parent flow
	 * @return the ended session
	 * @throws IllegalStateException when the flow execution is not active
	 * @see Flow#end(RequestControlContext, MutableAttributeMap)
	 */
	public FlowSession endActiveFlowSession(MutableAttributeMap output) throws IllegalStateException;

	/**
	 * Execute this transition out of the current source state. Allows for
	 * privileged execution of an arbitrary transition.
	 * @param transition the transition
	 * @return a new view selection
	 * @see Transition#execute(State, RequestControlContext)
	 */
	public ViewSelection execute(Transition transition);

}