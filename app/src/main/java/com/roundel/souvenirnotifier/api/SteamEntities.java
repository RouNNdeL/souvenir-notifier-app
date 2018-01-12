package com.roundel.souvenirnotifier.api;
/*
 * Created by Krzysiek on 19/07/2017.
 */

import android.net.Uri;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SteamEntities {
    public static class PlayerSummariesResponse {
        @SerializedName("response")
        private PlayersSummaries playerSummaries;

        public PlayerSummariesResponse(PlayersSummaries playerSummaries) {
            this.playerSummaries = playerSummaries;
        }

        public List<PlayerSummary> getPlayerSummaries() {
            return playerSummaries.playerSummaries;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class PlayersSummaries {
        @SerializedName("players")
        private List<PlayerSummary> playerSummaries;
    }

    public static class PlayerSummary {
        @SerializedName("steamid")
        public long steamId64;

        @SerializedName("personaname")
        public String username;

        @SerializedName("avatar")
        public Uri avatar;

        @SerializedName("avatarmedium")
        public Uri avatarMedium;

        @SerializedName("avatarfull")
        public Uri avatarFull;

        @SerializedName("loccountrycode")
        public String countryCode;
    }

    public static class Inventory {
        @SerializedName("total_inventory_count")
        public int totalCount;
    }

    public static class VanityUrlResponse {
        @SerializedName("response")
        public SteamEntities.VanityUrl data;
    }

    @SuppressWarnings("WeakerAccess")
    public static class VanityUrl {
        @SerializedName("steamid")
        private long steamId64;

        @SerializedName("success")
        private int successCode;

        @SerializedName("message")
        private String errorMessage;

        public VanityUrl(long steamId64, int successCode, String errorMessage) {
            this.steamId64 = steamId64;
            this.successCode = successCode;
            this.errorMessage = errorMessage;
        }

        public long getSteamId64() {
            return steamId64;
        }

        public int getSuccessCode() {
            return successCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
