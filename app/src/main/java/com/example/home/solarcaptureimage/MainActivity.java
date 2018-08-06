package com.example.home.solarcaptureimage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.home.solarcaptureimage.api.ApiService;
import com.example.home.solarcaptureimage.api.RetroClient;
import com.example.home.solarcaptureimage.api.response.UploadImageResult;
import com.example.home.solarcaptureimage.permission.PermissionsChecker;
import com.example.home.solarcaptureimage.utils.InternetConnection;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends Activity implements OnClickListener,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener {
    Button captureBtn = null;
    final int CAMERA_CAPTURE = 1;
    private Uri picUri;
    TextView lat;
    String loc;
    Button submit,caploc;
    double latitude,longitude;
    public GoogleApiClient mGoogleApiClient;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private GridView grid;
    String _path,value,latlog,add;
    PermissionsChecker checker;
    private  List<String> listOfImagesPath;
    Context mContext;
    public static  String GridViewDemo_ImagePath;
    GpsTracker gps;
    Geocoder geocoder;
    List<Address> addresses;
    private ListView list;
    private Adpter adapter;
     ArrayList<String> arrayList;
    File file;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_capture);

        submit=(Button)findViewById(R.id.submit);
        captureBtn = (Button)findViewById(R.id.capture_btn1);
        caploc = (Button)findViewById(R.id.cap_location);
        list = (ListView) findViewById(R.id.listview);
        arrayList = new ArrayList<String>();
        adapter = new Adpter(getApplicationContext(),arrayList);
        // Here, you set the data in your ListView
        list.setAdapter(adapter);
        captureBtn.setOnClickListener(this);
        lat=(TextView)findViewById(R.id.lat);
        grid = ( GridView) findViewById(R.id.gridviewimg);
        checker = new PermissionsChecker(this);
        gps=new GpsTracker(MainActivity.this);
/*
        geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(gps.getLatitude(),gps.getLongitude(), 1);
            add=addresses.get(0).getAddressLine(0);
            // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        //lat.setText(String.valueOf(location.getLatitude()+"_"+location.getLongitude()));
        loc=String.valueOf(gps.getLatitude()+"_"+gps.getLongitude());
        lat.setText(add);
        latlog=String.valueOf(gps.getLatitude()+","+gps.getLongitude());
        value=loc;
        GridViewDemo_ImagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Solars/"+value+"/";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                //Location Permission already granted
                buildGoogleApiClient();

            } else {
                //Request Location Permission
                checkLocationPermission();
            }

        }

        else {

            buildGoogleApiClient();

        }

        caploc.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                arrayList.add(latlog);
                adapter = new Adpter(MainActivity.this,arrayList);
                // Here, you set the data in your ListView
                list.setAdapter(adapter);
                // next thing you have to do is check if your adapter has changed
                adapter.notifyDataSetChanged();

            }
        });

        submit.setOnClickListener(new OnClickListener() {
    @Override
    public void onClick(View view) {

        Log.e("paths", String.valueOf(listOfImagesPath));

        if(!TextUtils.isEmpty(_path)){
            if (!TextUtils.isEmpty(String.valueOf(listOfImagesPath))) {

                if (InternetConnection.checkConnection(MainActivity.this)) {

                    /******************Retrofit***************/
                    uploadImage();

                } else {
                    Toast.makeText(MainActivity.this,R.string.string_internet_connection_warning,Toast.LENGTH_LONG).show();
                    //Snackbar.make(parentView, R.string.string_internet_connection_warning, Snackbar.LENGTH_INDEFINITE).show();
                }
            }
        }
        else {

            Toast.makeText(MainActivity.this,R.string.string_message_to_attach_file,Toast.LENGTH_LONG).show();
            //Snackbar.make(parentView, R.string.string_message_to_attach_file, Snackbar.LENGTH_INDEFINITE).show();
        }

    /*  File root = new File(GridViewDemo_ImagePath);
        File[] Files = root.listFiles();
        if(Files != null) {
            int j;
            for(j = 0; j < Files.length; j++) {
                System.out.println(Files[j].getAbsolutePath());
                System.out.println(Files[j].delete());
            }
        }*/

      // grid.setAdapter(null);
    }
});



        listOfImagesPath = null;
        listOfImagesPath = RetriveCapturedImagePath();
        if(listOfImagesPath!=null){
            grid.setAdapter(new ImageListAdapter(this,listOfImagesPath));
        }
    }

    public boolean isStoragePermissionGranted() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (getApplicationContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            return true;
        }
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        gps=new GpsTracker(this);
        if(gps.canGetLocation){
            geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            latitude=gps.getLatitude();
            longitude=gps.getLongitude();
            try {
                addresses = geocoder.getFromLocation(gps.getLatitude(),gps.getLongitude(), 1);
                add=addresses.get(0).getAddressLine(0);
                isStoragePermissionGranted();

                // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            } catch (IOException e) {
                e.printStackTrace();
            }
            //lat.setText(String.valueOf(location.getLatitude()+"_"+location.getLongitude()));
            loc=String.valueOf(gps.getLatitude()+"_"+gps.getLongitude());
            lat.setText(add);
            latlog=String.valueOf(gps.getLatitude()+","+gps.getLongitude());
            value=loc;
        }
        else {
            gps.showSettingsAlert();
        }

    }

    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    private void uploadImage() {

        final ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage(getString(R.string.string_title_upload_progressbar_));
        progressDialog.show();

        //Create Upload Server Client
        ApiService service = RetroClient.getApiService();

        //File creating from selected URL

        // create RequestBody instance from file
     //   RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        //Log.e("request", String.valueOf(requestFile));
        // MultipartBody.Part is used to send also the actual file name
    //    MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file", file.getName(), requestFile);

         file = new File(_path);
        MultipartBody.Part[] surveyImagesParts = new MultipartBody.Part[listOfImagesPath.size()];

        for (int index = 0; index < listOfImagesPath.size(); index++) {
            file = new File(listOfImagesPath.get(index));
            RequestBody surveyBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            surveyImagesParts[index] = MultipartBody.Part.createFormData("SurveyImage", file.getName(), surveyBody);
        }

        Call<UploadImageResult> resultCall = service.uploadImage(surveyImagesParts,latlog,add);

//        Call<Result> resultCall = service.test();
        // finally, execute the request
        resultCall.enqueue(new Callback<UploadImageResult>() {
            @Override
            public void onResponse(Call<UploadImageResult> call, Response<UploadImageResult> response) {
                progressDialog.dismiss();
                _path = "";
                if(response.isSuccessful()){

                   /* File root = new File(GridViewDemo_ImagePath);
                        File[] Files = root.listFiles();
                        if(Files != null) {
                            int j;
                            for(j = 0; j < Files.length; j++) {
                                System.out.println(Files[j].getAbsolutePath());
                                System.out.println(Files[j].delete());
                            }
                        }
                   */
                   grid.setAdapter(null);
                    Toast.makeText(MainActivity.this,"Successfully Uploaded",Toast.LENGTH_LONG).show();
                }
                else {


                }
            }

            @Override
            public void onFailure(Call<UploadImageResult> call, Throwable t) {

                Toast.makeText(MainActivity.this,"Bad Network",Toast.LENGTH_LONG).show();

                progressDialog.dismiss();
            }
        });

    }


    @Override
    public void onClick(View arg0) {

// TODO Auto-generated method stub
        if (arg0.getId() == R.id.capture_btn1) {

            try {
//use standard intent to capture an image
                Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//we will handle the returned data in onActivityResult
                startActivityForResult(captureIntent, CAMERA_CAPTURE);
            } catch(ActivityNotFoundException anfe){
//display an error message
                String errorMessage = "Whoops - your device doesn't support capturing images!";
                Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
//user is returning from capturing an image using the camera
            if(requestCode == CAMERA_CAPTURE){
                Bundle extras = data.getExtras();
                Bitmap thePic = extras.getParcelable("data");
                String imgcurTime = dateFormat.format(new Date());
                File imageDirectory = new File(GridViewDemo_ImagePath);
                imageDirectory.mkdirs();
                 _path = GridViewDemo_ImagePath + imgcurTime+".jpg";
                Log.e("path", String.valueOf(_path));
                try {
                    FileOutputStream out = new FileOutputStream(_path);
                    thePic.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.flush();
                    out.close();
                } catch (FileNotFoundException e) {
                    e.getMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                listOfImagesPath = null;
                listOfImagesPath = RetriveCapturedImagePath();
                Log.e("values", String.valueOf(listOfImagesPath));

                if(listOfImagesPath!=null){
                    grid.setAdapter(new ImageListAdapter(this,listOfImagesPath));
                }
            }
        }
    }

    private List<String> RetriveCapturedImagePath() {
        List<String> tFileList = new ArrayList<String>();
        File f = new File(GridViewDemo_ImagePath);
        if (f.exists()) {
            File[] files=f.listFiles();
            Arrays.sort(files);

            for(int i=0; i<files.length; i++){
                File file = files[i];
                if(file.isDirectory())
                    continue;
                tFileList.add(file.getPath());
            }
        }
        return tFileList;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        LocationRequest mLocationRequest = createLocationRequest();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        latitude=location.getLatitude();
        longitude=location.getLongitude();

        latlog=String.valueOf(latitude+","+longitude);

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale((this),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                if(gps.canGetLocation){
                    geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    latitude=gps.getLatitude();
                    longitude=gps.getLongitude();
                    try {
                        addresses = geocoder.getFromLocation(gps.getLatitude(),gps.getLongitude(), 1);
                        add=addresses.get(0).getAddressLine(0);
                        // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //lat.setText(String.valueOf(location.getLatitude()+"_"+location.getLongitude()));
                    loc=String.valueOf(gps.getLatitude()+"_"+gps.getLongitude());
                    lat.setText(add);
                    latlog=String.valueOf(gps.getLatitude()+","+gps.getLongitude());
                    value=loc;
                }
                else {
                    gps.showSettingsAlert();
                }

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public class ImageListAdapter extends BaseAdapter
    {
        private Context context;
        private List<String> imgPic;
        public ImageListAdapter(Context c, List<String> thePic)
        {
            context = c;
            imgPic = thePic;
        }
        public int getCount() {
            if(imgPic != null)
                return imgPic.size();
            else
                return 0;
        }

        //---returns the ID of an item---
        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        //---returns an ImageView view---
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ImageView imageView;
            BitmapFactory.Options bfOptions=new BitmapFactory.Options();
            bfOptions.inDither=false;                     //Disable Dithering mode
            bfOptions.inPurgeable=true;                   //Tell to gc that whether it needs free memory, the Bitmap can be cleared
            bfOptions.inInputShareable=true;              //Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future
            bfOptions.inTempStorage=new byte[32 * 1024];
            if (convertView == null) {
                imageView = new ImageView(context);
                GridView.LayoutParams lp= new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
               // view.setLayoutParams(/* your layout params */); //where view is cell view

                imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                imageView.setPadding(10, 10, 10, 10);
              // imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                imageView = (ImageView) convertView;
            }
            FileInputStream fs = null;
            Bitmap bm;
            try {
                fs = new FileInputStream(new File(imgPic.get(position).toString()));

                if(fs!=null) {
                    bm=BitmapFactory.decodeFileDescriptor(fs.getFD(), null, bfOptions);
                    imageView.setImageBitmap(bm);
                    imageView.setId(position);
                    imageView.setLayoutParams(new GridView.LayoutParams(500, 500));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally{
                if(fs!=null) {
                    try {
                        fs.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return imageView;
        }
    }
}