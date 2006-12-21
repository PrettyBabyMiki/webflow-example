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
package org.springframework.webflow.conversation.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.conversation.Conversation;
import org.springframework.webflow.conversation.ConversationException;
import org.springframework.webflow.conversation.ConversationId;
import org.springframework.webflow.conversation.ConversationManager;
import org.springframework.webflow.conversation.ConversationParameters;
import org.springframework.webflow.core.collection.SharedAttributeMap;
import org.springframework.webflow.util.RandomGuidUidGenerator;
import org.springframework.webflow.util.UidGenerator;

/**
 * Simple implementation of a conversation manager that stores conversations in
 * the session attribute map.
 * <p>
 * Using the {@link #setMaxConversations(int) maxConversations} property, you can
 * limit the number of concurrently active conversations allowed in a single
 * session. If the default is exceeded, the conversation manager will automatically
 * end the oldest conversation. The default is 5, which should be fine for most
 * situations. Set it to -1 for no limit. Setting maxConversations to 1 allows
 * easy resource cleanup in situations where there should only be one active
 * conversation per session.
 * 
 * @author Erwin Vervaet
 */
public class SessionBindingConversationManager implements ConversationManager {

	private static final Log logger = LogFactory.getLog(SessionBindingConversationManager.class);

	/**
	 * Key of the session attribute holding the conversation container.
	 */
	private static final String CONVERSATION_CONTAINER_KEY = "webflow.conversation.container";
	
	/**
	 * The conversation uid generation strategy to use.
	 */
	private UidGenerator conversationIdGenerator = new RandomGuidUidGenerator();

	/**
	 * The maximum number of active conversations allowed in a session.
	 * The default is 5. This is high enough for most practical situations and low enough
	 * to avoid excessive resource usage or easy denial of service attacks.
	 */
	private int maxConversations = 5;
	
	/**
	 * Returns the used generator for conversation ids. Defaults to
	 * {@link RandomGuidUidGenerator}.
	 */
	public UidGenerator getConversationIdGenerator() {
		return conversationIdGenerator;
	}

	/**
	 * Sets the configured generator for conversation ids.
	 */
	public void setConversationIdGenerator(UidGenerator uidGenerator) {
		this.conversationIdGenerator = uidGenerator;
	}
	
	/**
	 * Returns the maximum number of allowed concurrent conversations. The
	 * default is 5.
	 */
	public int getMaxConversations() {
		return maxConversations;
	}

	/**
	 * Set the maximum number of allowed concurrent conversations. Set to -1 for
	 * no limit. The default is 5.
	 */
	public void setMaxConversations(int maxConversations) {
		this.maxConversations = maxConversations;
	}

	public Conversation beginConversation(ConversationParameters conversationParameters) throws ConversationException {
		ConversationId conversationId = new SimpleConversationId(conversationIdGenerator.generateUid());
		if (logger.isDebugEnabled()) {
			logger.debug("Beginning conversation " + conversationParameters +
					"; unique conversation id = " + conversationId);
		}		
		return getConversationContainer().createAndAddConversation(conversationId, conversationParameters);
	}

	public Conversation getConversation(ConversationId id) throws ConversationException {
		if (logger.isDebugEnabled()) {
			logger.debug("Getting conversation " + id);
		}
		return getConversationContainer().getConversation(id);
	}

	public ConversationId parseConversationId(String encodedId) throws ConversationException {
		return new SimpleConversationId(conversationIdGenerator.parseUid(encodedId));
	}

	// internal helpers

	/**
	 * Obtain the conversation container from the session. Create a new empty
	 * container and add it to the session if no existing container can be
	 * found.
	 */
	private ConversationContainer getConversationContainer() {
		SharedAttributeMap sessionMap = ExternalContextHolder.getExternalContext().getSessionMap();
		synchronized (sessionMap.getMutex()) {
			ConversationContainer container = (ConversationContainer)sessionMap.get(CONVERSATION_CONTAINER_KEY);
			if (container == null) {
				container = new ConversationContainer(maxConversations);
				sessionMap.put(CONVERSATION_CONTAINER_KEY, container);
			}
			return container;
		}
	}
}