package edu.sjsu.cmpe.cache.client;


public interface CacheServiceInterface {

    public void put(long key, String value);
    public void delete(long key);
    public String get(long key);

}
