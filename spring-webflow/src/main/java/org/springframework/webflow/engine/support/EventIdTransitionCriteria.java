/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.engine.support;

import java.io.Serializable;

import org.springframework.util.Assert;
import org.springframework.webflow.engine.TransitionCriteria;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Simple transition criteria that matches on an eventId and nothing else.
 * Specifically, if the id of the last event that occured equals
 * {@link #getEventId()} this criteria will return true.
 * 
 * @see RequestContext#getLastEvent()
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class EventIdTransitionCriteria implements TransitionCriteria, Serializable {

	/**
	 * The event id to match.
	 */
	private String eventId;
	
	/**
	 * Whether or not to match case sensitively. Default is true.
	 */
	private boolean caseSensitive = true;

	/**
	 * Create a new event id matching criteria object.
	 * @param eventId the event id
	 */
	public EventIdTransitionCriteria(String eventId) {
		Assert.hasText(eventId, "The event id is required");
		this.eventId = eventId;
	}

	/**
	 * Returns the event id to match.
	 */
	public String getEventId() {
		return eventId;
	}
	
	/**
	 * Set whether or not the event id should be matched in a case sensitve
	 * manner. Defaults to true.
	 */
	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	public boolean test(RequestContext context) {
		Event lastEvent = context.getLastEvent();
		if (lastEvent == null) {
			return false;
		}
		if (caseSensitive) {
			return eventId.equals(lastEvent.getId());
		}
		else {
			return eventId.equalsIgnoreCase(lastEvent.getId());
		}
	}

	public String toString() {
		return "[eventId = '" + eventId + "']";
	}
}