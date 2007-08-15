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
package org.springframework.webflow.action;

import org.springframework.binding.expression.EvaluationContext;
import org.springframework.binding.expression.Expression;
import org.springframework.util.Assert;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * An action that evaluates an expression and optionally exposes its result.
 * <p>
 * Delegates to a helper {@link ResultEventFactorySelector} strategy to determine how to map the evaluation result to an
 * action outcome {@link Event}.
 * 
 * @see Expression
 * @see ActionResultExposer
 * @see ResultEventFactorySelector
 * 
 * @author Keith Donald
 */
public class EvaluateAction extends AbstractAction {

	/**
	 * The expression to evaluate when this action is invoked. Required.
	 */
	private Expression expression;

	/**
	 * The helper that will expose the expression evaluation result. Optional.
	 */
	private ActionResultExposer evaluationResultExposer;

	/**
	 * The selector for the factory that will create the action result event callers can respond to.
	 */
	private ResultEventFactorySelector resultEventFactorySelector = new ResultEventFactorySelector();

	/**
	 * Create a new evaluate action.
	 * @param expression the expression to evaluate
	 */
	public EvaluateAction(Expression expression) {
		this(expression, null);
	}

	/**
	 * Create a new evaluate action.
	 * @param expression the expression to evaluate
	 * @param evaluationResultExposer the strategy for how the expression result will be exposed to the flow
	 */
	public EvaluateAction(Expression expression, ActionResultExposer evaluationResultExposer) {
		Assert.notNull(expression, "The expression this action should evaluate is required");
		this.expression = expression;
		this.evaluationResultExposer = evaluationResultExposer;
	}

	protected Event doExecute(RequestContext context) throws Exception {
		Object result = expression.evaluate(context, getEvaluationContext(context));
		if (evaluationResultExposer != null) {
			evaluationResultExposer.exposeResult(result, context);
		}
		return resultEventFactorySelector.forResult(result).createResultEvent(this, result, context);
	}

	/**
	 * Template method subclasses may override to customize the expressin evaluation context. This implementation
	 * returns null.
	 * @param context the request context
	 * @return the evaluation context
	 */
	protected EvaluationContext getEvaluationContext(RequestContext context) {
		return null;
	}
}