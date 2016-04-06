package com.example.aaron.talker;

import android.app.ListFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DeviceListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DeviceListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DeviceListFragment extends ListFragment {


    public interface OnDeviceSelectedListener{

        public void deviceSelected(int position);

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.layout.simple_expandable_list_item_1 : android.R.layout.simple_expandable_list_item_1;



        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), layout, MainActivity.mBTdeviceNameList);

        setListAdapter(adapter);


    }
}
