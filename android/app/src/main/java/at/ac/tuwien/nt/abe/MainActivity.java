package at.ac.tuwien.nt.abe;

import android.annotation.SuppressLint;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import at.ac.tuwien.nt.abe.adapters.TabAdapter;
import at.ac.tuwien.nt.abe.fragments.MeasurementFragment;
import at.ac.tuwien.nt.abe.fragments.ResultsFragment;
import at.ac.tuwien.nt.abe.fragments.SettingsFragment;
import at.ac.tuwien.nt.abe.services.NetworkInfoService;

public class MainActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private TabAdapter tabAdapter;

    static {
        /*
            The loadLibrary call in the static initializer is responsible for
            discovering and loading the shared library.
         */
        System.loadLibrary("measurement_client_rust");
    }

    private NetworkInfoService networkInfoService;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        this.tabAdapter = new TabAdapter(getSupportFragmentManager());

        if(savedInstanceState != null) {
            tabAdapter.addFragment(getSupportFragmentManager().getFragment(savedInstanceState,"settingsFragment"), "Settings");
            tabAdapter.addFragment(getSupportFragmentManager().getFragment(savedInstanceState,"measurementFragment"), "Measurement");
            tabAdapter.addFragment(getSupportFragmentManager().getFragment(savedInstanceState,"resultsFragment"), "Results");
        } else {
            MeasurementFragment measurementFragment = new MeasurementFragment();
            SettingsFragment settingsFragment = new SettingsFragment();

            tabAdapter.addFragment(settingsFragment, "Settings");
            tabAdapter.addFragment(measurementFragment, "Measurement");
            tabAdapter.addFragment(new ResultsFragment(), "Results");
        }

        viewPager.setAdapter(tabAdapter);
        viewPager.setCurrentItem(1);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (tabAdapter.getItem(0) != null && tabAdapter.getItem(0).isAdded()) {
            getSupportFragmentManager().putFragment(outState, "settingsFragment", tabAdapter.getItem(0));
        }

        if (tabAdapter.getItem(1) != null  && tabAdapter.getItem(1).isAdded()) {
            getSupportFragmentManager().putFragment(outState, "measurementFragment", tabAdapter.getItem(1));
        }

        if (tabAdapter.getItem(2) != null && tabAdapter.getItem(2).isAdded()) {
            getSupportFragmentManager().putFragment(outState, "resultsFragment", tabAdapter.getItem(2));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

        this.networkInfoService = new NetworkInfoService();
        this.registerReceiver(networkInfoService, filter);
    }

    @Override
    protected  void onPause() {
        super.onPause();

        this.unregisterReceiver(networkInfoService);
    }
}
