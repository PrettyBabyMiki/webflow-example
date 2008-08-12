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
package org.springframework.webflow.engine.model;

import java.util.LinkedList;

import junit.framework.TestCase;

/**
 * Unit tests for {@link FlowModel}.
 */
public class FlowModelTests extends TestCase {

	public void testMergeable() {
		FlowModel child = new FlowModel();
		FlowModel parent = new FlowModel();
		assertTrue(child.isMergeableWith(parent));
	}

	public void testNotMergeableWithNull() {
		FlowModel child = new FlowModel();
		assertFalse(child.isMergeableWith(null));
	}

	public void testMergeAttributes() {
		FlowModel child = new FlowModel();
		FlowModel parent = new FlowModel();
		child.setAttributes(singleList(new AttributeModel("name", "value")));

		AttributeModel parentAttribute1 = new AttributeModel("name", "value");
		parentAttribute1.setType("type");
		AttributeModel parentAttribute2 = new AttributeModel("name2", "value2");
		parentAttribute2.setType("type2");
		parent.setAttributes(doubleList(parentAttribute1, parentAttribute2));

		child.merge(parent);
		assertEquals(2, child.getAttributes().size());
		assertEquals("name", ((AttributeModel) child.getAttributes().get(0)).getName());
		assertEquals("type", ((AttributeModel) child.getAttributes().get(0)).getType());
		assertEquals("name2", ((AttributeModel) child.getAttributes().get(1)).getName());
		assertEquals("type2", ((AttributeModel) child.getAttributes().get(1)).getType());
	}

	public void testMergeSecured() {
		FlowModel child = new FlowModel();
		FlowModel parent = new FlowModel();
		child.setSecured(new SecuredModel("secured"));
		SecuredModel secured = new SecuredModel("secured");
		secured.setMatch("all");
		parent.setSecured(secured);
		child.merge(parent);
		assertEquals("all", child.getSecured().getMatch());
	}

	public void testMergePersistenceContext() {
		FlowModel child = new FlowModel();
		FlowModel parent = new FlowModel();
		parent.setPersistenceContext(new PersistenceContextModel());
		child.merge(parent);
		assertNotNull(child.getPersistenceContext());
	}

	public void testMergeVars() {
		FlowModel parent = new FlowModel();
		parent.setVars(singleList(new VarModel("name", "value")));

		FlowModel child = new FlowModel();
		child.setVars(singleList(new VarModel("name", "value2")));

		child.merge(parent);
		assertEquals(1, child.getVars().size());
		assertEquals("value2", ((VarModel) child.getVars().get(0)).getClassName());
	}

	public void testMergeMappings() {
		FlowModel child = new FlowModel();
		FlowModel parent = new FlowModel();

		InputModel input1 = new InputModel("name", "value");
		input1.setType("type");
		input1.setType("required");

		InputModel input2 = new InputModel("name2", "value2");
		input2.setType("type2");
		input2.setRequired("required2");

		InputModel input3 = new InputModel("name3", "value3");
		input2.setType("type3");
		input2.setRequired("required3");

		child.setInputs(tripleList(input1, input2, input3));

		InputModel parentInput = new InputModel("name3", "value3");
		parentInput.setType("type3");
		parentInput.setRequired("required3");
		parent.setInputs(singleList(parentInput));

		child.merge(parent);
		assertEquals(3, child.getInputs().size());
	}

	public void testMergeOnStart() {
		FlowModel child = new FlowModel();
		child.setOnStartActions(tripleList(new EvaluateModel("expression"), new RenderModel("expression"),
				new SetModel("expression", "value")));

		FlowModel parent = new FlowModel();

		EvaluateModel eval = new EvaluateModel("expression");
		eval.setResult("result");
		parent.setOnStartActions(tripleList(eval, new RenderModel("expression"), new SetModel("expression", "value")));

		child.merge(parent);
		assertEquals(6, child.getOnStartActions().size());
		assertNotNull(((EvaluateModel) child.getOnStartActions().get(0)).getResult());
	}

	public void testMergeStates() {
		FlowModel child = new FlowModel();
		child.setStates(doubleList(new ViewStateModel("view"), new EndStateModel("end")));

		FlowModel parent = new FlowModel();
		ViewStateModel view = new ViewStateModel("view");
		view.setView("jsp");
		parent.setStates(tripleList(view, new DecisionStateModel("decider"), new ActionStateModel("action")));

		child.merge(parent);
		assertEquals(4, child.getStates().size());
		assertEquals("jsp", ((ViewStateModel) child.getStates().get(0)).getView());
	}

	public void testMergeGlobalTransitions() {
		FlowModel child = new FlowModel();
		TransitionModel transition1 = new TransitionModel();
		transition1.setOn("end");
		TransitionModel transition2 = new TransitionModel();
		transition2.setOn("start");
		child.setGlobalTransitions(doubleList(transition1, transition2));

		FlowModel parent = new FlowModel();
		transition1 = new TransitionModel();
		transition1.setOn("search");
		transition2 = new TransitionModel();
		transition2.setOn("end");
		transition2.setTo("theend");
		parent.setGlobalTransitions(doubleList(transition1, transition2));

		child.merge(parent);
		assertEquals(3, child.getGlobalTransitions().size());
		assertEquals("theend", ((TransitionModel) child.getGlobalTransitions().get(0)).getTo());
	}

	public void testMergeOnEnd() {
		FlowModel child = new FlowModel();
		child.setOnEndActions(tripleList(new EvaluateModel("expression"), new RenderModel("expression"), new SetModel(
				"expression", "value")));

		FlowModel parent = new FlowModel();
		EvaluateModel eval = new EvaluateModel("expression");
		eval.setResult("result");
		parent.setOnEndActions(tripleList(eval, new RenderModel("expression"), new SetModel("expression", "value")));

		child.merge(parent);
		assertEquals(6, child.getOnEndActions().size());
		assertNotNull(((EvaluateModel) child.getOnEndActions().get(0)).getResult());
	}

	public void testMergeExceptionHandlers() {
		FlowModel child = new FlowModel();
		child.setExceptionHandlers(doubleList(new ExceptionHandlerModel("bean1"), new ExceptionHandlerModel("bean2")));

		FlowModel parent = new FlowModel();
		parent.setExceptionHandlers(doubleList(new ExceptionHandlerModel("bean2"), new ExceptionHandlerModel("bean3")));

		child.merge(parent);
		assertEquals(4, child.getExceptionHandlers().size());
	}

	public void testMergeBeanImports() {
		FlowModel child = new FlowModel();
		child.setBeanImports(doubleList(new BeanImportModel("path1"), new BeanImportModel("path2")));

		FlowModel parent = new FlowModel();
		parent.setBeanImports(doubleList(new BeanImportModel("path1"), new BeanImportModel("path2")));

		child.merge(parent);
		assertEquals(4, child.getBeanImports().size());
	}

	private LinkedList singleList(Model model) {
		LinkedList list = new LinkedList();
		list.add(model);
		return list;
	}

	private LinkedList doubleList(Model model, Model model2) {
		LinkedList list = new LinkedList();
		list.add(model);
		list.add(model2);
		return list;
	}

	private LinkedList tripleList(Model model, Model model2, Model model3) {
		LinkedList list = new LinkedList();
		list.add(model);
		list.add(model2);
		list.add(model3);
		return list;
	}

}
