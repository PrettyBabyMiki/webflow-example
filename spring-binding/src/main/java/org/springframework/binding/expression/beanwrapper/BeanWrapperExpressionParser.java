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
package org.springframework.binding.expression.beanwrapper;

import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ParserContext;
import org.springframework.binding.expression.ParserException;
import org.springframework.binding.expression.support.AbstractExpressionParser;

/**
 * An expression parser that parses Ognl expressions.
 * 
 * @author Keith Donald
 */
public class BeanWrapperExpressionParser extends AbstractExpressionParser {

	private ConversionService conversionService;

	public ConversionService getConversionService() {
		return conversionService;
	}

	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	protected Expression doParseExpression(String expressionString, ParserContext context) throws ParserException {
		return new BeanWrapperExpression(expressionString, conversionService);
	}
}