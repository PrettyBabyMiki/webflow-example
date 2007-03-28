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
package org.springframework.binding.expression.support;

import java.util.LinkedList;
import java.util.List;

import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.ParserException;
import org.springframework.binding.expression.SettableExpression;
import org.springframework.util.StringUtils;

/**
 * Abstract base class for expression parsers.
 * 
 * @author Keith Donald
 */
public abstract class AbstractExpressionParser implements ExpressionParser {

	/**
	 * The expression prefix.
	 */
	private static final String DEFAULT_EXPRESSION_PREFIX = "${";

	/**
	 * The expression suffix.
	 */
	private static final String DEFAULT_EXPRESSION_SUFFIX = "}";

	/**
	 * The marked expression delimter prefix.
	 */
	private String expressionPrefix = DEFAULT_EXPRESSION_PREFIX;

	/**
	 * The marked expression delimiter suffix.
	 */
	private String expressionSuffix = DEFAULT_EXPRESSION_SUFFIX;

	/**
	 * Returns the configured expression delimiter prefix. Defaults to "${".
	 */
	public String getExpressionPrefix() {
		return expressionPrefix;
	}

	/**
	 * Sets the expression delimiter prefix.
	 */
	public void setExpressionPrefix(String expressionPrefix) {
		this.expressionPrefix = expressionPrefix;
	}

	/**
	 * Returns the expression delimiter suffix. Defaults to "}".
	 */
	public String getExpressionSuffix() {
		return expressionSuffix;
	}

	/**
	 * Sets the expression delimiter suffix.
	 */
	public void setExpressionSuffix(String expressionSuffix) {
		this.expressionSuffix = expressionSuffix;
	}

	/**
	 * Check whether or not given criteria are expressed as an expression.
	 */
	public boolean isDelimitedExpression(String expressionString) {
		int prefixIndex = expressionString.indexOf(getExpressionPrefix());
		if (prefixIndex == -1) {
			return false;
		}
		int suffixIndex = expressionString.indexOf(getExpressionSuffix(), prefixIndex);
		if (suffixIndex == -1) {
			return false;
		}
		else {
			if (suffixIndex == prefixIndex + getExpressionPrefix().length()) {
				return false;
			}
			else {
				return true;
			}
		}
	}

	public final Expression parseExpression(String expressionString) throws ParserException {
		Expression[] expressions = parseExpressions(expressionString);
		if (expressions.length == 1) {
			return expressions[0];
		}
		else {
			return new CompositeStringExpression(expressions);
		}
	}

	public abstract SettableExpression parseSettableExpression(String expressionString) throws ParserException,
			UnsupportedOperationException;

	/**
	 * Helper that parses given expression string using the configured parser.
	 * The expression string can contain any number of expressions all contained
	 * in "${...}" markers. For instance: "foo${expr0}bar${expr1}". The static
	 * pieces of text will also be returned as Expressions that just return that
	 * static piece of text. As a result, evaluating all returned expressions
	 * and concatenating the results produces the complete evaluated string.
	 * @param expressionString the expression string
	 * @return the parsed expressions
	 * @throws ParserException when the expressions cannot be parsed
	 */
	private Expression[] parseExpressions(String expressionString) throws ParserException {
		List expressions = new LinkedList();
		if (StringUtils.hasText(expressionString)) {
			int startIdx = 0;
			while (startIdx < expressionString.length()) {
				int prefixIndex = expressionString.indexOf(getExpressionPrefix(), startIdx);
				if (prefixIndex >= startIdx) {
					// an expression was found
					if (prefixIndex > startIdx) {
						expressions.add(new StaticExpression(expressionString.substring(startIdx, prefixIndex)));
						startIdx = prefixIndex;
					}
					int nextPrefixIndex = expressionString.indexOf(getExpressionPrefix(),
							prefixIndex + getExpressionPrefix().length());
					int suffixIndex;
					if (nextPrefixIndex == -1) { 
						// this is the last expression in the expression string
						suffixIndex = expressionString.lastIndexOf(getExpressionSuffix()); 
					}
					else { 
						// another expression exists after this one in the expression string
						suffixIndex = expressionString.lastIndexOf(getExpressionSuffix(), nextPrefixIndex); 
					} 
					if (suffixIndex < (prefixIndex + getExpressionPrefix().length())) {
						throw new ParserException(expressionString, "No ending suffix '" + getExpressionSuffix()
								+ "' for expression starting at character " + prefixIndex + ": "
								+ expressionString.substring(prefixIndex), null);
					}
					else if (suffixIndex == prefixIndex + getExpressionPrefix().length()) {
						throw new ParserException(expressionString, "No expression defined within delimiter '"
								+ getExpressionPrefix() + getExpressionSuffix() + "' at character " + prefixIndex,
								null);
					}
					else {
						String expr = expressionString.substring(prefixIndex + getExpressionPrefix().length(),
								suffixIndex);
						expressions.add(doParseExpression(expr));
						startIdx = suffixIndex + 1;
					}
				}
				else {
					if (startIdx == 0) {
						// treat entire string as one expression
						expressions.add(doParseExpression(expressionString));
					}
					else {
						// no more ${expressions} found in string
						expressions.add(new StaticExpression(expressionString.substring(startIdx)));
					}
					startIdx = expressionString.length();
				}
			}
		}
		else {
			expressions.add(new StaticExpression(expressionString));
		}
		return (Expression[]) expressions.toArray(new Expression[expressions.size()]);
	}

	/**
	 * Template method for parsing a filtered expression string. Subclasses should
	 * override.
	 * @param expressionString the expression string
	 * @return the parsed expression
	 */
	protected abstract Expression doParseExpression(String expressionString);

}