package org.springframework.faces.mvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Locale;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.faces.webflow.JSFMockHelper;
import org.springframework.faces.webflow.MockViewHandler;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

public class JsfViewTests {

	UrlBasedViewResolver resolver;

	private final JSFMockHelper jsfMock = new JSFMockHelper();

	@Before
	public void setUp() throws Exception {
		this.jsfMock.setUp();
		this.jsfMock.facesContext().getApplication().setViewHandler(new ResourceCheckingViewHandler());

		this.resolver = new UrlBasedViewResolver();
		this.resolver.setPrefix("/WEB-INF/views/");
		this.resolver.setSuffix(".xhtml");
		this.resolver.setViewClass(JsfView.class);
		this.resolver.setApplicationContext(new StaticWebApplicationContext());
	}

	@After
	public void tearDown() throws Exception {
		this.jsfMock.tearDown();
	}

	@Test
	public void testViewResolution() throws Exception {
		View view = this.resolver.resolveViewName("intro", new Locale("EN"));
		assertTrue(view instanceof JsfView);
	}

	@Test
	public void testViewRender() throws Exception {
		JsfView view = (JsfView) this.resolver.resolveViewName("intro", new Locale("EN"));
		view.setApplicationContext(new StaticWebApplicationContext());
		view.setServletContext(new MockServletContext());
		view.render(new HashMap<>(), new MockHttpServletRequest(), new MockHttpServletResponse());
	}

	private class ResourceCheckingViewHandler extends MockViewHandler {

		public UIViewRoot createView(FacesContext context, String viewId) {
			assertNotNull(viewId);
			assertEquals("/WEB-INF/views/intro.xhtml", viewId);
			return new UIViewRoot();
		}

	}
}
