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
package de.onyxbits.raccoon;

import java.net.URI;
import java.util.Currency;
import java.util.Locale;

/**
 * A central registry for urls used in various places.
 * 
 * @author patrick
 * 
 */
public final class Bookmarks {

	public static final URI BASE = URI.create("https://raccoon.onyxbits.de/");
	public static final URI PREMIUM = URI.create("https://billing.onyxbits.de/");

	public static final URI HANDBOOK = BASE.resolve("/handbook");
	public static final URI RELEASES = BASE.resolve("/releases");
	public static final URI LATEST = BASE.resolve("latestversion");
	public static final URI SETUP = BASE
			.resolve("/content/first-steps-setup-wizard");

	public static final URI SHOUTBOXFEED = BASE.resolve("/feed/shoutbox");

	public static final URI USB_DEBUGGING = BASE
			.resolve("/content/enable-usb-debugging");

	public static final URI FEATURELIST;
	public static final URI ORDER;
	public static final URI SUPPORT;

	static {
		Currency currency = Currency.getInstance(Locale.getDefault());
		String cc = currency.getCurrencyCode().toLowerCase();
		FEATURELIST = PREMIUM.resolve("/benefits?offers=" + cc);
		ORDER = PREMIUM.resolve("/?offers=" + cc);
		SUPPORT = PREMIUM.resolve("/support/email?offers=" + cc);
	}
}
