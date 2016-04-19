package com.ken.base.redis;

import redis.clients.jedis.Jedis;

/**
 * Created by Ken on 2015/12/18.
 */
public interface RedisCallback {

    Object doWithRedis(Jedis jedis);
}
