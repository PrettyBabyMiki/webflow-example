package org.springframework.faces.webflow;

import java.util.Iterator;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.springframework.binding.message.Message;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.message.MessageCriteria;
import org.springframework.binding.message.MessageResolver;
import org.springframework.binding.message.Severity;
import org.springframework.webflow.execution.RequestContext;

public class FlowFacesContextTests extends TestCase {

	JSFMockHelper jsf = new JSFMockHelper();

	FacesContext facesContext;

	RequestContext requestContext = (RequestContext) EasyMock.createMock(RequestContext.class);

	MessageContext messageContext;

	protected void setUp() throws Exception {
		jsf.setUp();
		facesContext = new FlowFacesContext(requestContext, jsf.facesContext());
	}

	protected void tearDown() throws Exception {
		jsf.tearDown();
	}

	public final void testCurrentInstance() {
		assertSame(FacesContext.getCurrentInstance(), facesContext);
	}

	public final void testAddMessage() {
		messageContext = new TestAddMessageContext();
		EasyMock.expect(requestContext.getMessageContext()).andStubReturn(messageContext);
		EasyMock.replay(new Object[] { requestContext });

		facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "foo", "foo"));

		assertEquals("Message count is incorrect", 1, ((TestAddMessageContext) messageContext).messageCount);
	}

	public final void testGetMessages() {
		messageContext = new TestGetMessagesContext();
		EasyMock.expect(requestContext.getMessageContext()).andStubReturn(messageContext);
		EasyMock.replay(new Object[] { requestContext });

		int iterationCount = 0;
		Iterator i = facesContext.getMessages();
		while (i.hasNext()) {
			assertNotNull(i.next());
			iterationCount++;
		}
		assertEquals(3, iterationCount);
	}

	public final void testGetMessagesByClientId() {
		messageContext = new TestGetMessagesContext();
		EasyMock.expect(requestContext.getMessageContext()).andStubReturn(messageContext);
		EasyMock.replay(new Object[] { requestContext });

		int iterationCount = 0;
		Iterator i = facesContext.getMessages("componentId");
		while (i.hasNext()) {
			assertNotNull(i.next());
			iterationCount++;
		}
		assertEquals(1, iterationCount);
	}

	public final void testGetClientIdsWithMessages() {
		messageContext = new TestGetMessagesContext();
		EasyMock.expect(requestContext.getMessageContext()).andStubReturn(messageContext);
		EasyMock.replay(new Object[] { requestContext });

		int iterationCount = 0;
		Iterator i = facesContext.getClientIdsWithMessages();
		while (i.hasNext()) {
			String id = (String) i.next();
			assertEquals("componentId", id);
			iterationCount++;
		}
		assertEquals(1, iterationCount);
	}

	public final void testGetMaximumSeverity() {
		messageContext = new TestGetMessagesContext();
		EasyMock.expect(requestContext.getMessageContext()).andStubReturn(messageContext);
		EasyMock.replay(new Object[] { requestContext });

		assertEquals(FacesMessage.SEVERITY_ERROR, facesContext.getMaximumSeverity());
	}

	private class TestAddMessageContext implements MessageContext {
		int messageCount = 0;

		public void addMessage(MessageResolver messageResolver) {
			messageCount++;
		}

		public Message[] getAllMessages() {
			return null;
		}

		public Message[] getMessagesBySource(Object source) {
			throw new UnsupportedOperationException("Auto-generated method stub");
		}

		public Message[] getMessagesByCriteria(MessageCriteria criteria) {
			throw new UnsupportedOperationException("Auto-generated method stub");
		}

		public void clearMessages() {
			throw new UnsupportedOperationException("Auto-generated method stub");
		}
	}

	private class TestGetMessagesContext implements MessageContext {

		Message[] messages;

		TestGetMessagesContext() {
			messages = new Message[3];
			messages[0] = new Message(null, "foo", Severity.INFO);
			messages[1] = new Message("componentId", "bar", Severity.WARNING);
			messages[2] = new Message(null, "baz", Severity.ERROR);
		}

		public void addMessage(MessageResolver messageResolver) {

		}

		public Message[] getAllMessages() {
			return messages;
		}

		public Message[] getMessagesBySource(Object source) {
			return new Message[] { messages[1] };
		}

		public Message[] getMessagesByCriteria(MessageCriteria criteria) {
			throw new UnsupportedOperationException("Auto-generated method stub");
		}

		public void clearMessages() {
			throw new UnsupportedOperationException("Auto-generated method stub");
		}

	}

}
