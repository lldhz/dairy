package com.ken.base.redis.cache;

import com.ken.base.properties.RedisLoader;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by Ken on 2015/12/18.
 */
public class RedisCachePool {
    private JedisPool pool;
    private static final RedisCachePool cachePool = new RedisCachePool();
    private RedisLoader configLoader = RedisLoader.getInstance();

    public static RedisCachePool getInstance() {
        return cachePool;
    }

    private RedisCachePool() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(500);
        config.setMaxIdle(5);
        config.setMaxWaitMillis(1000 * 100);
        config.setTestOnBorrow(false);
        pool = new JedisPool(config,
                configLoader.getProperty("mybatis.host"),
                Integer.valueOf(configLoader.getProperty("mybatis.port")),
                Integer.valueOf(configLoader.getProperty("maxWaitMillis")),
                configLoader.getProperty("mybatis.password"));
    }

    public Jedis getJedis() {
        return pool.getResource();
    }

    public void returnBrokenResource(Jedis jedis) {
        if (jedis != null)
            pool.returnBrokenResource(jedis);
    }

    public void returnResource(Jedis jedis) {
        if (jedis != null)
            pool.returnResource(jedis);
    }

    public void release(Jedis jedis, boolean isBroken) {
        if (jedis != null) {
            if (isBroken) {
                pool.returnBrokenResource(jedis);
            } else {
                pool.returnResource(jedis);
            }
        }
    }
    // public JedisPool getJedisPool() {
    // return this.pool;
    // }
}