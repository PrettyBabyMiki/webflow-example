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
package org.springframework.webflow.engine.builder;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.Assert;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.definition.registry.NoSuchFlowDefinitionException;
import org.springframework.webflow.engine.Flow;

/**
 * The default flow service locator implementation that obtains subflow
 * definitions from a dedicated {@link FlowDefinitionRegistry} and obtains the
 * remaining services from a generic Spring {@link BeanFactory}.
 * 
 * @see FlowDefinitionRegistry
 * @see FlowServiceLocator#getSubflow(String)
 * @see BeanFactory
 * 
 * @author Keith Donald
 */
public class DefaultFlowServiceLocator extends BaseFlowServiceLocator {

	/**
	 * The registry for locating subflows.
	 */
	private FlowDefinitionRegistry subflowRegistry;

	/**
	 * The Spring bean factory used.
	 */
	private BeanFactory beanFactory;

	/**
	 * Creates a flow service locator that retrieves subflows from the provided
	 * registry and additional artifacts from the provided bean factory.
	 * @param subflowRegistry the registry for loading subflows
	 * @param beanFactory the spring bean factory
	 */
	public DefaultFlowServiceLocator(FlowDefinitionRegistry subflowRegistry, BeanFactory beanFactory) {
		Assert.notNull(subflowRegistry, "The subflow registry is required");
		Assert.notNull(beanFactory, "The bean factory is required");
		this.subflowRegistry = subflowRegistry;
		this.beanFactory = beanFactory;
	}
	
	/**
	 * Convenience flow service locator constructor that looks up a flow definition
	 * registry using given bean id in given bean factory. The registry is used
	 * to retrieve subflows. All additional artifacts are looked up in the provided
	 * bean factory.
	 * @param subflowRegistryBeanId the bean id of the subflow FlowDefinitionRegistry
	 * @param beanFactory the Spring bean factory
	 * @since 1.0.2
	 */
	public DefaultFlowServiceLocator(String subflowRegistryBeanId, BeanFactory beanFactory) {
		Assert.notNull(subflowRegistryBeanId, "The subflow registry bean id is required");
		Assert.notNull(beanFactory, "The bean factory is required");
		this.subflowRegistry =
			(FlowDefinitionRegistry)beanFactory.getBean(subflowRegistryBeanId, FlowDefinitionRegistry.class);
		this.beanFactory = beanFactory;
	}

	public Flow getSubflow(String id) throws FlowArtifactLookupException {
		try {
			return (Flow)subflowRegistry.getFlowDefinition(id);
		}
		catch (NoSuchFlowDefinitionException e) {
			throw new FlowArtifactLookupException(id, Flow.class,
					"Could not locate subflow definition with id '" + id + "'", e);
		}
	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	/**
	 * Returns the flow definition registry used to lookup subflows.
	 * @return the flow definition registry
	 */
	protected FlowDefinitionRegistry getSubflowRegistry() {
		return subflowRegistry;
	}	
}