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
package org.springframework.webflow.execution;

import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;

/**
 * An execution of a flow definition. This is the central interface for manipulating a runtime execution of a flow
 * definition.
 * 
 * A FlowExecution instance is typically created by a {@link FlowExecutionFactory factory}. A FlowExecution instance is
 * persisted using a {@link FlowExecutionRepository repository}. A FlowExecution's lifecycle is observed by zero or
 * more {@link FlowExecutionListener listeners}
 * 
 * @see FlowDefinition
 * @see FlowSession
 * @see RequestContext
 * @see FlowExecutionListener
 * @see org.springframework.webflow.execution.repository.FlowExecutionRepository
 * @see org.springframework.webflow.executor.FlowExecutor
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowExecution extends FlowExecutionContext {

	/**
	 * Start this flow execution. This method should only be called once.
	 * <p>
	 * When this method returns, execution status is either "paused" or "ended". If ended, the flow execution cannot be
	 * used again. If "paused", the flow execution may be {@link #resume(ExternalContext)}.
	 * @param context the external context representing the calling environment
	 * @throws FlowExecutionException if an exception was thrown within a state of the flow execution during request
	 * processing
	 */
	public void start(ExternalContext context) throws FlowExecutionException;

	/**
	 * Resume this flow execution. May be called when the flow execution is paused.
	 * 
	 * When this method returns, execution status is either "paused" or "ended". If ended, the flow execution cannot be
	 * used again. If "paused", the flow execution may be resumed again.
	 * @param context the external context, representing the calling environment, where something happened this flow
	 * execution should respond to
	 * @throws FlowExecutionException if an exception was thrown within a state of the resumed flow execution during
	 * event processing
	 */
	public void resume(ExternalContext context) throws FlowExecutionException;

}