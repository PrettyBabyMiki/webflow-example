/*
 * Copyright 2004-2012 the original author or authors.
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
package org.springframework.webflow.engine.model;

import java.util.LinkedList;

import junit.framework.TestCase;

/**
 * Unit tests for {@link AbstractModel}.
 */
public class AbstractModelTests extends TestCase {

	public void testStringMerge() {
		AbstractModel obj = new PersistenceContextModel();
		String child = "child";
		String parent = "parent";
		assertEquals("child", obj.merge(child, parent));
	}

	public void testStringMergeNullParent() {
		AbstractModel obj = new PersistenceContextModel();
		String child = "child";
		String parent = null;
		assertEquals("child", obj.merge(child, parent));
	}

	public void testStringMergeNullChild() {
		AbstractModel obj = new PersistenceContextModel();
		String child = null;
		String parent = "parent";
		assertEquals("parent", obj.merge(child, parent));
	}

	public void testStringMergeNulls() {
		AbstractModel obj = new PersistenceContextModel();
		String child = null;
		String parent = null;
		assertEquals(null, obj.merge(child, parent));
	}

	public void testMergeModel() {
		AttributeModel parent = new AttributeModel("foo", "bar");
		AttributeModel child = new AttributeModel("foo", null);
		FlowModel model = new FlowModel();
		model.merge(child, parent);
		assertEquals("bar", child.getValue());
	}

	public void testMergeParentCreateCopy() {
		AttributeModel parent = new AttributeModel("foo", "bar");
		AttributeModel child = null;
		FlowModel model = new FlowModel();
		child = (AttributeModel) model.merge(child, parent);
		assertEquals("bar", child.getValue());
		assertNotSame(parent, child);
	}

	public void testListMergeAddAtEndFalse() {
		LinkedList<SecuredModel> child = new LinkedList<>();
		child.add(new SecuredModel("1"));
		child.add(new SecuredModel("3"));
		LinkedList<SecuredModel> parent = new LinkedList<>();
		parent.add(new SecuredModel("2"));
		SecuredModel match = new SecuredModel("3");
		match.setMatch("foo");
		parent.add(match);
		AbstractModel obj = new PersistenceContextModel();
		LinkedList<SecuredModel> result = obj.merge(child, parent, false);
		assertEquals(3, result.size());
		assertEquals("2", result.get(0).getAttributes());
		assertEquals("1", result.get(1).getAttributes());
		assertEquals("3", result.get(2).getAttributes());
		assertNotSame(parent.get(0), result.get(1));
		assertEquals("foo", result.get(2).getMatch());
	}

	public void testListMergeAddAtEndTrue() {
		LinkedList<SecuredModel> child = new LinkedList<>();
		child.add(new SecuredModel("1"));
		child.add(new SecuredModel("3"));
		LinkedList<SecuredModel> parent = new LinkedList<>();
		parent.add(new SecuredModel("2"));
		SecuredModel match = new SecuredModel("3");
		match.setMatch("foo");
		parent.add(match);
		AbstractModel obj = new PersistenceContextModel();
		LinkedList<SecuredModel> result = obj.merge(child, parent, true);
		assertEquals(3, result.size());
		assertEquals("1", result.get(0).getAttributes());
		assertEquals("3", result.get(1).getAttributes());
		assertEquals("2", result.get(2).getAttributes());
		assertNotSame(parent.get(0), result.get(1));
		assertEquals("foo", result.get(1).getMatch());
	}

	public void testListMergeNullParent() {
		AbstractModel obj = new PersistenceContextModel();
		LinkedList<SecuredModel> child = new LinkedList<>();
		child.add(new SecuredModel("1"));
		LinkedList<SecuredModel> parent = null;
		LinkedList<SecuredModel> result = obj.merge(child, parent);
		assertEquals(1, result.size());
		assertEquals("1", result.get(0).getAttributes());
	}

	public void testListMergeNullChild() {
		LinkedList<SecuredModel> child = null;
		LinkedList<SecuredModel> parent = new LinkedList<>();
		parent.add(new SecuredModel("2"));
		AbstractModel obj = new PersistenceContextModel();
		LinkedList<SecuredModel> result = obj.merge(child, parent);
		assertEquals(1, result.size());
		assertEquals("2", result.get(0).getAttributes());
		assertNotSame(parent.get(0), result.get(0));
	}

	public void testListMergeNulls() {
		AbstractModel obj = new PersistenceContextModel();
		LinkedList<Model> child = null;
		LinkedList<Model> parent = null;
		LinkedList<Model> result = obj.merge(child, parent);
		assertEquals(null, result);
	}

	public void testCopyModel() {
		AttributeModel model = new AttributeModel("foo", "bar");
		FlowModel m = new FlowModel();
		AttributeModel copy = (AttributeModel) m.copy(model);
		assertEquals("foo", copy.getName());
		assertEquals("bar", copy.getValue());
	}

	public void testCopyModelNull() {
		FlowModel m = new FlowModel();
		AttributeModel copy = (AttributeModel) m.copy(null);
		assertNull(copy);
	}
}
