package org.springframework.binding.expression.el;

import java.util.HashMap;
import java.util.Map;

import javax.el.ELContext;

import junit.framework.TestCase;

import org.springframework.binding.collection.MapAdaptable;

public class MapAdaptableELResolverTests extends TestCase {

	private ELContext context;

	public void setUp() {
		context = new DefaultELContext(new MapAdaptableELResolver(), null, null);
	}

	public void testGetType() {
		Class type = context.getELResolver().getType(context, new TestMapAdaptable(), "bar");
		assertTrue(context.isPropertyResolved());
		assertEquals(String.class, type);
	}

	public void testGetType_UnknownProperty() {
		Class type = context.getELResolver().getType(context, new TestMapAdaptable(), "foo");
		assertTrue(context.isPropertyResolved());
		assertEquals(null, type);
	}

	public void testGetValue() {
		Object value = context.getELResolver().getValue(context, new TestMapAdaptable(), "bar");
		assertTrue(context.isPropertyResolved());
		assertEquals("bar", value);
	}

	public void testGetValue_UnknownProperty() {
		Object value = context.getELResolver().getValue(context, new TestMapAdaptable(), "foo");
		assertTrue(context.isPropertyResolved());
		assertEquals(null, value);
	}

	public void testSetValue() {
		MapAdaptable testMap = new TestMapAdaptable();
		context.getELResolver().setValue(context, testMap, "foo", "foo");
		assertTrue(context.isPropertyResolved());
		assertEquals("foo", testMap.asMap().get("foo"));
	}

	public void testSetValue_OverWrite() {
		MapAdaptable testMap = new TestMapAdaptable();
		context.getELResolver().setValue(context, testMap, "bar", "foo");
		assertTrue(context.isPropertyResolved());
		assertEquals("foo", testMap.asMap().get("bar"));
	}

	private class TestMapAdaptable implements MapAdaptable {
		private Map map = new HashMap();

		public TestMapAdaptable() {
			map.put("bar", "bar");
		}

		public Map asMap() {
			return map;
		}
	}
}
