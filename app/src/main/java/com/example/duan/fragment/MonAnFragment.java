package com.example.duan.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.example.duan.R;
import com.example.duan.adapter.MonAnRecycleAdapter;
import com.example.duan.model.Dish;


public class MonAnFragment extends Fragment implements View.OnClickListener {
    private MonAnRecycleAdapter adapter;
    private RecyclerView recyclerView;
    private FloatingActionButton actionButton;
    private ImageView img;
    private StorageReference storageRef;
    private DatabaseReference databaseRef;
    private Uri uri;
    private int check = -1;
    private EditText edt_search;
    private List<Dish> listDish = new ArrayList<>();

    public MonAnFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_monan, container, false);
    }
// ádasdasda
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        actionButton = view.findViewById(R.id.btn_float_themMonAn);
        actionButton.setOnClickListener(this);
        actionButton.setColorFilter(Color.WHITE);
        edt_search = view.findViewById(R.id.edtSearch_dish);
        recyclerView = view.findViewById(R.id.recycle_monAn);
        databaseRef = FirebaseDatabase.getInstance().getReference().child("Dish");
        getListDishFromFireBase();
        adapter = new MonAnRecycleAdapter(getActivity(),R.layout.item_monan, dish -> openDiaLogUpdateDish(dish));
        adapter.setData(listDish);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        edt_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(edt_search.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void getListDishFromFireBase(){

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listDish.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Dish dish = dataSnapshot.getValue(Dish.class);
                    listDish.add(dish);
                }
                adapter.setData(listDish);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_float_themMonAn:
                diaLogAddMonAn();
                break;
        }
    }
    public void diaLogAddMonAn() {
        Calendar calendar = Calendar.getInstance();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_monan, null);
        img = view.findViewById(R.id.img_diaLogMonAn_anh);
        EditText ed_ten = view.findViewById(R.id.ed_diaLogMonAn_ten);
        EditText ed_gia = view.findViewById(R.id.ed_diaLogMonAn_gia);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        img.setOnClickListener(v -> {
            requestPermission();
        });
        view.findViewById(R.id.btn_diaLogMonAn_luu).setOnClickListener(v -> {
            if(check == 0){
                uploadGallery(uri,calendar);
            }
            if(check==1){
                uploadCamera(calendar);
            }
            Dish monAn = new Dish("dish"+calendar.getTimeInMillis(), ed_ten.getText().toString(),Double.parseDouble(ed_gia.getText().toString()), "");
            databaseRef.child("dish"+calendar.getTimeInMillis()).setValue(monAn);
            alertDialog.cancel();
        });
        view.findViewById(R.id.btn_diaLogMonAn_huy).setOnClickListener(v -> {
            alertDialog.cancel();
        });
        alertDialog.show();

    }

    public void diaLogGalleryOrCamera() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_gallery_or_camera, null);
        LinearLayout linear_camera = view.findViewById(R.id.linear_camera);
        LinearLayout linear_gallery = view.findViewById(R.id.linear_gallery);
        builder.setView(view);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                check =-1;
            }
        });
        AlertDialog alertDialog = builder.create();
        linear_camera.setOnClickListener(v -> {
            check = 1;
            cameraIntent();
            alertDialog.cancel();
        });
        linear_gallery.setOnClickListener(v -> {
            check = 0;
            galleryIntent();
            alertDialog.cancel();
        });
        alertDialog.show();
    }

    public void requestPermission() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                diaLogGalleryOrCamera();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(getActivity(), "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };
        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();
    }

    public void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activityResultLauncher_gallery.launch(Intent.createChooser(intent, "Select Picture"));
        //startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    public void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        activityResultLauncher_camera.launch(intent);
        //startActivityForResult(intent, REQUEST_CAMERA);
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode == PICK_IMAGE){
//
//        }
//        if(requestCode == REQUEST_CAMERA){
//                Bitmap photo = (Bitmap) data.getExtras().get("data");
//                img.setImageBitmap(photo);
//                ImageView.ScaleType scaleType = ImageView.ScaleType.FIT_XY;
//                img.setScaleType(scaleType);
//        }
//    }


    private ActivityResultLauncher<Intent> activityResultLauncher_gallery = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Intent data = result.getData();
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (data == null) {
                            return;
                        }
                        uri = data.getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                            img.setImageBitmap(bitmap);
                            ImageView.ScaleType scaleType = ImageView.ScaleType.FIT_XY;
                            img.setScaleType(scaleType);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );
    private ActivityResultLauncher<Intent> activityResultLauncher_camera = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Intent data = result.getData();
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (data == null) {
                            return;
                        }
                        Bitmap photo = (Bitmap) data.getExtras().get("data");
                        img.setImageBitmap(photo);
                        ImageView.ScaleType scaleType = ImageView.ScaleType.FIT_XY;
                        img.setScaleType(scaleType);
                    }
                }
            }
    );

    private void uploadGallery(Uri uri,Calendar calendar) {
        if (uri != null) {
            storageRef = FirebaseStorage.getInstance().getReference("image/Dish"+ calendar.getTimeInMillis());
            storageRef.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    databaseRef.child("dish"+calendar.getTimeInMillis()+"/img").setValue(uri.toString());
                                    Toast.makeText(getActivity(), "Thành công", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), "Thất bại", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    private void uploadCamera(Calendar calendar) {
        Bitmap bitmap = ((BitmapDrawable) img.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        storageRef = FirebaseStorage.getInstance().getReference("image/dish" + calendar.getTimeInMillis()  );
        storageRef.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Toast.makeText(getActivity(), "Thành công", Toast.LENGTH_SHORT).show();
                                databaseRef.child("dish"+calendar.getTimeInMillis()+"/img").setValue(uri.toString());
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void upDateGallery(Uri uri,Dish dish) {
        Calendar calendar = Calendar.getInstance();
        if (uri != null) {
            storageRef = FirebaseStorage.getInstance().getReference("image/Dish"+ calendar.getTimeInMillis());
            storageRef.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    databaseRef.child(dish.getId()+"/img").setValue(uri.toString());
                                    Toast.makeText(getActivity(), "Thành công", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), "Thất bại", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    private void upDateCamera(Dish dish) {
        Calendar calendar = Calendar.getInstance();
        Bitmap bitmap = ((BitmapDrawable) img.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        storageRef = FirebaseStorage.getInstance().getReference("image/dish" + calendar.getTimeInMillis()  );
        storageRef.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Toast.makeText(getActivity(), "Thành công", Toast.LENGTH_SHORT).show();
                                databaseRef.child(dish.getId()+"/img").setValue(uri.toString());
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openDiaLogUpdateDish(Dish dish){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_monan, null);
        img = view.findViewById(R.id.img_diaLogMonAn_anh);
        EditText ed_ten = view.findViewById(R.id.ed_diaLogMonAn_ten);
        EditText ed_gia = view.findViewById(R.id.ed_diaLogMonAn_gia);
        ed_ten.setText(dish.getTen());
        ed_gia.setText(String.valueOf(dish.getGia()));
        Glide.with(getActivity()).load(dish.getImg()).into(img);
        ImageView.ScaleType scaleType = ImageView.ScaleType.FIT_XY;
        img.setScaleType(scaleType);
        builder.setView(view);
        AlertDialog alertDialog = builder.create();
        img.setOnClickListener(v -> {
            requestPermission();
        });
        view.findViewById(R.id.btn_diaLogMonAn_luu).setOnClickListener(v -> {
            if(check == 0){
                upDateGallery(uri,dish);
            }
            if(check==1){
                upDateCamera(dish);
            }
            String tenDish = ed_ten.getText().toString();
            double giaDish = Double.parseDouble(ed_gia.getText().toString());
            Dish dish1 = new Dish(tenDish,giaDish,dish.getImg());
            databaseRef.child(String.valueOf(dish.getId())).updateChildren(dish1.toMap(), new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                    openSuccessDialog("Thành công");
                }
            });
            alertDialog.cancel();
        });
        view.findViewById(R.id.btn_diaLogMonAn_huy).setOnClickListener(v -> {
            alertDialog.cancel();
        });
        alertDialog.show();
    }

    public void openSuccessDialog (String text) {
        Dialog dialog = new Dialog(getContext());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_success_notification);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvNotifyContent = dialog.findViewById(R.id.tvNotifyContent);
        tvNotifyContent.setText(text);
        dialog.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            dialog.dismiss();
        });
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.show();
    }

    public void openFailDialog (String text) {
        Dialog dialog = new Dialog(getContext());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_fail_notification);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvNotifyContent = dialog.findViewById(R.id.tvNotifyContent);
        tvNotifyContent.setText(text);
        dialog.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            dialog.dismiss();
        });
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.show();
    }
}