package com.enriclop.pokebot.twitchConnection;

public class Spawn extends Thread {

    TwitchConnection conn;

    public Boolean active = true;

    private static final int CD_MINUTES = 10;

    public Spawn(TwitchConnection twitchConnection) {
        this.conn = twitchConnection;
    }

    public void run() {
        while (active) {
            try {
                Thread.sleep(1000 * 60 * CD_MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            conn.spawnPokemon();
        }
    }

}
