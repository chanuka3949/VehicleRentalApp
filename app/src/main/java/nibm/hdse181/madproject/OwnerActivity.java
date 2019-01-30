package nibm.hdse181.madproject;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.scalified.fab.ActionButton;

import java.util.ArrayList;
import java.util.List;

public class OwnerActivity extends AppCompatActivity implements OwnerVehicleListRecyclerAdapter.OnVehicleListener {

    List<Vehicle> vehicleList = new ArrayList<>();
    RecyclerView recyclerView;
    OwnerVehicleListRecyclerAdapter viewAdapter;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    String uid = null;
    boolean newuser = false;
    ActionButton addNewVehicle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner);

        Intent intent = getIntent();
        uid = intent.getStringExtra("id");
        db = FirebaseFirestore.getInstance();

        addNewVehicle = findViewById(R.id.addVehicle);

        getUserData();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("445523781896-effhg62a2n8kfnetc9o32l3p2f459jn7.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.vehicleListRecyclerView);
        recyclerView.setHasFixedSize(true);
        viewAdapter = new OwnerVehicleListRecyclerAdapter(getApplicationContext(),vehicleList,this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(viewAdapter);

        addNewVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OwnerActivity.this,RegisterNewVehicleActivity.class);
                intent.putExtra("uid",uid);
                intent.putExtra("isNew",true);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        Log.d("AdapterCheck","onStart");
        super.onStart();
        viewAdapter.clearData();
        getVehicleData();
    }

    @Override
    protected void onRestart() {
        Log.d("AdapterCheck","onRestart");
        super.onRestart();
    }

    private void getVehicleData(){
        db.collection("Vehicles").whereEqualTo("owner",uid).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    Log.d("ABC", "Successful");
                    initializeAdapter(task);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("ABC", e.getMessage());
            }
        });
    }

    private void initializeAdapter(Task<QuerySnapshot> task){
        for(DocumentSnapshot documentSnapshot : task.getResult()){
            vehicleList.add(documentSnapshot.toObject(Vehicle.class));
        }
        Log.d("ABC","Initialized Adapter");
        viewAdapter = new OwnerVehicleListRecyclerAdapter(getApplicationContext(),vehicleList,this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(viewAdapter);
        Log.d("ABC","Adapter Set Complete");
    }

    @Override
    public void onVehicleClick(int position) {
        vehicleList.get(position);
        Intent intent = new Intent(this, RegisterNewVehicleActivity.class);
        intent.putExtra("vid",vehicleList.get(position).getId());
        intent.putExtra("isNew",false);
        startActivity(intent);
    }

    @Override
    public void onAvailabilityClick(int position, boolean checked) {
        String id = vehicleList.get(position).getId();
        db.collection("Vehicles").document(id).update("available",checked)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d("Switch","Checked Changed");
                        }else {
                            Log.d("Switch","Checked Un Changed");
                        }
                    }
                });
    }

    private void getUserData(){
        DocumentReference user = db.collection("Owners").document(uid);
        user.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot snapshot = task.getResult();
                    if(snapshot.exists()){
                        if(!snapshot.contains("mobile") || !snapshot.contains("location")){
                            newuser = true;
                            Intent intent = new Intent(getApplicationContext(),OwnerInfoActivity.class);
                            intent.putExtra("id",uid);
                            intent.putExtra("newuser",newuser);
                            startActivity(intent);
                        }
                    }
                }
            }
        });
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(false);

    }
    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                        finish();
                    }
                });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.signOut:{
                signOut();
            }
            case R.id.userInfo:{
                Intent intent = new Intent(this,OwnerInfoActivity.class);
                intent.putExtra("id",uid);
                intent.putExtra("newuser",newuser);
                startActivity(intent);
            }
        }
        return super.onOptionsItemSelected(item);
    }


}
