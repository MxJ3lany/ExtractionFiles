/*
 *
 *    Copyright (C) 2016 BoBoMEe(wbwjx115@gmail.com)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.bobomee.android.mvp.detail;

import android.support.annotation.NonNull;
import com.bobomee.android.mvp.data.Repo;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created on 16/6/11.上午11:12.
 *
 * @author bobomee
 */
public class DetailPresenter implements DetailContract.Presenter {
  private DetailContract.View detailView;
  private Repo repo;

  @Override public void start() {
  }

  public DetailPresenter(Repo repo, @NonNull DetailContract.View detailView) {
    this.detailView = checkNotNull(detailView);
    this.repo = checkNotNull(repo);
    this.detailView.setPresenter(this);
  }

  @Override public void loadDetail() {

    if (null != detailView) {

      detailView.showDetail(repo.name);
    }
  }

  @Override public void onDestroy() {
    detailView = null;
  }
}
