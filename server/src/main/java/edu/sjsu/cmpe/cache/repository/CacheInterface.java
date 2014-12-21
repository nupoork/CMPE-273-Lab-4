package edu.sjsu.cmpe.cache.repository;

import java.util.List;

import edu.sjsu.cmpe.cache.domain.Entry;

public interface CacheInterface {
    /**
     * Save a new entry in the repository
     *

     *            a entry instance to be create in the repository
     * @return an entry instance
     */
    Entry save(Entry newEntry);

    /**
     * Retrieve an existing entry by key
     *
     * @param key
     *            a valid key
     * @return a entry instance
     */
    Entry get(Long key);

    /*
    Delete exiting entry by key
     */
    void delete(Long key);

    /**
     * Retrieve all entries
     * 
     * @return a list of entries
     */
    List<Entry> getAll();

}
