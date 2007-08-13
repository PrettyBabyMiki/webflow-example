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
package org.springframework.binding.expression;

import java.util.Map;

/**
 * A context object with two main responsibities:
 * <ol>
 * <li>Exposing information to an expression to influence an evaluation attempt.
 * <li>Providing operations for recording progress or errors during the expression evaluation process.
 * </ol>
 * 
 * @author Keith Donald
 */
public interface EvaluationContext {

	/**
	 * Returns a map of attributes that can be used to influence expression evaluation.
	 * @return the evaluation attributes
	 */
	public Map getAttributes();

}