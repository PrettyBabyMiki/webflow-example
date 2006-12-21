/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.execution.repository.continuation;

import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryException;

/**
 * Thrown when a continuation snapshot could not be taken of flow execution
 * state.
 * 
 * @author Keith Donald
 */
public class ContinuationCreationException extends FlowExecutionRepositoryException {

	/**
	 * The flow execution that could not be snapshotted.
	 */
	private FlowExecution flowExecution;

	/**
	 * Creates a new continuation creation exception.
	 * @param flowExecution the flow execution
	 * @param message a descriptive message
	 * @param cause the cause
	 */
	public ContinuationCreationException(FlowExecution flowExecution, String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Returns the flow execution that could not be snapshotted.
	 */
	public FlowExecution getFlowExecution() {
		return flowExecution;
	}
}