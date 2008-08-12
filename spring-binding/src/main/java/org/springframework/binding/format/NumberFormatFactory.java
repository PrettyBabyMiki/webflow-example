package org.springframework.binding.format;

import java.text.NumberFormat;

/**
 * A factory for {@link NumberFormat} objects. Conceals the complexity associated with configuring, constructing, and/or
 * caching number format instances.
 * 
 * @author Keith Donald
 */
public interface NumberFormatFactory {

	/**
	 * Factory method that returns a fully-configured {@link NumberFormat} instance to use to format an object for
	 * display.
	 * @return the number format
	 */
	public NumberFormat getNumberFormat();

}