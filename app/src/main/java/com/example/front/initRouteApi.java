package com.example.front;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface initRouteApi {
    //@통신 방식("통신 API명")
    @GET("/users/poisearch")
    Call<LoginResponse> getRouteResponse(@Body LoginRequest loginRequest);

}