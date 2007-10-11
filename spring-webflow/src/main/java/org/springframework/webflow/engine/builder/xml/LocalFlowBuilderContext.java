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
package org.springframework.webflow.engine.builder.xml;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.core.io.ResourceLoader;
import org.springframework.webflow.action.BeanInvokingActionFactory;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;
import org.springframework.webflow.engine.builder.FlowArtifactFactory;
import org.springframework.webflow.engine.builder.FlowBuilderContext;
import org.springframework.webflow.engine.builder.ViewFactoryCreator;

class LocalFlowBuilderContext implements FlowBuilderContext {

	private FlowBuilderContext parent;

	private BeanFactory localFlowBeanFactory;

	public LocalFlowBuilderContext(FlowBuilderContext parent, BeanFactory localFlowBeanFactory) {
		this.parent = parent;
		this.localFlowBeanFactory = localFlowBeanFactory;
	}

	public String getFlowId() {
		return parent.getFlowId();
	}

	public AttributeMap getFlowAttributes() {
		return parent.getFlowAttributes();
	}

	public FlowDefinitionLocator getFlowDefinitionLocator() {
		return (FlowDefinitionLocator) localFlowBeanFactory.getBean("flowRegistry", FlowDefinitionLocator.class);
	}

	public FlowArtifactFactory getFlowArtifactFactory() {
		if (localFlowBeanFactory.containsBean("flowArtifactFactory")) {
			return (FlowArtifactFactory) localFlowBeanFactory.getBean("flowArtifactFactory", FlowArtifactFactory.class);
		} else {
			return parent.getFlowArtifactFactory();
		}
	}

	public BeanInvokingActionFactory getBeanInvokingActionFactory() {
		if (localFlowBeanFactory.containsBean("beanInvokingActionFactory")) {
			return (BeanInvokingActionFactory) localFlowBeanFactory.getBean("beanInvokingActionFactory",
					BeanInvokingActionFactory.class);
		} else {
			return parent.getBeanInvokingActionFactory();
		}
	}

	public ViewFactoryCreator getViewFactoryCreator() {
		if (localFlowBeanFactory.containsBean("viewFactoryCreator")) {
			return (ViewFactoryCreator) localFlowBeanFactory.getBean("viewFactoryCreator", ViewFactoryCreator.class);
		} else {
			return parent.getViewFactoryCreator();
		}
	}

	public ConversionService getConversionService() {
		if (localFlowBeanFactory.containsBean("conversionService")) {
			return (ConversionService) localFlowBeanFactory.getBean("conversionService", ConversionService.class);
		} else {
			return parent.getConversionService();
		}
	}

	public ExpressionParser getExpressionParser() {
		if (localFlowBeanFactory.containsBean("expressionParser")) {
			return (ExpressionParser) localFlowBeanFactory.getBean("expressionParser", ExpressionParser.class);
		} else {
			return parent.getExpressionParser();
		}
	}

	public ResourceLoader getResourceLoader() {
		return (ResourceLoader) getBeanFactory();
	}

	public BeanFactory getBeanFactory() {
		return localFlowBeanFactory;
	}
}