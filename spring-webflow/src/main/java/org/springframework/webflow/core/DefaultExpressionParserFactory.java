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
package org.springframework.webflow.core;

import org.springframework.binding.expression.ExpressionParser;

/**
 * Static helper factory that creates instances of the default expression parser
 * used by Spring Web Flow when requested. Marked final with a private
 * constructor to prevent subclassing.
 * <p>
 * The default is an OGNL based expression parser. Also asserts that OGNL is in
 * the classpath when this class is loaded.
 * 
 * @author Keith Donald
 */
public final class DefaultExpressionParserFactory {

	/**
	 * The singleton instance.
	 */
	private static ExpressionParser INSTANCE;

	// static factory - not instantiable
	private DefaultExpressionParserFactory() {
	}

	/**
	 * Returns the default expression parser. The returned expression parser is
	 * a thread-safe object.
	 * @return the expression parser
	 */
	public static synchronized ExpressionParser getExpressionParser() {
		if (INSTANCE == null) {
			INSTANCE = createDefaultExpressionParser();
		}
		return INSTANCE;
	}

	/**
	 * Create the default expression parser.
	 * @return the default expression parser
	 */
	private static ExpressionParser createDefaultExpressionParser() {
		try {
			Class.forName("ognl.Ognl");
			return new WebFlowOgnlExpressionParser();
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(
					"Unable to load the default expression parser: OGNL could not be found in the classpath.  "
					+ "Please add OGNL 2.x to your classpath or set the default ExpressionParser instance to something that is in the classpath.  "
					+ "Details: " + e.getMessage());
		} catch (NoClassDefFoundError e) {
			throw new IllegalStateException(
					"Unable to construct the default expression parser: ognl.Ognl could not be instantiated.  "
					+ "Please add OGNL 2.x to your classpath or set the default ExpressionParser instance to something that is in the classpath.  "
					+ "Details: " + e);
		}
	}
}