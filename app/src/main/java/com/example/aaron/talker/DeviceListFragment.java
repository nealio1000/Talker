package com.example.aaron.talker;

import android.app.ListFragment;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;


public class DeviceListFragment extends ListFragment {

    public ArrayList<BluetoothDevice> mBTdeviceList;
    public ArrayList<String> mBTdeviceNameList;
    private OnDeviceSelectedListener mDeviceSelectedListener;

    ArrayAdapter<String> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBTdeviceList = new ArrayList<>();
        mBTdeviceNameList = new ArrayList<>();
    }

    @Override
    public void onStart() {
        super.onStart();

        // When in two-pane layout, set the listview to highlight the selected list item
        // (We do this during onStart because at the point the listview is available.)
        if (getFragmentManager().findFragmentById(R.id.list) != null) {
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }
    }

    public void addDevice(BluetoothDevice device){
        mBTdeviceList.add(device);
    }

    public void addDeviceName(String deviceName){
        mBTdeviceNameList.add(deviceName);
    }

    public void setAdapter(){
        int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.layout.simple_expandable_list_item_1 : android.R.layout.simple_expandable_list_item_1;
        setListAdapter(new ArrayAdapter<>(getActivity(), layout, mBTdeviceNameList));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDeviceSelectedListener) {
            mDeviceSelectedListener = (OnDeviceSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDeviceSelectedListener = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id){
        String selectedValue = (String) getListAdapter().getItem(position);

        // Notify the parent activity of selected item
        mDeviceSelectedListener.onDeviceSelected(position, selectedValue);

        // Set the item as checked to be highlighted when in two-pane layout
        getListView().setItemChecked(position, true);

    }

    public interface OnDeviceSelectedListener {
        /**
         * Called by DeviceListFragment fragment when a list item is selected
         */
        void onDeviceSelected(int position, String deviceName);
    }
}
