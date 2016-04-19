package com.ken.base.redis.database;

import com.ken.base.properties.RedisLoader;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ken on 19/4/16.
 */
public class JedisPools {

    //private JedisPool pool;
    private RedisLoader redisLoader = RedisLoader.getInstance();

    private static  JedisPools jedisPools = null;

    private Map<String,JedisPool> poolMap = null;

    private Map<String,Jedis> jedisMap = null;

    private JedisPools()
    {
        poolMap = new HashMap<String,JedisPool>();
        jedisMap = new HashMap<String,Jedis>();
    }

    public static JedisPools getInstance()
    {
        if(jedisPools == null)
            jedisPools = new JedisPools();
        return jedisPools;
    }

    private JedisPool CreatePool(String name)
    {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(500);
        config.setMaxIdle(5);
        config.setMaxWaitMillis(1000 * 100);
        config.setTestOnBorrow(false);
        return new JedisPool(config,
                redisLoader.getProperty(name + ".host"),
                Integer.valueOf(redisLoader.getProperty(name +".port")),
                Integer.valueOf(redisLoader.getProperty("maxWaitMillis")),
                redisLoader.getProperty(name+".password"));
    }

    public Jedis getJedis(String name)
    {
        JedisPool pool = poolMap.get(name);
        if(pool == null)
        {
            pool = CreatePool(name);
            poolMap.put(name,pool);
        }
        Jedis jedis = pool.getResource();
        jedisMap.put(name,jedis);
        return jedis;
    }

    public void returnBrokenResource(String name) {
        Jedis jedis = jedisMap.get(name);
        if (jedis != null)
            poolMap.get(name).returnBrokenResource(jedis);
    }

    public void returnResource(String name) {
        Jedis jedis = jedisMap.get(name);
        if (jedis != null)
            poolMap.get(name).returnResource(jedis);
    }

    public void release(String name, boolean isBroken) {
        Jedis jedis = jedisMap.get(name);
        if (jedis != null) {
            if (isBroken) {
                poolMap.get(name).returnBrokenResource(jedis);
            } else {
                poolMap.get(name).returnResource(jedis);
            }
        }
    }

}
