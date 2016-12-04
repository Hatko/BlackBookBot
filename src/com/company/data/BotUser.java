package com.company.data;

/**
 * Created by vlad on 9/11/16.
 */
public class BotUser {
    public String userId;
    public String firstName;
    public String lastName;
    public String userName;
    public int commandState = 0;

    BotUser() {

    }

    public BotUser(String userId, String firstName, String lastName, String userName) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
    }
}
