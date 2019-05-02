package app.com.miraz;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.MyHolder> {

    public static final String EXTRAS_DEVICE_NAME="DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS="DEVICE_ADDRESS";

    Context mContext;
    ArrayList<BluetoothDevice> mLeDevices;

    public RecycleViewAdapter(Context mContext, ArrayList<BluetoothDevice> mLeDevices) {
        this.mContext = mContext;
        this.mLeDevices = mLeDevices;
    }

    public RecycleViewAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // create a new view
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.devices_view_layout, viewGroup, false);
        MyHolder vh = new MyHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {

        final BluetoothDevice device = mLeDevices.get(i);
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0)
            myHolder.txtName.setText(deviceName);
        myHolder.txtAddress.setText(device.getAddress());

        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //final Intent intent = new Intent(mContext, DeviceControlActivity.class);
                //intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
                //intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());

                final Intent intent = new Intent(mContext, DeviceControlService.class);
                intent.putExtra(DeviceControlService.EXTRAS_DEVICE_NAME, device.getName());
                intent.putExtra(DeviceControlService.EXTRAS_DEVICE_ADDRESS, device.getAddress());

                mContext.startService(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mLeDevices.size();
//        return 10;
    }

    class MyHolder extends RecyclerView.ViewHolder {

        TextView txtName, txtAddress;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            txtName = (TextView) itemView.findViewById(R.id.txtName);
            txtAddress = (TextView) itemView.findViewById(R.id.txtAddress);
        }
    }
}
