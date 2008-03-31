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
package org.springframework.webflow.engine.impl;

import java.util.ListIterator;

import org.springframework.util.Assert;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionKey;
import org.springframework.webflow.execution.FlowExecutionKeyFactory;
import org.springframework.webflow.execution.repository.support.FlowExecutionStateRestorer;

/**
 * Restores the transient state of deserialized {@link FlowExecutionImpl} objects.
 * 
 * @author Keith Donald
 */
public class FlowExecutionImplStateRestorer extends FlowExecutionImplServicesConfigurer implements
		FlowExecutionStateRestorer {

	/**
	 * Used to restore the flow execution's flow definition.
	 */
	private FlowDefinitionLocator definitionLocator;

	/**
	 * Creates a new execution transient state restorer.
	 * @param definitionLocator the flow definition locator
	 */
	public FlowExecutionImplStateRestorer(FlowDefinitionLocator definitionLocator) {
		Assert.notNull(definitionLocator, "The flow definition locator is required");
		this.definitionLocator = definitionLocator;
	}

	public FlowExecution restoreState(FlowExecution flowExecution, FlowExecutionKey key,
			MutableAttributeMap conversationScope, FlowExecutionKeyFactory keyFactory) {
		FlowExecutionImpl execution = (FlowExecutionImpl) flowExecution;
		if (execution.getFlowId() == null) {
			throw new IllegalStateException("Cannot restore flow execution impl: the flow id is null");
		}
		if (execution.getFlowSessions() == null) {
			throw new IllegalStateException("Cannot restore flow execution impl: the flowSessions list is null");
		}
		Flow flow = (Flow) definitionLocator.getFlowDefinition(execution.getFlowId());
		execution.setFlow(flow);
		if (execution.hasSessions()) {
			FlowSessionImpl rootSession = execution.getRootSession();
			rootSession.setFlow(flow);
			rootSession.setState(flow.getStateInstance(rootSession.getStateId()));
			if (execution.hasSubflowSessions()) {
				for (ListIterator it = execution.getSubflowSessionIterator(); it.hasNext();) {
					FlowSessionImpl subflowSession = (FlowSessionImpl) it.next();
					Flow definition = (Flow) definitionLocator.getFlowDefinition(subflowSession.getFlowId());
					subflowSession.setFlow(definition);
					subflowSession.setState(definition.getStateInstance(subflowSession.getStateId()));
				}
			}
		}
		execution.setKey(key);
		if (conversationScope == null) {
			conversationScope = new LocalAttributeMap();
		}
		execution.setConversationScope(conversationScope);
		configureServices(execution);
		execution.setKeyFactory(keyFactory);
		return execution;
	}
}