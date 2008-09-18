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
package org.springframework.binding.convert.service;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import junit.framework.TestCase;

import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.ConversionExecutionException;
import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.convert.ConversionExecutorNotFoundException;
import org.springframework.binding.convert.converters.Converter;
import org.springframework.binding.convert.converters.FormattedStringToNumber;
import org.springframework.binding.convert.converters.StringToBoolean;
import org.springframework.binding.convert.converters.TwoWayConverter;
import org.springframework.binding.format.DefaultNumberFormatFactory;

/**
 * Test case for the default conversion service.
 * 
 * @author Keith Donald
 */
public class DefaultConversionServiceTests extends TestCase {

	public void testConvertCompatibleTypes() {
		DefaultConversionService service = new DefaultConversionService();
		List lst = new ArrayList();
		assertSame(lst, service.getConversionExecutor(ArrayList.class, List.class).execute(lst));
	}

	public void testOverrideConverter() {
		Converter customConverter = new StringToBoolean("ja", "nee");
		DefaultConversionService service = new DefaultConversionService();
		StaticConversionExecutor executor = (StaticConversionExecutor) service.getConversionExecutor(String.class,
				Boolean.class);
		assertNotSame(customConverter, executor.getConverter());
		try {
			executor.execute("ja");
			fail();
		} catch (ConversionExecutionException e) {
			// expected
		}
		service.addConverter(customConverter);
		executor = (StaticConversionExecutor) service.getConversionExecutor(String.class, Boolean.class);
		assertSame(customConverter, executor.getConverter());
		assertTrue(((Boolean) executor.execute("ja")).booleanValue());
	}

	public void testTargetClassNotSupported() {
		DefaultConversionService service = new DefaultConversionService();
		try {
			service.getConversionExecutor(String.class, HashMap.class);
			fail("Should have thrown an exception");
		} catch (ConversionExecutorNotFoundException e) {
		}
	}

	public void testValidConversion() {
		DefaultConversionService service = new DefaultConversionService();
		ConversionExecutor executor = service.getConversionExecutor(String.class, Integer.class);
		Integer three = (Integer) executor.execute("3");
		assertEquals(3, three.intValue());

		ConversionExecutor executor2 = service.getConversionExecutor(Integer.class, String.class);
		String threeString = (String) executor2.execute(new Integer(3));
		assertEquals("3", threeString);
	}

	public void testRegisterConverter() {
		GenericConversionService service = new GenericConversionService();
		FormattedStringToNumber converter = new FormattedStringToNumber();
		DefaultNumberFormatFactory numberFormatFactory = new DefaultNumberFormatFactory();
		numberFormatFactory.setLocale(Locale.US);
		converter.setNumberFormatFactory(numberFormatFactory);
		service.addConverter(converter);
		ConversionExecutor executor = service.getConversionExecutor(String.class, Integer.class);
		Integer three = (Integer) executor.execute("3,000");
		assertEquals(3000, three.intValue());
		ConversionExecutor executor2 = service.getConversionExecutor(Integer.class, String.class);
		String string = (String) executor2.execute(new Integer(3000));
		assertEquals("3,000", string);
	}

	public void testRegisterCustomConverter() {
		DefaultConversionService service = new DefaultConversionService();
		FormattedStringToNumber converter = new FormattedStringToNumber();
		DefaultNumberFormatFactory numberFormatFactory = new DefaultNumberFormatFactory();
		numberFormatFactory.setLocale(Locale.US);
		converter.setNumberFormatFactory(numberFormatFactory);
		service.addConverter("usaNumber", converter);
		ConversionExecutor executor = service.getConversionExecutor("usaNumber", String.class, Integer.class);
		Integer three = (Integer) executor.execute("3,000");
		assertEquals(3000, three.intValue());
		ConversionExecutor executor2 = service.getConversionExecutor("usaNumber", Integer.class, String.class);
		String string = (String) executor2.execute(new Integer(3000));
		assertEquals("3,000", string);
	}

	public void testRegisterCustomConverterForSameType() {
		DefaultConversionService service = new DefaultConversionService();
		service.addConverter("trimmer", new Trimmer());
		ConversionExecutor executor = service.getConversionExecutor("trimmer", String.class, String.class);
		assertEquals("a string", executor.execute("a string   "));
	}

	public void testRegisterCustomConverterForSameTypeNotCompatibleSource() {
		DefaultConversionService service = new DefaultConversionService();
		service.addConverter("trimmer", new Trimmer());
		try {
			service.getConversionExecutor("trimmer", Object.class, String.class);
		} catch (ConversionException e) {

		}
	}

	public void testRegisterCustomConverterForSameTypeNotCompatibleTarget() {
		DefaultConversionService service = new DefaultConversionService();
		service.addConverter("trimmer", new Trimmer());
		try {
			service.getConversionExecutor("trimmer", String.class, Object.class);
		} catch (ConversionException e) {

		}
	}

	public void testRegisterCustomConverterReverseComparsion() {
		DefaultConversionService service = new DefaultConversionService();
		service.addConverter("princy", new CustomTwoWayConverter());
		ConversionExecutor executor = service.getConversionExecutor("princy", Principal.class, String.class);
		assertEquals("name", executor.execute(new Principal() {
			public String getName() {
				return "name";
			}
		}));
	}

	public void testRegisterCustomConverterReverseNotCompatibleSource() {
		DefaultConversionService service = new DefaultConversionService();
		service.addConverter("princy", new CustomTwoWayConverter());
		try {
			service.getConversionExecutor("trimmer", Principal.class, Object.class);
		} catch (ConversionException e) {

		}
	}

	public void testConversionPrimitive() {
		DefaultConversionService service = new DefaultConversionService();
		ConversionExecutor executor = service.getConversionExecutor(String.class, int.class);
		Integer three = (Integer) executor.execute("3");
		assertEquals(3, three.intValue());
	}

	public void testArrayConversion() {
		DefaultConversionService service = new DefaultConversionService();
		ConversionExecutor executor = service.getConversionExecutor(String[].class, Integer[].class);
		Integer[] result = (Integer[]) executor.execute(new String[] { "1", "2", "3" });
		assertEquals(new Integer(1), result[0]);
		assertEquals(new Integer(2), result[1]);
		assertEquals(new Integer(3), result[2]);
	}

	public void testPrimitiveArrayConversion() {
		DefaultConversionService service = new DefaultConversionService();
		ConversionExecutor executor = service.getConversionExecutor(String[].class, int[].class);
		int[] result = (int[]) executor.execute(new String[] { "1", "2", "3" });
		assertEquals(1, result[0]);
		assertEquals(2, result[1]);
		assertEquals(3, result[2]);
	}

	public void testArrayListConversion() {
		DefaultConversionService service = new DefaultConversionService();
		ConversionExecutor executor = service.getConversionExecutor(String[].class, List.class);
		List result = (List) executor.execute(new String[] { "1", "2", "3" });
		assertEquals("1", result.get(0));
		assertEquals("2", result.get(1));
		assertEquals("3", result.get(2));
	}

	public void testListArrayConversion() {
		DefaultConversionService service = new DefaultConversionService();
		ConversionExecutor executor = service.getConversionExecutor(Collection.class, String[].class);
		List list = new ArrayList();
		list.add("1");
		list.add("2");
		list.add("3");
		String[] result = (String[]) executor.execute(list);
		assertEquals("1", result[0]);
		assertEquals("2", result[1]);
		assertEquals("3", result[2]);
	}

	public void testListArrayConversionWithComponentConversion() {
		DefaultConversionService service = new DefaultConversionService();
		ConversionExecutor executor = service.getConversionExecutor(Collection.class, Integer[].class);
		List list = new ArrayList();
		list.add("1");
		list.add("2");
		list.add("3");
		Integer[] result = (Integer[]) executor.execute(list);
		assertEquals(new Integer(1), result[0]);
		assertEquals(new Integer(2), result[1]);
		assertEquals(new Integer(3), result[2]);
	}

	public void testArrayLinkedListConversion() {
		DefaultConversionService service = new DefaultConversionService();
		ConversionExecutor executor = service.getConversionExecutor(String[].class, LinkedList.class);
		LinkedList result = (LinkedList) executor.execute(new String[] { "1", "2", "3" });
		assertEquals("1", result.get(0));
		assertEquals("2", result.get(1));
		assertEquals("3", result.get(2));
	}

	public void testArrayAbstractListConversion() {
		DefaultConversionService service = new DefaultConversionService();
		try {
			service.getConversionExecutor(String[].class, AbstractList.class);
		} catch (IllegalArgumentException e) {

		}
	}

	public void testToArrayConversion() {
		DefaultConversionService service = new DefaultConversionService();
		ConversionExecutor executor = service.getConversionExecutor(String.class, String[].class);
		String[] result = (String[]) executor.execute("1,2,3");
		assertEquals(1, result.length);
		assertEquals("1,2,3", result[0]);
	}

	public void testToListConversion() {
		DefaultConversionService service = new DefaultConversionService();
		ConversionExecutor executor = service.getConversionExecutor(String.class, List.class);
		List result = (List) executor.execute("1,2,3");
		assertEquals(1, result.size());
		assertEquals("1,2,3", result.get(0));
	}

	public void testToArrayConversionWithElementConversion() {
		DefaultConversionService service = new DefaultConversionService();
		ConversionExecutor executor = service.getConversionExecutor(String.class, Integer[].class);
		Integer[] result = (Integer[]) executor.execute("123");
		assertEquals(1, result.length);
		assertEquals(new Integer(123), result[0]);
	}

	public void testGetConversionExecutorsForSource() {
		DefaultConversionService service1 = new DefaultConversionService();
		service1.addConverter(new CustomConverter());
		GenericConversionService service2 = new GenericConversionService();
		FormattedStringToNumber formatterConverter = new FormattedStringToNumber(BigDecimal.class);
		service2.addConverter(formatterConverter);
		service2.setParent(service1);
		Set converters = service2.getConversionExecutors(String.class);
		Iterator it = converters.iterator();
		while (it.hasNext()) {
			ConversionExecutor executor = (ConversionExecutor) it.next();
			if (executor.getTargetClass().equals(BigDecimal.class)) {
				StaticConversionExecutor se = (StaticConversionExecutor) executor;
				assertSame(formatterConverter, se.getConverter());
			}
		}
		assertEquals(15, converters.size());
	}

	private static class CustomConverter implements Converter {

		public Object convertSourceToTargetClass(final Object source, Class targetClass) throws Exception {
			return new Principal() {

				public String getName() {
					return (String) source;
				}

			};
		}

		public Class getSourceClass() {
			return String.class;
		}

		public Class getTargetClass() {
			return Principal.class;
		}

	}

	private static class CustomTwoWayConverter extends CustomConverter implements TwoWayConverter {
		public Object convertTargetToSourceClass(Object target, Class sourceClass) throws Exception {
			return ((Principal) target).getName();
		}
	}

	private static class Trimmer implements Converter {

		public Object convertSourceToTargetClass(Object source, Class targetClass) throws Exception {
			return ((String) source).trim();
		}

		public Class getSourceClass() {
			return String.class;
		}

		public Class getTargetClass() {
			return String.class;
		}

	}

}