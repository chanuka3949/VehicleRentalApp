package nibm.hdse181.madproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;


public class ViewSearchResultActivity extends AppCompatActivity {

    String ownerID = null;
    ImageView imgView;
    TextView title, type, seats, tp, name, Email;
    Vehicle vehicle = new Vehicle();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_search_result);

        set();
        db = FirebaseFirestore.getInstance();
        title = findViewById(R.id.textTitle);
        type = findViewById(R.id.textType);
        seats = findViewById(R.id.txtSeats);
        tp = (TextView) findViewById(R.id.textContact);
        name = findViewById(R.id.textName);
        Email = findViewById(R.id.txtMail);
        imgView = (ImageView) findViewById(R.id.vehicle_image);
        getData();

        tp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = "tel:"+tp.getText().toString();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(number));
                startActivity(intent);
            }
        });
    }
    private void getData() {
        db.collection("Owners").document(ownerID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    title.setText(vehicle.getTitle());
                    seats.setText(String.valueOf(vehicle.getPassengers()));
                    Picasso.get().load(vehicle.getImageURI()).fit().centerInside().into(imgView);
                    type.setText(vehicle.getType());
                    tp.setText(snapshot.getString("mobile"));
                    name.setText(snapshot.getString("name"));
                    Email.setText(snapshot.getString("email"));

                }
            }
        });
    }
    private void set() {
        Intent intent = getIntent();
        vehicle.setTitle(intent.getStringExtra("title"));
        vehicle.setType(intent.getStringExtra("type"));
        vehicle.setPassengers(intent.getIntExtra("passengers",0));
        vehicle.setImageURI(intent.getStringExtra("image"));
        ownerID = intent.getStringExtra("owner");
    }
}

