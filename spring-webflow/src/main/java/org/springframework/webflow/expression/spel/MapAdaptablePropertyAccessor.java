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
package org.springframework.webflow.expression.spel;

import org.springframework.binding.collection.MapAdaptable;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.webflow.core.collection.MutableAttributeMap;

/**
 * Spring EL PropertyAccessor for reading from {@link MapAdaptable} and writing to {@link MutableAttributeMap}.
 * 
 * @author Rossen Stoyanchev
 * @since 2.1
 */
public class MapAdaptablePropertyAccessor implements PropertyAccessor {

	public Class<?>[] getSpecificTargetClasses() {
		return new Class[] { MapAdaptable.class };
	}

	public boolean canRead(EvaluationContext context, Object target, String name) {
		return true;
	}

	public TypedValue read(EvaluationContext context, Object target, String name) {
		MapAdaptable<?, ?> map = (MapAdaptable<?, ?>) target;
		return new TypedValue(map.asMap().get(name));
	}

	public boolean canWrite(EvaluationContext context, Object target, String name) {
		return (target instanceof MutableAttributeMap);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void write(EvaluationContext context, Object target, String name, Object newValue) {
		MutableAttributeMap map = (MutableAttributeMap) target;
		map.put(name, newValue);
	}

}
