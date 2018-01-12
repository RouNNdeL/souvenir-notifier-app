package com.roundel.souvenirnotifier.api;
/*
 * Created by Krzysiek on 19/07/2017.
 */

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SteamCalls {
    String API_URL = "https://api.steampowered.com";
    String COMMUNITY_URL = "https://steamcommunity.com";

    @GET("ISteamUser/ResolveVanityURL/v0001")
    Call<SteamEntities.VanityUrlResponse> resolveId(@Query("key") String apiKey, @Query("vanityurl") String vanityName);

    @GET("ISteamUser/GetPlayerSummaries/v0002")
    Call<SteamEntities.PlayerSummariesResponse> getSummary(@Query("key") String apiKey, @Query("steamids") long steam64Id);

    @GET("inventory/{user_id}/730/2")
    Call<SteamEntities.Inventory> getInventory(@Path("user_id") long steam64Id, @Query("count") int count);
}
