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
package org.springframework.webflow.engine.support;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.JdkVersion;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.engine.ActionList;
import org.springframework.webflow.engine.FlowExecutionExceptionHandler;
import org.springframework.webflow.engine.RequestControlContext;
import org.springframework.webflow.engine.TargetStateResolver;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.execution.FlowExecutionException;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.ViewSelection;

/**
 * A flow execution exception handler that maps the occurence of a specific type of
 * exception to a transition to a new {@link org.springframework.webflow.engine.State}.
 * <p>
 * The handled {@link FlowExecutionException} will be exposed in flash scope as
 * {@link #STATE_EXCEPTION_ATTRIBUTE}. The underlying root cause of that exception
 * will be exposed in flash scope as {@link #ROOT_CAUSE_EXCEPTION_ATTRIBUTE}.
 * 
 * @author Keith Donald
 */
public class TransitionExecutingStateExceptionHandler implements FlowExecutionExceptionHandler {
	
	// this class should really have been called TransitionExecutingFlowExceptionHandler

	private static final Log logger = LogFactory.getLog(TransitionExecutingStateExceptionHandler.class);

	/**
	 * The name of the attribute to expose a handled exception under in
	 * flash scope ("stateException").
	 */
	public static final String STATE_EXCEPTION_ATTRIBUTE = "stateException";

	/**
	 * The name of the attribute to expose a root cause of a handled exception
	 * under in flash scope ("rootCauseException").
	 */
	public static final String ROOT_CAUSE_EXCEPTION_ATTRIBUTE = "rootCauseException";


	/**
	 * The exceptionType->targetStateResolver map.
	 */
	private Map exceptionTargetStateMappings = new HashMap();

	/**
	 * The list of actions to execute when this handler handles an exception.
	 */
	private ActionList actionList = new ActionList();

	/**
	 * Adds an exception->state mapping to this handler.
	 * @param exceptionClass the type of exception to map
	 * @param targetStateId the id of the state to transition to if the
	 * specified type of exception is handled
	 * @return this handler, to allow for adding multiple mappings in a single
	 * statement
	 */
	public TransitionExecutingStateExceptionHandler add(Class exceptionClass, String targetStateId) {
		return add(exceptionClass, new DefaultTargetStateResolver(targetStateId));
	}

	/**
	 * Adds a exception->state mapping to this handler.
	 * @param exceptionClass the type of exception to map
	 * @param targetStateResolver the resolver to calculate the state to
	 * transition to if the specified type of exception is handled
	 * @return this handler, to allow for adding multiple mappings in a single
	 * statement
	 */
	public TransitionExecutingStateExceptionHandler add(Class exceptionClass, TargetStateResolver targetStateResolver) {
		Assert.notNull(exceptionClass, "The exception class is required");
		Assert.notNull(targetStateResolver, "The target state resolver is required");
		exceptionTargetStateMappings.put(exceptionClass, targetStateResolver);
		return this;
	}
	
	/**
	 * Returns the list of actions to execute when this handler handles an exception.
	 * The returned list is mutable.
	 */
	public ActionList getActionList() {
		return actionList;
	}

	public boolean handles(FlowExecutionException e) {
		return getTargetStateResolver(e) != null;
	}

	public ViewSelection handle(FlowExecutionException exception, RequestControlContext context) {
		if (logger.isDebugEnabled()) {
			logger.debug("Handling state exception " + exception, exception);
		}
		exposeException(context, exception, findRootCause(exception));
		actionList.execute(context);
		return context.execute(new Transition(getTargetStateResolver(exception)));
	}

	// helpers
	
	/**
	 * Exposes the given flow exception and root cause in flash scope to make
	 * them available for response rendering. Subclasses can override this
	 * if they way to expose the exceptions in a different way or do special
	 * processing before the exceptions are exposed.
	 * @param context the request control context
	 * @param exception the exception being handled
	 * @param rootCause root cause of the exception being handled (could be null) 
	 */
	protected void exposeException(RequestContext context, FlowExecutionException exception, Throwable rootCause) {
		context.getFlashScope().put(STATE_EXCEPTION_ATTRIBUTE, exception);
		if (logger.isDebugEnabled()) {
			logger.debug("Exposing state exception root cause " + rootCause + " under attribute '"
					+ ROOT_CAUSE_EXCEPTION_ATTRIBUTE + "'");
		}
		context.getFlashScope().put(ROOT_CAUSE_EXCEPTION_ATTRIBUTE, rootCause);
	}

	/**
	 * Find the mapped target state resolver for given exception. Returns
	 * <code>null</code> if no mapping can be found for given exception. Will
	 * try all exceptions in the exception cause chain.
	 */
	protected TargetStateResolver getTargetStateResolver(FlowExecutionException e) {
		if (JdkVersion.getMajorJavaVersion() == JdkVersion.JAVA_13) {
			return getTargetStateResolver13(e);
		}
		else {
			return getTargetStateResolver14(e);
		}
	}

	/**
	 * Internal getTargetStateResolver implementation for use with JDK 1.3.
	 */
	private TargetStateResolver getTargetStateResolver13(NestedRuntimeException e) {
		TargetStateResolver targetStateResolver;
		if (isRootCause13(e)) {
			return findTargetStateResolver(e.getClass());
		}
		else {
			targetStateResolver = (TargetStateResolver)exceptionTargetStateMappings.get(e.getClass());
			if (targetStateResolver != null) {
				return targetStateResolver;
			}
			else {
				if (e.getCause() instanceof NestedRuntimeException) {
					return getTargetStateResolver13((NestedRuntimeException)e.getCause());
				}
				else {
					return null;
				}
			}
		}
	}

	/**
	 * Internal getTargetStateResolver implementation for use with JDK 1.4 or later.
	 */
	private TargetStateResolver getTargetStateResolver14(Throwable t) {
		TargetStateResolver targetStateResolver;
		if (isRootCause14(t)) {
			return findTargetStateResolver(t.getClass());
		}
		else {
			targetStateResolver = (TargetStateResolver)exceptionTargetStateMappings.get(t.getClass());
			if (targetStateResolver != null) {
				return targetStateResolver;
			}
			else {
				return getTargetStateResolver14(t.getCause());
			}
		}
	}

	/**
	 * Check if given exception is the root of the exception cause chain.
	 * For use with JDK 1.3.
	 */
	private boolean isRootCause13(NestedRuntimeException e) {
		return e.getCause() == null;
	}

	/**
	 * Check if given exception is the root of the exception cause chain.
	 * For use with JDK 1.4 or later.
	 */
	private boolean isRootCause14(Throwable t) {
		return t.getCause() == null;
	}

	/**
	 * Try to find a mapped target state resolver for given exception type. Will
	 * also try to lookup using the class hierarchy of given exception type.
	 * @param exceptionType the exception type to lookup
	 * @return the target state id or null if not found
	 */
	private TargetStateResolver findTargetStateResolver(Class exceptionType) {
		while (exceptionType != null && exceptionType != Object.class) {
			if (exceptionTargetStateMappings.containsKey(exceptionType)) {
				return (TargetStateResolver)exceptionTargetStateMappings.get(exceptionType);
			}
			else {
				exceptionType = exceptionType.getSuperclass();
			}
		}
		return null;
	}

	/**
	 * Find the root cause of given throwable.
	 */
	protected Throwable findRootCause(Throwable t) {
		if (JdkVersion.getMajorJavaVersion() == JdkVersion.JAVA_13) {
			return findRootCause13(t);
		}
		else {
			return findRootCause14(t);
		}
	}

	/**
	 * Find the root cause of given throwable. For use on JDK 1.3.
	 */
	private Throwable findRootCause13(Throwable t) {
		if (t instanceof NestedRuntimeException) {
			NestedRuntimeException nre = (NestedRuntimeException)t;
			Throwable cause = nre.getCause();
			if (cause == null) {
				return nre;
			}
			else {
				return findRootCause13(cause);
			}
		}
		else {
			return t;
		}
	}

	/**
	 * Find the root cause of given throwable. For use on JDK 1.4 or later.
	 */
	private Throwable findRootCause14(Throwable e) {
		Throwable cause = e.getCause();
		if (cause == null) {
			return e;
		}
		else {
			return findRootCause14(cause);
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("exceptionTargetStateMappings", exceptionTargetStateMappings)
				.toString();
	}
}