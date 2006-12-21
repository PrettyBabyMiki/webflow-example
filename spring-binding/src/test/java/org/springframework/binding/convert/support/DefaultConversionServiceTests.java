/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.binding.convert.support;

import java.util.HashMap;

import junit.framework.TestCase;

import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.convert.Converter;
import org.springframework.core.enums.ShortCodedLabeledEnum;

/**
 * Test case for the default conversion service.
 * 
 * @author Keith Donald
 */
public class DefaultConversionServiceTests extends TestCase {
	
	public void testOverrideConverter() {
		Converter customConverter = new TextToBoolean("ja", "nee");
		
		DefaultConversionService service = new DefaultConversionService();
		
		ConversionExecutor executor = service.getConversionExecutor(String.class, Boolean.class);
		assertNotSame(customConverter, executor.getConverter());
		try {
			executor.execute("ja");
			fail();
		}
		catch (ConversionException e) {
			// expected
		}
		
		service.addConverter(customConverter);
		
		executor = service.getConversionExecutor(String.class, Boolean.class);
		assertSame(customConverter, executor.getConverter());
		assertTrue(((Boolean)executor.execute("ja")).booleanValue());
	}

	public void testTargetClassNotSupported() {
		DefaultConversionService service = new DefaultConversionService();
		try {
			service.getConversionExecutor(String.class, HashMap.class);
			fail("Should have thrown an exception");
		}
		catch (ConversionException e) {
		}
	}

	public void testValidConversion() {
		DefaultConversionService service = new DefaultConversionService();
		ConversionExecutor executor = service.getConversionExecutor(String.class, Integer.class);
		Integer three = (Integer)executor.execute("3");
		assertEquals(3, three.intValue());
	}

	public void testLabeledEnumConversionNoSuchEnum() {
		DefaultConversionService service = new DefaultConversionService();
		ConversionExecutor executor = service.getConversionExecutor(String.class, MyEnum.class);
		try {
			executor.execute("My Invalid Label");
			fail("Should have failed");
		}
		catch (ConversionException e) {
		}
	}

	public void testValidLabeledEnumConversion() {
		DefaultConversionService service = new DefaultConversionService();
		ConversionExecutor executor = service.getConversionExecutor(String.class, MyEnum.class);
		MyEnum myEnum = (MyEnum)executor.execute("My Label 1");
		assertEquals(MyEnum.ONE, myEnum);
	}

	public static class MyEnum extends ShortCodedLabeledEnum {
		public static MyEnum ONE = new MyEnum(0, "My Label 1");

		public static MyEnum TWO = new MyEnum(1, "My Label 2");

		private MyEnum(int code, String label) {
			super(code, label);
		}
	}
}