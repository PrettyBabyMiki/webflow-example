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
package org.springframework.faces.webflow;

import java.lang.reflect.Method;
import java.util.Iterator;

import javax.el.ELContext;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.render.RenderKit;

import org.springframework.binding.message.Message;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.Severity;
import org.springframework.context.MessageSource;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.webflow.execution.RequestContext;

/**
 * Custom {@link FacesContext} implementation that delegates all standard FacesContext messaging functionality to a
 * Spring {@link MessageSource} made accessible as part of the current Web Flow request. Additionally, it manages the
 * {@code responseComplete} and {@code renderResponse} flags in flash scope so that the execution of the JSF
 * {@link Lifecycle} may span multiple requests in the case of the POST+REDIRECT+GET pattern being enabled.
 * 
 * @author Jeremy Grelle
 */
public class FlowFacesContext extends FacesContext {

	/**
	 * The key for storing the responseComplete flag
	 */
	static final String RESPONSE_COMPLETE_KEY = "flowResponseComplete";

	/**
	 * The key for storing the renderResponse flag
	 */
	static final String RENDER_RESPONSE_KEY = "flowRenderResponse";

	/**
	 * The key for storing the renderResponse flag
	 */
	private RequestContext context;

	/**
	 * The base FacesContext delegate
	 */
	private FacesContext delegate;

	public static FlowFacesContext newInstance(RequestContext context, Lifecycle lifecycle) {
		FacesContextFactory facesContextFactory = (FacesContextFactory) FactoryFinder
				.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
		FacesContext defaultFacesContext = facesContextFactory.getFacesContext(context.getExternalContext()
				.getNativeContext(), context.getExternalContext().getNativeRequest(), context.getExternalContext()
				.getNativeResponse(), lifecycle);
		return new FlowFacesContext(context, defaultFacesContext);
	}

	public FlowFacesContext(RequestContext context, FacesContext delegate) {
		this.context = context;
		this.delegate = delegate;
		setCurrentInstance(this);
	}

	/**
	 * Translates a FacesMessage to an SWF Message and adds it to the current MessageContext
	 */
	public void addMessage(String clientId, FacesMessage message) {
		MessageBuilder builder = new MessageBuilder();
		StringBuffer msgText = new StringBuffer();
		if (StringUtils.hasText(message.getSummary())) {
			msgText.append(message.getSummary());
		}

		String source = null;
		if (StringUtils.hasText(clientId)) {
			source = clientId;
		}

		if (message.getSeverity() == FacesMessage.SEVERITY_INFO) {
			builder.source(source).defaultText(msgText.toString()).info();
		} else if (message.getSeverity() == FacesMessage.SEVERITY_WARN) {
			builder.source(source).defaultText(msgText.toString()).warning();
		} else {
			builder.source(source).defaultText(msgText.toString()).error();
		}
		context.getMessageContext().addMessage(builder.build());
	}

	/**
	 * Returns an Iterator for all component clientId's for which messages have been added.
	 */
	public Iterator getClientIdsWithMessages() {
		return new ClientIdIterator();
	}

	/**
	 * Return the maximum severity level recorded on any FacesMessages that has been queued, whether or not they are
	 * associated with any specific UIComponent. If no such messages have been queued, return null.
	 */
	public FacesMessage.Severity getMaximumSeverity() {
		if (context.getMessageContext().getAllMessages().length == 0) {
			return null;
		}
		FacesMessage.Severity max = FacesMessage.SEVERITY_INFO;
		Iterator i = getMessages();
		while (i.hasNext()) {
			FacesMessage message = (FacesMessage) i.next();
			if (message.getSeverity().getOrdinal() > max.getOrdinal()) {
				max = message.getSeverity();
			}
			if (max.getOrdinal() == FacesMessage.SEVERITY_ERROR.getOrdinal())
				break;
		}
		return max;
	}

	/**
	 * Returns an Iterator for all Messages in the current MessageContext that does translation to FacesMessages.
	 */
	public Iterator getMessages() {
		return new FacesMessageIterator();
	}

	/**
	 * Returns an Iterator for all Messages with the given clientId in the current MessageContext that does translation
	 * to FacesMessages.
	 */
	public Iterator getMessages(String clientId) {
		return new FacesMessageIterator(clientId);
	}

	public boolean getRenderResponse() {
		Boolean renderResponse = context.getFlashScope().getBoolean(RENDER_RESPONSE_KEY);
		if (renderResponse == null) {
			return false;
		}
		return renderResponse.booleanValue();
	}

	public boolean getResponseComplete() {
		Boolean responseComplete = context.getFlashScope().getBoolean(RESPONSE_COMPLETE_KEY);
		if (responseComplete == null) {
			return false;
		}
		return responseComplete.booleanValue();
	}

	public void renderResponse() {
		// stored in flash scope to survive a redirect when transitioning from one view to another
		context.getFlashScope().put(RENDER_RESPONSE_KEY, Boolean.TRUE);
	}

	public void responseComplete() {
		context.getFlashScope().put(RESPONSE_COMPLETE_KEY, Boolean.TRUE);
	}

	// ------------------ Pass-through delegate methods ----------------------//

	public Application getApplication() {
		return delegate.getApplication();
	}

	public ELContext getELContext() {
		Method delegateMethod = ClassUtils.getMethodIfAvailable(delegate.getClass(), "getELContext", null);
		if (delegateMethod != null) {
			try {
				return (ELContext) delegateMethod.invoke(delegate, null);
			} catch (Exception e) {
				return null;
			}
		} else {
			return null;
		}
	}

	public ExternalContext getExternalContext() {
		return delegate.getExternalContext();
	}

	public RenderKit getRenderKit() {
		return delegate.getRenderKit();
	}

	public ResponseStream getResponseStream() {
		return delegate.getResponseStream();
	}

	public ResponseWriter getResponseWriter() {
		return delegate.getResponseWriter();
	}

	public UIViewRoot getViewRoot() {
		return delegate.getViewRoot();
	}

	public void release() {
		delegate.release();
		setCurrentInstance(null);
	}

	public void setResponseStream(ResponseStream responseStream) {
		delegate.setResponseStream(responseStream);
	}

	public void setResponseWriter(ResponseWriter responseWriter) {
		delegate.setResponseWriter(responseWriter);
	}

	public void setViewRoot(UIViewRoot root) {
		delegate.setViewRoot(root);
	}

	protected FacesContext getDelegate() {
		return delegate;
	}

	private class FacesMessageIterator implements Iterator {

		private Message[] messages;

		private int currentIndex = -1;

		protected FacesMessageIterator() {
			this.messages = context.getMessageContext().getAllMessages();
		}

		protected FacesMessageIterator(String clientId) {
			this.messages = context.getMessageContext().getMessagesBySource(clientId);
		}

		public boolean hasNext() {
			return messages.length > currentIndex + 1;
		}

		public Object next() {
			currentIndex++;
			Message nextMessage = messages[currentIndex];
			FacesMessage facesMessage;
			if (nextMessage.getSeverity() == Severity.INFO) {
				facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, nextMessage.getText(), nextMessage
						.getText());
			} else if (nextMessage.getSeverity() == Severity.WARNING) {
				facesMessage = new FacesMessage(FacesMessage.SEVERITY_WARN, nextMessage.getText(), nextMessage
						.getText());
			} else {
				facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, nextMessage.getText(), nextMessage
						.getText());
			}
			return facesMessage;
		}

		public void remove() {
			throw new UnsupportedOperationException("Messages cannot be removed through this iterator.");
		}

	}

	private class ClientIdIterator implements Iterator {

		private Message[] messages;

		int currentIndex = -1;

		protected ClientIdIterator() {
			this.messages = context.getMessageContext().getAllMessages();
		}

		public boolean hasNext() {
			while (messages.length > currentIndex + 1) {
				Message next = messages[currentIndex + 1];
				if (next.getSource() != null && !"".equals(next.getSource())) {
					return true;
				}
				currentIndex++;
			}
			return false;
		}

		public Object next() {
			Message next = messages[++currentIndex];
			while (next.getSource() == null || "".equals(next.getSource())) {
				next = messages[++currentIndex];
			}
			return next.getSource().toString();
		}

		public void remove() {
			throw new UnsupportedOperationException("Messages cannot be removed through this iterator.");
		}

	}

}
