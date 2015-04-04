package net.grappendorf.caretaker.newdevice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import net.grappendorf.caretaker.R;

public class NewDeviceListActivity extends Activity
    implements net.grappendorf.caretaker.newdevice.NewDeviceListFragment.Callbacks {

  private boolean isTwoPaneLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_newdevice_list);

    if (findViewById(R.id.newdevice_detail_container) != null) {
      isTwoPaneLayout = true;

      ((net.grappendorf.caretaker.newdevice.NewDeviceListFragment) getFragmentManager()
          .findFragmentById(R.id.newdevice_list))
          .setActivateOnItemClick(true);
    }
  }

  @Override
  public void onItemSelected(String id) {
    if (isTwoPaneLayout) {
      Bundle arguments = new Bundle();
      arguments.putString(NewDeviceDetailFragment.ARG_ITEM_ID, id);
      NewDeviceDetailFragment fragment = new NewDeviceDetailFragment();
      fragment.setArguments(arguments);
      getFragmentManager().beginTransaction()
          .replace(R.id.newdevice_detail_container, fragment)
          .commit();

    } else {
      Intent detailIntent = new Intent(this, NewDeviceDetailActivity.class);
      detailIntent.putExtra(NewDeviceDetailFragment.ARG_ITEM_ID, id);
      startActivity(detailIntent);
    }
  }
}
