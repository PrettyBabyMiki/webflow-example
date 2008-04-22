/*
 * Copyright 2004-2008 the original author or authors.
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
package org.springframework.webflow.execution.repository.impl;

import org.springframework.webflow.conversation.Conversation;
import org.springframework.webflow.conversation.ConversationManager;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionKey;
import org.springframework.webflow.execution.repository.FlowExecutionRestorationFailureException;
import org.springframework.webflow.execution.repository.continuation.AbstractFlowExecutionContinuationRepository;
import org.springframework.webflow.execution.repository.continuation.ContinuationNotFoundException;
import org.springframework.webflow.execution.repository.continuation.ContinuationUnmarshalException;
import org.springframework.webflow.execution.repository.continuation.FlowExecutionContinuation;
import org.springframework.webflow.execution.repository.continuation.FlowExecutionContinuationFactory;
import org.springframework.webflow.execution.repository.continuation.SerializedFlowExecutionContinuationFactory;
import org.springframework.webflow.execution.repository.support.FlowExecutionStateRestorer;

/**
 * Stores <i>one to many</i> flow execution continuations (snapshots) per conversation, where each continuation
 * represents a paused, restorable view-state of a flow execution snapshotted at a point in time.
 * <p>
 * The set of active user conversations are managed by a {@link ConversationManager} implementation, which this
 * repository delegates to.
 * <p>
 * This repository is responsible for:
 * <ul>
 * <li>Beginning a new conversation when a new flow execution is made persistent. Each conversation is assigned a
 * unique conversation id which forms one part of the flow execution key.
 * <li>Associating a flow execution with that conversation by adding a {@link FlowExecutionContinuation} to a
 * continuation group.<br>
 * When a flow execution is placed in this repository a new continuation snapshot is created, assigned an id, and added
 * to the group. Each continuation logically represents a state of the conversation at a point in time <i>that can be
 * restored and continued</i>. These continuations can be restored to support users going back in their browser to
 * continue a conversation from a previous point.
 * <li>Ending existing conversations when persistent flow executions end, as part of a repository removal operation.
 * </ul>
 * <p>
 * This repository implementation also provides support for <i>conversation invalidation after completion</i>, where
 * once a logical conversation completes (by one of its FlowExecution's reaching an end state), the entire conversation
 * (including all continuations) is invalidated. This prevents the possibility of duplicate submission after completion.
 * <p>
 * This repository implementation should be considered when you do have to support browser navigational button use, e.g.
 * you cannot lock down the browser and require that all navigational events to be routed explicitly through Spring Web
 * Flow.
 * 
 * @author Keith Donald
 */
public class DefaultFlowExecutionRepository extends AbstractFlowExecutionContinuationRepository {

	/**
	 * The conversation attribute that stores the "continuation group".
	 */
	private static final String CONTINUATION_GROUP_ATTRIBUTE = "continuationGroup";

	/**
	 * The maximum number of continuations that can be active per conversation. The default is 30, which is high enough
	 * not to interfere with the user experience of normal users using the back button, but low enough to avoid
	 * excessive resource usage or easy denial of service attacks.
	 */
	private int maxContinuations = 30;

	/**
	 * Create a new continuation based flow execution repository using the given state restorer and conversation
	 * manager. Defaults to a {@link SerializedFlowExecutionContinuationFactory}.
	 * @param conversationManager the conversation manager to use
	 * @param executionStateRestorer the state restoration strategy to use
	 */
	public DefaultFlowExecutionRepository(ConversationManager conversationManager,
			FlowExecutionStateRestorer executionStateRestorer) {
		super(conversationManager, executionStateRestorer, new SerializedFlowExecutionContinuationFactory());
	}

	/**
	 * Create a new continuation based flow execution repository using the given state restorer, conversation manager,
	 * and continuation factory.
	 * @param conversationManager the conversation manager to use
	 * @param executionStateRestorer the state restoration strategy to use
	 * @param continuationFactory the continuation factory to use
	 */
	public DefaultFlowExecutionRepository(ConversationManager conversationManager,
			FlowExecutionStateRestorer executionStateRestorer, FlowExecutionContinuationFactory continuationFactory) {
		super(conversationManager, executionStateRestorer, continuationFactory);
	}

	/**
	 * Returns the max number of continuations allowed per conversation by this repository.
	 */
	public int getMaxContinuations() {
		return maxContinuations;
	}

	/**
	 * Sets the maximum number of continuations allowed per conversation by this repository. Use -1 for unlimited. The
	 * default is 30.
	 */
	public void setMaxContinuations(int maxContinuations) {
		this.maxContinuations = maxContinuations;
	}

	// implementing flow execution repository

	public FlowExecution getFlowExecution(FlowExecutionKey key) {
		if (logger.isDebugEnabled()) {
			logger.debug("Getting flow execution with key '" + key + "'");
		}
		Conversation conversation = getConversation(key);
		FlowExecutionContinuation snapshot;
		try {
			snapshot = getContinuationGroup(conversation).get(getContinuationId(key));
		} catch (ContinuationNotFoundException e) {
			throw new FlowExecutionRestorationFailureException(key, e);
		}
		try {
			FlowExecution execution = snapshot.unmarshal();
			return restoreTransientState(execution, key, conversation);
		} catch (ContinuationUnmarshalException e) {
			throw new FlowExecutionRestorationFailureException(key, e);
		}
	}

	public void putFlowExecution(FlowExecution flowExecution) {
		assertKeySet(flowExecution);
		if (logger.isDebugEnabled()) {
			logger.debug("Putting flow execution '" + flowExecution + "' into repository");
		}
		FlowExecutionKey key = flowExecution.getKey();
		Conversation conversation = getConversation(key);
		FlowExecutionContinuationGroup continuationGroup = getContinuationGroup(conversation);
		FlowExecutionContinuation snapshot = snapshot(flowExecution);
		if (logger.isDebugEnabled()) {
			logger.debug("Adding new snapshot to group with id " + getContinuationId(key));
		}
		continuationGroup.add(getContinuationId(key), snapshot);
		putConversationScope(flowExecution, conversation);
	}

	// implementing flow execution key factory

	public void removeAllFlowExecutionSnapshots(FlowExecution execution) {
		Conversation conversation = getConversation(execution.getKey());
		getContinuationGroup(conversation).removeAllContinuations();
	}

	public void removeFlowExecutionSnapshot(FlowExecution execution) {
		FlowExecutionKey key = execution.getKey();
		Conversation conversation = getConversation(key);
		getContinuationGroup(conversation).removeContinuation(getContinuationId(key));
	}

	public void updateFlowExecutionSnapshot(FlowExecution execution) {
		FlowExecutionKey key = execution.getKey();
		Conversation conversation = getConversation(key);
		getContinuationGroup(conversation).updateContinuation(getContinuationId(key), snapshot(execution));
	}

	// hooks for subclassing

	protected FlowExecutionContinuationGroup createFlowExecutionContinuationGroup() {
		return new FlowExecutionContinuationGroup(maxContinuations);
	}

	/**
	 * Returns the continuation group associated with the governing conversation.
	 * @param conversation the conversation where the continuation group is stored
	 * @return the continuation group
	 */
	protected FlowExecutionContinuationGroup getContinuationGroup(Conversation conversation) {
		FlowExecutionContinuationGroup group = (FlowExecutionContinuationGroup) conversation
				.getAttribute(CONTINUATION_GROUP_ATTRIBUTE);
		if (group == null) {
			group = createFlowExecutionContinuationGroup();
			conversation.putAttribute(CONTINUATION_GROUP_ATTRIBUTE, group);
		}
		return group;
	}
}