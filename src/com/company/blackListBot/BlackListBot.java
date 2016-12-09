package com.company.blackListBot;

import com.company.data.BotUser;
import com.company.data.Complaint;
import com.company.dataBase.DataBaseManager;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.MessageEntity;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by vlad on 10/8/16.
 */
public class BlackListBot extends TelegramLongPollingBot implements IBotCommanderDelegate {
    @Override
    public String getBotToken() {
        return BotConfig.BOT_TOKEN;
    }

    @Override
    public String getBotUsername() {
        return BotConfig.BOT_USERNAME;
    }

    @Override
    public void onUpdateReceived(Update update) {
        DataBaseManager.sharedInstance();

        User user = update.getMessage().getFrom();

        String userId = user.getId().toString();

        BotUser botUser = DataBaseManager.sharedInstance().userWithId(userId);

        if (botUser == null) {
            botUser = new BotUser(user.getId().toString(), user.getFirstName(), user.getLastName(), user.getUserName());

            DataBaseManager.sharedInstance().insertNewUserIfNeeded(botUser);
        }

        BotCommander commander = commanders.get(botUser.userId);

        if (commander == null) {
            commander = new BotCommander(botUser.userId, this);
            commanders.put(botUser.userId, commander);
        }

        Boolean commandProcessed = false;

        List<MessageEntity> entities = update.getMessage().getEntities();
        if (entities != null) {
            List<MessageEntity> commands = entities.stream().filter(entity -> entity.getType() != null && entity.getType().equals("bot_command")).collect(Collectors.toList());

            for (MessageEntity command : commands) {
                commander.processCommand(command.getText());
                commandProcessed = true;
            }
        }

        if (!commandProcessed) {
            commander.processInput(update.getMessage().getText());
        }
    }

    private void sendMessage(String msg, String userId) {
        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.enableHtml(true);
        sendMessageRequest.setChatId(userId); //who should get from the message the sender that sent it.
        sendMessageRequest.setText(msg);
        try {
            sendMessage(sendMessageRequest); //at the end, so some magic and send the message ;)
        } catch (TelegramApiException e) {
            System.out.println("SendMessage request failed " + e.getLocalizedMessage());
        }
    }

    private Map<String, BotCommander> commanders = new HashMap<>();


    //region IBotCommanderDelegate
    @Override
    public void printMessage(String msg, BotCommander commander) {
        sendMessage(msg, commander.userId);
    }

    @Override
    public void changeCommandState(BotCommander commander) {
        DataBaseManager.sharedInstance().updateCommandState(commander.getState().value, commander.userId);
    }

    @Override
    public void addNumberToBlackList(Complaint complaint) {
        DataBaseManager.sharedInstance().addComplaintToBlackList(complaint);
    }

    @Override
    public void canBeRemoved(BotCommander commander) {
        commanders.remove(commander.userId);
    }

    @Override
    public ArrayList<Complaint> fetchAllComplaints(String number) {
        return DataBaseManager.sharedInstance().fetchComplaints(number);
    }

    @Override
    public ArrayList<Complaint> fetchAllComplaintsForUser(String userId) {
        return DataBaseManager.sharedInstance().fetchAllComplaintsForUser(userId);
    }

    @Override
    public Complaint existedComplaint(String userId, String number) {
        return DataBaseManager.sharedInstance().existedComplaint(userId, number);
    }

    @Override
    public boolean removeComplaint(String userId, String number) {
        return DataBaseManager.sharedInstance().removeComplaint(userId, number);
    }
    //endregion
}
