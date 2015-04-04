package net.grappendorf.caretaker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import net.grappendorf.caretaker.newdevice.NewDeviceListActivity;

public class MainActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        scan_for_new_devices();
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private void scan_for_new_devices() {
    startActivity(new Intent(this, NewDeviceListActivity.class));
  }
}
