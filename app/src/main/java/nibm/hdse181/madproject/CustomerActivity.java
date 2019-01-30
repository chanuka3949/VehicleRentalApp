package nibm.hdse181.madproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class CustomerActivity extends AppCompatActivity {

    Spinner passengers, types;
    Button search;
    FirebaseFirestore db;
    List<Integer> passengerCounts;
    List<String> vehicleTypes;
    ArrayAdapter<Integer> adapterPassengerCount;
    ArrayAdapter<String> adapterVehicleTypes;
    int maxCount = 1;
    SweetAlertDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);

        db = FirebaseFirestore.getInstance();
        passengers = findViewById(R.id.spinnerPassengers);
        types = findViewById(R.id.spinnerType);
        search = findViewById(R.id.btnSearch);

        progressBar = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        progressBar.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        progressBar.setTitleText("Loading");
        progressBar.setCancelable(false);

        getMaxPassengerCount();

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToResultsPage();
            }
        });
    }

    private void goToResultsPage(){
        Intent intent = new Intent(this,SearchResultsActivity.class);
        intent.putExtra("passengerCount",Integer.valueOf(passengers.getSelectedItem().toString()));
        intent.putExtra("vehicleType",types.getSelectedItem().toString());
        startActivity(intent);
    }

    private void getMaxPassengerCount() {
        progressBar.show();
        db.collection("Vehicles").orderBy("passengers", Query.Direction.DESCENDING).limit(1).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        QuerySnapshot snapshot = task.getResult();
                        maxCount = Integer.valueOf(snapshot.getDocuments().get(0).get("passengers").toString());
                        initializeData();
                    }
                });
    }

    private void initializeData() {
        passengerCounts = new ArrayList<>();
        for (int i = 1; i <= maxCount; i++) {
            passengerCounts.add(i);
        }
        adapterPassengerCount = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, passengerCounts);
        adapterPassengerCount.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        passengers.setAdapter(adapterPassengerCount);
        adapterPassengerCount.notifyDataSetChanged();

        Resources resources = this.getResources();
        String[] strings = resources.getStringArray(R.array.vehicleTypes);
        vehicleTypes = new ArrayList<>(Arrays.asList(strings));

        adapterVehicleTypes = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, vehicleTypes);
        adapterVehicleTypes.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        types.setAdapter(adapterVehicleTypes);
        adapterVehicleTypes.notifyDataSetChanged();

        progressBar.hide();
    }

}
