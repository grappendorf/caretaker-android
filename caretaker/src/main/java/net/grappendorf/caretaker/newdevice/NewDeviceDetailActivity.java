package net.grappendorf.caretaker.newdevice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import net.grappendorf.caretaker.R;

public class NewDeviceDetailActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_newdevice_detail);

    if (getActionBar() != null) {
      getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    if (savedInstanceState == null) {
      Bundle arguments = new Bundle();
      arguments.putString(NewDeviceDetailFragment.ARG_ITEM_ID,
          getIntent().getStringExtra(NewDeviceDetailFragment.ARG_ITEM_ID));
      NewDeviceDetailFragment fragment = new NewDeviceDetailFragment();
      fragment.setArguments(arguments);
      getFragmentManager().beginTransaction()
          .add(R.id.newdevice_detail_container, fragment)
          .commit();
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == android.R.id.home) {
      navigateUpTo(new Intent(this, net.grappendorf.caretaker.newdevice.NewDeviceListActivity.class));
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
