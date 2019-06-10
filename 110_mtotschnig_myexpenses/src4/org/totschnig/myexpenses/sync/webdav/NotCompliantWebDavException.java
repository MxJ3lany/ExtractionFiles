package org.totschnig.myexpenses.sync.webdav;

import java.io.IOException;

public class NotCompliantWebDavException extends IOException {
  public NotCompliantWebDavException(boolean fallbackToClass1) {
    this.fallbackToClass1 = fallbackToClass1;
  }

  private boolean fallbackToClass1 = false;

  public boolean isFallbackToClass1() {
    return fallbackToClass1;
  }
}
