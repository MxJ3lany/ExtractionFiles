package net.wigle.wigleandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import net.wigle.wigleandroid.listener.GPSListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class DashboardFragment extends Fragment {
  private final Handler timer = new Handler();
  private AtomicBoolean finishing;
  private NumberFormat numberFormat;
  private ScrollView scrollView;
  private View landscape;
  private View portrait;

  private static final int MENU_EXIT = 11;
  private static final int MENU_SETTINGS = 12;

  /** Called when the activity is first created. */
  @Override
  public void onCreate( final Bundle savedInstanceState ) {
    MainActivity.info("DASH: onCreate");
    super.onCreate( savedInstanceState );
    setHasOptionsMenu(true);
    // set language
    MainActivity.setLocale( getActivity() );

    // media volume
    getActivity().setVolumeControlStream( AudioManager.STREAM_MUSIC );

    finishing = new AtomicBoolean( false );
    numberFormat = NumberFormat.getNumberInstance( Locale.US );
    if ( numberFormat instanceof DecimalFormat ) {
      numberFormat.setMinimumFractionDigits(2);
      numberFormat.setMaximumFractionDigits(2);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    final int orientation = getResources().getConfiguration().orientation;
    MainActivity.info("DASH: onCreateView. orientation: " + orientation);
    scrollView = (ScrollView) inflater.inflate(R.layout.dash, container, false);
    landscape = inflater.inflate(R.layout.dashlandscape, container, false);
    portrait = inflater.inflate(R.layout.dashportrait, container, false);

    switchView();

    return scrollView;
  }

  private void switchView() {
    if (scrollView != null) {
      final int orientation = getResources().getConfiguration().orientation;
      View component = portrait;
      if (orientation == 2) {
        component = landscape;
      }
      scrollView.removeAllViews();
      scrollView.addView(component);
    }
  }

  private final Runnable mUpdateTimeTask = new Runnable() {
    @Override
    public void run() {
        // make sure the app isn't trying to finish
        if ( ! finishing.get() ) {
          final View view = getView();
          if (view != null) {
            updateUI( view );
          }

          final long period = 1000L;
          // info("wifitimer: " + period );
          timer.postDelayed( this, period );
        }
        else {
          MainActivity.info( "finishing mapping timer" );
        }
    }
  };

  private void setupTimer() {
    timer.removeCallbacks( mUpdateTimeTask );
    timer.postDelayed( mUpdateTimeTask, 250 );
  }

  private void updateUI( final View view ) {

        View topBar =  view.findViewById( R.id.dash_status_bar );
        if (MainActivity.isScanning(getActivity())) {
            topBar.setVisibility(View.GONE);
        } else {
            topBar.setVisibility(View.VISIBLE);
            TextView dashScanstatus = view.findViewById(R.id.dash_scanstatus);
            dashScanstatus.setText(getString(R.string.dash_scan_off));
        }

        TextView tv = (TextView) view.findViewById( R.id.runnets );
        tv.setText( (ListFragment.lameStatic.runNets + ListFragment.lameStatic.runBt )+ " ");

        tv = (TextView) view.findViewById( R.id.runcaption );
        tv.setText( (getString(R.string.run)));

        tv = (TextView) view.findViewById( R.id.newwifi );
        tv.setText( ListFragment.lameStatic.newWifi + " " );

        tv = (TextView) view.findViewById( R.id.newbt );
        tv.setText( ListFragment.lameStatic.newBt + " " );

        tv = (TextView) view.findViewById( R.id.currnets );
        tv.setText( getString(R.string.dash_vis_nets) + " " + ListFragment.lameStatic.currNets );

        tv = (TextView) view.findViewById( R.id.newNetsSinceUpload );
        tv.setText( getString(R.string.dash_new_upload) + " " + newNetsSinceUpload() );

        tv = (TextView) view.findViewById( R.id.newcells );
        tv.setText( ListFragment.lameStatic.newCells + " ");

        updateDist( view, R.id.rundist, ListFragment.PREF_DISTANCE_RUN, getString(R.string.dash_dist_run) );
        updateTime(view, R.id.run_dur, ListFragment.PREF_STARTTIME_RUN );
        updateTimeTare(view, R.id.scan_dur, ListFragment.PREF_CUMULATIVE_SCANTIME_RUN,
                ListFragment.PREF_STARTTIME_RUN, MainActivity.isScanning(getActivity()));
        updateDist( view, R.id.totaldist, ListFragment.PREF_DISTANCE_TOTAL, getString(R.string.dash_dist_total) );
        updateDist( view, R.id.prevrundist, ListFragment.PREF_DISTANCE_PREV_RUN, getString(R.string.dash_dist_prev) );

        tv = (TextView) view.findViewById( R.id.queuesize );
        tv.setText( getString(R.string.dash_db_queue) + " " + ListFragment.lameStatic.preQueueSize );

        tv = (TextView) view.findViewById( R.id.dbNets );
        tv.setText( getString(R.string.dash_db_nets) + " " + ListFragment.lameStatic.dbNets );

        tv = (TextView) view.findViewById( R.id.dbLocs );
        tv.setText( getString(R.string.dash_db_locs) + " " + ListFragment.lameStatic.dbLocs );

        tv = (TextView) view.findViewById( R.id.gpsstatus );
        Location location = ListFragment.lameStatic.location;

        tv.setText( getString(R.string.dash_short_loc) + " ");

        TextView fixMeta = view.findViewById(R.id.fixmeta);
        TextView conType = view.findViewById(R.id.contype);
        TextView conCount = view.findViewById(R.id.concount);

        ImageView iv = (ImageView) view.findViewById(R.id.fixtype);
        if (location == null) {
            tv.setTextColor(Color.RED);
            iv.setImageResource(R.drawable.gpsnone);
            iv.setVisibility(View.VISIBLE);
            iv.setColorFilter(Color.argb(255, 255, 0, 0));
            fixMeta.setVisibility(View.INVISIBLE);
            conType.setVisibility(View.INVISIBLE);
            conCount.setVisibility(View.INVISIBLE);
        } else {
            if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {

                String satString = null;
                String conKeyString = null;
                String conValString = null;
                if (MainActivity.getMainActivity() != null && MainActivity.getMainActivity().getGPSListener() != null) {
                    final GPSListener listener = MainActivity.getMainActivity().getGPSListener();
                    satString = "("+listener.getSatCount()+")";
                    conKeyString = MainActivity.join("\n", listener.getConstellations().keySet());
                    conValString = MainActivity.join("\n", listener.getConstellations().values());
                }
                if (satString == null) {
                    fixMeta.setVisibility(View.INVISIBLE);
                } else {
                    fixMeta.setTextColor(Color.GREEN);
                    fixMeta.setVisibility(View.VISIBLE);
                    fixMeta.setText(satString);
                }
                tv.setTextColor(Color.GREEN);
                iv.setImageResource(R.drawable.gps);
                iv.setColorFilter(Color.GREEN);
                iv.setVisibility(View.VISIBLE);

                if (conKeyString == null) {
                    conType.setVisibility(View.INVISIBLE);
                    conCount.setVisibility(View.INVISIBLE);
                } else {
                    conType.setVisibility(View.VISIBLE);
                    conType.setText(conKeyString);

                    conCount.setVisibility(View.VISIBLE);
                    conCount.setText(conValString);
                }

            } else if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
                fixMeta.setVisibility(View.INVISIBLE);
                tv.setTextColor(Color.YELLOW);
                iv.setImageResource(R.drawable.wifi);
                iv.setVisibility(View.VISIBLE);
                iv.setColorFilter(Color.YELLOW);
            } else if (location.getProvider().equals(LocationManager.PASSIVE_PROVIDER)) {
                fixMeta.setVisibility(View.INVISIBLE);
                tv.setTextColor(Color.parseColor("#FFA500"));
                iv.setImageResource(R.drawable.cell);
                iv.setVisibility(View.VISIBLE);
                iv.setColorFilter(Color.parseColor("#FFA500"));
            } else {
                //ALIBI: fall back on string version
                fixMeta.setVisibility(View.INVISIBLE);
                tv.setTextColor(Color.parseColor("#AAA"));
                iv.setVisibility(View.GONE);
                tv.setText( getString(R.string.dash_short_loc) + " "+location.getProvider());
                iv.setColorFilter(Color.parseColor("#AAA"));
            }
        }

  }

  private long newNetsSinceUpload() {
    final SharedPreferences prefs = getActivity().getSharedPreferences( ListFragment.SHARED_PREFS, 0 );
    final long marker = prefs.getLong( ListFragment.PREF_DB_MARKER, 0L );
    final long uploaded = prefs.getLong( ListFragment.PREF_NETS_UPLOADED, 0L );
    long newSinceUpload = 0;
    // marker is set but no uploaded, a migration situation, so return zero
    if (marker == 0 || uploaded != 0) {
      newSinceUpload = ListFragment.lameStatic.dbNets - uploaded;
      if ( newSinceUpload < 0 ) {
        newSinceUpload = 0;
      }
    }
    return newSinceUpload;
  }

  private void updateDist( final View view, final int id, final String pref, final String title ) {
    final SharedPreferences prefs = getActivity().getSharedPreferences( ListFragment.SHARED_PREFS, 0 );

    float dist = prefs.getFloat( pref, 0f );
    final String distString = metersToString( numberFormat, getActivity(), dist, false );
    final TextView tv = (TextView) view.findViewById( id );
    tv.setText( title + " " + distString );
  }

  private void updateTime( final View view, final int id, final String pref) {
    final SharedPreferences prefs = getActivity().getSharedPreferences( ListFragment.SHARED_PREFS, 0 );

    long millis = System.currentTimeMillis();
    long duration =  millis - prefs.getLong( pref,  millis);

    final String durString = timeString(duration);

    final TextView tv = (TextView) view.findViewById( id );
    tv.setText( durString );
  }

  private void updateTimeTare(final View view, final int id, final String prefCumulative,
                              final String prefCurrent, final boolean isScanning) {
    final SharedPreferences prefs = getActivity().getSharedPreferences( ListFragment.SHARED_PREFS, 0 );

    long cumulative = prefs.getLong(ListFragment.PREF_CUMULATIVE_SCANTIME_RUN, 0L);

    if (isScanning) {
      cumulative += System.currentTimeMillis() - prefs.getLong(ListFragment.PREF_STARTTIME_CURRENT_SCAN, System.currentTimeMillis());
    }

    final String durString = timeString(cumulative);
    final TextView tv = (TextView) view.findViewById( id );
    tv.setText(durString );

  }

  public static String metersToString(final NumberFormat numberFormat, final Context context, final float meters,
      final boolean useShort ) {
    final SharedPreferences prefs = context.getSharedPreferences( ListFragment.SHARED_PREFS, 0 );
    final boolean metric = prefs.getBoolean( ListFragment.PREF_METRIC, false );

    String retval;
    if ( meters > 3000f ) {
      if ( metric ) {
        retval = numberFormat.format( meters / 1000f ) + " " + context.getString(R.string.km_short);
      }
      else {
        retval = numberFormat.format( meters / 1609.344f ) + " " +
            (useShort ? context.getString(R.string.mi_short) : context.getString(R.string.miles));
      }
    }
    else if ( metric ){
      retval = numberFormat.format( meters ) + " " +
          (useShort ? context.getString(R.string.m_short) : context.getString(R.string.meters));
    }
    else {
      retval = numberFormat.format( meters * 3.2808399f  ) + " " +
          (useShort ? context.getString(R.string.ft_short) : context.getString(R.string.feet));
    }
    return retval;
  }

  @Override
  public void onDestroy() {
    MainActivity.info( "DASH: onDestroy" );
    finishing.set( true );

    super.onDestroy();
  }

  @Override
  public void onResume() {
    MainActivity.info( "DASH: onResume" );
    super.onResume();
    setupTimer();
    getActivity().setTitle(R.string.dashboard_app_name);
  }

  @Override
  public void onStart() {
    MainActivity.info( "DASH: onStart" );
    super.onStart();
  }

  @Override
  public void onPause() {
    MainActivity.info( "DASH: onPause" );
    super.onPause();
  }

  @Override
  public void onStop() {
    MainActivity.info( "DASH: onStop" );
    super.onStop();
  }

  @Override
  public void onConfigurationChanged( final Configuration newConfig ) {
    MainActivity.info( "DASH: config changed" );
    switchView();
    super.onConfigurationChanged( newConfig );
  }

  /* Creates the menu items */
  @Override
  public void onCreateOptionsMenu (final Menu menu, final MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
  }

  /* Handles item selections */
  @Override
  public boolean onOptionsItemSelected( final MenuItem item ) {
      return false;
  }

  private String timeString(final long duration) {
    //TODO: better to just use TimeUnit?
    int seconds = (int) (duration / 1000) % 60 ;
    int minutes = (int) ((duration / (1000*60)) % 60);
    int hours   = (int) ((duration / (1000*60*60)) % 24);
    String durString = String.format("%02d", minutes)+":"+String.format("%02d", seconds);
    if (hours > 0) {
      durString = String.format("%d", hours) + ":" + durString;
    }
    return " " +durString;
  }

}
