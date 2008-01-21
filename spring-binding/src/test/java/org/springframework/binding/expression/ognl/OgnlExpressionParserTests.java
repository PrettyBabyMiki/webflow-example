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
package org.springframework.binding.expression.ognl;

import junit.framework.TestCase;

import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionVariable;
import org.springframework.binding.expression.ParserException;
import org.springframework.binding.expression.support.ParserContextImpl;

public class OgnlExpressionParserTests extends TestCase {

	private OgnlExpressionParser parser = new OgnlExpressionParser();

	private TestBean bean = new TestBean();

	public void testParseSimple() {
		String exp = "${flag}";
		Expression e = parser.parseExpression(exp, null);
		assertNotNull(e);
		Boolean b = (Boolean) e.getValue(bean);
		assertFalse(b.booleanValue());
	}

	public void testIsTemplateExpressionAllowUndelimitedOgnlExpressions() {
		String exp = "some literal text";
		String exp2 = "${a delimited expression}";
		String exp3 = "${a malformed delimited expression";
		String exp4 = "a malformed delimited expression}";
		String exp5 = "${}";
		String exp6 = "a ${composite} expression";
		String exp7 = "a ${composite} ${malformed expression";
		assertTrue(parser.isTemplateExpression(exp));
		assertTrue(parser.isTemplateExpression(exp2));
		assertTrue(parser.isTemplateExpression(exp3));
		assertTrue(parser.isTemplateExpression(exp4));
		assertTrue(parser.isTemplateExpression(exp5));
		assertTrue(parser.isTemplateExpression(exp6));
		assertTrue(parser.isTemplateExpression(exp7));
	}

	public void testIsTemplateExpressionDoNotAllowUndelimitedOgnlExpressions() {
		parser.setAllowUndelimitedEvalExpressions(true);
		String exp = "some literal text";
		String exp2 = "${a delimited expression}";
		String exp3 = "${a malformed delimited expression";
		String exp4 = "a malformed delimited expression}";
		String exp5 = "${}";
		String exp6 = "a ${composite} expression";
		String exp7 = "a ${composite} ${malformed expression";

		assertFalse(parser.isTemplateExpression(exp));
		assertTrue(parser.isTemplateExpression(exp2));
		assertFalse(parser.isTemplateExpression(exp3));
		assertFalse(parser.isTemplateExpression(exp4));
		assertFalse(parser.isTemplateExpression(exp5));
		assertTrue(parser.isTemplateExpression(exp6));
		assertTrue(parser.isTemplateExpression(exp7));
	}

	public void testParseSimpleNotDelimitedAllowUndelimited() {
		parser.setAllowUndelimitedEvalExpressions(true);
		String exp = "flag";
		Expression e = parser.parseExpression(exp, null);
		assertNotNull(e);
		Boolean b = (Boolean) e.getValue(bean);
		assertFalse(b.booleanValue());
	}

	public void testParseSimpleLiteral() {
		String exp = "flag";
		Expression e = parser.parseExpression(exp, null);
		assertNotNull(e);
		assertEquals("flag", e.getValue(bean));
	}

	public void testParseEmpty() {
		Expression e = parser.parseExpression("", null);
		assertNotNull(e);
		assertEquals("", e.getValue(bean));
	}

	public void testParseComposite() {
		String exp = "hello ${flag} ${flag} ${flag}";
		assertTrue(parser.isTemplateExpression(exp));
		Expression e = parser.parseExpression(exp, null);
		assertNotNull(e);
		String str = (String) e.getValue(bean);
		assertEquals("hello false false false", str);
	}

	public void testEnclosedCompositeNotSupported() {
		String exp = "${hello ${flag} ${flag} ${flag}}";
		try {
			parser.parseExpression(exp, null);
			fail("Should've failed - not intended use");
		} catch (ParserException e) {
		}
	}

	public void testSyntaxError1() {
		try {
			parser.parseExpression("${", null);
			fail();
		} catch (ParserException e) {
		}
		try {
			String exp = "hello ${flag} ${abcd defg";
			parser.parseExpression(exp, null);
			fail("Should've failed - not intended use");
		} catch (ParserException e) {
		}
	}

	public void testSyntaxError2() {
		try {
			parser.parseExpression("${}", null);
			fail("Should've failed - not intended use");
		} catch (ParserException e) {
		}
		try {
			String exp = "hello ${flag} ${}";
			parser.parseExpression(exp, null);
			fail("Should've failed - not intended use");
		} catch (ParserException e) {
		}
	}

	public void testCollectionConstructionSyntax() {
		// lists
		parser.parseExpression("name in {null, \"Untitled\"}", null);
		parser.parseExpression("${name in {null, \"Untitled\"}}", null);

		// native arrays
		parser.parseExpression("new int[] {1, 2, 3}", null);
		parser.parseExpression("${new int[] {1, 2, 3}}", null);

		// maps
		parser.parseExpression("#{ 'foo' : 'foo value', 'bar' : 'bar value' }", null);
		parser.parseExpression("${#{ 'foo' : 'foo value', 'bar' : 'bar value' }}", null);
		parser.parseExpression("#@java.util.LinkedHashMap@{ 'foo' : 'foo value', 'bar' : 'bar value' }", null);
		parser.parseExpression("${#@java.util.LinkedHashMap@{ 'foo' : 'foo value', 'bar' : 'bar value' }}", null);

		// complex examples
		parser.parseExpression("b,#{1:2}", null);
		parser.parseExpression("${b,#{1:2}}", null);
		parser.parseExpression("a${b,#{1:2},e}f${g,#{3:4},j}k", null);
	}

	public void testVariables() {
		Expression exp = parser.parseExpression("${#var}", new ParserContextImpl().variable(new ExpressionVariable(
				"var", "${flag}")));
		assertEquals(false, ((Boolean) exp.getValue(bean)).booleanValue());
	}
}