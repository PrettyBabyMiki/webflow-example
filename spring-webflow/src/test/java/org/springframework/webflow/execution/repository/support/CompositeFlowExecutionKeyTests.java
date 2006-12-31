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
package org.springframework.webflow.execution.repository.support;

import junit.framework.TestCase;

import org.springframework.webflow.conversation.impl.SimpleConversationId;

/**
 * Unit tests for {@link CompositeFlowExecutionKey}.
 */
public class CompositeFlowExecutionKeyTests extends TestCase {

	public void testValidKey() {
		CompositeFlowExecutionKey key = new CompositeFlowExecutionKey(new SimpleConversationId("foo"), "bar");
		assertEquals("_cfoo_kbar", key.toString());
	}

	public void testKeyEquals() {
		CompositeFlowExecutionKey key = new CompositeFlowExecutionKey(new SimpleConversationId("foo"), "bar");
		CompositeFlowExecutionKey key2 = new CompositeFlowExecutionKey(new SimpleConversationId("foo"), "bar");
		assertEquals(key, key2);
	}

}
