/*
 * Copyright (C) 2014-2018 Open Whisper Systems
 *
 * Licensed according to the LICENSE file in this repository.
 */

package org.whispersystems.signalservice.api.messages.multidevice;

import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.InvalidMessageException;
import org.whispersystems.libsignal.logging.Log;
import org.whispersystems.libsignal.util.guava.Optional;
import org.whispersystems.signalservice.api.messages.SignalServiceAttachmentStream;
import org.whispersystems.signalservice.internal.push.SignalServiceProtos;
import org.whispersystems.signalservice.internal.util.Util;

import java.io.IOException;
import java.io.InputStream;

public class DeviceContactsInputStream extends ChunkedInputStream {

  private static final String TAG = DeviceContactsInputStream.class.getSimpleName();

  public DeviceContactsInputStream(InputStream in) {
    super(in);
  }

  public DeviceContact read() throws IOException {
    long   detailsLength     = readRawVarint32();
    byte[] detailsSerialized = new byte[(int)detailsLength];
    Util.readFully(in, detailsSerialized);

    SignalServiceProtos.ContactDetails      details     = SignalServiceProtos.ContactDetails.parseFrom(detailsSerialized);
    String                                  number      = details.getNumber();
    Optional<String>                        name        = Optional.fromNullable(details.getName());
    Optional<SignalServiceAttachmentStream> avatar      = Optional.absent();
    Optional<String>                        color       = details.hasColor() ? Optional.of(details.getColor()) : Optional.<String>absent();
    Optional<VerifiedMessage>               verified    = Optional.absent();
    Optional<byte[]>                        profileKey  = Optional.absent();
    boolean                                 blocked     = false;
    Optional<Integer>                       expireTimer = Optional.absent();

    if (details.hasAvatar()) {
      long        avatarLength      = details.getAvatar().getLength();
      InputStream avatarStream      = new LimitedInputStream(in, avatarLength);
      String      avatarContentType = details.getAvatar().getContentType();

      avatar = Optional.of(new SignalServiceAttachmentStream(avatarStream, avatarContentType, avatarLength, Optional.<String>absent(), false, null));
    }

    if (details.hasVerified()) {
      try {
        String      destination = details.getVerified().getDestination();
        IdentityKey identityKey = new IdentityKey(details.getVerified().getIdentityKey().toByteArray(), 0);

        VerifiedMessage.VerifiedState state;

        switch (details.getVerified().getState()) {
          case VERIFIED:  state = VerifiedMessage.VerifiedState.VERIFIED;   break;
          case UNVERIFIED:state = VerifiedMessage.VerifiedState.UNVERIFIED; break;
          case DEFAULT:   state = VerifiedMessage.VerifiedState.DEFAULT;    break;
          default:        throw new InvalidMessageException("Unknown state: " + details.getVerified().getState());
        }

        verified = Optional.of(new VerifiedMessage(destination, identityKey, state, System.currentTimeMillis()));
      } catch (InvalidKeyException | InvalidMessageException e) {
        Log.w(TAG, e);
        verified = Optional.absent();
      }
    }

    if (details.hasProfileKey()) {
      profileKey = Optional.fromNullable(details.getProfileKey().toByteArray());
    }

    if (details.hasExpireTimer() && details.getExpireTimer() > 0) {
      expireTimer = Optional.of(details.getExpireTimer());
    }

    blocked = details.getBlocked();

    return new DeviceContact(number, name, avatar, color, verified, profileKey, blocked, expireTimer);
  }

}
