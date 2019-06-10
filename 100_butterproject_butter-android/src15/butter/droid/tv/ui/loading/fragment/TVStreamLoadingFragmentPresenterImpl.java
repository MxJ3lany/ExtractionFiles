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

package butter.droid.tv.ui.loading.fragment;

import android.content.Context;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.subtitle.SubtitleManager;
import butter.droid.base.manager.internal.vlc.PlayerManager;
import butter.droid.base.providers.media.model.StreamInfo;
import butter.droid.base.ui.loading.fragment.BaseStreamLoadingFragmentPresenterImpl;

public class TVStreamLoadingFragmentPresenterImpl extends BaseStreamLoadingFragmentPresenterImpl
        implements TVStreamLoadingFragmentPresenter {

    private final TVStreamLoadingFragmentView view;

    public TVStreamLoadingFragmentPresenterImpl(TVStreamLoadingFragmentView view, ProviderManager providerManager,
            SubtitleManager subtitleManager, PlayerManager playerManager, Context context) {
        super(view, providerManager, subtitleManager, playerManager, context);
        this.view = view;
    }

    @Override public void onCreate(StreamInfo streamInfo) {
        super.onCreate(streamInfo);

        view.updateBackground(streamInfo.getBackdropImage());
    }

    @Override protected void startPlayerActivity(int resumePosition) {
        view.startPlayerActivity(streamInfo, resumePosition);
    }

}
