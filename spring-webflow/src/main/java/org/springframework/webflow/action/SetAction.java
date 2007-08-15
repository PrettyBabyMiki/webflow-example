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
import org.springframework.binding.expression.SettableExpression;
import org.springframework.util.Assert;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.ScopeType;

/**
 * An action that sets an attribute in a {@link ScopeType scope} when executed. Always returns the "success" event.
 * 
 * @author Keith Donald
 */
public class SetAction extends AbstractAction {

	/**
	 * The expression for setting the scoped attribute value.
	 */
	private SettableExpression attributeExpression;

	/**
	 * The target scope.
	 */
	private ScopeType scope;

	/**
	 * The expression for resolving the scoped attribute value.
	 */
	private Expression valueExpression;

	/**
	 * Creates a new set attribute action.
	 * @param attributeExpression the writeable attribute expression
	 * @param scope the target scope of the attribute
	 * @param valueExpression the evaluatable attribute value expression
	 */
	public SetAction(SettableExpression attributeExpression, ScopeType scope, Expression valueExpression) {
		Assert.notNull(attributeExpression, "The attribute expression is required");
		Assert.notNull(scope, "The scope type is required");
		Assert.notNull(valueExpression, "The value expression is required");
		this.attributeExpression = attributeExpression;
		this.scope = scope;
		this.valueExpression = valueExpression;
	}

	protected Event doExecute(RequestContext context) throws Exception {
		EvaluationContext evaluationContext = getEvaluationContext(context);
		Object value = valueExpression.evaluate(context, evaluationContext);
		MutableAttributeMap scopeMap = scope.getScope(context);
		attributeExpression.evaluateToSet(scopeMap, value, evaluationContext);
		return success();
	}

	/**
	 * Template method subclasses may override to customize the expression evaluation context. This implementation
	 * returns null.
	 * @param context the request context
	 * @return the evaluation context
	 */
	protected EvaluationContext getEvaluationContext(RequestContext context) {
		return null;
	}
}