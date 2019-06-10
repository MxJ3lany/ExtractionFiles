package io.plactal.eoscommander.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import javax.inject.Inject;

import io.plactal.eoscommander.R;
import io.plactal.eoscommander.data.EoscDataManager;
import io.plactal.eoscommander.data.remote.model.types.TypeSymbol;
import io.plactal.eoscommander.ui.base.BaseActivity;
import io.plactal.eoscommander.ui.settings.SettingsActivity;
import io.plactal.eoscommander.util.StringUtils;
import timber.log.Timber;


public class MainActivity extends BaseActivity {
    private static final int REQ_OPEN_CONNECTION_INFO = 10;

    @Inject
    EoscDataManager mDataManager;

    @Inject
    CmdPagerAdapter mPagerAdapter;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // di
        getActivityComponent().inject(this);

        setContentView(R.layout.activity_main);
        setToolbarConfig(R.id.toolbar, false);
        TabLayout tabLayout = findViewById(R.id.tabs);

        tabLayout.addTab(tabLayout.newTab().setText( R.string.wallet));
        tabLayout.addTab(tabLayout.newTab().setText( R.string.account ));
        tabLayout.addTab(tabLayout.newTab().setText( R.string.transfer ));
        tabLayout.addTab(tabLayout.newTab().setText( R.string.currency ));
        tabLayout.addTab(tabLayout.newTab().setText( R.string.push ));
        tabLayout.addTab(tabLayout.newTab().setText( R.string.get_table));
        tabLayout.setTabGravity(TabLayout.MODE_SCROLLABLE);

        mViewPager =findViewById(R.id.container);
        mPagerAdapter.setTabCount( tabLayout.getTabCount());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                if ( null != mPagerAdapter.getFragment( position ) ) {
                    mPagerAdapter.getFragment( position ).onSelected();
                }
            }

            @Override public void onPageScrollStateChanged(int state) {}
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {
                hideKeyboard();
            }

            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });


        mDataManager.clearAbiObjects();

        if (StringUtils.isEmpty( mDataManager.getPreferenceHelper().getNodeosConnInfo( null, null)) ) {
            openSettingsActivity();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void openSettingsActivity() {
        startActivityForResult( new Intent(this, SettingsActivity.class), REQ_OPEN_CONNECTION_INFO);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            openSettingsActivity();
            return true;
        }else if (id == R.id.action_about) {
            startActivity( new Intent( this, InfoActivity.class) );
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( requestCode == REQ_OPEN_CONNECTION_INFO ){
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
