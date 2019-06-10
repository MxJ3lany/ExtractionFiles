/*
 * Copyright (C) 2017 Schürmann & Breitmoser GbR
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.sufficientlysecure.keychain.keyimport;

import com.textuality.keybase.lib.KeybaseException;
import com.textuality.keybase.lib.KeybaseQuery;
import com.textuality.keybase.lib.Match;
import com.textuality.keybase.lib.User;

import org.sufficientlysecure.keychain.ui.util.KeyFormattingUtils;
import org.sufficientlysecure.keychain.network.OkHttpKeybaseClient;
import org.sufficientlysecure.keychain.util.ParcelableProxy;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.List;

public class KeybaseKeyserverClient implements KeyserverClient {

    public static KeybaseKeyserverClient getInstance() {
        return new KeybaseKeyserverClient();
    }

    private KeybaseKeyserverClient() { }

    @Override
    public ArrayList<ImportKeysListEntry> search(String query, ParcelableProxy proxy) throws QueryFailedException,
            QueryNeedsRepairException {
        ArrayList<ImportKeysListEntry> results = new ArrayList<>();

        if (query.startsWith("0x")) {
            // cut off "0x" if a user is searching for a key id
            query = query.substring(2);
        }
        if (query.isEmpty()) {
            throw new QueryTooShortException();
        }

        try {
            KeybaseQuery keybaseQuery = new KeybaseQuery(new OkHttpKeybaseClient());
            keybaseQuery.setProxy(proxy.getProxy());
            Iterable<Match> matches = keybaseQuery.search(query);
            for (Match match : matches) {
                results.add(makeEntry(match, query));
            }
        } catch (KeybaseException e) {
            Timber.e(e, "keybase result parsing error");
            throw new QueryFailedException("Unexpected structure in keybase search result: " + e.getMessage());
        }

        return results;
    }

    private ImportKeysListEntry makeEntry(Match match, String query) throws KeybaseException {
        final ImportKeysListEntry entry = new ImportKeysListEntry();
        entry.setQuery(query);

        entry.setRevoked(false); // keybase doesn’t say anything about revoked keys

        String username = match.getUsername();
        String fullName = match.getFullName();
        String fingerprint = match.getFingerprint();
        entry.setFingerprint(KeyFormattingUtils.convertFingerprintHexFingerprint(fingerprint));

        entry.setKeyIdHex("0x" + match.getKeyID());
        // so we can query for the keybase id directly, and to identify the location from which the
        // key is to be retrieved
        entry.setKeybaseName(username);

        final int bitStrength = match.getBitStrength();
        entry.setBitStrength(bitStrength);
        final int algorithmId = match.getAlgorithmId();
        entry.setAlgorithm(KeyFormattingUtils.getAlgorithmInfo(algorithmId, bitStrength, null));

        ArrayList<String> userIds = new ArrayList<>();
        String name = "<keybase.io/" + username + ">";
        if (fullName != null) {
            name = fullName + " " + name;
        }
        userIds.add(name);

        List<String> proofLabels = match.getProofLabels();
        for (String proofLabel : proofLabels) {
            userIds.add(proofLabel);
        }
        entry.setUserIds(userIds);
        entry.setPrimaryUserId(name);
        return entry;
    }

    @Override
    public String get(String id, ParcelableProxy proxy) throws QueryFailedException {
        try {
            KeybaseQuery keybaseQuery = new KeybaseQuery(new OkHttpKeybaseClient());
            keybaseQuery.setProxy(proxy.getProxy());
            return User.keyForUsername(keybaseQuery, id);
        } catch (KeybaseException e) {
            throw new QueryFailedException(e.getMessage());
        }
    }

    @Override
    public void add(String armoredKey, ParcelableProxy proxy) throws AddKeyException {
        throw new AddKeyException();
    }
}
