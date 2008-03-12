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
package org.springframework.webflow.expression.el;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.el.BeanELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.PropertyNotWritableException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

/**
 * Resolves "implicit" or well-known flow variables; for example "flowScope" in an expression like #{flowScope.foo}. The
 * list of implicit flow variables consists of:
 * 
 * <pre>
 * requestParameters
 * requestScope
 * flashScope
 * flowScope
 * conversationScope
 * messageContext
 * flowExecutionContext
 * flowExecutionUrl
 * </pre>
 * 
 * @author Keith Donald
 */
public class ImplicitFlowVariableELResolver extends ELResolver {

	private static final Log logger = LogFactory.getLog(ImplicitFlowVariableELResolver.class);

	private RequestContext requestContext;

	public ImplicitFlowVariableELResolver() {
	}

	public ImplicitFlowVariableELResolver(RequestContext requestContext) {
		this.requestContext = requestContext;
	}

	public Class getCommonPropertyType(ELContext context, Object base) {
		return Object.class;
	}

	public Iterator getFeatureDescriptors(ELContext context, Object base) {
		return null;
	}

	public Class getType(ELContext context, Object base, Object property) {
		RequestContext requestContext = getRequestContext();
		if (base != null || requestContext == null) {
			return null;
		}
		if (ImplicitVariables.matches(property)) {
			context.setPropertyResolved(true);
			return ImplicitVariables.value(context, requestContext, property).getClass();
		} else {
			return null;
		}
	}

	public Object getValue(ELContext context, Object base, Object property) {
		RequestContext requestContext = getRequestContext();
		if (base != null || requestContext == null) {
			return null;
		}
		if (ImplicitVariables.matches(property)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Successfully resolved implicit flow variable '" + property + "'");
			}
			context.setPropertyResolved(true);
			return ImplicitVariables.value(context, requestContext, property);
		} else {
			return null;
		}
	}

	public boolean isReadOnly(ELContext context, Object base, Object property) {
		if (base != null) {
			return false;
		}
		if (ImplicitVariables.matches(property)) {
			context.setPropertyResolved(true);
			return true;
		} else {
			return false;
		}
	}

	public void setValue(ELContext context, Object base, Object property, Object value) {
		if (base != null) {
			return;
		}
		if (ImplicitVariables.matches(property)) {
			context.setPropertyResolved(true);
			throw new PropertyNotWritableException("The implicit flow variable " + property + " is not writable.");
		}
	}

	protected RequestContext getRequestContext() {
		return requestContext != null ? requestContext : RequestContextHolder.getRequestContext();
	}

	private static final class ImplicitVariables {
		private static final Set vars = new HashSet();

		static {
			vars.add("requestParameters");
			vars.add("requestScope");
			vars.add("flashScope");
			vars.add("flowScope");
			vars.add("conversationScope");
			vars.add("messageContext");
			vars.add("flowExecutionContext");
			vars.add("flowExecutionUrl");
		}

		private static final BeanELResolver internalResolver = new BeanELResolver();

		public static boolean matches(Object property) {
			return vars.contains(property);
		}

		public static Object value(ELContext elContext, RequestContext requestContext, Object property) {
			return internalResolver.getValue(elContext, requestContext, property);
		}
	}
}
