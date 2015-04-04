package net.grappendorf.caretaker.newdevice;

import android.app.Activity;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import net.grappendorf.caretaker.R;

import java.util.LinkedList;
import java.util.List;

public class NewDeviceListFragment extends ListFragment {

  private static final String STATE_ACTIVATED_POSITION = "activated_position";

  private Callbacks callbacks = dummyCallbacks;

  private int activatedPosition = ListView.INVALID_POSITION;

  public interface Callbacks {
    void onItemSelected(String id);
  }

  private static Callbacks dummyCallbacks = new Callbacks() {
    @Override
    public void onItemSelected(String id) {
    }
  };

  private List<String> caretakerSSIDs;

  private BroadcastReceiver wifiScanReceiver;

  public NewDeviceListFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final WifiManager wifi = ((net.grappendorf.caretaker.CaretakerApplication) getActivity().getApplication()).getWiFiManager(getActivity());
    if (!wifi.isWifiEnabled()) {
      Toast.makeText(getActivity().getApplicationContext(),
          "WiFi is disabled...making it enabled", Toast.LENGTH_LONG).show();
      wifi.setWifiEnabled(true);
    }

    wifiScanReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        List<ScanResult> results = wifi.getScanResults();
        caretakerSSIDs = new LinkedList<>();
        for (ScanResult result : results) {
          if (result.SSID.startsWith("Caretaker-")) {
            caretakerSSIDs.add(result.SSID);
          }
        }
        setListAdapter(new ArrayAdapter<>(
            getActivity(),
            R.layout.newdevice_list_item,
            android.R.id.text1,
            caretakerSSIDs));
      }
    };
    getActivity().registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

    Toast.makeText(getActivity().getApplicationContext(),
        "Scanning WLAN for Caretaker Devices", Toast.LENGTH_LONG).show();

    wifi.startScan();
  }

  @Override
  public void onStop() {
    super.onStop();
    if (wifiScanReceiver != null) {
      getActivity().unregisterReceiver(wifiScanReceiver);
      wifiScanReceiver = null;
    }
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    if (savedInstanceState != null
        && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
      setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
    }
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    callbacks = (Callbacks) activity;
  }

  @Override
  public void onDetach() {
    super.onDetach();

    callbacks = dummyCallbacks;
  }

  @Override
  public void onListItemClick(ListView listView, View view, int position, long id) {
    super.onListItemClick(listView, view, position, id);

    callbacks.onItemSelected(caretakerSSIDs.get(position));
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (activatedPosition != ListView.INVALID_POSITION) {
      outState.putInt(STATE_ACTIVATED_POSITION, activatedPosition);
    }
  }

  public void setActivateOnItemClick(boolean activateOnItemClick) {
    getListView().setChoiceMode(activateOnItemClick
        ? ListView.CHOICE_MODE_SINGLE
        : ListView.CHOICE_MODE_NONE);
  }

  private void setActivatedPosition(int position) {
    if (position == ListView.INVALID_POSITION) {
      getListView().setItemChecked(activatedPosition, false);
    } else {
      getListView().setItemChecked(position, true);
    }

    activatedPosition = position;
  }
}
