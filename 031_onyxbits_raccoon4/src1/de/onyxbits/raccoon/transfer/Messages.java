/*
 * Copyright 2015 Patrick Ahlbrecht
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.onyxbits.raccoon.transfer;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import de.onyxbits.weave.swing.ActionLocalizer;

public final class Messages {
	
	private static final Locale locale = Locale.getDefault();
	public static final String BUNDLE_NAME = Messages.class.getName()
			.toLowerCase(locale);

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME, locale);
	
	private static ActionLocalizer actionLocalizer;

	private Messages() {
	}

	public static String getString(String key) {
		try {
			return (String) RESOURCE_BUNDLE.getObject(key);
		}
		catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
	public static ActionLocalizer getLocalizer() {
		if (actionLocalizer == null) {
			actionLocalizer = new ActionLocalizer(RESOURCE_BUNDLE, "action");
		}
		return actionLocalizer;
	}
	
}
