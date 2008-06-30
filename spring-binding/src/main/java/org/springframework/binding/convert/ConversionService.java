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
package org.springframework.binding.convert;

/**
 * A service interface for retrieving type conversion executors. The returned command objects are thread-safe and may be
 * safely cached for use by client code.
 * 
 * Type converters convert from one type to another. They are more generic than formatters, which convert from string to
 * object and back.
 * 
 * @author Keith Donald
 */
public interface ConversionService {

	/**
	 * Return the default conversion executor capable of converting source objects of the specified
	 * <code>sourceClass</code> to instances of the <code>targetClass</code>.
	 * <p>
	 * The returned ConversionExecutor is thread-safe and may safely be cached for use in client code.
	 * @param sourceClass the source class to convert from
	 * @param targetClass the target class to convert to
	 * @return the executor that can execute instance type conversion, never null
	 * @throws ConversionExecutorNotFoundException when no suitable conversion executor could be found
	 */
	public ConversionExecutor getConversionExecutor(Class sourceClass, Class targetClass)
			throws ConversionExecutorNotFoundException;

	/**
	 * Return a custom conversion executor capable of converting source objects of the specified
	 * <code>sourceClass</code> to instances of the <code>targetClass</code>.
	 * <p>
	 * The returned ConversionExecutor is thread-safe and may safely be cached for use in client code.
	 * @param id the id of the custom conversion executor (should be unique among custom converters)
	 * @param sourceClass the source class to convert from
	 * @param targetClass the target class to convert to
	 * @return the executor that can execute instance type conversion, never null
	 * @throws ConversionExecutorNotFoundException when no suitable conversion executor could be found
	 */
	public ConversionExecutor getConversionExecutor(String id, Class sourceClass, Class targetClass)
			throws ConversionExecutorNotFoundException;

}