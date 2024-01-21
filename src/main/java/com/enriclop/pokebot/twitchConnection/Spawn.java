package com.enriclop.pokebot.twitchConnection;

public class Spawn extends Thread {

    TwitchConnection conn;

    public Boolean active = true;

    public int cdMinutes = 5;

    public Spawn() {
    }

    public Spawn(TwitchConnection conn, int cdMinutes) {
        this.conn = conn;
        this.cdMinutes = cdMinutes;
    }

    public void run() {
        System.out.println("Spawn started");
        System.out.println("cdMinutes: " + cdMinutes);
        while (active) {
            try {
                Thread.sleep(1000 * 60 * cdMinutes);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (active)  {
                conn.spawnPokemon();
            }
         }
    }

}
