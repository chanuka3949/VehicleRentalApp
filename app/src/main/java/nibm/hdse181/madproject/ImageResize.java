package nibm.hdse181.madproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageResize extends AsyncTask<Uri,Integer,byte[]> {

    Context context;
    Bitmap bitmap;
    Uri imageUri;
    byte[] compressedImage = null;

    public ImageResize(Context context) {
        super();
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected byte[] doInBackground(Uri... uris) {

        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(),imageUri);
        }catch (IOException e){
            Toast.makeText(context, "Couldn't get image from storage", Toast.LENGTH_SHORT).show();
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream);
        return stream.toByteArray();
    }

    @Override
    protected void onPostExecute(byte[] bytes) {
        super.onPostExecute(bytes);
        compressedImage = bytes;
    }
}
