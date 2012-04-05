/*
 * Copyright 2004-2012 the original author or authors.
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
package org.springframework.webflow.engine.model.builder.xml;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.springframework.core.io.Resource;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.springframework.webflow.engine.model.AbstractActionModel;
import org.springframework.webflow.engine.model.AbstractStateModel;
import org.springframework.webflow.engine.model.ActionStateModel;
import org.springframework.webflow.engine.model.AttributeModel;
import org.springframework.webflow.engine.model.BeanImportModel;
import org.springframework.webflow.engine.model.BinderModel;
import org.springframework.webflow.engine.model.BindingModel;
import org.springframework.webflow.engine.model.DecisionStateModel;
import org.springframework.webflow.engine.model.EndStateModel;
import org.springframework.webflow.engine.model.EvaluateModel;
import org.springframework.webflow.engine.model.ExceptionHandlerModel;
import org.springframework.webflow.engine.model.FlowModel;
import org.springframework.webflow.engine.model.IfModel;
import org.springframework.webflow.engine.model.InputModel;
import org.springframework.webflow.engine.model.OutputModel;
import org.springframework.webflow.engine.model.PersistenceContextModel;
import org.springframework.webflow.engine.model.RenderModel;
import org.springframework.webflow.engine.model.SecuredModel;
import org.springframework.webflow.engine.model.SetModel;
import org.springframework.webflow.engine.model.SubflowStateModel;
import org.springframework.webflow.engine.model.TransitionModel;
import org.springframework.webflow.engine.model.VarModel;
import org.springframework.webflow.engine.model.ViewStateModel;
import org.springframework.webflow.engine.model.builder.FlowModelBuilder;
import org.springframework.webflow.engine.model.builder.FlowModelBuilderException;
import org.springframework.webflow.engine.model.registry.FlowModelLocator;
import org.springframework.webflow.engine.model.registry.NoSuchFlowModelException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Builds a flow model from a XML-based flow definition resource.
 * 
 * @author Keith Donald
 * @author Scott Andrews
 */
public class XmlFlowModelBuilder implements FlowModelBuilder {

	private Resource resource;

	private FlowModelLocator modelLocator;

	private DocumentLoader documentLoader = new DefaultDocumentLoader();

	private Document document;

	private long lastModifiedTimestamp;

	private FlowModel flowModel;

	/**
	 * Create a new XML flow model builder that will parse the XML document at the specified resource location and use
	 * the provided locator to access parent flow models.
	 * @param resource the path to the XML flow definition (required)
	 */
	public XmlFlowModelBuilder(Resource resource) {
		init(resource, null);
	}

	/**
	 * Create a new XML flow model builder that will parse the XML document at the specified resource location and use
	 * the provided locator to access parent flow models.
	 * @param resource the path to the XML flow definition (required)
	 * @param modelLocator a locator for parent flow models to support flow inheritance
	 */
	public XmlFlowModelBuilder(Resource resource, FlowModelLocator modelLocator) {
		init(resource, modelLocator);
	}

	/**
	 * Sets the loader that will load the XML-based flow definition document. Optional, defaults to
	 * {@link DefaultDocumentLoader}.
	 * @param documentLoader the document loader
	 */
	public void setDocumentLoader(DocumentLoader documentLoader) {
		Assert.notNull(documentLoader, "The XML document loader is required");
		this.documentLoader = documentLoader;
	}

	public void init() throws FlowModelBuilderException {
		try {
			document = documentLoader.loadDocument(resource);
			initLastModifiedTimestamp();
		} catch (IOException e) {
			throw new FlowModelBuilderException("Could not access the XML flow definition at " + resource, e);
		} catch (ParserConfigurationException e) {
			throw new FlowModelBuilderException("Could not configure the parser to parse the XML flow definition at "
					+ resource, e);
		} catch (SAXException e) {
			throw new FlowModelBuilderException("Could not parse the XML flow definition document at " + resource, e);
		}
	}

	public void build() throws FlowModelBuilderException {
		if (getDocumentElement() == null) {
			throw new FlowModelBuilderException(
					"The FlowModelBuilder must be initialized first -- called init() before calling build()");
		}
		flowModel = parseFlow(getDocumentElement());
		mergeFlows();
		mergeStates();
	}

	public FlowModel getFlowModel() throws FlowModelBuilderException {
		if (flowModel == null) {
			throw new FlowModelBuilderException(
					"The FlowModel must be built first -- called init() and build() before calling getFlowModel()");
		}
		return flowModel;
	}

	public void dispose() throws FlowModelBuilderException {
		document = null;
		flowModel = null;
	}

	public Resource getFlowModelResource() {
		return resource;
	}

	public boolean hasFlowModelResourceChanged() {
		if (lastModifiedTimestamp == -1) {
			return false;
		}
		try {
			long lastModified = resource.lastModified();
			if (lastModified > lastModifiedTimestamp) {
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Returns the DOM document parsed from the XML file.
	 */
	protected Document getDocument() {
		return document;
	}

	/**
	 * Returns the root document element.
	 */
	protected Element getDocumentElement() {
		return document != null ? document.getDocumentElement() : null;
	}

	private void init(Resource resource, FlowModelLocator modelLocator) {
		Assert.notNull(resource, "The location of the XML-based flow definition is required");
		this.resource = resource;
		this.modelLocator = modelLocator;
	}

	private void initLastModifiedTimestamp() {
		try {
			lastModifiedTimestamp = resource.lastModified();
		} catch (IOException e) {
			lastModifiedTimestamp = -1;
		}
	}

	private FlowModel parseFlow(Element element) {
		FlowModel flow = new FlowModel();
		flow.setAbstract(element.getAttribute("abstract"));
		flow.setParent(element.getAttribute("parent"));
		flow.setStartStateId(element.getAttribute("start-state"));
		flow.setAttributes(parseAttributes(element));
		flow.setSecured(parseSecured(element));
		flow.setPersistenceContext(parsePersistenceContext(element));
		flow.setVars(parseVars(element));
		flow.setInputs(parseInputs(element));
		flow.setOnStartActions(parseOnStartActions(element));
		flow.setStates(parseStates(element));
		flow.setGlobalTransitions(parseGlobalTransitions(element));
		flow.setOnEndActions(parseOnEndActions(element));
		flow.setOutputs(parseOutputs(element));
		flow.setExceptionHandlers(parseExceptionHandlers(element));
		flow.setBeanImports(parseBeanImports(element));
		return flow;
	}

	private LinkedList<AttributeModel> parseAttributes(Element element) {
		List<Element> attributeElements = DomUtils.getChildElementsByTagName(element, "attribute");
		if (attributeElements.isEmpty()) {
			return null;
		}
		LinkedList<AttributeModel> attributes = new LinkedList<AttributeModel>();
		for (Element element2 : attributeElements) {
			attributes.add(parseAttribute(element2));
		}
		return attributes;
	}

	private LinkedList<VarModel> parseVars(Element element) {
		List<Element> varElements = DomUtils.getChildElementsByTagName(element, "var");
		if (varElements.isEmpty()) {
			return null;
		}
		LinkedList<VarModel> vars = new LinkedList<VarModel>();
		for (Element element2 : varElements) {
			vars.add(parseVar(element2));
		}
		return vars;
	}

	private LinkedList<InputModel> parseInputs(Element element) {
		List<Element> inputElements = DomUtils.getChildElementsByTagName(element, "input");
		if (inputElements.isEmpty()) {
			return null;
		}
		LinkedList<InputModel> inputs = new LinkedList<InputModel>();
		for (Element element2 : inputElements) {
			inputs.add(parseInput(element2));
		}
		return inputs;
	}

	private LinkedList<OutputModel> parseOutputs(Element element) {
		List<Element> outputElements = DomUtils.getChildElementsByTagName(element, "output");
		if (outputElements.isEmpty()) {
			return null;
		}
		LinkedList<OutputModel> outputs = new LinkedList<OutputModel>();
		for (Element element2 : outputElements) {
			outputs.add(parseOutput(element2));
		}
		return outputs;
	}

	private LinkedList<AbstractActionModel> parseActions(Element element) {
		List<Element> actionElements = DomUtils.getChildElementsByTagName(element, new String[] { "evaluate", "render",
				"set" });
		if (actionElements.isEmpty()) {
			return null;
		}
		LinkedList<AbstractActionModel> actions = new LinkedList<AbstractActionModel>();
		for (Element element2 : actionElements) {
			actions.add(parseAction(element2));
		}
		return actions;
	}

	private LinkedList<AbstractStateModel> parseStates(Element element) {
		List<Element> stateElements = DomUtils.getChildElementsByTagName(element, new String[] { "view-state",
				"action-state", "decision-state", "subflow-state", "end-state" });
		if (stateElements.isEmpty()) {
			return null;
		}
		LinkedList<AbstractStateModel> states = new LinkedList<AbstractStateModel>();
		for (Element element2 : stateElements) {
			states.add(parseState(element2));
		}
		return states;
	}

	private LinkedList<TransitionModel> parseTransitions(Element element) {
		List<Element> transitionElements = DomUtils.getChildElementsByTagName(element, "transition");
		if (transitionElements.isEmpty()) {
			return null;
		}
		LinkedList<TransitionModel> transitions = new LinkedList<TransitionModel>();
		for (Element element2 : transitionElements) {
			transitions.add(parseTransition(element2));
		}
		return transitions;
	}

	private LinkedList<ExceptionHandlerModel> parseExceptionHandlers(Element element) {
		List<Element> exceptionHandlerElements = DomUtils.getChildElementsByTagName(element, "exception-handler");
		if (exceptionHandlerElements.isEmpty()) {
			return null;
		}
		LinkedList<ExceptionHandlerModel> exceptionHandlers = new LinkedList<ExceptionHandlerModel>();
		for (Element element2 : exceptionHandlerElements) {
			exceptionHandlers.add(parseExceptionHandler(element2));
		}
		return exceptionHandlers;
	}

	private LinkedList<BeanImportModel> parseBeanImports(Element element) {
		List<Element> importElements = DomUtils.getChildElementsByTagName(element, "bean-import");
		if (importElements.isEmpty()) {
			return null;
		}
		LinkedList<BeanImportModel> beanImports = new LinkedList<BeanImportModel>();
		for (Element element2 : importElements) {
			beanImports.add(parseBeanImport(element2));
		}
		return beanImports;
	}

	private LinkedList<IfModel> parseIfs(Element element) {
		List<Element> ifElements = DomUtils.getChildElementsByTagName(element, "if");
		if (ifElements.isEmpty()) {
			return null;
		}
		LinkedList<IfModel> ifs = new LinkedList<IfModel>();
		for (Element element2 : ifElements) {
			ifs.add(parseIf(element2));
		}
		return ifs;
	}

	private AbstractActionModel parseAction(Element element) {
		if (DomUtils.nodeNameEquals(element, "evaluate")) {
			return parseEvaluate(element);
		} else if (DomUtils.nodeNameEquals(element, "render")) {
			return parseRender(element);
		} else if (DomUtils.nodeNameEquals(element, "set")) {
			return parseSet(element);
		} else {
			throw new FlowModelBuilderException("Unknown action element encountered '" + element.getLocalName() + "'");
		}
	}

	private AbstractStateModel parseState(Element element) {
		if (DomUtils.nodeNameEquals(element, "view-state")) {
			return parseViewState(element);
		} else if (DomUtils.nodeNameEquals(element, "action-state")) {
			return parseActionState(element);
		} else if (DomUtils.nodeNameEquals(element, "decision-state")) {
			return parseDecisionState(element);
		} else if (DomUtils.nodeNameEquals(element, "subflow-state")) {
			return parseSubflowState(element);
		} else if (DomUtils.nodeNameEquals(element, "end-state")) {
			return parseEndState(element);
		} else {
			throw new FlowModelBuilderException("Unknown state element encountered '" + element.getLocalName() + "'");
		}
	}

	private LinkedList<TransitionModel> parseGlobalTransitions(Element element) {
		element = DomUtils.getChildElementByTagName(element, "global-transitions");
		if (element == null) {
			return null;
		} else {
			return parseTransitions(element);
		}
	}

	private AttributeModel parseAttribute(Element element) {
		AttributeModel attribute = new AttributeModel(element.getAttribute("name"), parseAttributeValue(element));
		attribute.setType(element.getAttribute("type"));
		return attribute;
	}

	private String parseAttributeValue(Element element) {
		if (element.hasAttribute("value")) {
			return element.getAttribute("value");
		} else {
			Element valueElement = DomUtils.getChildElementByTagName(element, "value");
			if (valueElement != null) {
				return DomUtils.getTextValue(valueElement);
			} else {
				return null;
			}
		}
	}

	private SecuredModel parseSecured(Element element) {
		element = DomUtils.getChildElementByTagName(element, "secured");
		if (element == null) {
			return null;
		} else {
			SecuredModel secured = new SecuredModel(element.getAttribute("attributes"));
			secured.setMatch(element.getAttribute("match"));
			return secured;
		}
	}

	private PersistenceContextModel parsePersistenceContext(Element element) {
		element = DomUtils.getChildElementByTagName(element, "persistence-context");
		if (element == null) {
			return null;
		} else {
			return new PersistenceContextModel();
		}
	}

	private VarModel parseVar(Element element) {
		return new VarModel(element.getAttribute("name"), element.getAttribute("class"));
	}

	private InputModel parseInput(Element element) {
		InputModel input = new InputModel(element.getAttribute("name"), element.getAttribute("value"));
		input.setType(element.getAttribute("type"));
		input.setRequired(element.getAttribute("required"));
		return input;
	}

	private OutputModel parseOutput(Element element) {
		OutputModel output = new OutputModel(element.getAttribute("name"), element.getAttribute("value"));
		output.setType(element.getAttribute("type"));
		output.setRequired(element.getAttribute("required"));
		return output;
	}

	private TransitionModel parseTransition(Element element) {
		TransitionModel transition = new TransitionModel();
		transition.setOn(element.getAttribute("on"));
		transition.setTo(element.getAttribute("to"));
		transition.setOnException(element.getAttribute("on-exception"));
		transition.setBind(element.getAttribute("bind"));
		transition.setValidate(element.getAttribute("validate"));
		transition.setHistory(element.getAttribute("history"));
		transition.setAttributes(parseAttributes(element));
		transition.setSecured(parseSecured(element));
		transition.setActions(parseActions(element));
		return transition;
	}

	private ExceptionHandlerModel parseExceptionHandler(Element element) {
		return new ExceptionHandlerModel(element.getAttribute("bean"));
	}

	private BeanImportModel parseBeanImport(Element element) {
		return new BeanImportModel(element.getAttribute("resource"));
	}

	private IfModel parseIf(Element element) {
		IfModel ifModel = new IfModel(element.getAttribute("test"), element.getAttribute("then"));
		ifModel.setElse(element.getAttribute("else"));
		return ifModel;
	}

	private LinkedList<AbstractActionModel> parseOnStartActions(Element element) {
		Element onStartElement = DomUtils.getChildElementByTagName(element, "on-start");
		if (onStartElement != null) {
			return parseActions(onStartElement);
		} else {
			return null;
		}
	}

	private LinkedList<AbstractActionModel> parseOnEntryActions(Element element) {
		Element onEntryElement = DomUtils.getChildElementByTagName(element, "on-entry");
		if (onEntryElement != null) {
			return parseActions(onEntryElement);
		} else {
			return null;
		}
	}

	private LinkedList<AbstractActionModel> parseOnRenderActions(Element element) {
		Element onRenderElement = DomUtils.getChildElementByTagName(element, "on-render");
		if (onRenderElement != null) {
			return parseActions(onRenderElement);
		} else {
			return null;
		}
	}

	private BinderModel parseBinder(Element element) {
		Element binderElement = DomUtils.getChildElementByTagName(element, "binder");
		if (binderElement != null) {
			BinderModel binder = new BinderModel();
			binder.setBindings(parseBindings(binderElement));
			return binder;
		} else {
			return null;
		}
	}

	private LinkedList<BindingModel> parseBindings(Element element) {
		List<Element> bindingElements = DomUtils.getChildElementsByTagName(element, "binding");
		if (bindingElements.isEmpty()) {
			return null;
		}
		LinkedList<BindingModel> bindings = new LinkedList<BindingModel>();
		for (Element element2 : bindingElements) {
			bindings.add(parseBinding(element2));
		}
		return bindings;
	}

	private BindingModel parseBinding(Element element) {
		return new BindingModel(element.getAttribute("property"), element.getAttribute("converter"),
				element.getAttribute("required"));
	}

	private LinkedList<AbstractActionModel> parseOnExitActions(Element element) {
		Element onExitElement = DomUtils.getChildElementByTagName(element, "on-exit");
		if (onExitElement != null) {
			return parseActions(onExitElement);
		} else {
			return null;
		}
	}

	private LinkedList<AbstractActionModel> parseOnEndActions(Element element) {
		Element onEndElement = DomUtils.getChildElementByTagName(element, "on-end");
		if (onEndElement != null) {
			return parseActions(onEndElement);
		} else {
			return null;
		}
	}

	private EvaluateModel parseEvaluate(Element element) {
		EvaluateModel evaluate = new EvaluateModel(element.getAttribute("expression"));
		evaluate.setResult(element.getAttribute("result"));
		evaluate.setResultType(element.getAttribute("result-type"));
		evaluate.setAttributes(parseAttributes(element));
		return evaluate;
	}

	private RenderModel parseRender(Element element) {
		RenderModel render = new RenderModel(element.getAttribute("fragments"));
		render.setAttributes(parseAttributes(element));
		return render;
	}

	private SetModel parseSet(Element element) {
		SetModel set = new SetModel(element.getAttribute("name"), element.getAttribute("value"));
		set.setType(element.getAttribute("type"));
		set.setAttributes(parseAttributes(element));
		return set;
	}

	private ActionStateModel parseActionState(Element element) {
		ActionStateModel state = new ActionStateModel(element.getAttribute("id"));
		state.setParent(element.getAttribute("parent"));
		state.setAttributes(parseAttributes(element));
		state.setSecured(parseSecured(element));
		state.setOnEntryActions(parseOnEntryActions(element));
		state.setTransitions(parseTransitions(element));
		state.setOnExitActions(parseOnExitActions(element));
		state.setActions(parseActions(element));
		state.setExceptionHandlers(parseExceptionHandlers(element));
		return state;
	}

	private ViewStateModel parseViewState(Element element) {
		ViewStateModel state = new ViewStateModel(element.getAttribute("id"));
		state.setParent(element.getAttribute("parent"));
		state.setView(element.getAttribute("view"));
		state.setRedirect(element.getAttribute("redirect"));
		state.setPopup(element.getAttribute("popup"));
		state.setModel(element.getAttribute("model"));
		state.setVars(parseVars(element));
		state.setBinder(parseBinder(element));
		state.setOnRenderActions(parseOnRenderActions(element));
		state.setAttributes(parseAttributes(element));
		state.setSecured(parseSecured(element));
		state.setOnEntryActions(parseOnEntryActions(element));
		state.setExceptionHandlers(parseExceptionHandlers(element));
		state.setTransitions(parseTransitions(element));
		state.setOnExitActions(parseOnExitActions(element));
		return state;
	}

	private DecisionStateModel parseDecisionState(Element element) {
		DecisionStateModel state = new DecisionStateModel(element.getAttribute("id"));
		state.setParent(element.getAttribute("parent"));
		state.setIfs(parseIfs(element));
		state.setOnExitActions(parseOnExitActions(element));
		state.setAttributes(parseAttributes(element));
		state.setSecured(parseSecured(element));
		state.setOnEntryActions(parseOnEntryActions(element));
		state.setExceptionHandlers(parseExceptionHandlers(element));
		return state;
	}

	private SubflowStateModel parseSubflowState(Element element) {
		SubflowStateModel state = new SubflowStateModel(element.getAttribute("id"), element.getAttribute("subflow"));
		state.setParent(element.getAttribute("parent"));
		state.setSubflowAttributeMapper(element.getAttribute("subflow-attribute-mapper"));
		state.setInputs(parseInputs(element));
		state.setOutputs(parseOutputs(element));
		state.setAttributes(parseAttributes(element));
		state.setSecured(parseSecured(element));
		state.setOnEntryActions(parseOnEntryActions(element));
		state.setExceptionHandlers(parseExceptionHandlers(element));
		state.setTransitions(parseTransitions(element));
		state.setOnExitActions(parseOnExitActions(element));
		return state;
	}

	private EndStateModel parseEndState(Element element) {
		EndStateModel state = new EndStateModel(element.getAttribute("id"));
		state.setParent(element.getAttribute("parent"));
		state.setView(element.getAttribute("view"));
		state.setCommit(element.getAttribute("commit"));
		state.setOutputs(parseOutputs(element));
		state.setAttributes(parseAttributes(element));
		state.setSecured(parseSecured(element));
		state.setOnEntryActions(parseOnEntryActions(element));
		state.setExceptionHandlers(parseExceptionHandlers(element));
		return state;
	}

	private void mergeFlows() {
		if (flowModel.getParent() != null) {
			List<String> parents = Arrays.asList(StringUtils.trimArrayElements(flowModel.getParent().split(",")));
			for (String parentFlowId : parents) {
				if (StringUtils.hasText(parentFlowId)) {
					try {
						flowModel.merge(modelLocator.getFlowModel(parentFlowId));
					} catch (NoSuchFlowModelException e) {
						throw new FlowModelBuilderException("Unable to find flow '" + parentFlowId
								+ "' to inherit from", e);
					}
				}
			}
		}
	}

	private void mergeStates() {
		if (flowModel.getStates() == null) {
			return;
		}
		for (AbstractStateModel childState : flowModel.getStates()) {
			String parent = childState.getParent();
			if (childState.getParent() != null) {
				String flowId;
				String stateId;
				AbstractStateModel parentState = null;
				int hashIndex = parent.indexOf("#");
				if (hashIndex == -1) {
					throw new FlowModelBuilderException("Invalid parent syntax '" + parent
							+ "', should take form 'flowId#stateId'");
				}
				flowId = parent.substring(0, hashIndex).trim();
				stateId = parent.substring(hashIndex + 1).trim();
				try {
					if (StringUtils.hasText(flowId)) {
						parentState = modelLocator.getFlowModel(flowId).getStateById(stateId);
					} else {
						parentState = flowModel.getStateById(stateId);
					}
					if (parentState == null) {
						throw new FlowModelBuilderException("Unable to find state '" + stateId + "' in flow '" + flowId
								+ "'");
					}
					childState.merge(parentState);
				} catch (NoSuchFlowModelException e) {
					throw new FlowModelBuilderException("Unable to find flow '" + flowId + "' to inherit from", e);
				} catch (ClassCastException e) {
					throw new FlowModelBuilderException("Parent state type '" + parentState.getClass().getName()
							+ "' cannot be merged with state type '" + childState.getClass().getName() + "'", e);

				}
			}
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("resource", resource).toString();
	}

}
