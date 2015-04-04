package net.grappendorf.caretaker.newdevice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.iangclifton.android.floatlabel.FloatLabel;
import net.grappendorf.caretaker.AsyncFragment;
import net.grappendorf.caretaker.CaretakerApplication;
import net.grappendorf.caretaker.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NewDeviceDetailFragment extends AsyncFragment {

  private static final Map<String, Integer> deviceIcons;

  static {
    deviceIcons = new HashMap<String, Integer>();
    deviceIcons.put("Cipcam", R.drawable.camera);
    deviceIcons.put("Dimmer", R.drawable.slider);
    deviceIcons.put("DimmerRgb", R.drawable.slider);
    deviceIcons.put("Easyvr", R.drawable.speak);
    deviceIcons.put("ReflowOven", R.drawable.oven);
    deviceIcons.put("RemoteControl", R.drawable.button_red);
    deviceIcons.put("Switch", R.drawable.lightbulb_on);
    deviceIcons.put("Sensor", R.drawable.thermometer);
  }

  public static final String ARG_ITEM_ID = "item_id";

  private String caretakerSSID;

  private int caretakerNetId;

  private WifiInfo currentNet;

  private BroadcastReceiver wifiConnectReceiver;
  private BroadcastReceiver wifiScanReceiver;

  AsyncTask<Void, Void, Void> readDeviceInfoTask;
  AsyncTask<Void, Void, Void> configureDeviceTask;

  View rootView;

  private List<String> ssids;

  private int progressSteps;

  public NewDeviceDetailFragment() {
  }

  @Override
  public void onStart() {
    super.onStart();

    rootView = getActivity().getLayoutInflater().inflate(R.layout.fragment_newdevice_detail, null);

    ((FloatLabel) rootView.findViewById(R.id.wlan_phrase)).getEditText()
        .setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

    if (getArguments().containsKey(ARG_ITEM_ID)) {
      caretakerSSID = getArguments().getString(ARG_ITEM_ID);
      getActivity().setTitle("Device " + caretakerSSID);
    }

    progressSteps = 2;

    Toast.makeText(getActivity().getApplicationContext(),
        "Connecting WLAN to " + caretakerSSID, Toast.LENGTH_SHORT).show();

    if (caretakerSSID != null) {
      ((FloatLabel) rootView.findViewById(R.id.device_name)).getEditText().setText(caretakerSSID);
    }

    wifiConnectReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
        if (wifiInfo != null && wifiInfo.getSSID().equals("\"" + caretakerSSID + "\"") && networkInfo.isConnected()) {
          readDeviceInfo();
        }
      }
    };
    getActivity().registerReceiver(wifiConnectReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));

    final WifiManager wifi = ((net.grappendorf.caretaker.CaretakerApplication) getActivity().getApplication()).getWiFiManager(getActivity());
    currentNet = wifi.getConnectionInfo();
    WifiConfiguration wifiConfig = new WifiConfiguration();
    wifiConfig.SSID = "\"" + caretakerSSID + "\"";
    wifiConfig.preSharedKey = "\"" + CaretakerApplication.NEW_DEVICE_PASSPHRASE + "\"";
    wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
    caretakerNetId = wifi.addNetwork(wifiConfig);
    wifi.saveConfiguration();
    wifi.disconnect();
    wifi.enableNetwork(caretakerNetId, true);
    wifi.reconnect();

    rootView.findViewById(R.id.configure).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        configureDevice();
      }
    });

    setView(rootView);
  }

  private void readDeviceInfo() {
    class ReadDeviceInfoTask extends AsyncTask<Void, Void, Void> {
      @Override
      protected Void doInBackground(Void... params) {
        try {
          Socket client = new Socket();
          client.connect(new InetSocketAddress("192.168.0.1", 2000), 10000);
          BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
          if ("*HELLO*".equals(in.readLine())) {
            final String deviceMAC = in.readLine();
            final String deviceType = in.readLine();
            final String deviceDescription = in.readLine();
            getActivity().runOnUiThread(new Runnable() {
              @Override
              public void run() {
                ((ImageView) rootView.findViewById(R.id.device_icon)).setImageDrawable(getResources().getDrawable(deviceIcons.get(deviceType)));
                ((TextView) rootView.findViewById(R.id.device_description)).setText(deviceDescription);
                ((FloatLabel) rootView.findViewById(R.id.device_uuid)).getEditText().setText(deviceMAC);
              }
            });
          }
          client.close();
          progress();
        }
        catch (IOException e) {
          Log.e(net.grappendorf.caretaker.CaretakerApplication.LOG_TAG, e.getMessage());
        }
        return null;
      }
    }

    readDeviceInfoTask = new ReadDeviceInfoTask();
    readDeviceInfoTask.execute();

    final WifiManager wifi = ((net.grappendorf.caretaker.CaretakerApplication) getActivity().getApplication()).getWiFiManager(getActivity());

    wifiScanReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        List<ScanResult> results = wifi.getScanResults();
        ssids = new LinkedList<>();
        for (ScanResult result : results) {
          if (!result.SSID.isEmpty() && !result.SSID.startsWith("Caretaker-")) {
            ssids.add(result.SSID);
          }
        }
        getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            ((Spinner) getActivity().findViewById(R.id.wlan_ssid)).setAdapter(new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_spinner_dropdown_item,
                android.R.id.text1,
                ssids));

            progress();
          }
        });
      }
    };

    getActivity().registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    wifi.startScan();
  }

  private void configureDevice() {
    class ConfigureDeviceTask extends AsyncTask<Void, Void, Void> {
      @Override
      protected Void doInBackground(Void... params) {
        try {
          Socket client = new Socket();
          client.connect(new InetSocketAddress("192.168.0.1", 2000), 10000);
          BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
          PrintWriter out = new PrintWriter(client.getOutputStream(), true);
          if ("*HELLO*".equals(in.readLine())) {
            out.println(((FloatLabel) getView().findViewById(R.id.device_uuid)).getEditText().getText());
            out.println(((FloatLabel) getView().findViewById(R.id.device_name)).getEditText().getText());
            out.println(((Spinner) getView().findViewById(R.id.wlan_ssid)).getSelectedItem().toString().replace(" ", "$"));
            out.println(((FloatLabel) getView().findViewById(R.id.wlan_phrase)).getEditText().getText());
          }
          client.close();
          getActivity().finish();
        }
        catch (IOException e) {
          Log.e(net.grappendorf.caretaker.CaretakerApplication.LOG_TAG, e.getMessage());
        }
        return null;
      }
    }

    configureDeviceTask = new ConfigureDeviceTask();
    configureDeviceTask.execute();
  }

  @Override
  public void onPause() {
    super.onPause();

    if (readDeviceInfoTask != null) {
      readDeviceInfoTask.cancel(true);
      readDeviceInfoTask = null;
    }

    if (wifiConnectReceiver != null) {
      getActivity().unregisterReceiver(wifiConnectReceiver);
      wifiConnectReceiver = null;
    }

    if (wifiScanReceiver != null) {
      getActivity().unregisterReceiver(wifiScanReceiver);
      wifiScanReceiver = null;
    }

    if (currentNet != null) {
      Toast.makeText(getActivity().getApplicationContext(),
          "Reconnecting WLAN to " + currentNet.getSSID(), Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText(getActivity().getApplicationContext(),
          "Disconnecting WLAN from " + caretakerSSID, Toast.LENGTH_SHORT).show();
    }

    final WifiManager wifi = ((net.grappendorf.caretaker.CaretakerApplication) getActivity().getApplication()).getWiFiManager(getActivity());
    wifi.removeNetwork(caretakerNetId);
    if (currentNet != null) {
      wifi.enableNetwork(currentNet.getNetworkId(), false);
    }
    wifi.saveConfiguration();
    wifi.disconnect();
    wifi.reconnect();
  }

  private void progress() {
    --progressSteps;
    if (progressSteps == 0) {
      setContentShown(true);
    }
  }
}
