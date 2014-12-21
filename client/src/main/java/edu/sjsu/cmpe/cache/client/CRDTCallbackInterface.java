package edu.sjsu.cmpe.cache.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;

public interface CRDTCallbackInterface {

    void put_completed (HttpResponse<JsonNode> response, String serverUrl);
    void get_completed (HttpResponse<JsonNode> response, String serverUrl);
    void put_failed (Exception e);
    void get_failed (Exception e);
}