package nibm.hdse181.madproject;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class OwnerInfoActivity extends AppCompatActivity {

    private final static int GET_LATLNG_COORDINATES = 9005;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private final static String TAG1 = "LOCATION";
    final static String TAG2 = "IMAGE_RESIZE";
    private Button setLocation,saveInfo;
    private FirebaseFirestore db;
    private StorageReference storageReference;
    private String userID;
    private EditText name,email,mobile;
    private GeoPoint location = null;
    private ImageView image;
    byte[] compressedImage = null;
    private Uri imageUri = null;
    private SweetAlertDialog progressBar;
    private boolean newImageAdded = false;
    private Owner owner = new Owner();
    private boolean newuser = false;
    private LatLng point = null;
    private SweetAlertDialog success;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_info);

        Intent intent = getIntent();
        userID = intent.getStringExtra("id");
        newuser = intent.getBooleanExtra("newuser",false);
        Log.d("USER",userID);


        storageReference = FirebaseStorage.getInstance().getReference("avatars");
        db = FirebaseFirestore.getInstance();

        setLocation = findViewById(R.id.btnSetLocation);
        saveInfo = findViewById(R.id.btnSaveUserInfo);
        name = findViewById(R.id.txtName);
        mobile = findViewById(R.id.txtMobile);
        email = findViewById(R.id.txtEmail);
        image = findViewById(R.id.imageOwner);

        success = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE);
        success.setTitleText("Success");
        success.setContentText("");

        progressBar = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        progressBar.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        progressBar.setTitleText("Saving");
        progressBar.setCancelable(false);

        setLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent map = new Intent(getApplicationContext(),UserInfoMapActivity.class);
                map.putExtra("location",point);
                startActivityForResult(map,GET_LATLNG_COORDINATES);
            }
        });
        saveInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserData();
            }
        });
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });
        getUserData();
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
    private void getUserData(){
        if(newuser){
            final DocumentReference documentReference= db.collection("Owners").document(userID);
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if(documentSnapshot.exists()){
                        owner.setName(documentSnapshot.getString("name"));
                        owner.setEmail(documentSnapshot.getString("email"));
                        if(documentSnapshot.get("photoURI") != null){
                            owner.setPhotoURI(documentSnapshot.get("photoURI").toString());
                            Picasso.get()
                                    .load(owner.getPhotoURI())
                                    .fit()
                                    .centerCrop()
                                    .into(image);
                        }

                        name.setText(owner.getName());
                        email.setText(owner.getEmail());
                    }
                }
            });
        }else{
            final DocumentReference documentReference= db.collection("Owners").document(userID);
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if(documentSnapshot.exists()){
                        Log.d("Exists",documentSnapshot.getString("name"));
                        owner.setName(documentSnapshot.getString("name"));
                        owner.setEmail(documentSnapshot.getString("email"));
                        owner.setMobile(documentSnapshot.getString("mobile"));
                        owner.setLocation((GeoPoint) documentSnapshot.get("location"));
                        if(documentSnapshot.get("photoURI") != null){
                            owner.setPhotoURI(documentSnapshot.get("photoURI").toString());
                            Picasso.get()
                                    .load(owner.getPhotoURI())
                                    .fit()
                                    .centerCrop()
                                    .into(image);
                        }

                        name.setText(owner.getName());
                        email.setText(owner.getEmail());
                        mobile.setText(owner.getMobile());
                        location = owner.getLocation();
                        point = new LatLng(location.getLatitude(),location.getLongitude());
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if(newuser)
            moveTaskToBack(false);
        else
            super.onBackPressed();
    }

    private void saveUserData(){
        if(TextUtils.isEmpty(name.getText()) || TextUtils.isEmpty(email.getText()) || TextUtils.isEmpty(mobile.getText()) || location == null){
            Snackbar.make(findViewById(R.id.ownerInfoLayout),"Please fill all the details",Snackbar.LENGTH_LONG).show();
        }else{
            if(newImageAdded){
                progressBar.show();
                final Map<String,Object> map = new HashMap<>();
                map.put("name",name.getText().toString());
                map.put("email",email.getText().toString());
                map.put("mobile",mobile.getText().toString());
                map.put("location",location);

                final WriteBatch writeBatch = db.batch();
                db.collection("Vehicles").whereEqualTo("owner",userID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot snapshot:task.getResult()) {
                            Log.d("batchUpdate","Inside Foreach Loop");
                            writeBatch.update(db.collection("Vehicles").document(snapshot.getString("id")),"location",location);
                        }
                        writeBatch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d("batchUpdate","Batch Update Done");
                                db.collection("Owners").document(userID).update(map)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                bitmapUpload();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressBar.hide();
                                        Toast.makeText(OwnerInfoActivity.this, "Couldn't Save Data", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                });
            }else{
                progressBar.show();
                final Map<String,Object> map = new HashMap<>();
                map.put("name",name.getText().toString());
                map.put("email",email.getText().toString());
                map.put("mobile",mobile.getText().toString());
                map.put("location",location);
                final WriteBatch writeBatch = db.batch();
                db.collection("Vehicles").whereEqualTo("owner",userID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot snapshot:task.getResult()) {
                            Log.d("batchUpdate","Inside Foreach Loop");
                            writeBatch.update(db.collection("Vehicles").document(snapshot.getString("id")),"location",location);
                        }
                        writeBatch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d("batchUpdate","Batch Update Done");
                                db.collection("Owners").document(userID).update(map)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Log.d("batchUpdate","All Updates Done");
                                                progressBar.hide();
                                                success.show();
                                                success.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                    @Override
                                                    public void onDismiss(DialogInterface dialog) {
                                                        finish();
                                                    }
                                                });

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressBar.hide();
                                        Toast.makeText(OwnerInfoActivity.this, "Couldn't Save Data", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                });

            }
        }
    }

    private void openImageChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }
    private void openCamera(){
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent,CAMERA_REQUEST);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GET_LATLNG_COORDINATES && resultCode == RESULT_OK && data != null){
            Log.d(TAG1,"Coordinates Received");
            point = (LatLng) data.getExtras().get("location");
            location = new GeoPoint(point.latitude,point.longitude);
        }else if(requestCode == GET_LATLNG_COORDINATES && resultCode == RESULT_CANCELED ){
            Snackbar.make(findViewById(R.id.ownerInfoLayout),"No changes were made",Snackbar.LENGTH_SHORT).show();
        }else if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null){
            newImageAdded = true;
            imageUri = data.getData();
            Picasso.get()
                    .load(imageUri)
                    .fit()
                    .centerCrop()
                    .into(image);
            ImageResize imageResize = new ImageResize();
            imageResize.execute();
        }else if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK && data != null){
            newImageAdded = true;
            imageUri = data.getData();
            Picasso.get()
                    .load(imageUri)
                    .fit()
                    .centerCrop()
                    .into(image);
            ImageResize imageResize = new ImageResize();
            imageResize.execute();
        }
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
            Log.d("imagecompression","Upload Started");
            final StorageReference ref = storageReference.child(userID + "." + getFileExtention(imageUri));
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
                                Log.d("imagecompression","Upload Completed");
                                Uri downloadUri = task.getResult();
                                Map<String, Object> map = new HashMap<>();
                                map.put("photoURI", downloadUri.toString());
                                db.collection("Owners").document(userID).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Log.d("imagecompression","Database URI Updated");
                                            compressedImage = null;
                                            imageUri = null;
                                            newImageAdded = false;
                                            progressBar.hide();
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
                                        progressBar.hide();
                                        Snackbar.make(findViewById(R.id.ownerInfoLayout), "Couldn't upload image", Snackbar.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Snackbar.make(findViewById(R.id.ownerInfoLayout), "Couldn't upload image", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });
        }else {
            Snackbar.make(findViewById(R.id.ownerInfoLayout),"No Image Selected",Snackbar.LENGTH_SHORT).show();
        }
    }
    private String getFileExtention(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}
