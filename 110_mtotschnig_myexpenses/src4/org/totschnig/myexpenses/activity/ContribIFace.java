/*   This file is part of My Expenses.
 *   My Expenses is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   My Expenses is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with My Expenses.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.totschnig.myexpenses.activity;

import java.io.Serializable;

import org.totschnig.myexpenses.model.ContribFeature;

public interface ContribIFace {

  /**
   * @param feature
   * called when the user clicks on "not yet", and calls the requested feature
   * @param tag TODO
   */
  void contribFeatureCalled(ContribFeature feature, Serializable tag);

  /**
   * the user can either click on "Buy" or cancel the dialog
   * for the moment, we are fine with the same callback for both cases,
   * for example, in some cases, the calling activity might have to be finished
   * @param feature
   */
  void contribFeatureNotCalled(ContribFeature feature);

}