/*
 * Copyright 2004-2010 the original author or authors.
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
package org.springframework.faces.security;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

import org.springframework.security.core.context.SecurityContextHolder;

/**
 * A standard Facelets {@link TagHandler} for performing Spring Security authorization decisions. The tag supports the
 * following combinations attributes for authorization:
 * <ul>
 * <li>access</li>
 * <li>url, method</li>
 * <li>ifAllGranted, ifAnyGranted, ifNotGranted</li>
 * </ul>
 * The var attribute can be used to store the result of the authorization decision for later use in the view.
 * 
 * @author Rossen Stoyanchev
 * @since 2.2.0
 * @see FaceletsAuthorizeTag
 */
public class FaceletsAuthorizeTagHandler extends TagHandler {

	private final TagAttribute access;
	private final TagAttribute url;
	private final TagAttribute method;
	private final TagAttribute ifAllGranted;
	private final TagAttribute ifAnyGranted;
	private final TagAttribute ifNotGranted;
	private final TagAttribute var;

	/**
	 * @see TagHandler#TagHandler(TagConfig)
	 */
	public FaceletsAuthorizeTagHandler(TagConfig config) {
		super(config);
		this.access = this.getAttribute("access");
		this.url = this.getAttribute("url");
		this.method = this.getAttribute("method");
		this.ifAllGranted = this.getAttribute("ifAllGranted");
		this.ifAnyGranted = this.getAttribute("ifAnyGranted");
		this.ifNotGranted = this.getAttribute("ifNotGranted");
		this.var = this.getAttribute("var");
	}

	/**
	 * @see TagHandler#apply(FaceletContext, UIComponent)
	 */
	public void apply(FaceletContext faceletContext, UIComponent parent) throws IOException {
		if (SecurityContextHolder.getContext().getAuthentication() == null) {
			return;
		}

		FaceletsAuthorizeTag authorizeTag = new FaceletsAuthorizeTag(faceletContext, access, url, method, ifAllGranted,
				ifAnyGranted, ifNotGranted);

		boolean isAuthorized = authorizeTag.authorize();

		if (isAuthorized) {
			this.nextHandler.apply(faceletContext, parent);
		}

		if (this.var != null) {
			faceletContext.setAttribute(var.getValue(faceletContext), Boolean.valueOf(isAuthorized));
		}
	}

}
