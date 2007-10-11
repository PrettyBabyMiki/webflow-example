package org.springframework.webflow.engine.builder;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.core.io.ResourceLoader;
import org.springframework.webflow.action.BeanInvokingActionFactory;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;

public interface FlowBuilderContext {

	/**
	 * Returns an externally configured flow definition identifier to assign to the flow being built.
	 * @return the flow id
	 */
	public String getFlowId();

	/**
	 * Returns externally configured attributes to assign to the flow definition being built.
	 * @return the flow attributes
	 */
	public AttributeMap getFlowAttributes();

	/**
	 * Returns the locator for locating dependent flows (subflows).
	 * @return the flow definition locator
	 */
	public FlowDefinitionLocator getFlowDefinitionLocator();

	/**
	 * Returns the factory for core flow artifacts such as Flow and State.
	 * @return the flow artifact factory
	 */
	public FlowArtifactFactory getFlowArtifactFactory();

	/**
	 * Returns the factory for bean invoking actions.
	 * @return the bean invoking action factory
	 */
	public BeanInvokingActionFactory getBeanInvokingActionFactory();

	/**
	 * Returns the view factory creator for configuring a ViewFactory per view state
	 * @return the view factory creator
	 */
	public ViewFactoryCreator getViewFactoryCreator();

	/**
	 * Returns the expression parser for parsing expression strings.
	 * @return the expression parser
	 */
	public ExpressionParser getExpressionParser();

	/**
	 * Returns a generic type conversion service for converting between types, typically from string to a rich value
	 * object.
	 * @return the generic conversion service
	 */
	public ConversionService getConversionService();

	/**
	 * Returns a generic resource loader for accessing file-based resources.
	 * @return the generic resource loader
	 */
	public ResourceLoader getResourceLoader();

	/**
	 * Returns a generic bean factory for accessing arbitrary services by their id.
	 * @return the bean factory
	 */
	public BeanFactory getBeanFactory();
}