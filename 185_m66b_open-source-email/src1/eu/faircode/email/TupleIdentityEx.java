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

import java.util.Objects;

public class TupleIdentityEx extends EntityIdentity {
    public String accountName;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TupleIdentityEx) {
            TupleIdentityEx other = (TupleIdentityEx) obj;
            return (super.equals(obj) &&
                    Objects.equals(accountName, other.accountName));
        } else
            return false;
    }
}
