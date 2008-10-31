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
package org.springframework.binding.expression;

import org.springframework.core.style.StylerUtils;

/**
 * An evaluation exception indicating a expression failed to evaluate because the source value could not be coerced to
 * the target class
 * @author Scott Andrews
 */
public class ValueCoercionException extends EvaluationException {

	private Object value;
	private Class targetClass;

	/**
	 * Creates a new property not found exception
	 * @param contextClass the class of object upon which coercion was attempted
	 * @param property the property that could not be coerced
	 * @param value the value that could not be coerced
	 * @param targetClass the class the value could not be coerced to
	 */
	public ValueCoercionException(Class contextClass, String property, Object value, Class targetClass) {
		this(contextClass, property, value, targetClass, null);
		this.value = value;
		this.targetClass = targetClass;
	}

	/**
	 * Creates a new property not found exception
	 * @param contextClass the class of object upon which coercion was attempted
	 * @param property the property that could not be coerced
	 * @param value the value that could not be coerced
	 * @param targetClass the class the value could not be coerced to
	 * @param cause root cause of the failure
	 */
	public ValueCoercionException(Class contextClass, String property, Object value, Class targetClass, Throwable cause) {
		super(contextClass, property, "value " + StylerUtils.style(value) + " could not be coerced to ["
				+ targetClass.getName() + "]", cause);
		this.value = value;
		this.targetClass = targetClass;
	}

	/**
	 * @return the value that could not be coerced
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @return the class the value could not be coerced to
	 */
	public Class getTargetClass() {
		return targetClass;
	}

}
