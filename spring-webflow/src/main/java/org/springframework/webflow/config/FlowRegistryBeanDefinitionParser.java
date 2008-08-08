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
package org.springframework.webflow.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.service.DefaultConversionService;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.expression.DefaultExpressionParserFactory;
import org.springframework.webflow.mvc.builder.MvcViewFactoryCreator;
import org.w3c.dom.Element;

/**
 * {@link BeanDefinitionParser} for the flow <code>&lt;flow-registry&gt;</code> tag.
 * 
 * @author Keith Donald
 */
class FlowRegistryBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	protected Class getBeanClass(Element element) {
		return FlowRegistryFactoryBean.class;
	}

	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder definitionBuilder) {
		String flowBuilderServices = element.getAttribute("flow-builder-services");
		if (StringUtils.hasText(flowBuilderServices)) {
			definitionBuilder.addPropertyReference("flowBuilderServices", flowBuilderServices);
		} else {
			definitionBuilder.addPropertyValue("flowBuilderServices", createDefaultFlowBuilderServices(parserContext));
		}
		String parent = element.getAttribute("parent");
		if (StringUtils.hasText(parent)) {
			definitionBuilder.addPropertyReference("parent", parent);
		}
		definitionBuilder.addPropertyValue("flowLocations", parseLocations(element));
		definitionBuilder.addPropertyValue("flowLocationPatterns", parseLocationPatterns(element));
		definitionBuilder.addPropertyValue("flowBuilders", parseFlowBuilders(element));
	}

	private List parseLocations(Element element) {
		List locationElements = DomUtils.getChildElementsByTagName(element, "flow-location");
		if (locationElements.isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		List locations = new ArrayList(locationElements.size());
		for (Iterator it = locationElements.iterator(); it.hasNext();) {
			Element locationElement = (Element) it.next();
			String id = locationElement.getAttribute("id");
			String path = locationElement.getAttribute("path");
			locations.add(new FlowLocation(id, path, parseAttributes(locationElement)));
		}
		return locations;
	}

	private List parseLocationPatterns(Element element) {
		List locationPatternElements = DomUtils.getChildElementsByTagName(element, "flow-location-pattern");
		if (locationPatternElements.isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		List locationPatterns = new ArrayList(locationPatternElements.size());
		for (Iterator it = locationPatternElements.iterator(); it.hasNext();) {
			Element locationPatternElement = (Element) it.next();
			String value = locationPatternElement.getAttribute("value");
			locationPatterns.add(value);
		}
		return locationPatterns;
	}

	private Set parseAttributes(Element element) {
		Element definitionAttributesElement = DomUtils.getChildElementByTagName(element, "flow-definition-attributes");
		if (definitionAttributesElement != null) {
			List attributeElements = DomUtils.getChildElementsByTagName(definitionAttributesElement, "attribute");
			HashSet attributes = new HashSet(attributeElements.size());
			for (Iterator it = attributeElements.iterator(); it.hasNext();) {
				Element attributeElement = (Element) it.next();
				String name = attributeElement.getAttribute("name");
				String value = attributeElement.getAttribute("value");
				String type = attributeElement.getAttribute("type");
				attributes.add(new FlowElementAttribute(name, value, type));
			}
			return attributes;
		} else {
			return null;
		}
	}

	private List parseFlowBuilders(Element element) {
		List builderElements = DomUtils.getChildElementsByTagName(element, "flow-builder");
		if (builderElements.isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		List builders = new ArrayList(builderElements.size());
		for (Iterator it = builderElements.iterator(); it.hasNext();) {
			Element builderElement = (Element) it.next();
			String id = builderElement.getAttribute("id");
			String className = builderElement.getAttribute("class");
			builders.add(new FlowBuilderInfo(id, className, parseAttributes(builderElement)));
		}
		return builders;
	}

	private BeanDefinition createDefaultFlowBuilderServices(ParserContext context) {
		BeanDefinitionBuilder defaultBuilder = BeanDefinitionBuilder.genericBeanDefinition(FlowBuilderServices.class);
		ConversionService conversionService = new DefaultConversionService();
		defaultBuilder.addPropertyValue("conversionService", conversionService);
		defaultBuilder.addPropertyValue("expressionParser", DefaultExpressionParserFactory
				.getExpressionParser(conversionService));
		defaultBuilder.addPropertyValue("viewFactoryCreator", BeanDefinitionBuilder.genericBeanDefinition(
				MvcViewFactoryCreator.class).getBeanDefinition());
		return defaultBuilder.getBeanDefinition();
	}

}