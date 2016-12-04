package com.company;

import com.company.blackListBot.BlackListBot;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.logging.BotLogger;

public class Main {
    public static void main(String[] args) {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new BlackListBot());
        } catch (TelegramApiException e) {
            BotLogger.error("error", e);
        }
    }
}
