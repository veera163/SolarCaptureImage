package com.example.home.solarcaptureimage.api;



import com.example.home.solarcaptureimage.api.response.UploadImageResult;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {

    /*
    Retrofit get annotation with our URL
    And our method that will return us the List of Contacts
    */
    @Multipart
    @POST("upload/v3/latLng/{latlog}/location/{address}")
    Call<UploadImageResult> uploadImage(@Part MultipartBody.Part[]  file, @Path("latlog") String latlog, @Path("address") String address);
   /* @GET("location")
    Call<Result> test();*/
}
