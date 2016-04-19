package com.ken.base.properties;

import org.apache.log4j.Logger;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * Created by ken on 19/4/16.
 */
public class RedisLoader {
    private static Logger logger = Logger.getLogger(ConfigLoader.class);

    private Properties properties;

    private static RedisLoader redisLoader = null;

    private RedisLoader()
    {
        try {
            properties = PropertiesLoaderUtils.loadAllProperties("conf/redis.properties");
        } catch (IOException e) {
            // TODO 自动生成 catch 块
            logger.error("load chartServer Config failed!",e);
        }
    }

    public synchronized static RedisLoader getInstance()
    {
        if(redisLoader == null)
            redisLoader = new RedisLoader();
        return redisLoader;
    }


    public String getProperty(String key) {
        try {
            return new String(this.properties.getProperty(key).getBytes("ISO-8859-1"), "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            logger.error(e);
            return null;
        }
    }

    public void Reloader()
    {
        properties.clear();
        try {
            properties = PropertiesLoaderUtils.loadAllProperties("conf/redis.properties");
        } catch (IOException e) {
            logger.error("load Config failed!",e);
        }
    }
}