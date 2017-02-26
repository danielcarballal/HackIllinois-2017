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
import android.support.annotation.Nullable;
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

    int MAX_NUM_PLAYERS = 10;
    int BUFF_SIZE = 20;
    long DIFF_TIME = 1;
    int numPlayers;
    public HashMap<String, String> macToName;
    public HashMap<String, Integer> playerDists;
    public HashMap<String, Boolean> players; // Boolean checks if the player is a hider or seeker
    public HashMap<String, Long> timeStamp;

    public Timer time;
    public long startTime;
    private static GameLogic ref;

    private String[] mac_addr;
    private int[] head;
    private int[] tail;
    private int[][] rssi;
    private long[][] prev_timestamps;
    Date date;

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
        this.mac_addr = new String[MAX_NUM_PLAYERS];
        this.head = new int[MAX_NUM_PLAYERS];
        this.tail = new int[MAX_NUM_PLAYERS];
        this.rssi = new int[MAX_NUM_PLAYERS][BUFF_SIZE];
        this.prev_timestamps = new long[MAX_NUM_PLAYERS][BUFF_SIZE];
        this.date = new Date();
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
        this.mac_addr[numPlayers] = macAddress;
        this.head[numPlayers] = 0;
        this.tail[numPlayers] = 0;
        this.rssi[numPlayers] = new int[BUFF_SIZE];
        this.prev_timestamps[numPlayers] = new long[BUFF_SIZE];
        this.numPlayers++;
        macToName.put(macAddress, playerId);
        players.put(playerId, false);
        return true;
    }

    public int getNumPlayer(){return numPlayers;  }

    public String getMacFromPlayer(int player) {
        if (player < numPlayers) {
            return mac_addr[player];
        } else {
            return null;
        }
    }

    public void addLocation(String playerMac, int distance) {
        if(!playerMac.isEmpty()) {
            for (int player = 0; player < numPlayers; player++) {
                if (this.mac_addr[player].equals(playerMac)) {
                    //System.out.println("Cur Rssi: " + cur_rssi + "   Time:  " + date.getTime());
                    this.prev_timestamps[player][head[player]] = date.getTime();
                    this.rssi[player][head[player]] = distance;
                    head[player] = (head[player] + 1) % BUFF_SIZE;
                    if (tail[player] == head[player]) {
                        tail[player]++;
                    }
                }
            }
        }
        playerDists.put(playerMac, distance);
    }

    public int GetAvgRssi(int player) {
        int sum = 0;
        int total = 0;
        long cur_time = date.getTime();
        while(head[player] != tail[player]) {
            if(cur_time - prev_timestamps[player][tail[player]] < DIFF_TIME) {
                sum = sum + this.rssi[player][tail[player]];
                total = total + 1;
                //System.out.println("Head: " + head[player] + "   Tail: " + tail[player] + "  Rssi : " + this.rssi[player][tail[player]]);
            }
            tail[player] = (tail[player] + 1) % BUFF_SIZE;
        }

        if(total != 0) {
            int ret = sum / total;
            //System.out.println("Total: " + total + "   Sum: " + sum + "  ret : " + ret);
            return ret;
        }
        return 1;
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