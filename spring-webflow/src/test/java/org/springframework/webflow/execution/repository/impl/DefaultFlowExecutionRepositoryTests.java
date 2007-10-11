package org.springframework.webflow.execution.repository.impl;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.webflow.conversation.Conversation;
import org.springframework.webflow.conversation.ConversationException;
import org.springframework.webflow.conversation.ConversationId;
import org.springframework.webflow.conversation.ConversationManager;
import org.springframework.webflow.conversation.ConversationParameters;
import org.springframework.webflow.conversation.NoSuchConversationException;
import org.springframework.webflow.conversation.impl.SimpleConversationId;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionConstructionException;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;
import org.springframework.webflow.definition.registry.NoSuchFlowDefinitionException;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.RequestControlContext;
import org.springframework.webflow.engine.State;
import org.springframework.webflow.engine.impl.FlowExecutionImplFactory;
import org.springframework.webflow.engine.impl.FlowExecutionImplStateRestorer;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionException;
import org.springframework.webflow.execution.FlowExecutionKey;
import org.springframework.webflow.execution.repository.BadlyFormattedFlowExecutionKeyException;
import org.springframework.webflow.execution.repository.FlowExecutionLock;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;
import org.springframework.webflow.test.MockExternalContext;

public class DefaultFlowExecutionRepositoryTests extends TestCase {
	private Flow flow;
	private ConversationManager conversationManager;
	private FlowExecutionImplStateRestorer stateRestorer;
	private DefaultFlowExecutionRepository repository;

	protected void setUp() throws Exception {
		flow = new Flow("myFlow");
		new State(flow, "state") {
			protected void doEnter(RequestControlContext context) throws FlowExecutionException {
				context.assignFlowExecutionKey();
			}
		};
		conversationManager = new StubConversationManager();
		stateRestorer = new FlowExecutionImplStateRestorer(new FlowDefinitionLocator() {
			public FlowDefinition getFlowDefinition(String flowId) throws NoSuchFlowDefinitionException,
					FlowDefinitionConstructionException {
				return flow;
			}
		});
		repository = new DefaultFlowExecutionRepository(conversationManager, stateRestorer);
	}

	public void testParseFlowExecutionKey() {
		String key = "_c12345_k54321";
		FlowExecutionKey k = repository.parseFlowExecutionKey(key);
		assertEquals(key, k.toString());
	}

	public void testParseBadlyFormattedFlowExecutionKey() {
		String key = "_c12345";
		try {
			repository.parseFlowExecutionKey(key);
			fail("Should have failed");
		} catch (BadlyFormattedFlowExecutionKeyException e) {
			assertEquals("_c12345", e.getInvalidKey());
			assertNotNull(e.getFormat());
		}
	}

	public void testGetLock() {
		FlowExecutionKey key = repository.parseFlowExecutionKey("_c12345_k54321");
		FlowExecutionLock lock = repository.getLock(key);
		assertNotNull(lock);
		lock.unlock();
	}

	public void testGetLockNoSuchFlowExecution() {
		FlowExecutionKey key = repository.parseFlowExecutionKey("_cbogus_k54321");
		try {
			repository.getLock(key);
			fail("should have failed");
		} catch (NoSuchFlowExecutionException e) {

		}
	}

	public void testPutFlowExecution() {
		FlowExecutionImplFactory factory = new FlowExecutionImplFactory();
		factory.setExecutionKeyFactory(repository);
		FlowExecution execution = factory.createFlowExecution(flow);
		execution.start(new MockExternalContext());
		assertNotNull(execution.getKey());
		repository.putFlowExecution(execution);
		FlowExecution execution2 = repository.getFlowExecution(execution.getKey());
		assertSame(execution.getDefinition(), execution2.getDefinition());
		assertEquals(execution.getActiveSession().getState().getId(), execution2.getActiveSession().getState().getId());
	}

	public void testPutFlowExecutionNoKeyAssigned() {
		FlowExecutionImplFactory factory = new FlowExecutionImplFactory();
		FlowExecution execution = factory.createFlowExecution(flow);
		try {
			repository.putFlowExecution(execution);
			fail("Should have failed");
		} catch (IllegalStateException e) {

		}
	}

	public void testRemoveFlowExecution() {
		FlowExecutionImplFactory factory = new FlowExecutionImplFactory();
		factory.setExecutionKeyFactory(repository);
		FlowExecution execution = factory.createFlowExecution(flow);
		execution.start(new MockExternalContext());
		assertNotNull(execution.getKey());
		repository.putFlowExecution(execution);
		repository.removeFlowExecution(execution);
		try {
			repository.getFlowExecution(execution.getKey());
			fail("Should have failed");
		} catch (NoSuchFlowExecutionException e) {

		}
	}

	public void testRemoveKeyNotSet() {
		FlowExecutionImplFactory factory = new FlowExecutionImplFactory();
		FlowExecution execution = factory.createFlowExecution(flow);
		try {
			repository.removeFlowExecution(execution);
			fail("Should have failed");
		} catch (IllegalStateException e) {

		}
	}

	public void testRemoveNoSuchFlowExecution() {
		FlowExecutionImplFactory factory = new FlowExecutionImplFactory();
		factory.setExecutionKeyFactory(repository);
		FlowExecution execution = factory.createFlowExecution(flow);
		execution.start(new MockExternalContext());
		try {
			repository.removeFlowExecution(execution);
			repository.removeFlowExecution(execution);
			fail("Should have failed");
		} catch (NoSuchFlowExecutionException e) {

		}
	}

	public static class StubConversationManager implements ConversationManager {

		/**
		 * The single conversation managed by the manager.
		 */
		private final StubConversation INSTANCE = new StubConversation();

		public Conversation beginConversation(ConversationParameters conversationParameters)
				throws ConversationException {
			return INSTANCE;
		}

		public Conversation getConversation(ConversationId id) throws ConversationException {
			if (id.equals(INSTANCE.getId()) && !INSTANCE.hasEnded()) {
				return INSTANCE;
			} else {
				throw new NoSuchConversationException(id);
			}
		}

		public ConversationId parseConversationId(String encodedId) throws ConversationException {
			return new SimpleConversationId(encodedId);
		}

		private static class StubConversation implements Conversation {

			private final ConversationId ID = new SimpleConversationId("12345");

			private boolean locked;

			private boolean ended;

			private Map attributes = new HashMap();

			public boolean hasEnded() {
				return ended;
			}

			public boolean isLocked() {
				return locked;
			}

			public ConversationId getId() {
				return ID;
			}

			public void lock() {
				locked = true;
			}

			public Object getAttribute(Object name) {
				return attributes.get(name);
			}

			public void putAttribute(Object name, Object value) {
				attributes.put(name, value);
			}

			public void removeAttribute(Object name) {
				attributes.remove(name);
			}

			public void end() {
				ended = true;
			}

			public void unlock() {
				locked = false;
			}
		}
	}
}
