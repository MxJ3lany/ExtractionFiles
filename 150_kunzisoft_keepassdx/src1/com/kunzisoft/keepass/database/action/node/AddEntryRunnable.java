/*
 * Copyright 2017 Brian Pellin, Jeremy Jamet / Kunzisoft.
 *     
 * This file is part of KeePass DX.
 *
 *  KeePass DX is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  KeePass DX is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with KeePass DX.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.kunzisoft.keepass.database.action.node;

import android.content.Context;

import com.kunzisoft.keepass.database.Database;
import com.kunzisoft.keepass.database.PwEntry;

public class AddEntryRunnable extends ActionNodeDatabaseRunnable {

	private PwEntry mNewEntry;

	public AddEntryRunnable(Context ctx, Database db, PwEntry entryToAdd, AfterActionNodeOnFinish finish) {
		this(ctx, db, entryToAdd, finish, false);
	}

	public AddEntryRunnable(Context ctx, Database db, PwEntry entryToAdd, AfterActionNodeOnFinish finish, boolean dontSave) {
		super(ctx, db, finish, dontSave);

		this.mNewEntry = entryToAdd;
	}
	
	@Override
	public void run() {
		mDatabase.addEntryTo(mNewEntry, mNewEntry.getParent());
		
		// Commit to disk
		super.run();
	}

	@Override
	protected void onFinish(boolean success, String message) {
		if ( !success ) {
			mDatabase.removeEntryFrom(mNewEntry, mNewEntry.getParent());
		}
        callbackNodeAction(success, message, null, mNewEntry);
	}
}
