package com.example.aaron.talker;

import android.app.ListFragment;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;


public class DeviceListFragment extends ListFragment {

    public ArrayList<BluetoothDevice> mBTdeviceList;
    public ArrayList<String> mBTdeviceNameList;
    ArrayAdapter<String> adapter;





    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBTdeviceList = new ArrayList<>();
        mBTdeviceNameList = new ArrayList<>();
    }

    public void setAdapter(){
        int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.layout.simple_expandable_list_item_1 : android.R.layout.simple_expandable_list_item_1;
        adapter = new ArrayAdapter<>(getActivity(), layout, mBTdeviceNameList);
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id){

    }
}
