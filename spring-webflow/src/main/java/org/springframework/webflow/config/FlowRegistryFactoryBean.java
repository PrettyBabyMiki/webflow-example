package org.springframework.webflow.config;

import java.util.Iterator;
import java.util.Set;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionConstructionException;
import org.springframework.webflow.definition.registry.FlowDefinitionHolder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistryImpl;
import org.springframework.webflow.engine.builder.DefaultFlowHolder;
import org.springframework.webflow.engine.builder.FlowAssembler;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.FlowBuilderContext;
import org.springframework.webflow.engine.builder.model.FlowModelFlowBuilder;
import org.springframework.webflow.engine.builder.support.FlowBuilderContextImpl;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.engine.model.builder.FlowModelBuilder;
import org.springframework.webflow.engine.model.builder.xml.XmlFlowModelBuilder;
import org.springframework.webflow.engine.model.registry.DefaultFlowModelHolder;
import org.springframework.webflow.engine.model.registry.FlowModelHolder;
import org.springframework.webflow.engine.model.registry.FlowModelRegistryImpl;

/**
 * A factory for a flow definition registry. Is a Spring FactoryBean, for provision by the flow definition registry bean
 * definition parser. Is package-private, as people should not be using this class directly, but rather through the
 * higher-level webflow-config Spring 2.x configuration namespace.
 * 
 * @author Keith Donald
 * @author Jeremy Grelle
 */
class FlowRegistryFactoryBean implements FactoryBean, InitializingBean {

	private FlowLocation[] flowLocations;

	private FlowBuilderInfo[] flowBuilders;

	private FlowBuilderServices flowBuilderServices;

	private FlowDefinitionRegistry parent;

	/**
	 * The definition registry produced by this factory bean.
	 */
	private FlowDefinitionRegistryImpl flowRegistry;

	/**
	 * The model registry used to build flow models that can be assembled into registerable Flows.
	 */
	private FlowModelRegistryImpl flowModelRegistry;

	/**
	 * A helper for creating abstract representation of externalized flow definition resources.
	 */
	private FlowDefinitionResourceFactory flowResourceFactory;

	/**
	 * Flow definitions defined in external files that should be registered in the registry produced by this factory
	 * bean.
	 */
	public void setFlowLocations(FlowLocation[] flowLocations) {
		this.flowLocations = flowLocations;
	}

	/**
	 * Java {@link FlowBuilder flow builder} classes that should be registered in the registry produced by this factory
	 * bean.
	 */
	public void setFlowBuilders(FlowBuilderInfo[] flowBuilders) {
		this.flowBuilders = flowBuilders;
	}

	/**
	 * The holder for services needed to build flow definitions registered in this registry.
	 */
	public void setFlowBuilderServices(FlowBuilderServices flowBuilderServices) {
		this.flowBuilderServices = flowBuilderServices;
	}

	/**
	 * The parent of the registry created by this factory bean.
	 */
	public void setParent(FlowDefinitionRegistry parent) {
		this.parent = parent;
	}

	public void afterPropertiesSet() throws Exception {
		flowResourceFactory = new FlowDefinitionResourceFactory(flowBuilderServices.getApplicationContext());
		flowRegistry = new FlowDefinitionRegistryImpl();
		flowRegistry.setParent(parent);
		flowModelRegistry = new FlowModelRegistryImpl();
		registerFlowLocations();
		registerFlowBuilders();
	}

	public Object getObject() throws Exception {
		return flowRegistry;
	}

	public Class getObjectType() {
		return FlowDefinitionRegistry.class;
	}

	public boolean isSingleton() {
		return true;
	}

	private void registerFlowLocations() {
		if (flowLocations != null) {
			for (int i = 0; i < flowLocations.length; i++) {
				FlowLocation location = flowLocations[i];
				flowRegistry.registerFlowDefinition(createFlowDefinitionHolder(location));
			}
		}
	}

	private void registerFlowBuilders() {
		if (flowBuilders != null) {
			for (int i = 0; i < flowBuilders.length; i++) {
				FlowBuilderInfo builderInfo = flowBuilders[i];
				flowRegistry.registerFlowDefinition(buildFlowDefinition(builderInfo));
			}
		}
	}

	private FlowDefinitionHolder createFlowDefinitionHolder(FlowLocation location) {
		FlowDefinitionResource flowResource = createResource(location);
		FlowBuilder builder = createFlowBuilder(flowResource);
		FlowBuilderContext builderContext = new FlowBuilderContextImpl(flowResource.getId(), flowResource
				.getAttributes(), flowRegistry, flowBuilderServices);
		FlowAssembler assembler = new FlowAssembler(builder, builderContext);
		return new DefaultFlowHolder(assembler);
	}

	private FlowDefinitionResource createResource(FlowLocation location) {
		AttributeMap flowAttributes = getFlowAttributes(location.getAttributes());
		return flowResourceFactory.createResource(location.getPath(), flowAttributes, location.getId());
	}

	private AttributeMap getFlowAttributes(Set attributes) {
		MutableAttributeMap flowAttributes = null;
		if (!attributes.isEmpty()) {
			flowAttributes = new LocalAttributeMap();
			for (Iterator it = attributes.iterator(); it.hasNext();) {
				FlowElementAttribute attribute = (FlowElementAttribute) it.next();
				flowAttributes.put(attribute.getName(), getConvertedValue(attribute));
			}
		}
		return flowAttributes;
	}

	private FlowBuilder createFlowBuilder(FlowDefinitionResource resource) {
		return new FlowModelFlowBuilder(createFlowModelHolder(resource));
	}

	private FlowModelHolder createFlowModelHolder(FlowDefinitionResource resource) {
		FlowModelHolder modelHolder = new DefaultFlowModelHolder(createFlowModelBuilder(resource));
		flowModelRegistry.registerFlowModel(resource.getId(), modelHolder);
		return modelHolder;
	}

	private FlowModelBuilder createFlowModelBuilder(FlowDefinitionResource resource) {
		if (isXml(resource.getPath())) {
			return new XmlFlowModelBuilder(resource.getPath(), flowModelRegistry);
		} else {
			throw new IllegalArgumentException(resource
					+ " is not a supported resource type; supported types are [.xml]");
		}
	}

	private boolean isXml(Resource flowResource) {
		return flowResource.getFilename().endsWith(".xml");
	}

	private Object getConvertedValue(FlowElementAttribute attribute) {
		if (attribute.needsTypeConversion()) {
			Class targetType = fromStringToClass(attribute.getType());
			ConversionExecutor converter = flowBuilderServices.getConversionService().getConversionExecutor(
					String.class, targetType);
			return converter.execute(attribute.getValue());
		} else {
			return attribute.getValue();
		}
	}

	private Class fromStringToClass(String type) {
		return (Class) flowBuilderServices.getConversionService().getConversionExecutor(String.class, Class.class)
				.execute(type);
	}

	private FlowDefinition buildFlowDefinition(FlowBuilderInfo builderInfo) {
		try {
			Class flowBuilderClass = ClassUtils.forName(builderInfo.getClassName());
			FlowBuilder builder = (FlowBuilder) flowBuilderClass.newInstance();
			AttributeMap flowAttributes = getFlowAttributes(builderInfo.getAttributes());
			FlowBuilderContext builderContext = new FlowBuilderContextImpl(builderInfo.getId(), flowAttributes,
					flowRegistry, flowBuilderServices);
			FlowAssembler assembler = new FlowAssembler(builder, builderContext);
			return assembler.assembleFlow();
		} catch (ClassNotFoundException e) {
			throw new FlowDefinitionConstructionException(builderInfo.getId(), e);
		} catch (InstantiationException e) {
			throw new FlowDefinitionConstructionException(builderInfo.getId(), e);
		} catch (IllegalAccessException e) {
			throw new FlowDefinitionConstructionException(builderInfo.getId(), e);
		}
	}

}