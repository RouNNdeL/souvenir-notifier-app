package com.roundel.souvenirnotifier.entities;
/*
 * Created by Krzysiek on 19/07/2017.
 */

import android.support.annotation.NonNull;
import android.util.Log;

import com.roundel.souvenirnotifier.api.ApiTokens;
import com.roundel.souvenirnotifier.api.SteamCalls;
import com.roundel.souvenirnotifier.api.SteamEntities;

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

    private String username;
    private long steamId64;

    private User(String username, long steamId64)
    {
        this.username = username;
        this.steamId64 = steamId64;
    }

    private static void fromVanityName(final String username, String vanityName, final OnUserResolvedListener listener)
    {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.steampowered.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        SteamCalls steamCalls = retrofit.create(SteamCalls.class);
        Call<SteamEntities.VanityUrlResponse> vanityUrlCall = steamCalls.resolveId(ApiTokens.STEAM, vanityName);
        vanityUrlCall.enqueue(new Callback<SteamEntities.VanityUrlResponse>()
        {
            @Override
            public void onResponse(@NonNull Call<SteamEntities.VanityUrlResponse> call, @NonNull Response<SteamEntities.VanityUrlResponse> response)
            {
                final SteamEntities.VanityUrlResponse body = response.body();
                if(body != null && body.data != null)
                    if(body.data.getSuccessCode() == 1)
                    {
                        listener.onUserResolved(new User(username, body.data.getSteamId64()));
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

    private static void fromSteamId64(String username, long steamId64, OnUserResolvedListener listener)
    {
        listener.onUserResolved(new User(username, steamId64));
    }

    private static void fromUrl(String username, String url, OnUserResolvedListener listener)
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
                    User.fromSteamId64(username, Long.parseLong(id), listener);
                    break;
                }
                case TYPE_VANITY_NAME:
                {
                    User.fromVanityName(username, id, listener);
                    break;
                }
                default:
                    throw new IllegalStateException("Steam url type is " + type);
            }
        }
        else
        {
            listener.onUserResolved(null);
        }
    }

    public static void autoDetect(String username, String input, OnUserResolvedListener listener)
    {
        Matcher urlMatcher = REGEX_URL.matcher(input);
        Matcher id64Matcher = REGEX_ID64.matcher(input);
        if(urlMatcher.matches())
        {
            User.fromUrl(username, input, listener);
        }
        else if(id64Matcher.matches())
        {
            User.fromSteamId64(username, Long.parseLong(input), listener);
        }
        else
        {
            User.fromVanityName(username, input, listener);
        }
    }

    public void checkInventory(OnInventoryCheckFinished listener)
    {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://steamcommunity.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        SteamCalls steamCalls = retrofit.create(SteamCalls.class);
        Call<SteamEntities.Inventory> inventoryCall = steamCalls.getInventory(this.steamId64, 1);
        inventoryCall.enqueue(new Callback<SteamEntities.Inventory>()
        {
            @Override
            public void onResponse(@NonNull Call<SteamEntities.Inventory> call, @NonNull Response<SteamEntities.Inventory> response)
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

    @Override
    public String toString()
    {
        return "User{" +
                "username='" + username + '\'' +
                ", steamId64=" + steamId64 +
                '}';
    }

    public interface OnUserResolvedListener
    {
        void onUserResolved(User user);
    }

    public interface OnInventoryCheckFinished
    {
        void onInventoryCheckFinished(boolean accessible);
    }
}
