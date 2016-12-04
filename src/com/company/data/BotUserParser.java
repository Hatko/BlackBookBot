package com.company.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by vlad on 11/13/16.
 */
public class BotUserParser {
    public static void setConnection(Connection connection) {
        BotUserParser.connection = connection;
    }

    public static BotUser userFromResultSet(ResultSet resultSet) {
        BotUser newUser = new BotUser();

        try {
            newUser.userId = resultSet.getString(1);
            newUser.firstName = resultSet.getString(2);
            newUser.lastName = resultSet.getString(3);
            newUser.userName = resultSet.getString(4);
            newUser.commandState = resultSet.getInt(5);
        } catch (SQLException e) {
            System.out.println("can't parse botUser from resultSet " + e.getLocalizedMessage());
        }

        return newUser;
    }

    public static Tag tagFromResultSet(ResultSet resultSet) {
        Tag newTag = new Tag();

        try {
            newTag.text = resultSet.getString(1);
            newTag.numberOfUses = resultSet.getInt(2);
        } catch (SQLException e) {
            System.out.println("can't parse botUser from resultSet " + e.getLocalizedMessage());
        }

        return newTag;
    }

    public static Complaint complaintFromResultSet(ResultSet resultSet) {
        Complaint complaint = new Complaint();

        try {
            complaint.complaintId = resultSet.getInt("complaintId");
            complaint.date = resultSet.getLong("date");
            complaint.number = resultSet.getString("number");
            complaint.description = resultSet.getString("description");
            complaint.userId = resultSet.getString("userId");
        } catch (SQLException e) {
            System.out.println("can't parse complaint with error: " + e.getLocalizedMessage());
        }

        return complaint;
    }

    public static PreparedStatement statementForUser(BotUser user) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO user (userId,firstName,lastName,userName) VALUES (?,?,?,?)"
            );
            statement.setString(1, user.userId);
            statement.setString(2, user.firstName);
            statement.setString(3, user.lastName);
            statement.setString(4, user.userName);
            return statement;
        } catch (SQLException e) {
            System.out.println("can't build statement for user with " + e.getLocalizedMessage());
        }

        return null;
    }

    public static PreparedStatement statementForTag(Tag tag) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO tag (text) VALUES (?)"
            );
            statement.setString(1, tag.text);
            return statement;
        } catch (SQLException e) {
            System.out.println("can't build statement for user with " + e.getLocalizedMessage());
        }

        return null;
    }

    public static PreparedStatement statementForTaggedComplaint(String tagText, Integer complaintId) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO tag_complaint_xref (tagText, complaintId) VALUES (?, ?)"
            );
            statement.setString(1, tagText);
            statement.setInt(2, complaintId);
            return statement;
        } catch (SQLException e) {
            System.out.println("can't build statement for user with " + e.getLocalizedMessage());
        }

        return null;
    }

    public static PreparedStatement statementForComplaint(Complaint complaint) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO complaint (complaintId, number, userId, description, date) VALUES (?,?,?,?,?)"
            );
            statement.setInt(1, complaint.complaintId);
            statement.setString(2, complaint.number);
            statement.setString(3, complaint.userId);
            statement.setString(4, complaint.description);
            statement.setLong(5, complaint.date);
            return statement;
        } catch (SQLException e) {
            System.out.println("can't build statement for complaint with " + e.getLocalizedMessage());
        }

        return null;
    }

    static private Connection connection;
}
