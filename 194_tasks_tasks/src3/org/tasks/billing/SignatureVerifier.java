package org.tasks.billing;

import javax.inject.Inject;

public class SignatureVerifier {

  @Inject
  public SignatureVerifier() {}

  public boolean verifySignature(Purchase purchase) {
    return true;
  }
}
