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
package de.onyxbits.raccoon.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class VariableDao extends DataAccessObject implements Variables {

	/**
	 * Table version
	 */
	protected static final int VERSION = 1;

	@Override
	protected void upgradeFrom(int oldVersion, Connection c) throws SQLException {
		switch (oldVersion + 1) {
			case 1: {
				v1(c);
			}
		}
	}

	@Override
	protected int getVersion() {
		return 1;
	}

	private static void v1(Connection c) throws SQLException {
		PreparedStatement st = c
				.prepareStatement("CREATE TABLE variables (name VARCHAR(255), value VARCHAR(2048))");
		st.execute();
		st = c
				.prepareStatement("INSERT INTO variables (name, value) VALUES (?, ?)");
		st.setString(1, CREATED);
		st.setString(2, "" + System.currentTimeMillis());
		st.execute();
	}

	public String getVar(String key, String def) {
		Connection c = manager.connect();
		PreparedStatement st = null;
		ResultSet res = null;
		try {
			st = c.prepareStatement("SELECT value FROM variables WHERE name = ?");
			st.setString(1, key);
			res = st.executeQuery();
			res.next();
			return res.getString(1);
		}
		catch (Exception e) {

		}
		finally {
			try {
				if (st != null) {
					st.close();
				}
				if (res != null) {
					res.close();
				}
			}
			catch (Exception e) {
			}
		}
		return def;
	}

	public void setVar(String key, String val) {
		Connection c = manager.connect();
		PreparedStatement st = null;
		ResultSet res = null;
		String old = getVar(key, null);
		try {
			if (val == null) {
				st = c.prepareStatement("DELETE FROM variables WHERE name = ?");
				st.setString(1, key);
				st.execute();
				st.close();
				fireOnDataSetChangeEvent(new VariableEvent(this, VariableEvent.DELETE,
						key, old, val));
			}
			else {
				st = c
						.prepareStatement("MERGE INTO variables USING (VALUES (?, ?)) AS vals(x,y) ON variables.name = vals.x WHEN MATCHED THEN UPDATE SET variables.value=vals.y WHEN NOT MATCHED THEN INSERT VALUES vals.x , vals.y");
				st.setString(1, key);
				st.setString(2, val);
				st.execute();
				fireOnDataSetChangeEvent(new VariableEvent(this, VariableEvent.UPDATE
						| VariableEvent.CREATE, key, old, val));
			}
		}
		catch (Exception e) {
			// e.printStackTrace();
		}
		finally {
			try {
				if (st != null) {
					st.close();
				}
				if (res != null) {
					res.close();
				}
			}
			catch (Exception e) {
			}
		}
	}
}
