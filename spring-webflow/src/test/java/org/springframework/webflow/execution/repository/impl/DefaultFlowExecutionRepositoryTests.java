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
import org.springframework.webflow.conversation.impl.BadlyFormattedConversationIdException;
import org.springframework.webflow.conversation.impl.SimpleConversationId;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionConstructionException;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;
import org.springframework.webflow.definition.registry.NoSuchFlowDefinitionException;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.StubViewFactory;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.impl.FlowExecutionImplFactory;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionKey;
import org.springframework.webflow.execution.repository.BadlyFormattedFlowExecutionKeyException;
import org.springframework.webflow.execution.repository.FlowExecutionLock;
import org.springframework.webflow.execution.repository.FlowExecutionRestorationFailureException;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;
import org.springframework.webflow.execution.repository.snapshot.SerializedFlowExecutionSnapshotFactory;
import org.springframework.webflow.test.MockExternalContext;

public class DefaultFlowExecutionRepositoryTests extends TestCase {
	private Flow flow;
	private ConversationManager conversationManager;
	private DefaultFlowExecutionRepository repository;
	FlowExecutionImplFactory executionFactory = new FlowExecutionImplFactory();

	protected void setUp() throws Exception {
		flow = new Flow("myFlow");
		ViewState s1 = new ViewState(flow, "state", new StubViewFactory());
		s1.getTransitionSet().add(new Transition(new DefaultTargetStateResolver("state2")));
		new ViewState(flow, "state2", new StubViewFactory());

		conversationManager = new StubConversationManager();
		FlowDefinitionLocator locator = new FlowDefinitionLocator() {
			public FlowDefinition getFlowDefinition(String flowId) throws NoSuchFlowDefinitionException,
					FlowDefinitionConstructionException {
				return flow;
			}
		};
		SerializedFlowExecutionSnapshotFactory snapshotFactory = new SerializedFlowExecutionSnapshotFactory(
				executionFactory, locator);
		repository = new DefaultFlowExecutionRepository(conversationManager, snapshotFactory);
		executionFactory.setExecutionKeyFactory(repository);
	}

	public void testParseFlowExecutionKey() {
		String key = "e12345s54321";
		FlowExecutionKey k = repository.parseFlowExecutionKey(key);
		assertEquals(key, k.toString());
	}

	public void testParseBadlyFormattedFlowExecutionKey() {
		String key = "e12345";
		try {
			repository.parseFlowExecutionKey(key);
			fail("Should have failed");
		} catch (BadlyFormattedFlowExecutionKeyException e) {
			assertEquals("e12345", e.getInvalidKey());
			assertNotNull(e.getFormat());
		}
	}

	public void testParseBadlyFormattedFlowExecutionKeyBadContinuationId() {
		String key = "c12345vaaaa";
		try {
			repository.parseFlowExecutionKey(key);
			fail("Should have failed");
		} catch (BadlyFormattedFlowExecutionKeyException e) {
			assertEquals("c12345vaaaa", e.getInvalidKey());
			assertNotNull(e.getFormat());
		}
	}

	public void testGetLock() {
		FlowExecutionKey key = repository.parseFlowExecutionKey("e12345s54321");
		FlowExecutionLock lock = repository.getLock(key);
		assertNotNull(lock);
		lock.unlock();
	}

	public void testGetLockNoSuchFlowExecution() {
		FlowExecutionKey key = repository.parseFlowExecutionKey("e99999s54321");
		try {
			repository.getLock(key);
			fail("should have failed");
		} catch (NoSuchFlowExecutionException e) {
			e.printStackTrace();
		}
	}

	public void testPutFlowExecution() {
		FlowExecution execution = executionFactory.createFlowExecution(flow);
		execution.start(null, new MockExternalContext());
		assertNotNull(execution.getKey());
		repository.putFlowExecution(execution);
		String key = execution.getKey().toString();
		FlowExecutionKey parsedKey = repository.parseFlowExecutionKey(key);
		FlowExecution execution2 = repository.getFlowExecution(parsedKey);
		assertSame(execution.getDefinition(), execution2.getDefinition());
		assertEquals(execution.getActiveSession().getState().getId(), execution2.getActiveSession().getState().getId());
	}

	public void testPutFlowExecutionNextSnapshotId() {
		FlowExecution execution = executionFactory.createFlowExecution(flow);
		execution.start(null, new MockExternalContext());
		assertNotNull(execution.getKey());
		repository.putFlowExecution(execution);
		String key = execution.getKey().toString();
		FlowExecutionKey parsedKey = repository.parseFlowExecutionKey(key);
		FlowExecution execution2 = repository.getFlowExecution(parsedKey);
		assertSame(execution.getDefinition(), execution2.getDefinition());
		assertEquals(execution.getActiveSession().getState().getId(), execution2.getActiveSession().getState().getId());
		MockExternalContext context = new MockExternalContext();
		context.setEventId("foo");
		execution2.resume(context);
		repository.putFlowExecution(execution2);
		assertNotSame(execution.getKey(), execution2.getKey());
	}

	public void testPutFlowExecutionNoKeyAssigned() {
		FlowExecution execution = executionFactory.createFlowExecution(flow);
		try {
			repository.putFlowExecution(execution);
			fail("Should have failed");
		} catch (IllegalStateException e) {

		}
	}

	public void testRemoveFlowExecution() {
		FlowExecution execution = executionFactory.createFlowExecution(flow);
		execution.start(null, new MockExternalContext());
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
		FlowExecution execution = executionFactory.createFlowExecution(flow);
		try {
			repository.removeFlowExecution(execution);
			fail("Should have failed");
		} catch (IllegalStateException e) {

		}
	}

	public void testRemoveNoSuchFlowExecution() {
		FlowExecution execution = executionFactory.createFlowExecution(flow);
		execution.start(null, new MockExternalContext());
		try {
			repository.removeFlowExecution(execution);
			repository.removeFlowExecution(execution);
			fail("Should have failed");
		} catch (NoSuchFlowExecutionException e) {

		}
	}

	public void testGetKey() {
		FlowExecution execution = executionFactory.createFlowExecution(flow);
		assertEquals("e12345s1", repository.getKey(execution).toString());
		assertEquals("e12345s2", repository.getKey(execution).toString());
		assertEquals("e12345s3", repository.getKey(execution).toString());
	}

	public void testUpdate() {
		FlowExecution execution = executionFactory.createFlowExecution(flow);
		execution.start(null, new MockExternalContext());
		repository.putFlowExecution(execution);
		execution.getActiveSession().getScope().put("foo", "bar");
		repository.updateFlowExecutionSnapshot(execution);
		FlowExecution execution2 = repository.getFlowExecution(execution.getKey());
		assertEquals("bar", execution2.getActiveSession().getScope().get("foo"));
	}

	public void testRemove() {
		FlowExecution execution = executionFactory.createFlowExecution(flow);
		execution.start(null, new MockExternalContext());
		repository.putFlowExecution(execution);
		repository.removeFlowExecutionSnapshot(execution);
		try {
			repository.getFlowExecution(execution.getKey());
			fail("Should have failed");
		} catch (FlowExecutionRestorationFailureException e) {

		}
	}

	public void testRemoveAll() {
		FlowExecution execution = executionFactory.createFlowExecution(flow);
		execution.start(null, new MockExternalContext());
		repository.putFlowExecution(execution);
		repository.removeAllFlowExecutionSnapshots(execution);
		try {
			repository.getFlowExecution(execution.getKey());
			fail("Should have failed");
		} catch (FlowExecutionRestorationFailureException e) {

		}

	}

	public void testUpdateNothingToDo() {
		FlowExecution execution = executionFactory.createFlowExecution(flow);
		repository.updateFlowExecutionSnapshot(execution);
	}

	public void testRemoveNothingToDo() {
		FlowExecution execution = executionFactory.createFlowExecution(flow);
		repository.removeFlowExecutionSnapshot(execution);
	}

	public void testRemoveAllSnapshotsNothingToDo() {
		FlowExecution execution = executionFactory.createFlowExecution(flow);
		repository.removeAllFlowExecutionSnapshots(execution);
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
			try {
				return new SimpleConversationId(new Integer(Integer.parseInt(encodedId)));
			} catch (NumberFormatException e) {
				throw new BadlyFormattedConversationIdException(encodedId, e);
			}
		}

		private static class StubConversation implements Conversation {

			private final ConversationId ID = new SimpleConversationId(new Integer(12345));

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
