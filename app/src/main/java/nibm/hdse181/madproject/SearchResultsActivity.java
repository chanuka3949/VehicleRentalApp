package nibm.hdse181.madproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchResultsActivity extends AppCompatActivity implements SearchResultListRecyclerViewAdapter.OnResultListener {

    List<Vehicle> vehicleList = new ArrayList<>();
    RecyclerView recyclerView;
    Location mLastKnownLocation;
    FusedLocationProviderClient mFusedLocationProviderClient;
    SearchResultListRecyclerViewAdapter viewAdapter;
    FirebaseFirestore db;
    List<String> distances = new ArrayList<>();
    List<GeoPoint> locations = new ArrayList<>();
    RequestQueue queue;
    int passengerCount = 0;
    String vehicleType = null;
    int passengers = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        passengerCount = intent.getIntExtra("passengerCount",0);
        vehicleType = intent.getStringExtra("vehicleType");

        queue = Volley.newRequestQueue(this);

        recyclerView = findViewById(R.id.searchResultRecyclerView);
        recyclerView.setHasFixedSize(true);
        viewAdapter = new SearchResultListRecyclerViewAdapter(getApplicationContext(),vehicleList,distances,this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(viewAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewAdapter.clearData();
        getDeviceLocation();
    }

    private void getVehicleData(){
        db.collection("Vehicles")
                .whereEqualTo("available",true)
                .whereEqualTo("type",vehicleType)
                .whereGreaterThanOrEqualTo("passengers",passengerCount)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                QuerySnapshot snapshot = task.getResult();
                if(!snapshot.isEmpty()){

                    Log.d("Check","Data Received");
                    for (DocumentSnapshot documentSnapshot:snapshot) {
                        Vehicle vehicle = new Vehicle();
//                        vehicleList.add(documentSnapshot.toObject(Vehicle.class));
                        vehicle.setTitle((String) documentSnapshot.get("title"));
                        vehicle.setImageURI((String) documentSnapshot.get("imageURI"));
                        vehicle.setType((String) documentSnapshot.get("type"));
                        vehicle.setOwner((String) documentSnapshot.get("owner"));
                        vehicle.setPassengers(Integer.parseInt(documentSnapshot.get("passengers").toString()));
                        vehicle.setId((String) documentSnapshot.get("id"));

                        vehicleList.add(vehicle);

                        locations.add((GeoPoint) documentSnapshot.get("location"));
                    }
                    getDistances();

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Check",e.getMessage());
            }
        });
    }

    private void initializeAdapter(){
        /*for(DocumentSnapshot documentSnapshot : task.getResult()){
            vehicleList.add(documentSnapshot.toObject(Vehicle.class));
        }*/
        Log.d("Check","Initialized Adapter");
        viewAdapter = new SearchResultListRecyclerViewAdapter(getApplicationContext(),vehicleList,distances,this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(viewAdapter);
        Log.d("Check","Adapter Set Complete");
    }

    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Task<android.location.Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if(task.isSuccessful()){
                        mLastKnownLocation = task.getResult();
                        Log.d("Check", "Got device location");
                        if(mLastKnownLocation != null){
                            getVehicleData();
                        }else{
                            Toast.makeText(SearchResultsActivity.this, "Location Services Required", Toast.LENGTH_SHORT).show();
                            return;
                        }

                    }
                }
            });
        }catch(Exception e) {
        }
    }

    private void getDistances(){
        StringBuilder builder = new StringBuilder();
        Resources resources = this.getResources();
        builder.append("https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=");
        builder.append(mLastKnownLocation.getLatitude()+","+mLastKnownLocation.getLongitude()+"&destinations=");
        for (GeoPoint point:locations) {
            builder.append(point.getLatitude());
            builder.append(",");
            builder.append(point.getLongitude());
            builder.append("|");
        }
        builder.deleteCharAt(builder.length()-1);
        builder.append("&key=");
        builder.append(resources.getString(R.string.google_maps_key));
        Log.d("Check", "API CALL URL : "+ builder);

        StringRequest request = new StringRequest(Request.Method.GET, builder.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try{
                    JSONObject jsonObject = new JSONObject(response);
                    for(int i = 0; i <locations.size(); i++){
//                        int length = jsonObject.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(i).getJSONObject("distance").getString("text").length();
//                        distances.add(Float.valueOf(jsonObject.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(i).getJSONObject("distance").getString("text").substring(0,length-2).trim()));
                        distances.add(jsonObject.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(i).getJSONObject("distance").getString("text"));
                        Log.d("Check", String.valueOf(distances.get(i)));
                        initializeAdapter();

                    }
                }catch (Exception e){
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Check","response error");
            }
        });
        queue.add(request);
    }
    @Override
    public void onResultClick(int position) {
        Vehicle vehicle = vehicleList.get(position);
        Intent intent = new Intent(this, ViewSearchResultActivity.class);
        intent.putExtra("title",vehicle.getTitle());
        intent.putExtra("type",vehicle.getType());
        intent.putExtra("passengers",vehicle.getPassengers());
        intent.putExtra("owner",vehicle.getOwner());
        intent.putExtra("image",vehicle.getImageURI());
        vehicleList.clear();
        distances.clear();
        startActivity(intent);
    }

}
