package com.example.lab11firebase;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class PrincipalActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_IMAGE =101 ;
    private ImageView ivMovieImage;
    private EditText edtMovieName;
    private TextView tvProgress;
    private ProgressBar progressBar;
    private Button btnUpload;

    Uri imageUri;
    boolean isImageAdded = false;

    DatabaseReference dataRef;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        ivMovieImage=(ImageView)findViewById(R.id.ivMovieImage);
        edtMovieName=(EditText)findViewById(R.id.edtMovieName);
        tvProgress=(TextView)findViewById(R.id.tvProgress);
        progressBar=(ProgressBar)findViewById(R.id.progressBar);
        btnUpload=(Button)findViewById(R.id.btnUpload);

        tvProgress.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        dataRef = FirebaseDatabase.getInstance().getReference().child("Movie");
        storageRef = FirebaseStorage.getInstance().getReference().child("MovieImage");

        ivMovieImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,REQUEST_CODE_IMAGE);
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String movieName = edtMovieName.getText().toString();
                if (isImageAdded && !movieName.isEmpty()) {
                    uploadImage(movieName);
                }
            }
        });
    }

    private void uploadImage(final String movieName) {
        tvProgress.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        final  String key=dataRef.push().getKey();
        storageRef.child(key+".jpg").putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(PrincipalActivity.this, "imagen", Toast.LENGTH_SHORT).show();
                storageRef.child(key +".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Toast.makeText(PrincipalActivity.this, "texto", Toast.LENGTH_SHORT).show();
                        HashMap hashMap = new HashMap();
                        hashMap.put("MovieName",movieName);
                        hashMap.put("ImageUrl",uri.toString());
                        dataRef.child(key).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(PrincipalActivity.this, "se abre otro inytent", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            }
                        });
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress=(taskSnapshot.getBytesTransferred()*100)/taskSnapshot.getTotalByteCount();
                progressBar.setProgress((int) progress);
                tvProgress.setText(progress +" %");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_CODE_IMAGE && data!=null) {
            imageUri=data.getData();
            isImageAdded=true;
            ivMovieImage.setImageURI(imageUri);
        }
    }
}