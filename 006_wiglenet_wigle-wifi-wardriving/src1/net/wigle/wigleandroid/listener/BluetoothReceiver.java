package net.wigle.wigleandroid.listener;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;

import com.google.android.gms.maps.model.LatLng;

import net.wigle.wigleandroid.FilterMatcher;
import net.wigle.wigleandroid.ListFragment;
import net.wigle.wigleandroid.MainActivity;
import net.wigle.wigleandroid.ui.SetNetworkListAdapter;
import net.wigle.wigleandroid.R;
import net.wigle.wigleandroid.db.DatabaseHelper;
import net.wigle.wigleandroid.model.ConcurrentLinkedHashMap;
import net.wigle.wigleandroid.model.Network;
import net.wigle.wigleandroid.model.NetworkType;
import net.wigle.wigleandroid.ui.NetworkListSorter;
import net.wigle.wigleandroid.ui.WiGLEToast;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;

import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;
import uk.co.alt236.bluetoothlelib.device.adrecord.AdRecord;
import uk.co.alt236.bluetoothlelib.device.adrecord.AdRecordStore;

import static net.wigle.wigleandroid.MainActivity.DEBUG_BLUETOOTH_DATA;

/**
 * Created by bobzilla on 12/20/15
 */
public final class BluetoothReceiver extends BroadcastReceiver {

    private static final Map<Integer, String> DEVICE_TYPE_LEGEND;
    //TODO: i18n
    static {
        Map<Integer, String> initMap = new HashMap<>();
        initMap.put(0, "Misc");
        initMap.put(BluetoothClass.Device.AUDIO_VIDEO_CAMCORDER, "Camcorder");
        initMap.put(BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO, "Car Audio");
        initMap.put(BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE, "Handsfree");
        initMap.put(BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES, "Headphones");
        initMap.put(BluetoothClass.Device.AUDIO_VIDEO_HIFI_AUDIO, "HiFi");
        initMap.put(BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER, "Speaker");
        initMap.put(BluetoothClass.Device.AUDIO_VIDEO_MICROPHONE, "Mic");
        initMap.put(BluetoothClass.Device.AUDIO_VIDEO_PORTABLE_AUDIO, "Portable Audio");
        initMap.put(BluetoothClass.Device.AUDIO_VIDEO_SET_TOP_BOX, "Settop");
        initMap.put(BluetoothClass.Device.AUDIO_VIDEO_UNCATEGORIZED, "A/V");
        initMap.put(BluetoothClass.Device.AUDIO_VIDEO_VCR, "VCR");
        initMap.put(BluetoothClass.Device.AUDIO_VIDEO_VIDEO_CAMERA, "Camera");
        initMap.put(BluetoothClass.Device.AUDIO_VIDEO_VIDEO_CONFERENCING, "Videoconf");
        initMap.put(BluetoothClass.Device.AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER, "Display/Speaker");
        initMap.put(BluetoothClass.Device.AUDIO_VIDEO_VIDEO_GAMING_TOY, "AV Toy");
        initMap.put(BluetoothClass.Device.AUDIO_VIDEO_VIDEO_MONITOR, "Monitor");
        initMap.put(BluetoothClass.Device.COMPUTER_DESKTOP, "Desktop");
        initMap.put(BluetoothClass.Device.COMPUTER_HANDHELD_PC_PDA, "PDA");
        initMap.put(BluetoothClass.Device.COMPUTER_LAPTOP, "Laptop");
        initMap.put(BluetoothClass.Device.COMPUTER_PALM_SIZE_PC_PDA, "Palm");
        initMap.put(BluetoothClass.Device.COMPUTER_SERVER, "Server");
        initMap.put(BluetoothClass.Device.COMPUTER_UNCATEGORIZED, "Computer");
        initMap.put(BluetoothClass.Device.COMPUTER_WEARABLE, "Wearable Computer");
        initMap.put(BluetoothClass.Device.HEALTH_BLOOD_PRESSURE, "Blood Pressure");
        initMap.put(BluetoothClass.Device.HEALTH_DATA_DISPLAY, "Health Display");
        initMap.put(BluetoothClass.Device.HEALTH_GLUCOSE, "Glucose");
        initMap.put(BluetoothClass.Device.HEALTH_PULSE_OXIMETER, "PulseOxy");
        initMap.put(BluetoothClass.Device.HEALTH_PULSE_RATE, "Pulse");
        initMap.put(BluetoothClass.Device.HEALTH_THERMOMETER, "Thermometer");
        initMap.put(BluetoothClass.Device.HEALTH_UNCATEGORIZED, "Health");
        initMap.put(BluetoothClass.Device.HEALTH_WEIGHING, "Scale");
        initMap.put(BluetoothClass.Device.PHONE_CELLULAR, "Cellphone");
        initMap.put(BluetoothClass.Device.PHONE_CORDLESS, "Cordless Phone");
        initMap.put(BluetoothClass.Device.PHONE_ISDN, "ISDN Phone");
        initMap.put(BluetoothClass.Device.PHONE_MODEM_OR_GATEWAY, "Modem/GW");
        initMap.put(BluetoothClass.Device.PHONE_SMART, "Smartphone");
        initMap.put(BluetoothClass.Device.PHONE_UNCATEGORIZED, "Phone");
        initMap.put(BluetoothClass.Device.TOY_CONTROLLER, "Controller");
        initMap.put(BluetoothClass.Device.TOY_DOLL_ACTION_FIGURE, "Doll");
        initMap.put(BluetoothClass.Device.TOY_GAME, "Game");
        initMap.put(BluetoothClass.Device.TOY_ROBOT, "Robot");
        initMap.put(BluetoothClass.Device.TOY_UNCATEGORIZED, "Toy");
        initMap.put(BluetoothClass.Device.TOY_VEHICLE, "Vehicle");
        initMap.put(BluetoothClass.Device.WEARABLE_GLASSES, "Glasses");
        initMap.put(BluetoothClass.Device.WEARABLE_HELMET, "Helmet");
        initMap.put(BluetoothClass.Device.WEARABLE_JACKET, "Jacket");
        initMap.put(BluetoothClass.Device.WEARABLE_PAGER, "Pager");
        initMap.put(BluetoothClass.Device.WEARABLE_UNCATEGORIZED, "Wearable");
        initMap.put(BluetoothClass.Device.WEARABLE_WRIST_WATCH, "Watch");
        initMap.put(BluetoothClass.Device.Major.UNCATEGORIZED, "Uncategorized");

        DEVICE_TYPE_LEGEND = Collections.unmodifiableMap(initMap);
    }

    private MainActivity mainActivity;
    private final DatabaseHelper dbHelper;
    private final AtomicBoolean scanning = new AtomicBoolean(false);
    //TODO: this is pretty redundant with the central network list,
    // but they all seem to be getting out of sync, which is annoying AF
    private final Set<String> unsafeRunNetworks = new HashSet<>();
    private final Set<String> runNetworks = Collections.synchronizedSet(unsafeRunNetworks);

    private SetNetworkListAdapter listAdapter;
    private final ScanCallback scanCallback;

    private Handler bluetoothTimer;
    private long scanRequestTime = Long.MIN_VALUE;
    private boolean scanInFlight = false;
    private long lastScanResponseTime = Long.MIN_VALUE;
    private final long constructionTime = System.currentTimeMillis();

    // refresh thresholds - probably should either make these configurable
    // arguably expiration should live per element not-seen in n scans.
    private static final int EMPTY_LE_THRESHOLD = 10;
    private static final int EMPTY_BT_THRESHOLD = 2;

    // scan state
    private long lastDiscoveryAt = 0;

    private long adUuidNoScanUuid = 0;
    private long scanUuidNoAdUuid = 0;


    public BluetoothReceiver(final MainActivity mainActivity, final DatabaseHelper dbHelper ) {
        this.mainActivity = mainActivity;
        this.dbHelper = dbHelper;
        ListFragment.lameStatic.runBtNetworks = runNetworks;

        if (Build.VERSION.SDK_INT >= 21) {
            scanCallback = new ScanCallback() {
                final SharedPreferences prefs = mainActivity.getSharedPreferences( ListFragment.SHARED_PREFS, 0 );
                private int empties = 0;

                @Override
                public void onScanResult(int callbackType, ScanResult scanResult) {
                    final GPSListener gpsListener = mainActivity.getGPSListener();
                    //DEBUG:
                    MainActivity.info("LE scanResult: " + scanResult + " callbackType: " + callbackType);
                    Location location = null;
                    if (gpsListener != null) {
                        final long gpsTimeout = prefs.getLong(ListFragment.PREF_GPS_TIMEOUT, GPSListener.GPS_TIMEOUT_DEFAULT);
                        final long netLocTimeout = prefs.getLong(ListFragment.PREF_NET_LOC_TIMEOUT, GPSListener.NET_LOC_TIMEOUT_DEFAULT);
                        gpsListener.checkLocationOK(gpsTimeout, netLocTimeout);
                        location = gpsListener.getLocation();
                    } else {
                        MainActivity.warn("Null gpsListener in LE Single Scan Result");
                    }

                    handleLeScanResult(scanResult, location, false);
                    final long newBtCount = dbHelper.getNewBtCount();
                    ListFragment.lameStatic.newBt = newBtCount;
                    ListFragment.lameStatic.runBt = runNetworks.size();
                    sort(prefs);
                    if (listAdapter != null) listAdapter.notifyDataSetChanged();
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    //MainActivity.info("LE Batch results: " + results);
                    final GPSListener gpsListener = mainActivity.getGPSListener();

                    Location location = null;

                    boolean forceLeListReset = false;
                    if (results.isEmpty()) {
                        empties++;
                        //DEBUG: MainActivity.info("empty scan result ("+empties+"/"+EMPTY_LE_THRESHOLD+")");
                        //ALIBI: if it's been too long, we'll force-clear
                        if (EMPTY_LE_THRESHOLD < empties) {
                            forceLeListReset = true;
                            empties = 0;
                        }
                    } else {
                        empties = 0;
                    }

                    if ((listAdapter != null) && prefs.getBoolean( ListFragment.PREF_SHOW_CURRENT, true ) && forceLeListReset ) {
                        listAdapter.clearBluetoothLe();
                    }

                    if (gpsListener != null) {
                        location = gpsListener.checkGetLocation(prefs);
                    } else {
                        MainActivity.warn("Null gpsListener in LE Batch Scan Result");
                    }

                    for (final ScanResult scanResult : results) {
                        handleLeScanResult(scanResult, location, true);
                    }
                    if (listAdapter != null) {
                        listAdapter.batchUpdateBt(prefs.getBoolean(ListFragment.PREF_SHOW_CURRENT, true),
                                true, false);
                    }
                    final long newBtCount = dbHelper.getNewBtCount();
                    ListFragment.lameStatic.newBt = newBtCount;
                    ListFragment.lameStatic.runBt = runNetworks.size();
                    sort(prefs);
                    if (listAdapter != null) listAdapter.notifyDataSetChanged();
                }

                @Override
                public void onScanFailed(int errorCode) {
                    switch (errorCode) {
                        case SCAN_FAILED_ALREADY_STARTED:
                            MainActivity.info("BluetoothLEScan already started");
                            break;
                        default:
                            if ((listAdapter != null) && prefs.getBoolean( ListFragment.PREF_SHOW_CURRENT, true ) ) {
                                listAdapter.clearBluetoothLe();
                            }
                            MainActivity.error("Bluetooth LE scan error: " + errorCode);
                            scanning.set(false);
                    }
                }
            };
        } else {
            scanCallback = null;
        }
    }

    private void handleLeScanResult(final ScanResult scanResult, Location location, final boolean batch) {
        if (Build.VERSION.SDK_INT >= 21) {
            //DEBUG: MainActivity.info("LE scanResult: " + scanResult);
            final ScanRecord scanRecord = scanResult.getScanRecord();
            if (scanRecord != null) {
                final BluetoothDevice device = scanResult.getDevice();
                //BluetoothUtil.BleAdvertisedData adData = BluetoothUtil.parseAdvertisedData(scanRecord.getBytes());
                //final String adDeviceName = (adData != null) ? adData.getName(): null;

                final String bssid = device.getAddress();

                final String ssid =
                        (null ==  scanRecord.getDeviceName() || scanRecord.getDeviceName().isEmpty())
                                ? device.getName()
                                :scanRecord.getDeviceName();

                // This is questionable - of Major class being known when specific class seems thin
                final BluetoothClass bluetoothClass = device.getBluetoothClass();
                int type = BluetoothClass.Device.Major.UNCATEGORIZED;
                if (bluetoothClass != null) {
                    final int deviceClass = bluetoothClass.getDeviceClass();
                    type = (deviceClass == 0 || deviceClass == BluetoothClass.Device.Major.UNCATEGORIZED)
                            ? bluetoothClass.getMajorDeviceClass()
                            : deviceClass;
                }

                if (DEBUG_BLUETOOTH_DATA) {
                    MainActivity.info("LE deviceName: " + ssid
                            + "\n\taddress: " + bssid
                            + "\n\tname: " + scanRecord.getDeviceName() + " (vs. "+device.getName()+")"
                            //+ "\n\tadName: " + adDeviceName
                            + "\n\tclass:"
                            + (bluetoothClass == null ? null : DEVICE_TYPE_LEGEND.get(bluetoothClass.getDeviceClass()))
                            + "(" + bluetoothClass + ")"
                            + "\n\ttype:" + device.getType()
                            + "\n\tRSSI:" + scanResult.getRssi()
                            //+ "\n\tTX power:" + scanRecord.getTxPowerLevel() //THIS IS ALWAYS GARBAGE
                            //+ "\n\tbytes: " + Arrays.toString(scanRecord.getBytes())
                            );


                    /*final int scanCount = ((scanRecord != null) && (scanRecord.getServiceUuids() != null)) ? scanRecord.getServiceUuids().size() : 0;
                    final int adCount = ((adData != null) && (adData.getUuids() != null)) ? adData.getUuids().size() : 0;

                    if (adCount > 0 || scanCount > 0){
                        final List<java.util.UUID> adUuids = adData.getUuids();
                        final List<ParcelUuid> srUuids = scanRecord.getServiceUuids();
                        if (scanCount > adCount) {
                            for (ParcelUuid uuid: srUuids) {
                                if (! adUuids.contains(uuid.getUuid())) {
                                    MainActivity.error("\n\t\tSR: "+uuid.toString());
                                }
                            }
                            scanUuidNoAdUuid++;
                        } else if (adCount > scanCount) {
                            for (UUID uuid: adUuids) {
                                if (! srUuids.contains(new ParcelUuid(uuid))) {
                                    MainActivity.error("\n\t\tAD: "+uuid.toString());
                                }
                            }
                            adUuidNoScanUuid++;
                        } else if (scanCount > 0) {
                            for (ParcelUuid uuid: srUuids) {
                                MainActivity.info("\n\t\t==: "+uuid.toString());
                            }
                        }
                    }*/
                }
                try {
                    //TODO: not seeing a lot of value from these checks yet (vs. the adData name extraction above)
                    final BluetoothLeDevice deviceLe = new BluetoothLeDevice(device, scanResult.getRssi(),
                            scanRecord.getBytes(), System.currentTimeMillis());
                    final AdRecordStore adRecordStore = deviceLe.getAdRecordStore();
                    for (int i = 0; i < 200; i++) {
                        if (!adRecordStore.isRecordPresent(i)) {
                            continue;
                        }
                        final AdRecord adRecord = adRecordStore.getRecord(i);
                        if (DEBUG_BLUETOOTH_DATA) {
                            MainActivity.info("LE adRecord(" + i + "): " + adRecord);
                        }
                    }
                } catch (Exception ex) {
                    //TODO: so this happens:
                    MainActivity.warn("failed to parse LeDevice from ScanRecord", ex);
                    //parseScanRecordAsSparseArray explodes on array indices
                }

                final String capabilities = DEVICE_TYPE_LEGEND.get(
                        bluetoothClass == null ? null : bluetoothClass.getDeviceClass());
                final SharedPreferences prefs = mainActivity.getSharedPreferences( ListFragment.SHARED_PREFS, 0 );
                //ALIBI: shamelessly re-using frequency here for device type.
                final Network network = addOrUpdateBt(bssid, ssid, type, capabilities,
                        scanResult.getRssi(),
                        NetworkType.BLE, location, prefs, batch);
            }
        }
    }

    /**
     * initiate a bluetooth scan, if discovery is not currently in-progress (callbacks via onReceive)
     */
    public void bluetoothScan() {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            return;
        }

        // classic BT scan - basically "Always Be Discovering" times between discovery runs will be MAX(wifi delay) since this is called from wifi receiver
        if (!bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.startDiscovery();
            lastDiscoveryAt = System.currentTimeMillis();
        } else {
            if (DEBUG_BLUETOOTH_DATA) {
                MainActivity.info("skipping bluetooth scan; discover already in progress (last scan started "+(System.currentTimeMillis()-lastDiscoveryAt)+"ms ago)");
            }
        }

        if (Build.VERSION.SDK_INT >= 21) {
            final BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            if (bluetoothLeScanner == null) {
                MainActivity.info("bluetoothLeScanner is null");
            }  else {
                if (scanning.compareAndSet(false, true)) {
                    final ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();
                    scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
                    //TODO: make settable? NOTE: unset, you'll never get batch results, even with LOWER_POWER above
                    //  this is effectively how often we update the display
                    scanSettingsBuilder.setReportDelay(15000);
                    bluetoothLeScanner.startScan(
                            Collections.<ScanFilter>emptyList(), scanSettingsBuilder.build(), scanCallback);

                } else {
                    bluetoothLeScanner.flushPendingScanResults(scanCallback);
                }
            }
        }

        /*
        Paired device check?
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for(BluetoothDevice device : pairedDevices) {
            MainActivity.info("\tpareid device: "+device.getAddress()+" - "+device.getName() + device.getBluetoothClass());
            //BluetoothClass bluetoothClass = device.getBluetoothClass();
        }*/

        if (DEBUG_BLUETOOTH_DATA) {
            if (adUuidNoScanUuid > 0 || scanUuidNoAdUuid > 0) {
                MainActivity.error("AD but No Scan UUID: "+ adUuidNoScanUuid + " Scan but No Ad UUID: " + scanUuidNoAdUuid);
            }
        }
    }

    public void stopScanning() {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();

            final SharedPreferences prefs = mainActivity.getSharedPreferences( ListFragment.SHARED_PREFS, 0 );
            final boolean showCurrent = prefs.getBoolean( ListFragment.PREF_SHOW_CURRENT, true );

            if (listAdapter != null && showCurrent) {
                listAdapter.clearBluetoothLe();
                listAdapter.clearBluetooth();
            }


            if (Build.VERSION.SDK_INT >= 21) {
                final BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                if (bluetoothLeScanner != null) {
                    if (scanning.compareAndSet(true, false)) {
                        bluetoothLeScanner.stopScan(scanCallback);
                    } else {
                        MainActivity.error("Scanner present, comp-and-set prevented stop-scan");
                    }
                }
            }
        }
    }

    /**
     * General Bluetooth on-receive callback. Can register a BC or BLE network, although provides no means for distinguishing between them.
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {

        final SharedPreferences prefs = mainActivity.getSharedPreferences( ListFragment.SHARED_PREFS, 0 );
        final String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {

            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device == null) {
                // as reported in bug feed
                MainActivity.error("onReceive with null device - discarding this instance");
                return;
            }
            final BluetoothClass btClass = intent.getParcelableExtra(BluetoothDevice.EXTRA_CLASS);
            int  rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);

            final String bssid = device.getAddress();
            final String ssid = device.getName();

            int type;

            if (btClass == null && device != null) {
                type = (isMiscOrUncategorized(device.getBluetoothClass().getDeviceClass())) ?
                        device.getBluetoothClass().getMajorDeviceClass() : device.getBluetoothClass().getDeviceClass();
            } else {
                type = btClass.getDeviceClass();
            }

            if (DEBUG_BLUETOOTH_DATA) {
                String log = "BT deviceName: " + device.getName()
                        + "\n\taddress: " + bssid
                        + "\n\tname: " + ssid
                        + "\n\tRSSI dBM: " + rssi
                        + "\n\tclass: " + DEVICE_TYPE_LEGEND.get(type)
                        + "("+type+")"
                        + "\n\tbondState: " + device.getBondState();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    log += "\n\tuuids: " + device.getUuids();
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    log += "\n\ttype:" + device.getType();
                }

                MainActivity.info(log);
            }

            final String capabilities = DEVICE_TYPE_LEGEND.get(type)
                    /*+ " (" + device.getBluetoothClass().getMajorDeviceClass()
                    + ":" +device.getBluetoothClass().getDeviceClass() + ")"*/
                    + ";" + device.getBondState();
            final GPSListener gpsListener = mainActivity.getGPSListener();

            Location location = null;
            if (gpsListener != null) {
                final long gpsTimeout = prefs.getLong(ListFragment.PREF_GPS_TIMEOUT, GPSListener.GPS_TIMEOUT_DEFAULT);
                final long netLocTimeout = prefs.getLong(ListFragment.PREF_NET_LOC_TIMEOUT, GPSListener.NET_LOC_TIMEOUT_DEFAULT);
                gpsListener.checkLocationOK(gpsTimeout, netLocTimeout);
                location = gpsListener.getLocation();
            } else {
                MainActivity.warn("null gpsListener in BTR onReceive");
            }

            //ALIBI: shamelessly re-using frequency here for device type.
            final Network network =  addOrUpdateBt(bssid, ssid, type, capabilities, rssi, NetworkType.BT, location, prefs, false);
            sort(prefs);
            if (listAdapter != null) listAdapter.notifyDataSetChanged();

        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
            final boolean showCurrent = prefs.getBoolean( ListFragment.PREF_SHOW_CURRENT, true );
            if (listAdapter != null) listAdapter.batchUpdateBt(showCurrent, false, true);
            final long newBtCount = dbHelper.getNewBtCount();
            ListFragment.lameStatic.newBt = newBtCount;
            ListFragment.lameStatic.runBt = runNetworks.size();
            sort(prefs);
            if (listAdapter != null) listAdapter.notifyDataSetChanged();

        }
    }

    /**
     * TODO: DRY this up with the sort in WifiReceiver?
     * @param prefs
     */
    private void sort(final SharedPreferences prefs) {
        if (listAdapter != null) {
            try {
                listAdapter.sort(NetworkListSorter.getSort(prefs));
            } catch (IllegalArgumentException ex) {
                MainActivity.error("sort failed: ",ex);
            }
        }
    }

    public void setListAdapter( final SetNetworkListAdapter listAdapter ) {
        this.listAdapter = listAdapter;
    }

    public int getRunNetworkCount() {
        return runNetworks.size();
    }

    public void setupBluetoothTimer( final boolean turnedBtOn ) {
        MainActivity.info( "create Bluetooth timer" );
        if ( bluetoothTimer == null ) {
            bluetoothTimer = new Handler();
            final Runnable mUpdateTimeTask = new Runnable() {
                @Override
                public void run() {
                    // make sure the app isn't trying to finish
                    if ( ! mainActivity.isFinishing() ) {
                        // info( "timer start scan" );
                        // schedule a bluetooth scan
                        doBluetoothScan();
                        if ( scanRequestTime <= 0 ) {
                            scanRequestTime = System.currentTimeMillis();
                        }
                        long period = getScanPeriod();
                        // check if set to "continuous"
                        if ( period == 0L ) {
                            // set to default here, as a scan will also be requested on the scan result listener
                            period = MainActivity.SCAN_DEFAULT;
                        }
                        // info("bluetoothtimer: " + period );
                        bluetoothTimer.postDelayed( this, period );
                    }
                    else {
                        MainActivity.info( "finishing timer" );
                    }
                }
            };
            bluetoothTimer.removeCallbacks( mUpdateTimeTask );
            bluetoothTimer.postDelayed( mUpdateTimeTask, 100 );

            if ( turnedBtOn ) {
                MainActivity.info( "not immediately running BT scan, since it was just turned on"
                        + " it will block for a few seconds and fail anyway");
            }
            else {
                MainActivity.info( "start first bluetooth scan");
                // starts scan, sends event when done
                final boolean scanOK = doBluetoothScan();
                mainActivity.bluetoothScan();

                if ( scanRequestTime <= 0 ) {
                    scanRequestTime = System.currentTimeMillis();
                }
                MainActivity.info( "startup finished. BT scanOK: " + scanOK );
            }
        }
    }

    public boolean doBluetoothScan() {
        boolean success = false;

        if (mainActivity.isScanning()) {
            if ( ! scanInFlight ) {
                try {
                    //        mainActivity.bluetoothScan()
                    mainActivity.bluetoothScan();
                            //bluetoothManager.startScan();
                }
                catch (Exception ex) {
                    MainActivity.warn("exception starting bt scan: " + ex, ex);
                }
                if ( success ) {
                    scanInFlight = true;
                }
            }

            final long now = System.currentTimeMillis();
            if ( lastScanResponseTime < 0 ) {
                // use now, since we made a request
                lastScanResponseTime = now;
            } else {
                // are we seeing jams?
            }
        } else {
            // scanning is off. since we're the only timer, update the UI
            mainActivity.setNetCountUI();
            mainActivity.setLocationUI();
            mainActivity.setStatusUI("Scanning Turned Off" );
            // keep the scan times from getting huge
            scanRequestTime = System.currentTimeMillis();
            // reset this
            lastScanResponseTime = Long.MIN_VALUE;
        }

        // battery kill
        if ( ! mainActivity.isTransferring() ) {
            final SharedPreferences prefs = mainActivity.getSharedPreferences( ListFragment.SHARED_PREFS, 0 );
            long batteryKill = prefs.getLong(
                    ListFragment.PREF_BATTERY_KILL_PERCENT, MainActivity.DEFAULT_BATTERY_KILL_PERCENT);

            if ( mainActivity.getBatteryLevelReceiver() != null ) {
                final int batteryLevel = mainActivity.getBatteryLevelReceiver().getBatteryLevel();
                final int batteryStatus = mainActivity.getBatteryLevelReceiver().getBatteryStatus();
                // MainActivity.info("batteryStatus: " + batteryStatus);
                // give some time since starting up to change this configuration
                if ( batteryKill > 0 && batteryLevel > 0 && batteryLevel <= batteryKill
                        && batteryStatus != BatteryManager.BATTERY_STATUS_CHARGING
                        && (System.currentTimeMillis() - constructionTime) > 30000L) {
                    if (null != mainActivity) {
                        final String text = mainActivity.getString(R.string.battery_at) + " " + batteryLevel + " "
                                + mainActivity.getString(R.string.battery_postfix);
                        if (!mainActivity.isFinishing()) {
                            WiGLEToast.showOverActivity(mainActivity, R.string.error_general, text);
                        }
                        MainActivity.warn("low battery, shutting down");
                        mainActivity.speak(text);
                        mainActivity.finishSoon(4000L, false);
                    }
                }
            }
        }

        return success;
    }

    public long getScanPeriod() {
        //TODO: we should make this configurable through prefs!
        return 5000;
    }

    private Network addOrUpdateBt(final String bssid, final String ssid,
                                    final int frequency, /*final String networkTypeName*/final String capabilities,
                                    final int strength, final NetworkType type,
                                    final Location location, SharedPreferences prefs, final boolean batch) {

        //final String capabilities = networkTypeName + ";" + operator;

        final ConcurrentLinkedHashMap<String, Network> networkCache = MainActivity.getNetworkCache();
        final boolean showCurrent = prefs.getBoolean(ListFragment.PREF_SHOW_CURRENT, true);

        //ALIBI: addressing synchronization issues: if runNetworks syncset did not already contain this bssid
        //  AND the global ConcurrentLinkedHashMap network cache doesn't contain this key
        final boolean newForRun = runNetworks.add(bssid) && !networkCache.containsKey(bssid);

        Network network = networkCache.get(bssid);

        if (newForRun && network != null) {
            //ALIBI: sanity check used in debugging
            MainActivity.warn("runNetworks not working as expected (add -> true, but networkCache already contained)");
        }

        boolean deviceTypeUpdate = false;
        boolean btTypeUpdate = false;
        if (network == null) {
            //DEBUG: MainActivity.info("new BT net: "+bssid + "(new: "+newForRun+")");
            network = new Network(bssid, ssid, frequency, capabilities, strength, type);
            networkCache.put(bssid, network);
        } else if (NetworkType.BLE.equals(type) && NetworkType.BT.equals(network.getType())) {
            //ALIBI: detected via standard bluetooth, updated as LE (LE should win)
            //DEBUG: MainActivity.info("had a BC record, moving to BLE: "+network.getBssid()+ "(new: "+newForRun+")");
            String mergedSsid = (ssid == null || ssid.isEmpty()) ? network.getSsid() : ssid;
            int mergedDeviceType = (!isMiscOrUncategorized(network.getFrequency())?network.getFrequency():frequency);

            network.setSsid(mergedSsid);
            final int oldDevType = network.getFrequency();
            if (mergedDeviceType != oldDevType) {
                deviceTypeUpdate = true;
            }
            btTypeUpdate = true;
            network.setFrequency(mergedDeviceType);
            network.setLevel(strength);
            network.setType(NetworkType.BLE);
        } else if (NetworkType.BT.equals(type) && NetworkType.BLE.equals(network.getType())) {
            //fill in device type if not present
            //DEBUG: MainActivity.info("had a BLE record, got BC: "+network.getBssid() + "(new: "+newForRun+")");
            int mergedDeviceType = (!isMiscOrUncategorized(network.getFrequency())?network.getFrequency():frequency);
            final int oldDevType = network.getFrequency();
            if (mergedDeviceType != oldDevType) {
                deviceTypeUpdate = true;
            }
            network.setFrequency(mergedDeviceType);
            network.setLevel(strength);

            //fill in name if not present
            String mergedSsid = (ssid == null || ssid.isEmpty()) ? network.getSsid() : ssid;
            network.setSsid(mergedSsid);
        } else {
            //DEBUG: MainActivity.info("existing BT net: "+network.getBssid() + "(new: "+newForRun+")");
            //TODO: update capabilities? only if was Misc/Uncategorized, now recognized?
            //network.setCapabilities(capabilities);
            network.setLevel(strength);
            network.setFrequency(frequency);
            if (null != ssid) {
                network.setSsid(ssid);
            }

        }

        final boolean ssidSpeak = prefs.getBoolean(ListFragment.PREF_SPEAK_SSID, false)
                && !mainActivity.isMuted();

        if (newForRun) {
            // ALIBI: There are simply a lot of these - not sure this is practical
            /*if ( ssidSpeak ) {
                ssidSpeaker.add( network.getSsid() );
            }*/
        }
        //TODO: somethingAdded |= added;

        if ( location != null && (newForRun || network.getLatLng() == null) ) {
            // set the LatLng for mapping
            final LatLng LatLng = new LatLng( location.getLatitude(), location.getLongitude() );
            network.setLatLng( LatLng );
        }

        final Matcher ssidMatcher = FilterMatcher.getSsidFilterMatcher( prefs, ListFragment.FILTER_PREF_PREFIX );
        final Matcher bssidMatcher = mainActivity.getBssidFilterMatcher( ListFragment.PREF_EXCLUDE_DISPLAY_ADDRS );
        final Matcher bssidDbMatcher = mainActivity.getBssidFilterMatcher( ListFragment.PREF_EXCLUDE_LOG_ADDRS );

        //Update display
        if (listAdapter != null) {
            if (btTypeUpdate) {
                listAdapter.morphBluetoothToLe(network);
            }
            if ( showCurrent || newForRun ) {
                if ( FilterMatcher.isOk( ssidMatcher, bssidMatcher, prefs, ListFragment.FILTER_PREF_PREFIX, network ) ) {
                    if (batch) {
                        if (NetworkType.BT.equals(network.getType())) {
                            listAdapter.enqueueBluetooth(network);
                        } else if (NetworkType.BLE.equals(network.getType())) {
                            listAdapter.enqueueBluetoothLe(network);
                        }
                    } else {
                        if (NetworkType.BT.equals(network.getType())) {
                            listAdapter.addBluetooth(network);
                        } else if (NetworkType.BLE.equals(network.getType())) {
                            listAdapter.addBluetoothLe(network);
                        }
                    }
                }

            } else {
                network.setLevel(strength != Integer.MAX_VALUE?strength:-113);
            }
        }

        //Store to DB
        boolean matches = false;
        if (bssidDbMatcher != null) {
            bssidDbMatcher.reset(network.getBssid());
            matches = bssidDbMatcher.find();
        }
        if ( location != null ) {
            // w/ location
            if (!matches) {
                dbHelper.addObservation(network, location, newForRun, deviceTypeUpdate, btTypeUpdate);
            }
        } else {
            // bob asks "since BT are often indoors, should we be saving regardless of loc?"
            // w/out location
            if (!matches) {
                dbHelper.pendingObservation(network, newForRun, deviceTypeUpdate, btTypeUpdate);
            }
        }
        return network;
    }

    // check standard BT types undefined
    private boolean isMiscOrUncategorized(final int type) {
        if (type == 0 || type == 7936) {
            return true;
        }
        return false;
    }

}
