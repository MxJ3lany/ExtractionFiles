/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.ui.search;

import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.network.NetworkManager;
import butter.droid.base.ui.FragmentScope;
import dagger.Module;
import dagger.Provides;

@Module(includes = SearchBindModule.class)
public class SearchModule {

    @Provides @FragmentScope SearchPresenter providePresenter(SearchView view, ProviderManager providerManager,
            PreferencesHandler preferencesHandler, NetworkManager networkManager) {
        return new SearchPresenterImpl(view, providerManager, preferencesHandler, networkManager);
    }

}
