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

package butter.droid.tv.ui.loading;

import android.app.Activity;
import butter.droid.base.ui.ActivityScope;
import butter.droid.base.ui.FragmentScope;
import butter.droid.tv.ui.loading.TVStreamLoadingModule.TVStreamLoadingBindModule;
import butter.droid.tv.ui.loading.fragment.TVStreamLoadingFragment;
import butter.droid.tv.ui.loading.fragment.TVStreamLoadingFragmentModule;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

@Module(includes = TVStreamLoadingBindModule.class)
public class TVStreamLoadingModule {

    @Provides @ActivityScope public TVStreamLoadingPresenter providePresenter(TVStreamLoadingView view) {
        return new TVStreamLoadingPresenterImpl(view);
    }

    @Module
    public interface TVStreamLoadingBindModule {
        @Binds TVStreamLoadingView bindView(TVStreamLoadingActivity activity);

        @Binds Activity bindActivity(TVStreamLoadingActivity activity);

        @FragmentScope
        @ContributesAndroidInjector(modules = TVStreamLoadingFragmentModule.class)
        abstract TVStreamLoadingFragment contributeTVStreamLoadingFragmentInjector();
    }
}
