/*
 * Copyright 2004-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow.engine.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for {@link InputModel}.
 */
public class InputModelTests {

	@Test
	public void testMergeable() {
		InputModel child = new InputModel("child", "childvalue");
		assertTrue(child.isMergeableWith(child));
	}

	@Test
	public void testNotMergeable() {
		InputModel child = new InputModel("child", "childvalue");
		InputModel parent = new InputModel("parent", "parentvalue");
		assertFalse(child.isMergeableWith(parent));
	}

	@Test
	public void testNotMergeableWithNull() {
		InputModel child = new InputModel("child", "childvalue");
		assertFalse(child.isMergeableWith(null));
	}

	@Test
	public void testMerge() {
		InputModel child = new InputModel("child", "childvalue");
		InputModel parent = new InputModel("child", "parentvalue");
		parent.setType("parenttype");
		child.merge(parent);
		assertEquals("parenttype", child.getType());
	}

}
