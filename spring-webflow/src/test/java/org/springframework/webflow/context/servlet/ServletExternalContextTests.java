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
package org.springframework.webflow.context.servlet;

import java.io.IOException;
import java.io.Writer;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;

/**
 * Unit tests for {@link ServletExternalContext}.
 */
public class ServletExternalContextTests extends TestCase {

	private MockServletContext servletContext;

	private MockHttpServletRequest request;

	private MockHttpServletResponse response;

	private ServletExternalContext context;

	protected void setUp() {
		servletContext = new MockServletContext();
		servletContext.setAttribute("aFoo", "bar");
		request = new MockHttpServletRequest();
		request.setAttribute("rFoo", "bar");
		request.getSession(true).setAttribute("sFoo", "bar");
		response = new MockHttpServletResponse();
		context = new ServletExternalContext(servletContext, request, response);
	}

	public void testGetContextPath() {
		request.setContextPath("/foo");
		assertEquals("/foo", request.getContextPath());
	}

	public void testRequestParameters() {
		assertTrue(context.getRequestParameterMap().isEmpty());
	}

	public void testGetAppAttribute() {
		assertEquals("bar", context.getApplicationMap().get("aFoo"));
	}

	public void testGetSessionAttribute() {
		assertEquals("bar", context.getSessionMap().get("sFoo"));
	}

	public void testGetRequestAttribute() {
		assertEquals("bar", context.getRequestMap().get("rFoo"));
	}

	public void testGetNativeObjects() {
		assertEquals(servletContext, context.getNativeContext());
		assertEquals(request, context.getNativeRequest());
		assertEquals(response, context.getNativeResponse());
	}

	public void testGetExecutionUrl() {
		request.setRequestURI("/foo");
		String url = context.getFlowExecutionUrl("foo", "e1s1");
		assertEquals("/foo?execution=e1s1", url);
	}

	public void testNotAnAjaxRequest() {
		assertFalse(context.isAjaxRequest());
	}

	public void testAjaxRequestAcceptHeader() {
		context.setAjaxRequest(true);
		assertTrue(context.isAjaxRequest());
	}

	public void testNotResponseCommitted() {
		assertFalse(context.isResponseComplete());
	}

	public void testCommitExecutionRedirect() {
		context.requestFlowExecutionRedirect();
		assertTrue(context.getFlowExecutionRedirectRequested());
		assertTrue(context.isResponseComplete());
		assertTrue(context.isResponseCompleteFlowExecutionRedirect());
	}

	public void testCommitFlowRedirect() {
		context.requestFlowDefinitionRedirect("foo", null);
		assertTrue(context.getFlowDefinitionRedirectRequested());
		assertEquals("foo", context.getFlowRedirectFlowId());
		assertTrue(context.isResponseComplete());
		assertFalse(context.isResponseCompleteFlowExecutionRedirect());
		assertNotNull(context.getFlowRedirectFlowInput());
	}

	public void testCommitFlowRedirectWithInput() {
		LocalAttributeMap<Object> input = new LocalAttributeMap<Object>();
		context.requestFlowDefinitionRedirect("foo", input);
		assertTrue(context.getFlowDefinitionRedirectRequested());
		assertEquals("foo", context.getFlowRedirectFlowId());
		assertTrue(context.isResponseComplete());
		assertFalse(context.isResponseCompleteFlowExecutionRedirect());
		assertEquals(input, context.getFlowRedirectFlowInput());
	}

	public void testCommitExternalRedirect() {
		context.requestExternalRedirect("foo");
		assertTrue(context.getExternalRedirectRequested());
		assertEquals("foo", context.getExternalRedirectUrl());
		assertTrue(context.isResponseComplete());
		assertFalse(context.isResponseAllowed());
		assertFalse(context.isResponseCompleteFlowExecutionRedirect());
	}

	public void testCommitExecutionRedirectPopup() {
		context.requestFlowExecutionRedirect();
		context.requestRedirectInPopup();
		assertTrue(context.getFlowExecutionRedirectRequested());
		assertTrue(context.getRedirectInPopup());
		assertTrue(context.isResponseComplete());
		assertFalse(context.isResponseAllowed());
		assertTrue(context.isResponseCompleteFlowExecutionRedirect());
	}

	public void testCommitFlowRedirectPopup() {
		context.requestFlowDefinitionRedirect("foo", null);
		context.requestRedirectInPopup();
		assertTrue(context.getFlowDefinitionRedirectRequested());
		assertEquals("foo", context.getFlowRedirectFlowId());
		assertTrue(context.getRedirectInPopup());
		assertTrue(context.isResponseComplete());
		assertFalse(context.isResponseAllowed());
	}

	public void testCommitExternalRedirectPopup() {
		context.requestExternalRedirect("foo");
		context.requestRedirectInPopup();
		assertTrue(context.getExternalRedirectRequested());
		assertEquals("foo", context.getExternalRedirectUrl());
		assertTrue(context.getRedirectInPopup());
		assertFalse(context.isResponseAllowed());
	}

	public void testRecordResponseComplete() {
		context.recordResponseComplete();
		assertTrue(context.isResponseComplete());
		assertFalse(context.isResponseAllowed());
		assertFalse(context.isResponseCompleteFlowExecutionRedirect());
	}

	public void testDoubleCommitResponse() {
		context.recordResponseComplete();
		try {
			context.requestFlowExecutionRedirect();
			fail("Should have failed");
		} catch (IllegalStateException e) {
		}
		try {
			context.requestFlowDefinitionRedirect("foo", null);
			fail("Should have failed");
		} catch (IllegalStateException e) {
		}
		try {
			context.requestExternalRedirect("foo");
			fail("Should have failed");
		} catch (IllegalStateException e) {
		}
	}

	public void testDoubleCommitResponseExecutionRedirectFirst() {
		context.requestFlowExecutionRedirect();
		try {
			context.requestFlowDefinitionRedirect("foo", null);
			fail("Should have failed");
		} catch (IllegalStateException e) {
		}
	}

	public void testDoubleCommitResponseDefinitionRedirectFirst() {
		context.requestFlowDefinitionRedirect("foo", null);
		try {
			context.requestFlowDefinitionRedirect("foo", null);
			fail("Should have failed");
		} catch (IllegalStateException e) {
		}
	}

	public void testDoubleCommitResponseExternalRedirectFirst() {
		context.requestExternalRedirect("foo");
		try {
			context.requestFlowDefinitionRedirect("foo", null);
			fail("Should have failed");
		} catch (IllegalStateException e) {
		}
	}

	public void testRedirectInPopup() {
		context.requestFlowExecutionRedirect();
		assertTrue(context.isResponseComplete());
		assertTrue(context.isResponseCompleteFlowExecutionRedirect());
		assertFalse(context.isResponseAllowed());
		context.requestRedirectInPopup();
		assertTrue(context.getRedirectInPopup());
	}

	public void testRedirectInPopupNoRedirectRequested() {
		try {
			context.requestRedirectInPopup();
			fail("Should have failed");
		} catch (IllegalStateException e) {

		}
	}

	public void testGetResponseWriter() throws IOException {
		Writer writer = context.getResponseWriter();
		writer.append('t');
		assertEquals("t", response.getContentAsString());
	}

	public void testGetResponseWriterResponseComplete() throws IOException {
		context.recordResponseComplete();
		try {
			context.getResponseWriter();
			fail("Should have failed");
		} catch (IllegalStateException e) {

		}
	}

}
