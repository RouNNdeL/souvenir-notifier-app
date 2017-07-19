package com.roundel.souvenirnotifier.entities;
/*
 * Created by Krzysiek on 19/07/2017.
 */

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.GsonBuilder;
import com.roundel.souvenirnotifier.api.ApiTokens;
import com.roundel.souvenirnotifier.api.SteamCalls;
import com.roundel.souvenirnotifier.api.SteamEntities;
import com.roundel.souvenirnotifier.utils.GsonUriAdapter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class User
{
    private static final String TAG = User.class.getSimpleName();

    private static final Pattern REGEX_URL = Pattern.compile("^(https?://)?(www.)?steamcommunity\\.com/(id|profiles)/([^/]*)");
    private static final Pattern REGEX_ID64 = Pattern.compile("^[0-9]{17}$");

    private static final String TYPE_STEAM_ID64 = "profiles";
    private static final String TYPE_VANITY_NAME = "id";

    private long steamId64;
    private String username;
    private Uri avatar32;
    private Uri avatar64;
    private Uri avatarFull;

    private User(SteamEntities.PlayerSummary summary)
    {
        this.steamId64 = summary.steamId64;
        this.username = summary.username;
        this.avatar32 = summary.avatar;
        this.avatar64 = summary.avatarMedium;
        this.avatarFull = summary.avatarFull;
    }

    private static void fromVanityName(String vanityName, final OnUserResolvedListener listener)
    {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SteamCalls.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        SteamCalls steamCalls = retrofit.create(SteamCalls.class);
        Call<SteamEntities.VanityUrlResponse> vanityUrlCall = steamCalls.resolveId(ApiTokens.STEAM, vanityName);
        vanityUrlCall.enqueue(new Callback<SteamEntities.VanityUrlResponse>()
        {
            @Override
            public void onResponse(@NonNull Call<SteamEntities.VanityUrlResponse> call,
                                   @NonNull Response<SteamEntities.VanityUrlResponse> response)
            {
                final SteamEntities.VanityUrlResponse body = response.body();
                if(body != null && body.data != null)
                    if(body.data.getSuccessCode() == 1)
                    {
                        withSummary(body.data.getSteamId64(), listener);
                    }
                    else
                    {
                        Log.d(TAG, body.data.getErrorMessage());
                        listener.onUserResolved(null);
                    }
                else
                {
                    listener.onUserResolved(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<SteamEntities.VanityUrlResponse> call, @NonNull Throwable t)
            {
                listener.onUserResolved(null);
            }
        });
    }

    private static void fromSteamId64(long steamId64, OnUserResolvedListener listener)
    {
        withSummary(steamId64, listener);
    }

    private static void fromUrl(String url, OnUserResolvedListener listener)
    {
        Matcher matcher = REGEX_URL.matcher(url);
        if(matcher.matches())
        {
            String type = matcher.group(3);
            String id = matcher.group(4);
            switch(type)
            {
                case TYPE_STEAM_ID64:
                {
                    User.fromSteamId64(Long.parseLong(id), listener);
                    break;
                }
                case TYPE_VANITY_NAME:
                {
                    User.fromVanityName(id, listener);
                    break;
                }
                default:
                {
                    listener.onUserResolved(null);
                }
            }
        }
        else
        {
            listener.onUserResolved(null);
        }
    }

    private static void withSummary(long steamId64, OnUserResolvedListener listener)
    {
        loadSummary(steamId64, summary ->
        {
            if(summary != null)
            {
                listener.onUserResolved(new User(summary));
            }
            else
            {
                listener.onUserResolved(null);
            }
        });
    }

    public static void autoDetect(String username, String input, OnUserResolvedListener listener)
    {
        Matcher urlMatcher = REGEX_URL.matcher(input);
        Matcher id64Matcher = REGEX_ID64.matcher(input);
        if(urlMatcher.matches())
        {
            User.fromUrl(input, listener);
        }
        else if(id64Matcher.matches())
        {
            User.fromSteamId64(Long.parseLong(input), listener);
        }
        else
        {
            User.fromVanityName(input, listener);
        }
    }

    private static void loadSummary(long steamId64, OnSummaryLoadedListener listener)
    {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Uri.class, new GsonUriAdapter());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SteamCalls.API_URL)
                .addConverterFactory(GsonConverterFactory.create(builder.create()))
                .build();
        SteamCalls steamCalls = retrofit.create(SteamCalls.class);
        Call<SteamEntities.PlayerSummariesResponse>
                summariesResponseCall = steamCalls.getSummary(ApiTokens.STEAM, steamId64);
        summariesResponseCall.enqueue(new Callback<SteamEntities.PlayerSummariesResponse>()
        {
            @Override
            public void onResponse(@NonNull Call<SteamEntities.PlayerSummariesResponse> call,
                                   @NonNull Response<SteamEntities.PlayerSummariesResponse> response)
            {
                final SteamEntities.PlayerSummariesResponse summaries = response.body();
                if(summaries != null && summaries.getPlayerSummaries() != null &&
                        summaries.getPlayerSummaries().size() == 1)
                {
                    listener.onSummaryLoaded(summaries.getPlayerSummaries().get(0));
                }
            }

            @Override
            public void onFailure(@NonNull Call<SteamEntities.PlayerSummariesResponse> call, @NonNull Throwable t)
            {
                t.printStackTrace();
                listener.onSummaryLoaded(null);
            }
        });
    }

    @Override
    public String toString()
    {
        return "User{" +
                "username='" + username + '\'' +
                ", steamId64=" + steamId64 +
                '}';
    }

    public void checkInventory(OnInventoryCheckFinished listener)
    {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SteamCalls.COMMUNITY_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        SteamCalls steamCalls = retrofit.create(SteamCalls.class);
        Call<SteamEntities.Inventory> inventoryCall = steamCalls.getInventory(this.steamId64, 1);
        inventoryCall.enqueue(new Callback<SteamEntities.Inventory>()
        {
            @Override
            public void onResponse(@NonNull Call<SteamEntities.Inventory> call,
                                   @NonNull Response<SteamEntities.Inventory> response)
            {
                listener.onInventoryCheckFinished(response.body() != null);
            }

            @Override
            public void onFailure(@NonNull Call<SteamEntities.Inventory> call, @NonNull Throwable t)
            {
                listener.onInventoryCheckFinished(false);
            }
        });
    }

    public interface OnUserResolvedListener
    {
        void onUserResolved(User user);
    }

    @SuppressWarnings("WeakerAccess")
    public interface OnInventoryCheckFinished
    {
        void onInventoryCheckFinished(boolean accessible);
    }

    interface OnSummaryLoadedListener
    {
        void onSummaryLoaded(SteamEntities.PlayerSummary summary);
    }
}
