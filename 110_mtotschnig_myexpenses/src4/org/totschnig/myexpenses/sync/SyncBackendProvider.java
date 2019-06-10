package org.totschnig.myexpenses.sync;

import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.annimon.stream.Exceptional;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.totschnig.myexpenses.model.Account;
import org.totschnig.myexpenses.sync.json.AccountMetaData;
import org.totschnig.myexpenses.sync.json.ChangeSet;
import org.totschnig.myexpenses.sync.json.TransactionChange;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface SyncBackendProvider {

  void withAccount(Account account) throws IOException;

  void resetAccountData(String uuid) throws IOException;

  void lock() throws IOException;

  void unlock() throws IOException;

  @NonNull
  Optional<ChangeSet> getChangeSetSince(SequenceNumber sequenceNumber, Context context) throws IOException;

  @NonNull SequenceNumber writeChangeSet(SequenceNumber lastSequenceNumber, List<TransactionChange> changeSet, Context context) throws IOException;

  @NonNull
  Stream<AccountMetaData> getRemoteAccountList(android.accounts.Account account) throws IOException;

  Exceptional<Void> setUp(String authToken, String encryptionPassword);

  void tearDown();

  void storeBackup(Uri uri, String fileName) throws IOException;

  @NonNull
  List<String> getStoredBackups(android.accounts.Account account) throws IOException;

  InputStream getInputStreamForBackup(android.accounts.Account account, String backupFile) throws IOException;

  /**
   *
   * @param e Exception thrown during sync operation
   * @return true if exception is caused by invalid auth token
   */
  boolean isAuthException(Exception e);

  class SyncParseException extends Exception {
    SyncParseException(Exception e) {
      super(e);
    }
    SyncParseException(String message) {
      super(message);
    }
  }

  class ResolvableSetupException extends Exception {

    @Nullable final PendingIntent resolution;

    ResolvableSetupException(@Nullable PendingIntent resolution, @Nullable String errorMessage) {
      super(errorMessage);
      this.resolution = resolution;
    }

    @Nullable
    public PendingIntent getResolution() {
      return resolution;
    }
  }

  class EncryptionException extends Exception {
    public EncryptionException(String message) {
      super(message);
    }
  }
}
