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
package de.onyxbits.raccoon.appmgr;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import de.onyxbits.raccoon.db.DatabaseManager;
import de.onyxbits.raccoon.repo.AndroidApp;
import de.onyxbits.raccoon.repo.AndroidAppDao;
import de.onyxbits.raccoon.repo.Layout;
import de.onyxbits.weave.Globals;
import de.onyxbits.weave.LifecycleManager;

/**
 * The action for the "Delete selected" button
 * 
 * @author patrick
 * 
 */
class DeleteAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Globals globals;
	private List<AndroidApp> apps;

	public DeleteAction(Globals globals) {
		this.globals = globals;
		setEnabled(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Window parent = globals.get(LifecycleManager.class).getWindow(
				MyAppsViewBuilder.ID);
		Layout layout = globals.get(Layout.class);
		int choice = JOptionPane.showConfirmDialog(parent,
				Messages.getString("DeleteAction.message"),
				Messages.getString("DeleteAction.title"), JOptionPane.YES_NO_OPTION);
		if (choice == JOptionPane.YES_OPTION) {
			try {
				globals.get(DatabaseManager.class).get(AndroidAppDao.class)
						.delete(layout, apps.toArray(new AndroidApp[apps.size()]));

			}
			catch (Exception exp) {
				exp.printStackTrace();
			}
		}
	}

	public void setApps(List<AndroidApp> apps) {
		this.apps = apps;
		setEnabled(apps.size() > 0);
	}
}
