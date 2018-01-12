package com.roundel.souvenirnotifier.entities;
/*
 * Created by Krzysiek on 19/07/2017.
 */

import java.util.List;

public class UserData {
    private List<SteamUser> steamUsers;
    private String token;

    public UserData(List<SteamUser> steamSteamUsers, String token) {
        this.steamUsers = steamSteamUsers;
        this.token = token;
    }

    public UserData() {
    }

    public List<SteamUser> getSteamUsers() {
        return steamUsers;
    }

    public void setSteamUsers(List<SteamUser> steamSteamUsers) {
        this.steamUsers = steamSteamUsers;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void addSteamUser(SteamUser user) {
        this.steamUsers.add(user);
    }
}
