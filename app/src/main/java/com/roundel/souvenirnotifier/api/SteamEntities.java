package com.roundel.souvenirnotifier.api;
/*
 * Created by Krzysiek on 19/07/2017.
 */

import com.google.gson.annotations.SerializedName;

public class SteamEntities
{
    public class VanityUrlResponse
    {
        @SerializedName("response")
        public SteamEntities.VanityUrl data;
    }

    @SuppressWarnings("WeakerAccess")
    public class VanityUrl
    {
        @SerializedName("steamid")
        private long steamId64;

        @SerializedName("success")
        private int successCode;

        @SerializedName("message")
        private String errorMessage;

        public VanityUrl(long steamId64, int successCode, String errorMessage)
        {
            this.steamId64 = steamId64;
            this.successCode = successCode;
            this.errorMessage = errorMessage;
        }

        public long getSteamId64()
        {
            return steamId64;
        }

        public int getSuccessCode()
        {
            return successCode;
        }

        public String getErrorMessage()
        {
            return errorMessage;
        }
    }
}
