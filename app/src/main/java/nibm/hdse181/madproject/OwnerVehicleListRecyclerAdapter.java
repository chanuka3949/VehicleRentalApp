package nibm.hdse181.madproject;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import java.util.List;

public class OwnerVehicleListRecyclerAdapter extends RecyclerView.Adapter<OwnerVehicleListRecyclerAdapter.ViewHolder> {

    private static final String TAG = "VehicleListViewAdapter";

    private Context context;
    private List<Vehicle> vehicleList;
    private OnVehicleListener onVehicleListener;

    public OwnerVehicleListRecyclerAdapter(Context context, List<Vehicle> vehicleList, OnVehicleListener onVehicleListener) {
        this.context = context;
        this.vehicleList = vehicleList;
        this.onVehicleListener = onVehicleListener;
        Log.d("ABC","Inside Constructor");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Log.d("ABC","Inside OnCreateViewHolder");

        View view = LayoutInflater.from(context).inflate(R.layout.owner_vehicle_cardview,viewGroup,false);
        return new ViewHolder(view,onVehicleListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Log.d("ABC","Inside OnBind Method");
        Vehicle vehicle = vehicleList.get(i);
        Log.d("ABC", vehicle.getOwner());
        Log.d("ABC",vehicle.getTitle());
        viewHolder.vehicleTitle.setText(vehicle.getTitle());
        viewHolder.availabilitySwitch.setChecked(vehicle.isAvailable());
    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView vehicleTitle;
        Switch availabilitySwitch;
        CardView cardView;
        OnVehicleListener onVehicleListener;

        public ViewHolder(@NonNull View itemView , OnVehicleListener onVehicleListener) {
            super(itemView);

            vehicleTitle = itemView.findViewById(R.id.txtVehicleTitle);
            availabilitySwitch = itemView.findViewById(R.id.switchAvailability);
            cardView = itemView.findViewById(R.id.owner_vehicle_cardView);
            this.onVehicleListener = onVehicleListener;

            itemView.setOnClickListener(this);
            availabilitySwitch.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            try{
                if(v.getId() == R.id.owner_vehicle_cardView){
                    onVehicleListener.onVehicleClick(getAdapterPosition());
                    Log.d(TAG,"Item Clicked");
                }else if(v.getId() == R.id.switchAvailability){
                    onVehicleListener.onAvailabilityClick(getAdapterPosition(),availabilitySwitch.isChecked());
                    Log.d(TAG, "Clicked");
                }
            }catch (Exception e){
                return;
            }
        }
    }

    public interface OnVehicleListener{
        void onVehicleClick(int position);
        void onAvailabilityClick(int position, boolean checked);
    }

    public void clearData(){
        if(vehicleList.size() > 0){
            vehicleList.clear();
        }
    }
}
