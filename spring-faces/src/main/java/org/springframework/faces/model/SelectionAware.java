/*
 * Copyright 2004-2023 the original author or authors.
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
package org.springframework.faces.model;

import java.util.List;

import jakarta.faces.model.DataModel;

/**
 * Interface for {@link DataModel} implementations that need to track selected rows.
 * 
 * @author Jeremy Grelle
 */
public interface SelectionAware<T> {

	/**
	 * Checks whether the row pointed to by the model's current index is selected.
	 * @return true if the current row data object is selected
	 */
	boolean isCurrentRowSelected();

	/**
	 * Sets whether the row pointed to by the model's current index is selected
	 * @param rowSelected true to select the current row
	 */
	void setCurrentRowSelected(boolean rowSelected);

	/**
	 * Sets the list of selected row data objects for the model.
	 * @param selections the list of selected row data objects
	 */
	void setSelections(List<T> selections);

	/**
	 * Returns the list of selected row data objects for the model.
	 * @return the list of selected row data objects
	 */
	List<T> getSelections();

	/**
	 * Selects all row data objects in the model.
	 */
	void selectAll();

	/**
	 * Selects the given row data object in the model.
	 * @param rowData the row data object to select.
	 */
	void select(T rowData);
}
