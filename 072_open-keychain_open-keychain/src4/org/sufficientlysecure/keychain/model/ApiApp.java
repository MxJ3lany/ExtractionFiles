/*
 * Copyright (C) 2017 Schürmann & Breitmoser GbR
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.sufficientlysecure.keychain.model;


import com.google.auto.value.AutoValue;
import org.sufficientlysecure.keychain.ApiAppsModel;


@AutoValue
public abstract class ApiApp implements ApiAppsModel {
    public static final ApiAppsModel.Factory<ApiApp> FACTORY =
            new ApiAppsModel.Factory<ApiApp>(AutoValue_ApiApp::new);

    public static ApiApp create(String packageName, byte[] packageSignature) {
        return new AutoValue_ApiApp(null, packageName, packageSignature);
    }
}
