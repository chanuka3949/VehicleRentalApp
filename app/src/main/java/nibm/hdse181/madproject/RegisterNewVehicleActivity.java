package nibm.hdse181.madproject;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class RegisterNewVehicleActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    EditText txt_passengers,txt_title;
    Spinner v_type;
    Button btn_add;
    ImageView v_img;
    Uri imageUri;
    GeoPoint geo;
    private FirebaseFirestore db;
    private StorageReference storageReference;
    byte[] compressedImage = null;
    String userID;
    String v_id;
    private static final String TAG = "Register";
    SweetAlertDialog progressBar;
    Vehicle v1;
    boolean isNewVehicle = true;
    boolean isNewImage = false;
    String[] strings;
    List<String> stringList = new ArrayList<>();
    SweetAlertDialog success;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register_new_vehicle);
        Intent intent = getIntent();

        storageReference = FirebaseStorage.getInstance().getReference("vehicleImages");
        db = FirebaseFirestore.getInstance();

        success = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE);
        success.setTitleText("Success");
        success.setContentText("");

        progressBar = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        progressBar.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        progressBar.setTitleText("Saving");
        progressBar.setCancelable(false);

        v_img=(ImageView)findViewById(R.id.vehicle_image);
        txt_title=(EditText) findViewById(R.id.txtTitle);
        txt_passengers=(EditText)findViewById(R.id.txtSeats);
        v_type=(Spinner) findViewById(R.id.vehical_type);
        btn_add=(Button) findViewById(R.id.btnAdd);

        isNewVehicle = intent.getBooleanExtra("isNew",true);
        if(isNewVehicle){
            userID = intent.getStringExtra("uid");
        }else{
            v_id = intent.getStringExtra("vid");
            btn_add.setText("Save");
            Resources resources = this.getResources();
            strings = resources.getStringArray(R.array.vehicleTypes);
            for (String value:strings) {
                stringList.add(value);
            }
            getData();
        }

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        v_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });


    }

    private  void getData()
    {
        db.collection("Vehicles").document(v_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot snapshot= task.getResult();

                    txt_title.setText(snapshot.getString("title"));
                    txt_passengers.setText(snapshot.get("passengers").toString());
                    Picasso.get().load(snapshot.getString("imageURI")).fit().centerInside().into(v_img);
                    v_type.setSelection(stringList.indexOf(snapshot.getString("type")));
                }else {
                }
            }
        });
    }

    private void SelectImage(){
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(
                this);
        myAlertDialog.setTitle("Upload Pictures Option");
        myAlertDialog.setMessage("How do you want to set your picture?");
        myAlertDialog.setCancelable(true);

        myAlertDialog.setPositiveButton("Gallery",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        openImageChooser();
                    }
                });

        myAlertDialog.setNegativeButton("Camera",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        openCamera();
                    }
                });
        myAlertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK &&
                data != null && data.getData() != null){
            isNewImage = true;
            imageUri = data.getData();
            Picasso.get().load(imageUri).fit().centerInside().into(v_img);
                ImageResize resize = new ImageResize();
                resize.execute();
        }else if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK &&
                data != null && data.getData() != null){
            imageUri = data.getData();
            isNewImage = true;
            Picasso.get().load(imageUri).fit().centerInside().into(v_img);
                ImageResize resize = new ImageResize();
                resize.execute();
        }
    }
    private void openImageChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }
    private void openCamera(){
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent,2);
    }


    public class ImageResize extends AsyncTask<Uri,Integer,byte[]> {
        Bitmap mBitmap;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("imagecompression","Compression Pre Execution");
        }
        @Override
        protected byte[] doInBackground(Uri... uris) {
            Log.d("imagecompression","Compression Started");
            try{
                mBitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(),imageUri);
            }catch (IOException e){
                Log.e("TAG",e.getMessage());
            }
            byte[] bytes = null;
            bytes = getByteArrayFromBitmap(mBitmap,25);
            return bytes;
        }
        @Override
        protected void onPostExecute(byte[] bytes) {
            Log.d("imagecompression","Compression Finished");
            super.onPostExecute(bytes);
            compressedImage = bytes;
        }
    }

    public byte[] getByteArrayFromBitmap(Bitmap bitmap, int quality){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }
    private void bitmapUpload(){
        if(compressedImage != null){
            Log.d(TAG,"Inside Image Upload");
            final StorageReference ref = storageReference.child(v_id+ "." + getFileExtention(imageUri));
            ref.putBytes(compressedImage)
                    .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return ref.getDownloadUrl();
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG,"Upload Completed");
                                Uri downloadUri = task.getResult();
                                Map<String, Object> map = new HashMap<>();
                                if(isNewVehicle){
                                    map.put("imageURI", downloadUri.toString());
                                    map.put("id",v_id);
                                }else{
                                    map.put("imageURI", downloadUri.toString());
                                }
                                db.collection("Vehicles").document(v_id).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            progressBar.hide();
                                            Log.d(TAG,"Database Updated");
                                            compressedImage = null;
                                            imageUri = null;
                                            success.show();
                                            success.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                @Override
                                                public void onDismiss(DialogInterface dialog) {
                                                    finish();
                                                }
                                            });
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG,e.getMessage());
                                        progressBar.hide();
                                    }
                                });
                            } else {
                            }
                        }
                    });
        }else {
        }
    }
    private String getFileExtention(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    void saveData(){
        if(v_img.getDrawable() == null || TextUtils.isEmpty(txt_title.getText().toString().trim())
        || TextUtils.isEmpty(txt_passengers.getText().toString().trim())){
            Snackbar.make(findViewById(R.id.activity_vehicle),"Please fill all the details",Snackbar.LENGTH_LONG).show();
        }else{
            progressBar.show();
            v1=new Vehicle();
            if(isNewVehicle){
                db.collection("Owners").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            geo = task.getResult().getGeoPoint("location");
                            v1.setLocation(geo);
                            v1.setAvailable(true);
                            v1.setId(null);
                            v1.setType(v_type.getSelectedItem().toString());
                            v1.setPassengers(Integer.parseInt(txt_passengers.getText().toString()));
                            v1.setOwner(userID);
                            v1.setTitle(txt_title.getText().toString());
                            db.collection("Vehicles").add(v1)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            v_id=documentReference.getId();
                                            Log.d(TAG,"Document Created : "+v_id);
                                            bitmapUpload();
                                        }
                                    });
                        }
                    }
                });
            }else{
                if(isNewImage){
                    Map<String,Object> map = new HashMap<>();
                    map.put("title",txt_title.getText().toString());
                    map.put("passengers",Integer.valueOf(txt_passengers.getText().toString()));
                    map.put("type",v_type.getSelectedItem().toString());
                    db.collection("Vehicles").document(v_id).update(map)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    bitmapUpload();
                                }
                            });
                }else{
                    Map<String,Object> map = new HashMap<>();
                    map.put("title",txt_title.getText().toString());
                    map.put("passengers",Integer.valueOf(txt_passengers.getText().toString()));
                    map.put("type",v_type.getSelectedItem().toString());
                    db.collection("Vehicles").document(v_id).update(map)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressBar.hide();
                                    success.show();
                                    success.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            finish();
                                        }
                                    });
                                }
                            });
                }
            }
        }

    }
}
