package edu.sjsu.cmpe.cache.client;

import java.util.concurrent.Future;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.http.options.Options;

public class DistributedCacheService implements CacheServiceInterface {
    private final String cache_server_url;

    private CRDTCallbackInterface callback;
    
    public DistributedCacheService(String serverUrl, CRDTCallbackInterface callbk) {
        this.cache_server_url = serverUrl;
        this.callback = callbk;
    }


    public DistributedCacheService(String serverUrl) {
        this.cache_server_url = serverUrl;
    }


    @Override
    public void put(long key, String value) {
        Future<HttpResponse<JsonNode>> future = Unirest.put(this.cache_server_url + "/cache/{key}/{value}")
                .header("accept", "application/json")
                .routeParam("key", Long.toString(key))
                .routeParam("value", value)
                .asJsonAsync(new Callback<JsonNode>() {

                    public void failed(UnirestException e) {
                        callback.put_failed(e);
                    }

                    public void completed(HttpResponse<JsonNode> response) {
                        callback.put_completed(response, cache_server_url);
                    }

                    public void cancelled() {
                        System.out.println("Request cancelled");
                    }

                });
    }




    @Override
    public String get(long key) {
        Future<HttpResponse<JsonNode>> future = Unirest.get(this.cache_server_url + "/cache/{key}")
                .header("accept", "application/json")
                .routeParam("key", Long.toString(key))
                .asJsonAsync(new Callback<JsonNode>() {

                    public void failed(UnirestException e) {
                        callback.get_failed(e);
                    }

                    public void completed(HttpResponse<JsonNode> response) {
                        callback.get_completed(response, cache_server_url);
                    }

                    public void cancelled() {
                        System.out.println("The request has been cancelled");
                    }

                });

        return null;
    }



    @Override
    public void delete(long key) {
        HttpResponse<JsonNode> response = null;
        try {
            response = Unirest
                    .delete(this.cache_server_url + "/cache/{key}")
                    .header("accept", "application/json")
                    .routeParam("key", Long.toString(key))
                    .asJson();
        } catch (UnirestException e) {
            System.err.println(e);
        }

        System.out.println("Response is " + response);

        if (response == null || response.getCode() != 204) {
            System.out.println("Failed to delete from the cache.");
        } else {
            System.out.println("Deleted " + key + " from " + this.cache_server_url);
        }

    }
}
