/*
 * Copyright 2004-2015 the original author or authors.
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
package org.springframework.webflow.engine.model.registry;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.engine.model.FlowModel;

/**
 * A generic registry implementation for housing one or more flow models.
 * 
 * @author Keith Donald
 * @author Scott Andrews
 * @author Rossen Stoyanchev
 */
public class FlowModelRegistryImpl implements FlowModelRegistry, FlowModelHolderLocator {

	/**
	 * The map of loaded Flow models maintained in this registry.
	 */
	private Map<String, FlowModelHolder> flowModels;

	/**
	 * An optional parent flow model registry.
	 */
	private FlowModelRegistry parent;

	public FlowModelRegistryImpl() {
		flowModels = new TreeMap<String, FlowModelHolder>();
	}

	// implementing FlowModelLocator

	public FlowModel getFlowModel(String id) throws NoSuchFlowModelException {
		try {
			return getLocalFlowModelHolder(id).getFlowModel();
		} catch (NoSuchFlowModelException e) {
			if (parent != null) {
				// try parent
				return parent.getFlowModel(id);
			}
			throw e;
		}
	}

	// implementing FlowModelRegistry

	public void setParent(FlowModelRegistry parent) {
		this.parent = parent;
	}

	public void registerFlowModel(String id, FlowModelHolder modelHolder) {
		Assert.notNull(modelHolder, "The holder of the flow model to register is required");
		flowModels.put(id, modelHolder);
	}

	// implementing FlowModelHolderLocator

	public FlowModelHolder getFlowModelHolder(String id) throws NoSuchFlowModelException {
		try {
			return getLocalFlowModelHolder(id);
		} catch (NoSuchFlowModelException e) {
			if (parent != null && parent instanceof FlowModelHolderLocator) {
				return ((FlowModelHolderLocator) parent).getFlowModelHolder(id);
			}
			throw e;
		}
	}

	// internal helpers

	/**
	 * Returns the identified flow model holder. Throws an exception if it cannot be found.
	 */
	private FlowModelHolder getLocalFlowModelHolder(String id) throws NoSuchFlowModelException {
		FlowModelHolder holder = flowModels.get(id);
		if (holder == null) {
			throw new NoSuchFlowModelException(id);
		}
		return holder;
	}

	public String toString() {
		return new ToStringCreator(this).append("flowModels", flowModels).append("parent", parent).toString();
	}
}
