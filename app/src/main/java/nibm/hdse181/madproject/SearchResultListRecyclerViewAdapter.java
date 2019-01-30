package nibm.hdse181.madproject;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class SearchResultListRecyclerViewAdapter extends RecyclerView.Adapter<SearchResultListRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "SearchResultAdapter";

    Context context;
    List<Vehicle> vehicleList;
    List<String> distances;
    OnResultListener onResultListener;

    public SearchResultListRecyclerViewAdapter(Context context, List<Vehicle> vehicleList, List<String> distances, OnResultListener onResultListener) {
        this.context = context;
        this.vehicleList = vehicleList;
        this.distances = distances;
        this.onResultListener = onResultListener;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_result_cardview,viewGroup,false);
        return new ViewHolder(view,onResultListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Vehicle vehicle = vehicleList.get(i);
        String distance = distances.get(i);
        viewHolder.title.setText(vehicle.getTitle());
        Picasso.get().load(vehicle.getImageURI()).fit().centerCrop().into(viewHolder.vehicleImage);
        viewHolder.distance.setText(distance);
    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView title;
        TextView distance;
        ImageView vehicleImage;
        CardView cardView;
        OnResultListener onResultListener;


        public ViewHolder(@NonNull View itemView,OnResultListener onResultListener) {
            super(itemView);

            title = itemView.findViewById(R.id.txtSearchResultTitle);
            distance = itemView.findViewById(R.id.resultDistance);
            vehicleImage = itemView.findViewById(R.id.resultVehicleImage);
            cardView = itemView.findViewById(R.id.card_view_search_result);
            this.onResultListener = onResultListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //To do
            try{
                onResultListener.onResultClick(getAdapterPosition());
            }catch (Exception e){
                return;
            }
        }
    }

    public interface OnResultListener{
        void onResultClick(int position);
    }

    public void clearData(){
        if(vehicleList.size() > 0){
            vehicleList.clear();
            distances.clear();
        }
    }
}

