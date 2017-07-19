package com.roundel.souvenirnotifier.api;
/*
 * Created by Krzysiek on 19/07/2017.
 */

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SteamCalls
{
    @GET("ISteamUser/ResolveVanityURL/v0001")
    Call<SteamEntities.VanityUrlResponse> resolveId(@Query("key") String apiKey, @Query("vanityurl") String vanityName);

    @GET("inventory/{user_id}/730/2")
    Call<SteamEntities.Inventory> getInventory(@Path("user_id") long steam64Id, @Query("count") int count);
}
