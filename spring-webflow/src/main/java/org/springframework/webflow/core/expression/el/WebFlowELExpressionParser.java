package org.springframework.webflow.core.expression.el;

import java.util.ArrayList;
import java.util.List;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.VariableMapper;

import org.springframework.binding.expression.el.DefaultELResolver;
import org.springframework.binding.expression.el.ELContextFactory;
import org.springframework.binding.expression.el.ELExpressionParser;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;

/**
 * An ExpressionParser that allows Spring and Web Flow managed beans to be referenced in expressions in the
 * FlowDefinition.
 * 
 * @author Jeremy Grelle
 */
public class WebFlowELExpressionParser extends ELExpressionParser {

	public WebFlowELExpressionParser(ExpressionFactory expressionFactory) {
		super(expressionFactory);
		putContextFactory(RequestContext.class, new RequestContextELContextFactory());
		putContextFactory(MutableAttributeMap.class, new AttributeMapELContextFactory());
	}

	private static class RequestContextELContextFactory implements ELContextFactory {
		public ELContext getELContext(Object target, VariableMapper variableMapper) {
			List customResolvers = new ArrayList();
			customResolvers.add(new RequestContextELResolver());
			customResolvers.add(new ScopeSearchingELResolver());
			ELResolver resolver = new DefaultELResolver(target, customResolvers);
			return new WebFlowELContext(resolver, variableMapper);
		}
	}

	private static class AttributeMapELContextFactory implements ELContextFactory {
		public ELContext getELContext(Object target, VariableMapper variableMapper) {
			ELResolver resolver = new DefaultELResolver(target, null);
			return new WebFlowELContext(resolver, variableMapper);
		}
	}

	private static class WebFlowELContext extends ELContext {

		VariableMapper variableMapper;

		ELResolver resolver;

		public WebFlowELContext(ELResolver resolver, VariableMapper variableMapper) {
			this.resolver = resolver;
			this.variableMapper = variableMapper;
		}

		public ELResolver getELResolver() {
			return resolver;
		}

		public FunctionMapper getFunctionMapper() {
			return null;
		}

		public VariableMapper getVariableMapper() {
			return variableMapper;
		}
	}

}