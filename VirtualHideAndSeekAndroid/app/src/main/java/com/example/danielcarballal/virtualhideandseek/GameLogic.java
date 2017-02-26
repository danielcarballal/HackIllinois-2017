package com.example.danielcarballal.virtualhideandseek;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.Date;
import java.util.TimerTask;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import java.util.PriorityQueue;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * Created by danielcarballal on 25-2-17.
 */

public class GameLogic {
    // Hash map of strings representing players and their respective distances

    int numPlayers;
    public HashMap<String, String> macToName;
    public HashMap<String, Integer> playerDists;
    public HashMap<String, Boolean> players; // Boolean checks if the player is a hider or seeker
    public HashMap<String, Long> timeStamp;
    public Timer time;
    public long startTime;
    private static GameLogic ref;

    // Singleton constructor
    public static GameLogic getGameLogic(){
        if(ref == null)
            ref = new GameLogic();
        return ref;
    }

    private GameLogic(){
        this.numPlayers = 0;
        players = new HashMap<String, Boolean>();
        macToName = new HashMap<String, String>();
        playerDists = new HashMap<String, Integer>();
        timeStamp = new HashMap<String, Long>();
        startTime = SystemClock.elapsedRealtime();
    }

    public String getPlayer(String macAddress){
        return macToName.get(macAddress);
    }

    public boolean playerExists(String macAddress){
        return macToName.containsKey(macAddress);
    }
    // Returns false if player already exists
    public boolean addPlayer(String playerId, String macAddress){
        if(macToName.containsKey(macAddress)) return false;
        this.addLocation(macAddress, -100); // Default start
        this.numPlayers++;
        macToName.put(macAddress, playerId);
        players.put(playerId, false);
        return true;
    }

    public void addLocation(String playerMac, int distance) {
        playerDists.put(playerMac, distance);
    }

    public void startTimer(){
        startTime = SystemClock.elapsedRealtime();
    }

    // Returns the names of up to the top 5 players
    public ArrayList<String> getTopPlayerNames(){
        ArrayList<String> a = new ArrayList<String>();
        List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(playerDists.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {

            public int compare(Map.Entry<String, Integer> m1, Map.Entry<String, Integer> m2) {
                return (m2.getValue()).compareTo(m1.getValue());
            }
        });
        int count = 0;
        for (Map.Entry<String, Integer> entry : list) {
            a.add(entry.getKey());
            count++;
            if(count >= 5) break;
        }
        return a;
    }

    public ArrayList<Integer> getTopDistances(){
        ArrayList<Integer> a = new ArrayList<Integer>();
        List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(playerDists.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {

            public int compare(Map.Entry<String, Integer> m1, Map.Entry<String, Integer> m2) {
                return (m2.getValue()).compareTo(m1.getValue());
            }
        });
        int count = 0;
        for (Map.Entry<String, Integer> entry : list) {
            a.add(entry.getValue());
            count++;
            if(count >= 5) break;
        }
        return a;
    }
}