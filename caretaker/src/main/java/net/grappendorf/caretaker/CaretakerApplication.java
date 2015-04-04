package net.grappendorf.caretaker;

import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;

public class CaretakerApplication extends Application {

  public static final String LOG_TAG = "CARETAKER";

  public static final String NEW_DEVICE_PASSPHRASE = "0347342d";

  public boolean isRunningInEmulator() {
    return "goldfish".equals(Build.HARDWARE);
  }

  public WifiManager getWiFiManager(Context context) {
    if (isRunningInEmulator()) {
    }

    return (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
  }
}
