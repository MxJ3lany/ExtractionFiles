// Generated by the protocol buffer compiler.  DO NOT EDIT!

package org.chromium.chrome.browser.omnibox.geo;

@SuppressWarnings("hiding")
public interface PartnerLocationDescriptor {

  // enum LocationRole
  public static final int UNKNOWN_ROLE = 0;
  public static final int CURRENT_LOCATION = 1;

  // enum LocationProducer
  public static final int UNKNOWN_PRODUCER = 0;
  public static final int DEVICE_LOCATION = 12;

  public static final class LatLng extends
      com.google.protobuf.nano.ExtendableMessageNano<LatLng> {

    private static volatile LatLng[] _emptyArray;
    public static LatLng[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new LatLng[0];
          }
        }
      }
      return _emptyArray;
    }

    // optional sfixed32 latitude_e7 = 1;
    public java.lang.Integer latitudeE7;

    // optional sfixed32 longitude_e7 = 2;
    public java.lang.Integer longitudeE7;

    public LatLng() {
      clear();
    }

    public LatLng clear() {
      latitudeE7 = null;
      longitudeE7 = null;
      unknownFieldData = null;
      cachedSize = -1;
      return this;
    }

    @Override
    public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
        throws java.io.IOException {
      if (this.latitudeE7 != null) {
        output.writeSFixed32(1, this.latitudeE7);
      }
      if (this.longitudeE7 != null) {
        output.writeSFixed32(2, this.longitudeE7);
      }
      super.writeTo(output);
    }

    @Override
    protected int computeSerializedSize() {
      int size = super.computeSerializedSize();
      if (this.latitudeE7 != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeSFixed32Size(1, this.latitudeE7);
      }
      if (this.longitudeE7 != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeSFixed32Size(2, this.longitudeE7);
      }
      return size;
    }

    @Override
    public LatLng mergeFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      while (true) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            return this;
          default: {
            if (!storeUnknownField(input, tag)) {
              return this;
            }
            break;
          }
          case 13: {
            this.latitudeE7 = input.readSFixed32();
            break;
          }
          case 21: {
            this.longitudeE7 = input.readSFixed32();
            break;
          }
        }
      }
    }

    public static LatLng parseFrom(byte[] data)
        throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
      return com.google.protobuf.nano.MessageNano.mergeFrom(new LatLng(), data);
    }

    public static LatLng parseFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new LatLng().mergeFrom(input);
    }
  }

  public static final class VisibleNetwork extends
      com.google.protobuf.nano.ExtendableMessageNano<VisibleNetwork> {

    public static final class WiFi extends
        com.google.protobuf.nano.ExtendableMessageNano<WiFi> {

      private static volatile WiFi[] _emptyArray;
      public static WiFi[] emptyArray() {
        // Lazily initializes the empty array
        if (_emptyArray == null) {
          synchronized (
              com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
            if (_emptyArray == null) {
              _emptyArray = new WiFi[0];
            }
          }
        }
        return _emptyArray;
      }

      // optional string bssid = 1;
      public java.lang.String bssid;

      // optional int32 level_dbm = 2;
      public java.lang.Integer levelDbm;

      public WiFi() {
        clear();
      }

      public WiFi clear() {
        bssid = null;
        levelDbm = null;
        unknownFieldData = null;
        cachedSize = -1;
        return this;
      }

      @Override
      public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
          throws java.io.IOException {
        if (this.bssid != null) {
          output.writeString(1, this.bssid);
        }
        if (this.levelDbm != null) {
          output.writeInt32(2, this.levelDbm);
        }
        super.writeTo(output);
      }

      @Override
      protected int computeSerializedSize() {
        int size = super.computeSerializedSize();
        if (this.bssid != null) {
          size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeStringSize(1, this.bssid);
        }
        if (this.levelDbm != null) {
          size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeInt32Size(2, this.levelDbm);
        }
        return size;
      }

      @Override
      public WiFi mergeFrom(
              com.google.protobuf.nano.CodedInputByteBufferNano input)
          throws java.io.IOException {
        while (true) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              return this;
            default: {
              if (!storeUnknownField(input, tag)) {
                return this;
              }
              break;
            }
            case 10: {
              this.bssid = input.readString();
              break;
            }
            case 16: {
              this.levelDbm = input.readInt32();
              break;
            }
          }
        }
      }

      public static WiFi parseFrom(byte[] data)
          throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
        return com.google.protobuf.nano.MessageNano.mergeFrom(new WiFi(), data);
      }

      public static WiFi parseFrom(
              com.google.protobuf.nano.CodedInputByteBufferNano input)
          throws java.io.IOException {
        return new WiFi().mergeFrom(input);
      }
    }

    public static final class Cell extends
        com.google.protobuf.nano.ExtendableMessageNano<Cell> {

      // enum Type
      public static final int UNKNOWN = 0;
      public static final int GSM = 1;
      public static final int LTE = 2;
      public static final int CDMA = 3;
      public static final int WCDMA = 4;

      private static volatile Cell[] _emptyArray;
      public static Cell[] emptyArray() {
        // Lazily initializes the empty array
        if (_emptyArray == null) {
          synchronized (
              com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
            if (_emptyArray == null) {
              _emptyArray = new Cell[0];
            }
          }
        }
        return _emptyArray;
      }

      // optional .org.chromium.chrome.browser.omnibox.geo.VisibleNetwork.Cell.Type type = 1;
      public java.lang.Integer type;

      // optional int32 cell_id = 2;
      public java.lang.Integer cellId;

      // optional int32 location_area_code = 3;
      public java.lang.Integer locationAreaCode;

      // optional int32 mobile_country_code = 4;
      public java.lang.Integer mobileCountryCode;

      // optional int32 mobile_network_code = 5;
      public java.lang.Integer mobileNetworkCode;

      // optional int32 primary_scrambling_code = 6;
      public java.lang.Integer primaryScramblingCode;

      // optional int32 physical_cell_id = 7;
      public java.lang.Integer physicalCellId;

      // optional int32 tracking_area_code = 8;
      public java.lang.Integer trackingAreaCode;

      public Cell() {
        clear();
      }

      public Cell clear() {
        type = null;
        cellId = null;
        locationAreaCode = null;
        mobileCountryCode = null;
        mobileNetworkCode = null;
        primaryScramblingCode = null;
        physicalCellId = null;
        trackingAreaCode = null;
        unknownFieldData = null;
        cachedSize = -1;
        return this;
      }

      @Override
      public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
          throws java.io.IOException {
        if (this.type != null) {
          output.writeInt32(1, this.type);
        }
        if (this.cellId != null) {
          output.writeInt32(2, this.cellId);
        }
        if (this.locationAreaCode != null) {
          output.writeInt32(3, this.locationAreaCode);
        }
        if (this.mobileCountryCode != null) {
          output.writeInt32(4, this.mobileCountryCode);
        }
        if (this.mobileNetworkCode != null) {
          output.writeInt32(5, this.mobileNetworkCode);
        }
        if (this.primaryScramblingCode != null) {
          output.writeInt32(6, this.primaryScramblingCode);
        }
        if (this.physicalCellId != null) {
          output.writeInt32(7, this.physicalCellId);
        }
        if (this.trackingAreaCode != null) {
          output.writeInt32(8, this.trackingAreaCode);
        }
        super.writeTo(output);
      }

      @Override
      protected int computeSerializedSize() {
        int size = super.computeSerializedSize();
        if (this.type != null) {
          size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeInt32Size(1, this.type);
        }
        if (this.cellId != null) {
          size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeInt32Size(2, this.cellId);
        }
        if (this.locationAreaCode != null) {
          size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeInt32Size(3, this.locationAreaCode);
        }
        if (this.mobileCountryCode != null) {
          size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeInt32Size(4, this.mobileCountryCode);
        }
        if (this.mobileNetworkCode != null) {
          size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeInt32Size(5, this.mobileNetworkCode);
        }
        if (this.primaryScramblingCode != null) {
          size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeInt32Size(6, this.primaryScramblingCode);
        }
        if (this.physicalCellId != null) {
          size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeInt32Size(7, this.physicalCellId);
        }
        if (this.trackingAreaCode != null) {
          size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeInt32Size(8, this.trackingAreaCode);
        }
        return size;
      }

      @Override
      public Cell mergeFrom(
              com.google.protobuf.nano.CodedInputByteBufferNano input)
          throws java.io.IOException {
        while (true) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              return this;
            default: {
              if (!storeUnknownField(input, tag)) {
                return this;
              }
              break;
            }
            case 8: {
              int value = input.readInt32();
              switch (value) {
                case org.chromium.chrome.browser.omnibox.geo.PartnerLocationDescriptor.VisibleNetwork.Cell.UNKNOWN:
                case org.chromium.chrome.browser.omnibox.geo.PartnerLocationDescriptor.VisibleNetwork.Cell.GSM:
                case org.chromium.chrome.browser.omnibox.geo.PartnerLocationDescriptor.VisibleNetwork.Cell.LTE:
                case org.chromium.chrome.browser.omnibox.geo.PartnerLocationDescriptor.VisibleNetwork.Cell.CDMA:
                case org.chromium.chrome.browser.omnibox.geo.PartnerLocationDescriptor.VisibleNetwork.Cell.WCDMA:
                  this.type = value;
                  break;
              }
              break;
            }
            case 16: {
              this.cellId = input.readInt32();
              break;
            }
            case 24: {
              this.locationAreaCode = input.readInt32();
              break;
            }
            case 32: {
              this.mobileCountryCode = input.readInt32();
              break;
            }
            case 40: {
              this.mobileNetworkCode = input.readInt32();
              break;
            }
            case 48: {
              this.primaryScramblingCode = input.readInt32();
              break;
            }
            case 56: {
              this.physicalCellId = input.readInt32();
              break;
            }
            case 64: {
              this.trackingAreaCode = input.readInt32();
              break;
            }
          }
        }
      }

      public static Cell parseFrom(byte[] data)
          throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
        return com.google.protobuf.nano.MessageNano.mergeFrom(new Cell(), data);
      }

      public static Cell parseFrom(
              com.google.protobuf.nano.CodedInputByteBufferNano input)
          throws java.io.IOException {
        return new Cell().mergeFrom(input);
      }
    }

    private static volatile VisibleNetwork[] _emptyArray;
    public static VisibleNetwork[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new VisibleNetwork[0];
          }
        }
      }
      return _emptyArray;
    }

    // optional .org.chromium.chrome.browser.omnibox.geo.VisibleNetwork.WiFi wifi = 1;
    public org.chromium.chrome.browser.omnibox.geo.PartnerLocationDescriptor.VisibleNetwork.WiFi wifi;

    // optional .org.chromium.chrome.browser.omnibox.geo.VisibleNetwork.Cell cell = 2;
    public org.chromium.chrome.browser.omnibox.geo.PartnerLocationDescriptor.VisibleNetwork.Cell cell;

    // optional bool connected = 3;
    public java.lang.Boolean connected;

    // optional int64 timestamp_ms = 4;
    public java.lang.Long timestampMs;

    public VisibleNetwork() {
      clear();
    }

    public VisibleNetwork clear() {
      wifi = null;
      cell = null;
      connected = null;
      timestampMs = null;
      unknownFieldData = null;
      cachedSize = -1;
      return this;
    }

    @Override
    public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
        throws java.io.IOException {
      if (this.wifi != null) {
        output.writeMessage(1, this.wifi);
      }
      if (this.cell != null) {
        output.writeMessage(2, this.cell);
      }
      if (this.connected != null) {
        output.writeBool(3, this.connected);
      }
      if (this.timestampMs != null) {
        output.writeInt64(4, this.timestampMs);
      }
      super.writeTo(output);
    }

    @Override
    protected int computeSerializedSize() {
      int size = super.computeSerializedSize();
      if (this.wifi != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
          .computeMessageSize(1, this.wifi);
      }
      if (this.cell != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
          .computeMessageSize(2, this.cell);
      }
      if (this.connected != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeBoolSize(3, this.connected);
      }
      if (this.timestampMs != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeInt64Size(4, this.timestampMs);
      }
      return size;
    }

    @Override
    public VisibleNetwork mergeFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      while (true) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            return this;
          default: {
            if (!storeUnknownField(input, tag)) {
              return this;
            }
            break;
          }
          case 10: {
            if (this.wifi == null) {
              this.wifi = new org.chromium.chrome.browser.omnibox.geo.PartnerLocationDescriptor.VisibleNetwork.WiFi();
            }
            input.readMessage(this.wifi);
            break;
          }
          case 18: {
            if (this.cell == null) {
              this.cell = new org.chromium.chrome.browser.omnibox.geo.PartnerLocationDescriptor.VisibleNetwork.Cell();
            }
            input.readMessage(this.cell);
            break;
          }
          case 24: {
            this.connected = input.readBool();
            break;
          }
          case 32: {
            this.timestampMs = input.readInt64();
            break;
          }
        }
      }
    }

    public static VisibleNetwork parseFrom(byte[] data)
        throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
      return com.google.protobuf.nano.MessageNano.mergeFrom(new VisibleNetwork(), data);
    }

    public static VisibleNetwork parseFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new VisibleNetwork().mergeFrom(input);
    }
  }

  public static final class LocationDescriptor extends
      com.google.protobuf.nano.ExtendableMessageNano<LocationDescriptor> {

    private static volatile LocationDescriptor[] _emptyArray;
    public static LocationDescriptor[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new LocationDescriptor[0];
          }
        }
      }
      return _emptyArray;
    }

    // optional .org.chromium.chrome.browser.omnibox.geo.LocationRole role = 1 [default = UNKNOWN_ROLE];
    public java.lang.Integer role;

    // optional .org.chromium.chrome.browser.omnibox.geo.LocationProducer producer = 2 [default = UNKNOWN_PRODUCER];
    public java.lang.Integer producer;

    // optional int64 timestamp = 3;
    public java.lang.Long timestamp;

    // optional .org.chromium.chrome.browser.omnibox.geo.LatLng latlng = 5;
    public org.chromium.chrome.browser.omnibox.geo.PartnerLocationDescriptor.LatLng latlng;

    // optional float radius = 7;
    public java.lang.Float radius;

    // repeated .org.chromium.chrome.browser.omnibox.geo.VisibleNetwork visible_network = 23;
    public org.chromium.chrome.browser.omnibox.geo.PartnerLocationDescriptor.VisibleNetwork[] visibleNetwork;

    public LocationDescriptor() {
      clear();
    }

    public LocationDescriptor clear() {
      role = null;
      producer = null;
      timestamp = null;
      latlng = null;
      radius = null;
      visibleNetwork = org.chromium.chrome.browser.omnibox.geo.PartnerLocationDescriptor.VisibleNetwork.emptyArray();
      unknownFieldData = null;
      cachedSize = -1;
      return this;
    }

    @Override
    public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
        throws java.io.IOException {
      if (this.role != null) {
        output.writeInt32(1, this.role);
      }
      if (this.producer != null) {
        output.writeInt32(2, this.producer);
      }
      if (this.timestamp != null) {
        output.writeInt64(3, this.timestamp);
      }
      if (this.latlng != null) {
        output.writeMessage(5, this.latlng);
      }
      if (this.radius != null) {
        output.writeFloat(7, this.radius);
      }
      if (this.visibleNetwork != null && this.visibleNetwork.length > 0) {
        for (int i = 0; i < this.visibleNetwork.length; i++) {
          org.chromium.chrome.browser.omnibox.geo.PartnerLocationDescriptor.VisibleNetwork element = this.visibleNetwork[i];
          if (element != null) {
            output.writeMessage(23, element);
          }
        }
      }
      super.writeTo(output);
    }

    @Override
    protected int computeSerializedSize() {
      int size = super.computeSerializedSize();
      if (this.role != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
          .computeInt32Size(1, this.role);
      }
      if (this.producer != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
          .computeInt32Size(2, this.producer);
      }
      if (this.timestamp != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeInt64Size(3, this.timestamp);
      }
      if (this.latlng != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
          .computeMessageSize(5, this.latlng);
      }
      if (this.radius != null) {
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeFloatSize(7, this.radius);
      }
      if (this.visibleNetwork != null && this.visibleNetwork.length > 0) {
        for (int i = 0; i < this.visibleNetwork.length; i++) {
          org.chromium.chrome.browser.omnibox.geo.PartnerLocationDescriptor.VisibleNetwork element = this.visibleNetwork[i];
          if (element != null) {
            size += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeMessageSize(23, element);
          }
        }
      }
      return size;
    }

    @Override
    public LocationDescriptor mergeFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      while (true) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            return this;
          default: {
            if (!storeUnknownField(input, tag)) {
              return this;
            }
            break;
          }
          case 8: {
            int value = input.readInt32();
            switch (value) {
              case org.chromium.chrome.browser.omnibox.geo.PartnerLocationDescriptor.UNKNOWN_ROLE:
              case org.chromium.chrome.browser.omnibox.geo.PartnerLocationDescriptor.CURRENT_LOCATION:
                this.role = value;
                break;
            }
            break;
          }
          case 16: {
            int value = input.readInt32();
            switch (value) {
              case org.chromium.chrome.browser.omnibox.geo.PartnerLocationDescriptor.UNKNOWN_PRODUCER:
              case org.chromium.chrome.browser.omnibox.geo.PartnerLocationDescriptor.DEVICE_LOCATION:
                this.producer = value;
                break;
            }
            break;
          }
          case 24: {
            this.timestamp = input.readInt64();
            break;
          }
          case 42: {
            if (this.latlng == null) {
              this.latlng = new org.chromium.chrome.browser.omnibox.geo.PartnerLocationDescriptor.LatLng();
            }
            input.readMessage(this.latlng);
            break;
          }
          case 61: {
            this.radius = input.readFloat();
            break;
          }
          case 186: {
            int arrayLength = com.google.protobuf.nano.WireFormatNano
                .getRepeatedFieldArrayLength(input, 186);
            int i = this.visibleNetwork == null ? 0 : this.visibleNetwork.length;
            org.chromium.chrome.browser.omnibox.geo.PartnerLocationDescriptor.VisibleNetwork[] newArray =
                new org.chromium.chrome.browser.omnibox.geo.PartnerLocationDescriptor.VisibleNetwork[i + arrayLength];
            if (i != 0) {
              java.lang.System.arraycopy(this.visibleNetwork, 0, newArray, 0, i);
            }
            for (; i < newArray.length - 1; i++) {
              newArray[i] = new org.chromium.chrome.browser.omnibox.geo.PartnerLocationDescriptor.VisibleNetwork();
              input.readMessage(newArray[i]);
              input.readTag();
            }
            // Last one without readTag.
            newArray[i] = new org.chromium.chrome.browser.omnibox.geo.PartnerLocationDescriptor.VisibleNetwork();
            input.readMessage(newArray[i]);
            this.visibleNetwork = newArray;
            break;
          }
        }
      }
    }

    public static LocationDescriptor parseFrom(byte[] data)
        throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
      return com.google.protobuf.nano.MessageNano.mergeFrom(new LocationDescriptor(), data);
    }

    public static LocationDescriptor parseFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new LocationDescriptor().mergeFrom(input);
    }
  }
}
