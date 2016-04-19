package com.ken.base.dao;

/**
 * Created by ken on 19/4/16.
 */
public class BaseDao {
    private String uuid;
    private long timestamp;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public BaseDao()
    {
        timestamp = System.currentTimeMillis();
    }
}
