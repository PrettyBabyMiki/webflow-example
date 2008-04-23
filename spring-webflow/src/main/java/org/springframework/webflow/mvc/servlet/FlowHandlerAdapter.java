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
package org.springframework.webflow.mvc.servlet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.js.mvc.servlet.AjaxHandler;
import org.springframework.js.mvc.servlet.SpringJavascriptAjaxHandler;
import org.springframework.util.Assert;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.webflow.context.servlet.DefaultFlowUrlHandler;
import org.springframework.webflow.context.servlet.FlowUrlHandler;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.FlowException;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.FlowExecutionOutcome;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;
import org.springframework.webflow.executor.FlowExecutionResult;
import org.springframework.webflow.executor.FlowExecutor;

/**
 * A custom MVC HandlerAdapter that encapsulates the generic workflow associated with executing flows. Delegates to
 * mapped {@link FlowHandler flow handlers} to manage the interaction with executions of specific flow definitions.
 * 
 * @author Keith Donald
 */
public class FlowHandlerAdapter extends WebApplicationObjectSupport implements HandlerAdapter, InitializingBean {

	private static final Log logger = LogFactory.getLog(FlowHandlerAdapter.class);

	private static final String SERVLET_RELATIVE_LOCATION_PREFIX = "servletRelative:";

	private static final String CONTEXT_RELATIVE_LOCATION_PREFIX = "contextRelative:";

	private static final String SERVER_RELATIVE_LOCATION_PREFIX = "serverRelative:";

	private static final String URL_LOCATION_PREFIX = "url:";

	/**
	 * The entry point into Spring Web Flow.
	 */
	private FlowExecutor flowExecutor;

	/**
	 * A strategy for extracting flow arguments and generating flow urls.
	 */
	private FlowUrlHandler urlHandler;

	/**
	 * The representation of an Ajax client service capable of interacting with web flow.
	 */
	private AjaxHandler ajaxHandler;

	/**
	 * Creates a new flow handler adapter.
	 * @see #setFlowExecutor(FlowExecutor)
	 * @see #setFlowUrlHandler(FlowUrlHandler)
	 * @see #setAjaxHandler(AjaxHandler)
	 * @see #afterPropertiesSet()
	 */
	public FlowHandlerAdapter() {
	}

	/**
	 * Returns the central service for executing flows. Required.
	 */
	public FlowExecutor getFlowExecutor() {
		return flowExecutor;
	}

	/**
	 * Sets the central service for executing flows. Required.
	 * @param flowExecutor
	 */
	public void setFlowExecutor(FlowExecutor flowExecutor) {
		this.flowExecutor = flowExecutor;
	}

	/**
	 * Returns the flow url handler.
	 */
	public FlowUrlHandler getFlowUrlHandler() {
		return urlHandler;
	}

	/**
	 * Sets the flow url handler
	 * @param urlHandler the flow url handler
	 */
	public void setFlowUrlHandler(FlowUrlHandler urlHandler) {
		this.urlHandler = urlHandler;
	}

	/**
	 * Returns the configured Ajax handler.
	 */
	public AjaxHandler getAjaxHandler() {
		return ajaxHandler;
	}

	/**
	 * Sets the configured Ajax handler.
	 * @param ajaxHandler the ajax handler
	 */
	public void setAjaxHandler(AjaxHandler ajaxHandler) {
		this.ajaxHandler = ajaxHandler;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(flowExecutor, "The FlowExecutor to execute flows is required");
		if (urlHandler == null) {
			this.urlHandler = new DefaultFlowUrlHandler();
		}
		if (ajaxHandler == null) {
			this.ajaxHandler = new SpringJavascriptAjaxHandler();
		}
	}

	public boolean supports(Object handler) {
		return handler instanceof FlowHandler;
	}

	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		FlowHandler flowHandler = (FlowHandler) handler;
		String flowExecutionKey = urlHandler.getFlowExecutionKey(request);
		if (flowExecutionKey != null) {
			try {
				ServletExternalContext context = createServletExternalContext(request, response);
				FlowExecutionResult result = flowExecutor.resumeExecution(flowExecutionKey, context);
				handleFlowExecutionResult(result, context, request, response, flowHandler);
			} catch (FlowException e) {
				handleFlowException(e, request, response, flowHandler);
			}
		} else {
			try {
				String flowId = getFlowId(flowHandler, request);
				MutableAttributeMap input = getInputMap(flowHandler, request);
				ServletExternalContext context = createServletExternalContext(request, response);
				FlowExecutionResult result = flowExecutor.launchExecution(flowId, input, context);
				handleFlowExecutionResult(result, context, request, response, flowHandler);
			} catch (FlowException e) {
				handleFlowException(e, request, response, flowHandler);
			}
		}
		return null;
	}

	// subclassing hooks

	/**
	 * Creates the servlet external context for the current HTTP servlet request.
	 * @param request the current request
	 * @param response the current response
	 */
	protected ServletExternalContext createServletExternalContext(HttpServletRequest request,
			HttpServletResponse response) {
		ServletExternalContext context = new MvcExternalContext(getServletContext(), request, response, urlHandler);
		context.setAjaxRequest(ajaxHandler.isAjaxRequest(getServletContext(), request, response));
		return context;
	}

	/**
	 * The default algorithm to determine the id of the flow to launch from the current request. Only called if
	 * {@link FlowHandler#getFlowId()} returns null. This implementation delegates to the configured
	 * {@link FlowUrlHandler#getFlowId(HttpServletRequest)}.
	 * @param request the current request
	 */
	protected String defaultGetFlowId(HttpServletRequest request) {
		return urlHandler.getFlowId(request);
	}

	/**
	 * The default algorithm to create the flow execution input map. Only called if
	 * {@link FlowHandler#createExecutionInputMap(HttpServletRequest)} returns null. This implementation exposes all
	 * current request parameters as flow execution input attributes.
	 * @param request the current request
	 */
	protected MutableAttributeMap defaultCreateFlowExecutionInputMap(HttpServletRequest request) {
		Map parameterMap = request.getParameterMap();
		if (parameterMap.size() == 0) {
			return null;
		}
		LocalAttributeMap inputMap = new LocalAttributeMap(parameterMap.size(), 1);
		Iterator it = parameterMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String name = (String) entry.getKey();
			String[] values = (String[]) entry.getValue();
			if (values.length == 1) {
				inputMap.put(name, values[0]);
			} else {
				inputMap.put(name, values);
			}
		}
		return inputMap;
	}

	/**
	 * The default algorithm for handling a flow execution outcome. Only called if
	 * {@link FlowHandler#handleExecutionOutcome(FlowExecutionOutcome, HttpServletRequest, HttpServletResponse)} returns
	 * null. This implementation attempts to start a new execution of the ended flow. Any flow execution output is
	 * passed as input to the new execution.
	 * @param flowId the id of the ended flow
	 * @param outcome the flow execution outcome
	 * @param request the current request
	 * @param response the current response
	 */
	protected void defaultHandleExecutionOutcome(String flowId, FlowExecutionOutcome outcome,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (!response.isCommitted()) {
			// by default, just start the flow over passing the output as input
			if (logger.isDebugEnabled()) {
				logger.debug("Restarting a new execution of ended flow '" + flowId + "'");
			}
			response.sendRedirect(urlHandler.createFlowDefinitionUrl(flowId, outcome.getOutput(), request));
		}
	}

	/**
	 * The default algorithm for handling a {@link FlowException} now handled by the Web Flow system. Only called if
	 * {@link FlowHandler#handleException(FlowException, HttpServletRequest, HttpServletResponse)} returns null. This
	 * implementation rethrows the exception unless it is a {@link NoSuchFlowExecutionException}. If the exception is a
	 * NoSuchFlowExecutionException, this implementation attempts to start a new execution of the ended or expired flow.
	 * @param flowId the id of the ended flow
	 * @param e the flow exception
	 * @param request the current request
	 * @param response the current response
	 */
	protected void defaultHandleException(String flowId, FlowException e, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		if (e instanceof NoSuchFlowExecutionException && flowId != null) {
			if (!response.isCommitted()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Restarting a new execution of previously expired/ended flow '" + flowId + "'");
				}
				// by default, attempt to restart the flow
				response.sendRedirect(urlHandler.createFlowDefinitionUrl(flowId, null, request));
			}
		} else {
			throw e;
		}
	}

	// internal helpers

	private void handleFlowExecutionResult(FlowExecutionResult result, ServletExternalContext context,
			HttpServletRequest request, HttpServletResponse response, FlowHandler handler) throws IOException {
		if (result.isPaused()) {
			if (context.getFlowExecutionRedirectRequested()) {
				sendFlowExecutionRedirect(result, context, request, response);
			} else if (context.getFlowDefinitionRedirectRequested()) {
				sendFlowDefinitionRedirect(result, context, request, response);
			} else if (context.getExternalRedirectRequested()) {
				sendExternalRedirect(context.getExternalRedirectUrl(), request, response);
			}
		} else if (result.isEnded()) {
			if (context.getFlowDefinitionRedirectRequested()) {
				sendFlowDefinitionRedirect(result, context, request, response);
			} else if (context.getExternalRedirectRequested()) {
				sendExternalRedirect(context.getExternalRedirectUrl(), request, response);
			} else {
				String location = handler.handleExecutionOutcome(result.getOutcome(), request, response);
				if (location != null) {
					sendExternalRedirect(location, request, response);
				} else {
					defaultHandleExecutionOutcome(result.getFlowId(), result.getOutcome(), request, response);
				}
			}
		} else {
			throw new IllegalStateException("Execution result should have been one of [paused] or [ended]");
		}
	}

	private void sendFlowExecutionRedirect(FlowExecutionResult result, ServletExternalContext context,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		String url = urlHandler.createFlowExecutionUrl(result.getFlowId(), result.getPausedKey(), request);
		if (logger.isDebugEnabled()) {
			logger.debug("Sending flow execution redirect to '" + url + "'");
		}
		if (context.isAjaxRequest()) {
			ajaxHandler.sendAjaxRedirect(getServletContext(), request, response, url, context.getRedirectInPopup());
		} else {
			sendRedirect(url, response);
		}
	}

	private void sendFlowDefinitionRedirect(FlowExecutionResult result, ServletExternalContext context,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		String flowId = context.getFlowRedirectFlowId();
		MutableAttributeMap input = context.getFlowRedirectFlowInput();
		if (result.isPaused()) {
			input.put("refererExecution", result.getPausedKey());
		}
		String url = urlHandler.createFlowDefinitionUrl(flowId, input, request);
		if (logger.isDebugEnabled()) {
			logger.debug("Sending flow definition redirect to '" + url + "'");
		}
		sendRedirect(url, response);
	}

	private void sendExternalRedirect(String location, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Sending external redirect to '" + location + "'");
		}
		if (location.startsWith(SERVLET_RELATIVE_LOCATION_PREFIX)) {
			sendServletRelativeRedirect(location.substring(SERVLET_RELATIVE_LOCATION_PREFIX.length()), request,
					response);
		} else if (location.startsWith(CONTEXT_RELATIVE_LOCATION_PREFIX)) {
			StringBuffer url = new StringBuffer(request.getContextPath());
			String contextRelativeUrl = location.substring(CONTEXT_RELATIVE_LOCATION_PREFIX.length());
			if (!contextRelativeUrl.startsWith("/")) {
				url.append('/');
			}
			url.append(contextRelativeUrl);
			sendRedirect(url.toString(), response);
		} else if (location.startsWith(SERVER_RELATIVE_LOCATION_PREFIX)) {
			String url = location.substring(SERVER_RELATIVE_LOCATION_PREFIX.length());
			if (!url.startsWith("/")) {
				url = "/" + url;
			}
			sendRedirect(url, response);
		} else if (location.startsWith(URL_LOCATION_PREFIX)) {
			String url = location.substring(URL_LOCATION_PREFIX.length());
			try {
				new URL(url);
				sendRedirect(url, response);
			} catch (MalformedURLException e) {
				IllegalArgumentException iae = new IllegalArgumentException("The redirect url '" + url
						+ "' is invalid; specify a fully qualified URL when using the '" + URL_LOCATION_PREFIX
						+ "' location prefix");
				iae.initCause(e);
				throw iae;
			}
		} else {
			sendServletRelativeRedirect(location, request, response);
		}
	}

	private void sendServletRelativeRedirect(String location, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		StringBuffer url = new StringBuffer(request.getContextPath());
		url.append(request.getServletPath());
		if (!location.startsWith("/")) {
			url.append('/');
		}
		url.append(location);
		sendRedirect(url.toString(), response);
	}

	private void sendRedirect(String url, HttpServletResponse response) throws IOException {
		response.sendRedirect(response.encodeRedirectURL(url));
	}

	private void handleFlowException(FlowException e, HttpServletRequest request, HttpServletResponse response,
			FlowHandler handler) throws IOException {
		String location = handler.handleException(e, request, response);
		if (location != null) {
			sendExternalRedirect(location, request, response);
		} else {
			defaultHandleException(getFlowId(handler, request), e, request, response);
		}
	}

	public long getLastModified(HttpServletRequest request, Object handler) {
		return -1;
	}

	private String getFlowId(FlowHandler handler, HttpServletRequest request) {
		String flowId = handler.getFlowId();
		if (flowId != null) {
			return flowId;
		} else {
			return defaultGetFlowId(request);
		}
	}

	private MutableAttributeMap getInputMap(FlowHandler handler, HttpServletRequest request) {
		MutableAttributeMap input = handler.createExecutionInputMap(request);
		if (input != null) {
			return input;
		} else {
			return defaultCreateFlowExecutionInputMap(request);
		}
	}
}
