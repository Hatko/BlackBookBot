package com.company.dataBase;

import com.company.data.BotUser;
import com.company.data.BotUserParser;
import com.company.data.Complaint;
import com.company.data.Tag;

import java.sql.*;
import java.util.ArrayList;

/**
 * Created by vlad on 9/3/16.
 */
public class DataBaseManager {
    public static DataBaseManager sharedInstance() {
        if (instance == null) {
            instance = new DataBaseManager();
        }
        return instance;
    }

    private ResultSet fetchUser(String userId) {
        try {
            PreparedStatement checkStatement = connection.prepareStatement(
                    "SELECT * FROM user WHERE (userId)=(?) LIMIT 1"
            );
            checkStatement.setString(1, userId);
            return checkStatement.executeQuery();
        } catch (SQLException e) {
            System.out.println("user fetching doesn't succeed " + e.getLocalizedMessage());
        }

        return null;
    }

    private ResultSet fetchTag(String tag) {
        try {
            PreparedStatement checkStatement = connection.prepareStatement(
                    "SELECT * FROM tag WHERE (text)=(?) LIMIT 1"
            );
            checkStatement.setString(1, tag);
            return checkStatement.executeQuery();
        } catch (SQLException e) {
            System.out.println("user fetching doesn't succeed " + e.getLocalizedMessage());
        }

        return null;
    }

    public void insertNewUserIfNeeded(BotUser user) {
        try {
            ResultSet userResult = fetchUser(user.userId);

            if (userResult == null || !userResult.next()) {
                PreparedStatement statement = BotUserParser.statementForUser(user);

                if (statement != null) {
                    statement.executeUpdate();
                }
            }
        } catch (Exception e) {
            System.out.println("Values insertion failed");
        }
    }

    public void updateCommandState(int newState, String userId) {
        try {
            String updateTableSQL = "UPDATE user SET commandState = ? WHERE userId = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(updateTableSQL);
            preparedStatement.setInt(1, newState);
            preparedStatement.setString(2, userId);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("user doesn't update " + e.getLocalizedMessage());
        }
    }

    public BotUser userWithId(String userId) {
        try {
            ResultSet userResult = fetchUser(userId);

            if (userResult != null && userResult.next()) {
                return BotUserParser.userFromResultSet(userResult);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Complaint existedComplaint(String userId, String number) {
        try {
            PreparedStatement checkStatement = connection.prepareStatement(
                    "SELECT * FROM complaint WHERE (userId)=(?) AND (number)=(?) LIMIT 1"
            );
            checkStatement.setString(1, userId);
            checkStatement.setString(2, number);

            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet != null && resultSet.next()) {
                return BotUserParser.complaintFromResultSet(resultSet);
            }
        } catch (Exception e) {
            System.out.println("Values insertion failed");
        }

        return null;
    }

    public void addComplaintToBlackList(Complaint complaint) {
        try {
            PreparedStatement checkStatement = connection.prepareStatement(
                    "SELECT MAX(complaintId) AS max_id FROM complaint"
            );
            ResultSet resultSet = checkStatement.executeQuery();

            int nextComplaintID = 0;

            if (resultSet != null && resultSet.next()) {
                nextComplaintID = resultSet.getInt("max_id");
                nextComplaintID++;
            }

            complaint.complaintId = nextComplaintID;

            PreparedStatement statement = BotUserParser.statementForComplaint(complaint);

            if (statement != null) {
                statement.executeUpdate();
            }

            for (Tag tag : complaint.tags) {
                Tag fetchedTag = fetchOrCreateNewTag(tag);

                if (fetchedTag == null) {
                    fetchedTag = tag;
                }

                ++fetchedTag.numberOfUses;

                updateTag(fetchedTag);

                addNewLink(fetchedTag.text, complaint.complaintId);
            }
        } catch (Exception e) {
            System.out.println("Values insertion failed");
        }
    }

    private void updateTag(Tag tag) {
        try {
            PreparedStatement updateStatement = connection.prepareStatement(
                    "UPDATE tag SET numberOfUses = ? WHERE text = ?"
            );

            updateStatement.setInt(1, tag.numberOfUses);
            updateStatement.setString(2, tag.text);

            updateStatement.executeUpdate();
        } catch (Exception e) {
            System.out.println("Values update failed");
        }
    }

    public ArrayList<Complaint> fetchAllComplaintsForUser(String userId) {
        try {
            PreparedStatement checkStatement = connection.prepareStatement(
                    "SELECT * FROM complaint WHERE (userId)=(?)"
            );
            checkStatement.setString(1, userId);


            ResultSet resultSet = checkStatement.executeQuery();

            ArrayList<Complaint> complaints = new ArrayList<>();

            if (resultSet != null) {
                while (resultSet.next()) {
                    Complaint complaint = BotUserParser.complaintFromResultSet(resultSet);

                    if (complaint != null) {
                        complaints.add(complaint);
                    }
                }
            }

            return complaints;
        } catch (Exception e) {
            return null;
        }
    }

    public ArrayList<Complaint> fetchComplaints(String phoneNumber) {
        try {
            PreparedStatement checkStatement = connection.prepareStatement(
                    "SELECT * FROM complaint WHERE (number)=(?)"
            );
            checkStatement.setString(1, phoneNumber);

            ResultSet resultSet = checkStatement.executeQuery();

            ArrayList<Complaint> complaints = new ArrayList<>();

            if (resultSet != null) {
                while (resultSet.next()) {
                    Complaint complaint = BotUserParser.complaintFromResultSet(resultSet);

                    if (complaint != null) {
                        complaints.add(complaint);
                    }
                }
            }

            return complaints;
        } catch (Exception e) {
            return null;
        }
    }

    private Tag fetchOrCreateNewTag(Tag tag) {
        try {
            ResultSet userResult = fetchTag(tag.text);

            if (userResult == null || !userResult.next()) {
                saveTag(tag);
            } else {
                return BotUserParser.tagFromResultSet(userResult);
            }
        } catch (Exception e) {
            System.out.println("Values insertion failed");
        }

        return null;
    }

    private void saveTag(Tag tag) {
        try {
            PreparedStatement statement = BotUserParser.statementForTag(tag);

            if (statement != null) {
                statement.executeUpdate();
            }
        } catch (Exception e) {
            System.out.println("Values insertion failed");
        }
    }

    private void addNewLink(String tagText, int complaintId) {
        try {
            PreparedStatement statement = BotUserParser.statementForTaggedComplaint(tagText, complaintId);

            if (statement != null) {
                statement.executeUpdate();
            }
        } catch (Exception e) {
            System.out.println("Values insertion failed");
        }
    }

    public ArrayList<BotUser> getAllUserIDs() {
        ArrayList<BotUser> ids = new ArrayList<>();

        try {
            PreparedStatement checkStatement = connection.prepareStatement("SELECT * FROM user");
            ResultSet resultSet = checkStatement.executeQuery();

            while (resultSet.next()) {
                BotUser user = new BotUser(resultSet.getString("userId"), resultSet.getString("firstName"), resultSet.getString("lastName"), resultSet.getString("userName"));

                ids.add(user);
            }
        } catch (Exception e) {
            System.out.println("Values insertion failed");
        }

        return ids;
    }

    private static DataBaseManager instance = null;
    private static Connection connection;

    private DataBaseManager() {
        makeConnection();

        BotUserParser.setConnection(connection);
    }

    private void makeConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:res/telegramBotDB.db");
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");
    }
}
