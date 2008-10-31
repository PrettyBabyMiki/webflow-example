package org.springframework.binding.mapping;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.el.DefaultExpressionFactoryUtils;
import org.springframework.binding.expression.el.ELExpressionParser;
import org.springframework.binding.mapping.impl.DefaultMapper;
import org.springframework.binding.mapping.impl.DefaultMapping;
import org.springframework.binding.mapping.results.TypeConversionError;

public class DefaultMapperTests extends TestCase {
	private DefaultMapper mapper = new DefaultMapper();
	private ExpressionParser parser = new ELExpressionParser(DefaultExpressionFactoryUtils.createExpressionFactory());

	public void testMapping() {
		DefaultMapping mapping1 = new DefaultMapping(parser.parseExpression("foo", null), parser.parseExpression("bar",
				null));
		DefaultMapping mapping2 = new DefaultMapping(parser.parseExpression("foo", null), parser.parseExpression("baz",
				null));
		mapper.addMapping(mapping1);
		mapper.addMapping(mapping2);
		assertEquals(2, mapper.getMappings().length);
		TestBean bean1 = new TestBean();
		bean1.foo = "a";
		TestBean2 bean2 = new TestBean2();
		MappingResults results = mapper.map(bean1, bean2);
		assertSame(bean1, results.getSource());
		assertSame(bean2, results.getTarget());
		assertEquals(2, results.getAllResults().size());
		assertEquals(0, results.getErrorResults().size());
		assertEquals("a", bean2.bar);
		assertEquals("a", bean2.baz);
		assertEquals(1, results.getResults(new MappingResultsCriteria() {
			public boolean test(MappingResult result) {
				if (result.getMapping().getTargetExpression().getExpressionString().equals("baz")) {
					return true;
				} else {
					return false;
				}
			}
		}).size());
	}

	public void testMappingConversion() {
		DefaultMapping mapping1 = new DefaultMapping(parser.parseExpression("beep", null), parser.parseExpression(
				"beep", null));
		mapper.addMapping(mapping1);
		Map bean1 = new HashMap();
		bean1.put("beep", "en");
		TestBean2 bean2 = new TestBean2();
		MappingResults results = mapper.map(bean1, bean2);
		assertFalse(results.hasErrorResults());
		assertEquals(Locale.ENGLISH, bean2.beep);
	}

	public void testMappingConversionError() {
		DefaultMapping mapping1 = new DefaultMapping(parser.parseExpression("boop", null), parser.parseExpression(
				"boop", null));
		mapper.addMapping(mapping1);
		Map bean1 = new HashMap();
		bean1.put("boop", "bogus");
		TestBean2 bean2 = new TestBean2();
		MappingResults results = mapper.map(bean1, bean2);
		assertEquals(TypeConversionError.class, ((MappingResult) results.getErrorResults().get(0)).getResult()
				.getClass());
	}

	public static class TestBean {
		private String foo;

		public String getFoo() {
			return foo;
		}

		public void setFoo(String foo) {
			this.foo = foo;
		}

	}

	public static class TestBean2 {
		private String bar;
		private String baz;
		private Integer boop;
		private Locale beep;

		public String getBar() {
			return bar;
		}

		public String getBaz() {
			return baz;
		}

		public void setBaz(String baz) {
			this.baz = baz;
		}

		public void setBar(String bar) {
			this.bar = bar;
		}

		public Integer getBoop() {
			return boop;
		}

		public void setBoop(Integer boop) {
			this.boop = boop;
		}

		public Locale getBeep() {
			return beep;
		}

		public void setBeep(Locale beep) {
			this.beep = beep;
		}

	}
}