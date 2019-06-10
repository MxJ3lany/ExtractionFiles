package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.widget.RecyclerView;

public class ItemKeyProviderMessage extends ItemKeyProvider<Long> {
    private RecyclerView recyclerView;

    ItemKeyProviderMessage(RecyclerView recyclerView) {
        super(ItemKeyProvider.SCOPE_CACHED);
        this.recyclerView = recyclerView;
    }

    @Nullable
    @Override
    public Long getKey(int pos) {
        AdapterMessage adapter = (AdapterMessage) recyclerView.getAdapter();
        return adapter.getKeyAtPosition(pos);
    }

    @Override
    public int getPosition(@NonNull Long key) {
        AdapterMessage adapter = (AdapterMessage) recyclerView.getAdapter();
        return adapter.getPositionForKey(key);
    }
}
