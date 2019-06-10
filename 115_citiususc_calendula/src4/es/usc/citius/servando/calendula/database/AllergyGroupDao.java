/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2014-2018 CiTIUS - University of Santiago de Compostela
 *
 *    Calendula is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;

import es.usc.citius.servando.calendula.persistence.AllergyGroup;
import es.usc.citius.servando.calendula.util.LogUtil;

public class AllergyGroupDao extends GenericDao<AllergyGroup, Long> {

    private static final String TAG = "AllergyGroupDao";

    private Dao<AllergyGroup, Long> daoInstance = null;

    public AllergyGroupDao(DatabaseHelper db) {
        super(db);
    }

    @Override
    public Dao<AllergyGroup, Long> getConcreteDao() {
        try {
            if (daoInstance == null)
                daoInstance = dbHelper.getDao(AllergyGroup.class);
            return daoInstance;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating patients dao", e);
        }
    }

    public List<AllergyGroup> findAllOrderByPrecedence() {
        final QueryBuilder<AllergyGroup, Long> qb = dao.queryBuilder();
        qb.orderBy(AllergyGroup.COLUMN_PRECEDENCE, true);
        try {
            return dao.query(qb.prepare());
        } catch (SQLException e) {
            LogUtil.e(TAG, "findAllOrderByPrecedence: ", e);
            throw new RuntimeException("Error finding groups", e);
        }
    }


}
