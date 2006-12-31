/*
 * Copyright 2002-2007 the original author or authors.
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
package org.springframework.binding.expression.support;

import org.springframework.binding.expression.EvaluationContext;
import org.springframework.binding.expression.EvaluationException;
import org.springframework.binding.expression.Expression;
import org.springframework.util.ObjectUtils;

/**
 * A simple expression evaluator that just returns a fixed result on each
 * evaluation.
 * 
 * @author Keith Donald
 */
public class StaticExpression implements Expression {

	/**
	 * The value expression.
	 */
	private Object value;

	/**
	 * Create a static evaluator for the given value.
	 * @param value the value
	 */
	public StaticExpression(Object value) {
		this.value = value;
	}

	public int hashCode() {
		if (value == null) {
			return 0;
		}
		else {
			return value.hashCode();
		}
	}

	public boolean equals(Object o) {
		if (!(o instanceof StaticExpression)) {
			return false;
		}
		StaticExpression other = (StaticExpression)o;
		return ObjectUtils.nullSafeEquals(value, other.value);
	}

	public Object evaluate(Object target, EvaluationContext context) throws EvaluationException {
		return value;
	}

	public String toString() {
		return String.valueOf(value);
	}
}