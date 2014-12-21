package edu.sjsu.cmpe.cache.client;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.*;
import java.lang.InterruptedException;
import java.io.*;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.http.options.Options;

public class CRDTClient implements CRDTCallbackInterface {

    private ConcurrentHashMap<String, CacheServiceInterface> Servers;
    private ArrayList<String> success_servers;
    private ConcurrentHashMap<String, ArrayList<String>> dict_results;

    private static CountDownLatch countdown_Latch;

    public CRDTClient() {

    	Servers = new ConcurrentHashMap<String, CacheServiceInterface>(3);
        CacheServiceInterface cache0 = new DistributedCacheService("http://localhost:3000", this);
        CacheServiceInterface cache1 = new DistributedCacheService("http://localhost:3001", this);
        CacheServiceInterface cache2 = new DistributedCacheService("http://localhost:3002", this);
        Servers.put("http://localhost:3000", cache0);
        Servers.put("http://localhost:3001", cache1);
        Servers.put("http://localhost:3002", cache2);
    }

    
    @Override
    public void put_failed(Exception e) {
        System.out.println("Request failed");
        countdown_Latch.countDown();
    }

    @Override
    public void get_failed(Exception e) {
        System.out.println("Request failed");
        countdown_Latch.countDown();
    }
    
    @Override
    public void put_completed(HttpResponse<JsonNode> response, String serverUrl) {
        int c = response.getCode();
        System.out.println("Put response completed! The code " + c + " on server " + serverUrl);
        success_servers.add(serverUrl);
        countdown_Latch.countDown();
    }

    

    @Override
    public void get_completed(HttpResponse<JsonNode> response, String serverUrl) {

        String val = null;
        if (response != null && response.getCode() == 200) {
            val = response.getBody().getObject().getString("value");
                System.out.println("Value from server " + serverUrl + "is " + val);
            ArrayList servers_with_val = dict_results.get(val);
            if (servers_with_val == null) {
            	servers_with_val = new ArrayList(3);
            }
            servers_with_val.add(serverUrl);

   
            dict_results.put(val, servers_with_val);
        }

        countdown_Latch.countDown();
    }



    public boolean put(long key, String value) throws InterruptedException {
        success_servers = new ArrayList(Servers.size());
        countdown_Latch = new CountDownLatch(Servers.size());

        for (CacheServiceInterface cache : Servers.values()) {
            cache.put(key, value);
        }

        countdown_Latch.await();

        boolean is_success = Math.round((float)success_servers.size() / Servers.size()) == 1;

        if (! is_success) {
           delete(key, value);
        }
        return is_success;
    }


    
    public String get(long key) throws InterruptedException {
        dict_results = new ConcurrentHashMap<String, ArrayList<String>>();
        countdown_Latch = new CountDownLatch(Servers.size());

        for (final CacheServiceInterface server : Servers.values()) {
            server.get(key);
        }
        countdown_Latch.await();

                String right_val = dict_results.keys().nextElement();

        if (dict_results.keySet().size() > 1 || dict_results.get(right_val).size() != Servers.size()) {

            ArrayList<String> max_vals = max_key_table(dict_results);
            if (max_vals.size() == 1) {
        
                right_val = max_vals.get(0);

                ArrayList<String> server_repair = new ArrayList(Servers.keySet());
                server_repair.removeAll(dict_results.get(right_val));
  
                for (String serverUrl : server_repair) {
                    // Repair all Servers that don't have the correct value
                    System.out.println("Repairing: " + serverUrl + " value: " + right_val);
                    CacheServiceInterface server = Servers.get(serverUrl);
                    server.put(key, right_val);

                }

            } else {
                   }
        }

        return right_val;

    }


    public void delete(long key, String value) {

        for (final String serverUrl : success_servers) {
            CacheServiceInterface server = Servers.get(serverUrl);
            server.delete(key);
        }
    }


    public ArrayList<String> max_key_table(ConcurrentHashMap<String, ArrayList<String>> table) {
        ArrayList<String> max_keys= new ArrayList<String>();
        int max_val = -1;
        for(Map.Entry<String, ArrayList<String>> entry : table.entrySet()) {
            if(entry.getValue().size() > max_val) {
                max_keys.clear(); 
                max_keys.add(entry.getKey());
                max_val = entry.getValue().size();
            }
            else if(entry.getValue().size() == max_val)
            {
                max_keys.add(entry.getKey());
            }
        }
        return max_keys;
    }





}