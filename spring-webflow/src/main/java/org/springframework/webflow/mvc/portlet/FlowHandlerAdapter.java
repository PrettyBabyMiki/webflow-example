package org.springframework.webflow.mvc.portlet;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.web.portlet.HandlerAdapter;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.context.PortletApplicationObjectSupport;
import org.springframework.webflow.context.portlet.DefaultFlowUrlHandler;
import org.springframework.webflow.context.portlet.FlowUrlHandler;
import org.springframework.webflow.context.portlet.PortletExternalContext;
import org.springframework.webflow.core.FlowException;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;
import org.springframework.webflow.executor.FlowExecutionResult;
import org.springframework.webflow.executor.FlowExecutor;

public class FlowHandlerAdapter extends PortletApplicationObjectSupport implements HandlerAdapter {

	private static final String ACTION_FLOW_EXCEPTION_ATTRIBUTE = "actionFlowException";

	private static final String FLOW_EXECUTION_RESULT_ATTRIBUTE = "flowExecutionResult";

	private FlowExecutor flowExecutor;

	private FlowUrlHandler urlHandler;

	public FlowHandlerAdapter(FlowExecutor flowExecutor) {
		this.flowExecutor = flowExecutor;
		this.urlHandler = new DefaultFlowUrlHandler();
	}

	public boolean supports(Object handler) {
		return handler instanceof FlowHandler;
	}

	public ModelAndView handleRender(RenderRequest request, RenderResponse response, Object handler) throws Exception {
		FlowHandler flowHandler = (FlowHandler) handler;
		populateConveniencePortletProperties(request);
		PortletSession session = request.getPortletSession(false);
		if (session != null) {
			FlowException e = (FlowException) session.getAttribute(ACTION_FLOW_EXCEPTION_ATTRIBUTE);
			if (e != null) {
				session.removeAttribute(ACTION_FLOW_EXCEPTION_ATTRIBUTE);
				ModelAndView mv = flowHandler.handleException(e, request, response);
				return mv != null ? mv : defaultHandleResumeFlowException(flowHandler, e, request, response);
			}
		}
		String flowExecutionKey = urlHandler.getFlowExecutionKey(request);
		if (flowExecutionKey != null) {
			PortletExternalContext context = createPortletExternalContext(request, response);
			try {
				flowExecutor.resumeExecution(flowExecutionKey, context);
				return null;
			} catch (FlowException e) {
				ModelAndView mv = flowHandler.handleException(e, request, response);
				return mv != null ? mv : defaultHandleResumeFlowException(flowHandler, e, request, response);
			}
		} else {
			if (session != null) {
				FlowExecutionResult result = (FlowExecutionResult) session
						.getAttribute(FLOW_EXECUTION_RESULT_ATTRIBUTE);
				if (result != null) {
					session.removeAttribute(FLOW_EXECUTION_RESULT_ATTRIBUTE);
					String outcome = result.getEndedOutcome();
					AttributeMap output = result.getEndedOutput();
					String flowId = flowHandler.handleFlowOutcome(outcome, output, request, response);
					return defaultHandleFlowOutcome(flowHandler, outcome, output, flowId, request, response);
				} else {
					return startFlow(request, response, flowHandler);
				}
			} else {
				return startFlow(request, response, flowHandler);
			}
		}
	}

	public void handleAction(ActionRequest request, ActionResponse response, Object handler) throws Exception {
		populateConveniencePortletProperties(request);
		String flowExecutionKey = urlHandler.getFlowExecutionKey(request);
		PortletExternalContext context = createPortletExternalContext(request, response);
		try {
			FlowExecutionResult result = flowExecutor.resumeExecution(flowExecutionKey, context);
			if (result.paused()) {
				urlHandler.setFlowExecutionRenderParameter(result.getPausedKey(), response);
			} else {
				request.getPortletSession().setAttribute(FLOW_EXECUTION_RESULT_ATTRIBUTE, result);
			}
		} catch (FlowException e) {
			request.getPortletSession().setAttribute(ACTION_FLOW_EXCEPTION_ATTRIBUTE, e);
		}
	}

	// subclassing hooks

	protected void populateConveniencePortletProperties(PortletRequest request) {
		request.setAttribute("portletMode", request.getPortletMode().toString());
		request.setAttribute("portletWindowState", request.getWindowState().toString());
	}

	protected PortletExternalContext createPortletExternalContext(PortletRequest request, PortletResponse response) {
		return new PortletExternalContext(getPortletContext(), request, response);
	}

	protected MutableAttributeMap defaultFlowExecutionInputMap(PortletRequest request) {
		LocalAttributeMap inputMap = new LocalAttributeMap();
		Map parameterMap = request.getParameterMap();
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

	protected ModelAndView defaultHandleFlowOutcome(FlowHandler flowHandler, String outcome, AttributeMap output,
			String nextFlowId, RenderRequest request, RenderResponse response) throws IOException {
		if (nextFlowId == null) {
			nextFlowId = flowHandler.getFlowId();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Starting a new execution of flow '" + nextFlowId + "'");
		}
		PortletExternalContext context = createPortletExternalContext(request, response);
		flowExecutor.launchExecution(nextFlowId, new LocalAttributeMap(output.asMap()), context);
		return null;
	}

	protected ModelAndView defaultHandleResumeFlowException(FlowHandler flowHandler, FlowException e,
			RenderRequest request, RenderResponse response) throws IOException {
		if (e instanceof NoSuchFlowExecutionException) {
			String flowId = flowHandler.getFlowId();
			if (logger.isDebugEnabled()) {
				logger.debug("Restarting a new execution of previously expired/ended flow '" + flowId + "'");
			}
			// by default, attempt to restart the flow
			PortletExternalContext context = createPortletExternalContext(request, response);
			flowExecutor.launchExecution(flowId, null, context);
			return null;
		} else {
			throw e;
		}
	}

	// helpers

	private ModelAndView startFlow(RenderRequest request, RenderResponse response, FlowHandler flowHandler)
			throws Exception {
		MutableAttributeMap input = flowHandler.createExecutionInputMap(request);
		if (input == null) {
			input = defaultFlowExecutionInputMap(request);
		}
		PortletExternalContext context = createPortletExternalContext(request, response);
		try {
			FlowExecutionResult result = flowExecutor.launchExecution(flowHandler.getFlowId(), input, context);
			if (result.paused()) {
				urlHandler.setFlowExecutionInSession(result.getPausedKey(), request);
			}
			return null;
		} catch (FlowException e) {
			ModelAndView mv = flowHandler.handleException(e, request, response);
			if (mv != null) {
				return mv;
			} else {
				throw e;
			}
		}
	}
}
