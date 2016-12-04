package com.company.data;

/**
 * Created by vlad on 10/11/16.
 */
public class Complaint {
    public String userId;
    public Integer complaintId;
    public String number;
    public String description;
    public long date;

    public BotUser user;
    public Tag[] tags;
}
