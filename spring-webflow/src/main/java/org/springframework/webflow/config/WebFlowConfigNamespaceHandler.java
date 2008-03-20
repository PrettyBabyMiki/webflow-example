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
package org.springframework.webflow.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * <code>NamespaceHandler</code> for the <code>webflow-config</code> namespace.
 * 
 * @author Keith Donald
 * @author Ben Hale
 * @author Jeremy Grelle
 */
public class WebFlowConfigNamespaceHandler extends NamespaceHandlerSupport {
	public void init() {
		registerBeanDefinitionParser("flow-executor", new FlowExecutorBeanDefinitionParser());
		registerBeanDefinitionParser("flow-execution-listeners", new FlowExecutionListenerLoaderBeanDefinitionParser());
		registerBeanDefinitionParser("flow-registry", new FlowRegistryBeanDefinitionParser());
		registerBeanDefinitionParser("flow-builder-services", new FlowBuilderServicesBeanDefinitionParser());
	}
}