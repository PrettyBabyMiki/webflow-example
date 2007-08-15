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
package org.springframework.webflow.definition.registry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.springframework.core.style.ToStringCreator;

/**
 * A flow definition registrar that populates a flow definition registry from flow definitions defined within
 * externalized resources. Encapsulates registration behavior common to all externalized registrars and is not tied to a
 * specific flow definition format (e.g. xml).
 * <p>
 * Concrete subclasses are expected to derive from this class to provide knowledge about a particular kind of definition
 * format by implementing the abstract template methods in this class.
 * <p>
 * By default, when configuring the {@link #setLocations(Resource[]) locations} property, flow definitions at those
 * locations will be assigned a registry identifier equal to the filename of the underlying definition resource, minus
 * the filename extension. For example, a XML-based flow definition defined in the file "flow1.xml" will be identified
 * as "flow1" when registered in a registry.
 * <p>
 * For full control over the assignment of flow identifiers and flow properties, configure formal
 * {@link org.springframework.webflow.definition.registry.FlowDefinitionResource} instances using the
 * {@link #setResources(FlowDefinitionResource[] resources)} property.
 * 
 * @see org.springframework.webflow.definition.registry.FlowDefinitionResource
 * @see org.springframework.webflow.definition.registry.FlowDefinitionRegistry
 * 
 * @author Keith Donald
 */
public abstract class ExternalizedFlowDefinitionRegistrar implements FlowDefinitionRegistrar {

	/**
	 * File locations of externalized flow definition resources to load. A set of {@link Resource}} objects.
	 */
	private Set locations = new HashSet();

	/**
	 * A set of formal externalized flow definitions to load. A set of {@link FlowDefinitionResource} objects.
	 */
	private Set resources = new HashSet();

	/**
	 * A set of namespace mappings to load. A set of {@link NamespaceMapping} objects.
	 */
	private Set namespaceMappings = new HashSet();

	/**
	 * Sets the locations (file paths) pointing to externalized flow definitions.
	 * <p>
	 * Flows registered from this set will be automatically assigned an id based on the filename of the flow resource.
	 * @param locations the resource locations
	 */
	public void setLocations(Resource[] locations) {
		this.locations = new HashSet(Arrays.asList(locations));
	}

	/**
	 * Sets the formal set of externalized flow definitions this registrar will register.
	 * <p>
	 * Use this method when you want full control over the assigned flow id and the set of properties applied to the
	 * externalized flow resources.
	 * @param resources the externalized flow definition specifications
	 */
	public void setResources(FlowDefinitionResource[] resources) {
		this.resources = new HashSet(Arrays.asList(resources));
	}

	/**
	 * Sets the namespace mappings for a set of externalized flow definitions.
	 * <p>
	 * Use this method when you want to add a set of locations or resources in an explicitly named namespace.
	 * @param namespaceMappings
	 */
	public void setNamespaceMappings(NamespaceMapping[] namespaceMappings) {
		this.namespaceMappings = namespaceMappings;
	}

	/**
	 * Adds a flow location pointing to an externalized flow resource.
	 * <p>
	 * The flow registered from this location will automatically assigned an id based on the filename of the flow
	 * resource.
	 * @param location the definition location
	 */
	public boolean addLocation(Resource location) {
		return locations.add(location);
	}

	/**
	 * Adds the flow locations pointing to externalized flow resources.
	 * <p>
	 * The flow registered from this location will automatically assigned an id based on the filename of the flow
	 * resource.
	 * @param locations the definition locations
	 */
	public boolean addLocations(Resource[] locations) {
		if (locations == null) {
			return false;
		}
		return this.locations.addAll(Arrays.asList(locations));
	}

	/**
	 * Adds an externalized flow definition specification pointing to an externalized flow resource.
	 * <p>
	 * Use this method when you want full control over the assigned flow id and the set of properties applied to the
	 * externalized flow resource.
	 * @param resource the definition the definition resource
	 */
	public boolean addResource(FlowDefinitionResource resource) {
		return resources.add(resource);
	}

	/**
	 * Adds the externalized flow definitions pointing to externalized flow resources.
	 * <p>
	 * Use this method when you want full control over the assigned flow id and the set of properties applied to the
	 * externalized flow resources.
	 * @param resources the definitions
	 */
	public boolean addResources(FlowDefinitionResource[] resources) {
		if (resources == null) {
			return false;
		}
		return this.resources.addAll(Arrays.asList(resources));
	}

	/**
	 * Adds a namespace mapping.
	 * <p>
	 * Use this method when you want to add a set of locations or resources in an explicitly named namespace.
	 * @param namespaceMappings the namespace mapping
	 */
	public boolean addNamespaceMapping(NamespaceMapping namespaceMappings) {
		return namespaceMappings.add(namespaceMappings);
	}

	/**
	 * Adds namespace mappings.
	 * <p>
	 * Use this method when you want to add a set of locations or resources in an explicitly named namespace.
	 * @param namespaceMappings the namespace mappings
	 */
	public boolean addNamespaceMappings(NamespaceMapping[] namespaceMappings) {
		if (namespaceMappings == null) {
			return false;
		}
		return this.namespaceMappings.addAll(Arrays.asList(namespaceMappings));
	}

	public void registerFlowDefinitions(FlowDefinitionRegistry registry) {
		processLocations(registry);
		processResources(registry);
		processNamespaces(registry);
	}

	// internal helpers

	/**
	 * Register the flow definitions at the configured file locations.
	 * @param registry the registry
	 */
	private void processLocations(FlowDefinitionRegistry registry) {
		Iterator it = locations.iterator();
		while (it.hasNext()) {
			Resource location = (Resource) it.next();
			if (isFlowDefinitionResource(location)) {
				FlowDefinitionResource resource = createFlowDefinitionResource(location);
				register(resource, "", registry);
			}
		}
	}

	/**
	 * Register the flow definitions at the configured resource locations.
	 * @param registry the registry
	 */
	private void processResources(FlowDefinitionRegistry registry) {
		Iterator it = resources.iterator();
		while (it.hasNext()) {
			FlowDefinitionResource resource = (FlowDefinitionResource) it.next();
			register(resource, "", registry);
		}
	}

	/**
	 * Register the flow definitions at the configured resource locations to a given namespace.
	 * @param registry the registry
	 */
	private void processNamespaces(FlowDefinitionRegistry registry) {
		// TODO
	}

	/**
	 * Helper method to register the flow built from an externalized resource in the registry.
	 * @param resource representation of the externalized flow definition resource
	 * @param namespace the namespace to register the flow in
	 * @param registry the flow registry to register the flow in
	 */
	protected final void register(FlowDefinitionResource resource, String namespace, FlowDefinitionRegistry registry) {
		registry.registerFlowDefinition(createFlowDefinitionHolder(resource), namespace);
	}

	// subclassing hooks

	/**
	 * Template method that calculates if the given file resource is actually a flow definition resource. Resources that
	 * aren't flow definitions will be ignored. Subclasses may override; this implementation simply returns true.
	 * @param resource the underlying resource
	 * @return true if yes, false otherwise
	 */
	protected boolean isFlowDefinitionResource(Resource resource) {
		return true;
	}

	/**
	 * Factory method that creates a flow definition from an externalized resource location.
	 * @param location the location of the resource
	 * @return the externalized flow definition pointer
	 */
	protected FlowDefinitionResource createFlowDefinitionResource(Resource location) {
		return new FlowDefinitionResource(location);
	}

	/**
	 * Template factory method subclasses must override to return the holder for the flow definition to be registered
	 * loaded from the specified resource.
	 * @param resource the externalized resource
	 * @return the flow definition holder
	 */
	protected abstract FlowDefinitionHolder createFlowDefinitionHolder(FlowDefinitionResource resource);

	public String toString() {
		return new ToStringCreator(this).append("locations", locations).append("resources", resources).toString();
	}
}