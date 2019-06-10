/***
  Copyright (c) 2017 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.
  
  Covered in detail in the book _Android's Architecture Components_
    https://commonsware.com/AndroidArch
 */

package com.commonsware.android.todo.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.commonsware.android.todo.R;
import com.commonsware.android.todo.impl.Action;
import com.commonsware.android.todo.impl.RosterViewModel;
import com.commonsware.android.todo.impl.ToDoModel;
import com.commonsware.android.todo.impl.ViewState;

abstract class AbstractRosterFragment extends Fragment {
  abstract void render(ViewState state);

  private RecyclerView rv;
  private RosterViewModel viewModel;
  private TextView empty;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    viewModel=ViewModelProviders.of(getActivity()).get(RosterViewModel.class);
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return(inflater.inflate(R.layout.todo_roster, container, false));
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    rv=view.findViewById(R.id.items);
    empty=view.findViewById(R.id.empty);
    empty.setVisibility(View.INVISIBLE);
  }

  protected TextView getEmptyView() {
    return(empty);
  }

  protected void startObserving() {
    viewModel.stateStream().observe(this, this::render);
  }

  protected ViewState currentState() {
    return(viewModel.currentState());
  }

  protected RecyclerView getRecyclerView() {
    return(rv);
  }

  protected void process(Action action) {
    viewModel.process(action);
  }

  abstract protected static class ToDoModelViewHolder
    extends RecyclerView.ViewHolder {
    abstract public void bind(ToDoModel model);

    ToDoModelViewHolder(View itemView) {
      super(itemView);
    }
  }

  abstract protected static class BaseRosterAdapter<T extends ToDoModelViewHolder>
    extends RecyclerView.Adapter<T> {
    private ViewState state;

    @Override
    public void onBindViewHolder(ToDoModelViewHolder holder, int position) {
      holder.bind(state.filteredItems().get(position));
    }

    @Override
    public int getItemCount() {
      return(state==null ? 0 : state.filteredItems().size());
    }

    void setState(ViewState state) {
      ViewState previous=this.state;
      this.state=state;

      DiffUtil
        .calculateDiff(new ViewState.Differ(previous, state), true)
        .dispatchUpdatesTo(this);
    }

    ViewState getState() {
      return(state);
    }

    ToDoModel getItem(int position) {
      return(state.filteredItems().get(position));
    }

    int getPosition(String id) {
      return(state.getFilteredPosition(id));
    }
  }
}
