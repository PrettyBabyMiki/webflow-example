package org.springframework.webflow.mvc.view;

import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.format.FormatterRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ClassUtils;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.JstlView;
import org.springframework.webflow.context.portlet.PortletExternalContext;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.View;
import org.springframework.webflow.execution.ViewFactory;
import org.springframework.webflow.mvc.portlet.PortletMvcView;
import org.springframework.webflow.mvc.servlet.ServletMvcView;

/**
 * View factory implementation that creates a Spring-MVC Internal Resource view to render a flow-relative view resource
 * such as a JSP or Velocity template.
 * @author Keith Donald
 */
class InternalFlowResourceMvcViewFactory implements ViewFactory {

	private static final boolean JSTL_PRESENT = ClassUtils.isPresent("javax.servlet.jsp.jstl.fmt.LocalizationContext");

	private Expression viewIdExpression;

	private ApplicationContext applicationContext;

	private ResourceLoader resourceLoader;

	private ExpressionParser expressionParser;

	private FormatterRegistry formatterRegistry;

	public InternalFlowResourceMvcViewFactory(Expression viewIdExpression, ExpressionParser expressionParser,
			FormatterRegistry formatterRegistry, ApplicationContext context, ResourceLoader resourceLoader) {
		this.viewIdExpression = viewIdExpression;
		this.expressionParser = expressionParser;
		this.formatterRegistry = formatterRegistry;
		this.applicationContext = context;
		this.resourceLoader = resourceLoader;
	}

	public View getView(RequestContext context) {
		String viewId = (String) viewIdExpression.getValue(context);
		if (viewId.startsWith("/")) {
			return getViewInternal(viewId, context);
		} else {
			Resource viewResource = resourceLoader.getResource(viewId);
			if (!(viewResource instanceof ContextResource)) {
				throw new IllegalStateException(
						"A ContextResource is required to get relative view paths within this context");
			}
			return getViewInternal(((ContextResource) viewResource).getPathWithinContext(), context);
		}
	}

	private View getViewInternal(String viewPath, RequestContext context) {
		if (viewPath.endsWith(".jsp")) {
			if (JSTL_PRESENT) {
				JstlView view = new JstlView(viewPath);
				view.setApplicationContext(context.getActiveFlow().getApplicationContext());
				return createMvcView(view, context);
			} else {
				InternalResourceView view = new InternalResourceView(viewPath);
				return createMvcView(view, context);
			}
		} else {
			throw new IllegalArgumentException("Unsupported view type " + viewPath + " only types supported are [.jsp]");
		}
	}

	private MvcView createMvcView(org.springframework.web.servlet.View view, RequestContext context) {
		MvcView mvcView;
		if (context.getExternalContext() instanceof PortletExternalContext) {
			mvcView = new PortletMvcView(view, context);
		} else {
			mvcView = new ServletMvcView(view, context);
		}
		mvcView.setExpressionParser(expressionParser);
		mvcView.setFormatterRegistry(formatterRegistry);
		return mvcView;
	}

}