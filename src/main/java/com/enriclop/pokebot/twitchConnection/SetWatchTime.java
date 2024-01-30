package com.enriclop.pokebot.twitchConnection;

import com.enriclop.pokebot.modelo.User;

import java.util.List;

public class SetWatchTime extends Thread {

    private static final int MINUTES = 10;

    private TwitchConnection twitchConnection;

    private List<User> usersInChat;

    public SetWatchTime(TwitchConnection twitchConnection) {
        this.twitchConnection = twitchConnection;
    }

    @Override
    public void run() {
        while (true) {
            wait(MINUTES);
            setWatchTime();
        }
    }

    private void setWatchTime() {
        System.out.println("Comprobando usuarios en chat");

        List<User> usersChat = twitchConnection.getChattersUsers();

        if (usersInChat != null && !usersInChat.isEmpty()) {
            for (User user : usersInChat) {
                if (usersChat.contains(user)) {
                    user.getWatchTime().addMinutes(MINUTES);
                    twitchConnection.userService.saveUser(user);
                }
            }
        }
        usersInChat = usersChat;
    }

    private void wait(int minutes) {
        try {
            Thread.sleep(1000 * 60 * minutes);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
