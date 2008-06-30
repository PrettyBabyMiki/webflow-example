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
package org.springframework.binding.convert.converters;

/**
 * Converts a String to an Short using {@link Short#valueOf(String)}.
 * 
 * @author Keith Donald
 */
public class StringToFloat extends StringToObject {

	public StringToFloat() {
		super(Float.class);
	}

	public Object toObject(String string, Class objectClass) throws Exception {
		return Float.valueOf(string);
	}

	public String toString(Object object) throws Exception {
		Float number = (Float) object;
		return number.toString();
	}

}