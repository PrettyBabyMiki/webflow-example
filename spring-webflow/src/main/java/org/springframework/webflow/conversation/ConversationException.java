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
package org.springframework.webflow.conversation;

import org.springframework.webflow.core.FlowException;

/**
 * The root of the conversation service exception hierarchy.
 * 
 * @author Keith Donald
 */
public class ConversationException extends FlowException {

	/**
	 * Creates a conversation service exception.
	 * @param message a descriptive message
	 */
	public ConversationException(String message) {
		super(message);
	}

	/**
	 * Creates a conversation service exception.
	 * @param message a descriptive message
	 * @param cause the root cause of the problem
	 */
	public ConversationException(String message, Throwable cause) {
		super(message, cause);
	}
}