/*
 * Copyright 2004-2012 the original author or authors.
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
package org.springframework.faces.security;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ParseException;
import org.springframework.security.access.expression.ExpressionUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.WebInvocationPrivilegeEvaluator;
import org.springframework.security.web.access.expression.WebSecurityExpressionHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * <p>
 * A base class for an &lt;authorize&gt; tag used to make Spring Security based authorization decisions.
 * </p>
 *
 * <p>
 * This class is independent of tag rendering technology (JSP, Facelets). It treats tag attributes as simple strings
 * (with the notable exception of the "access" attribute, which is always expected to contain a Spring EL expression).
 * Therefore subclasses are expected to extract tag attribute values from the specific rendering technology, evaluate
 * them as expressions if necessary, and use the result to set the String-based attributes of this class.
 * </p>
 *
 * @author Francois Beausoleil
 * @author Luke Taylor
 * @author Rossen Stoyanchev
 *
 * @since 2.2.0
 */
public abstract class AbstractAuthorizeTag {

	private String access;
	private String url;
	private String method;
	private String ifAllGranted;
	private String ifAnyGranted;
	private String ifNotGranted;

	/**
	 * This method allows subclasses to provide a way to access the ServletRequest according to the rendering
	 * technology.
	 */
	protected abstract ServletRequest getRequest();

	/**
	 * This method allows subclasses to provide a way to access the ServletResponse according to the rendering
	 * technology.
	 */
	protected abstract ServletResponse getResponse();

	/**
	 * This method allows subclasses to provide a way to access the ServletContext according to the rendering
	 * technology.
	 */
	protected abstract ServletContext getServletContext();

	/**
	 * Make an authorization decision by considering all &lt;authorize&gt; tag attributes. The following are valid
	 * combinations of attributes:
	 * <ul>
	 * <li>access</li>
	 * <li>url, method</li>
	 * <li>ifAllGranted, ifAnyGranted, ifNotGranted</li>
	 * </ul>
	 * The above combinations are mutually exclusive and evaluated in the given order.
	 *
	 * @return the result of the authorization decision
	 *
	 * @throws IOException
	 */
	public boolean authorize() throws IOException {
		boolean isAuthorized = false;

		if (StringUtils.hasText(getAccess())) {
			isAuthorized = authorizeUsingAccessExpression();

		} else if (StringUtils.hasText(getUrl())) {
			isAuthorized = authorizeUsingUrlCheck();

		} else {
			isAuthorized = authorizeUsingGrantedAuthorities();

		}

		return isAuthorized;
	}

	/**
	 * Make an authorization decision by considering ifAllGranted, ifAnyGranted, and ifNotGranted. All 3 or any
	 * combination can be provided. All provided attributes must evaluate to true.
	 *
	 * @return the result of the authorization decision
	 */
	public boolean authorizeUsingGrantedAuthorities() {
		boolean hasTextAllGranted = StringUtils.hasText(getIfAllGranted());
		boolean hasTextAnyGranted = StringUtils.hasText(getIfAnyGranted());
		boolean hasTextNotGranted = StringUtils.hasText(getIfNotGranted());

		if ((!hasTextAllGranted) && (!hasTextAnyGranted) && (!hasTextNotGranted)) {
			return false;
		}

		final Collection<GrantedAuthority> granted = getPrincipalAuthorities();

		if (hasTextAllGranted) {
			if (!granted.containsAll(parseAuthoritiesString(getIfAllGranted()))) {
				return false;
			}
		}

		if (hasTextAnyGranted) {
			Set<GrantedAuthority> grantedCopy = retainAll(granted, parseAuthoritiesString(getIfAnyGranted()));
			if (grantedCopy.isEmpty()) {
				return false;
			}
		}

		if (hasTextNotGranted) {
			Set<GrantedAuthority> grantedCopy = retainAll(granted, parseAuthoritiesString(getIfNotGranted()));
			if (!grantedCopy.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Make an authorization decision based on a Spring EL expression. See the "Expression-Based Access Control" chapter
	 * in Spring Security for details on what expressions can be used.
	 *
	 * @return the result of the authorization decision
	 *
	 * @throws IOException
	 */
	public boolean authorizeUsingAccessExpression() throws IOException {
		Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
		if (currentUser == null) {
			return false;
		}

		WebSecurityExpressionHandler handler = getExpressionHandler();

		Expression accessExpression;
		try {
			accessExpression = handler.getExpressionParser().parseExpression(getAccess());

		} catch (ParseException e) {
			IOException ioException = new IOException();
			ioException.initCause(e);
			throw ioException;
		}

		FilterInvocation f = new FilterInvocation(getRequest(), getResponse(), new FilterChain() {
			public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
				throw new UnsupportedOperationException();
			}
		});

		return ExpressionUtils.evaluateAsBoolean(accessExpression, handler.createEvaluationContext(currentUser, f));
	}

	/**
	 * Make an authorization decision based on the URL and HTTP method attributes. True is returned if the user is
	 * allowed to access the given URL as defined.
	 *
	 * @return the result of the authorization decision
	 *
	 * @throws IOException
	 */
	public boolean authorizeUsingUrlCheck() throws IOException {
		String contextPath = ((HttpServletRequest) getRequest()).getContextPath();
		Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
		return getPrivilegeEvaluator().isAllowed(contextPath, getUrl(), getMethod(), currentUser);
	}

	public String getAccess() {
		return this.access;
	}

	public void setAccess(String access) {
		this.access = access;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMethod() {
		return this.method;
	}

	public void setMethod(String method) {
		this.method = (method != null) ? method.toUpperCase() : null;
	}

	public String getIfAllGranted() {
		return this.ifAllGranted;
	}

	public void setIfAllGranted(String ifAllGranted) {
		this.ifAllGranted = ifAllGranted;
	}

	public String getIfAnyGranted() {
		return this.ifAnyGranted;
	}

	public void setIfAnyGranted(String ifAnyGranted) {
		this.ifAnyGranted = ifAnyGranted;
	}

	public String getIfNotGranted() {
		return this.ifNotGranted;
	}

	public void setIfNotGranted(String ifNotGranted) {
		this.ifNotGranted = ifNotGranted;
	}

	/*------------- Private helper methods  -----------------*/

	private Collection<GrantedAuthority> getPrincipalAuthorities() {
		Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
		if (null == currentUser) {
			return Collections.emptyList();
		}
		return currentUser.getAuthorities();
	}

	private Set<GrantedAuthority> parseAuthoritiesString(String authorizationsString) {
		final Set<GrantedAuthority> requiredAuthorities = new HashSet<GrantedAuthority>();
		requiredAuthorities.addAll(AuthorityUtils.commaSeparatedStringToAuthorityList(authorizationsString));
		return requiredAuthorities;
	}

	private Set<GrantedAuthority> retainAll(final Collection<GrantedAuthority> granted,
			final Set<GrantedAuthority> required) {
		Set<String> grantedRoles = authoritiesToRoles(granted);
		Set<String> requiredRoles = authoritiesToRoles(required);
		grantedRoles.retainAll(requiredRoles);

		return rolesToAuthorities(grantedRoles, granted);
	}

	private Set<String> authoritiesToRoles(Collection<GrantedAuthority> c) {
		Set<String> target = new HashSet<String>();
		for (GrantedAuthority authority : c) {
			if (null == authority.getAuthority()) {
				throw new IllegalArgumentException(
						"Cannot process GrantedAuthority objects which return null from getAuthority() - attempting to process "
								+ authority.toString());
			}
			target.add(authority.getAuthority());
		}
		return target;
	}

	private Set<GrantedAuthority> rolesToAuthorities(Set<String> grantedRoles, Collection<GrantedAuthority> granted) {
		Set<GrantedAuthority> target = new HashSet<GrantedAuthority>();
		for (String role : grantedRoles) {
			for (GrantedAuthority authority : granted) {
				if (authority.getAuthority().equals(role)) {
					target.add(authority);
					break;
				}
			}
		}
		return target;
	}

	private WebSecurityExpressionHandler getExpressionHandler() throws IOException {
		ApplicationContext appContext = WebApplicationContextUtils
				.getRequiredWebApplicationContext(getServletContext());
		Map<String, WebSecurityExpressionHandler> expressionHandlres = appContext
				.getBeansOfType(WebSecurityExpressionHandler.class);

		if (expressionHandlres.size() == 0) {
			throw new IOException("No visible WebSecurityExpressionHandler instance could be found in the application "
					+ "context. There must be at least one in order to support expressions in JSP 'authorize' tags.");
		}

		return (WebSecurityExpressionHandler) expressionHandlres.values().toArray()[0];
	}

	private WebInvocationPrivilegeEvaluator getPrivilegeEvaluator() throws IOException {
		ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
		Map<String, WebInvocationPrivilegeEvaluator> wipes = ctx.getBeansOfType(WebInvocationPrivilegeEvaluator.class);

		if (wipes.size() == 0) {
			throw new IOException(
					"No visible WebInvocationPrivilegeEvaluator instance could be found in the application "
							+ "context. There must be at least one in order to support the use of URL access checks in 'authorize' tags.");
		}

		return (WebInvocationPrivilegeEvaluator) wipes.values().toArray()[0];
	}
}
