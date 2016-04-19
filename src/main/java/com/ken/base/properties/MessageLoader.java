package com.ken.base.properties;

import org.apache.log4j.Logger;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * Created by ken on 13/4/16.
 */
public class MessageLoader {

    private static Logger logger = Logger.getLogger(ConfigLoader.class);

    private Properties properties;

    private static MessageLoader messageLoader = null;

    private MessageLoader()
    {
        try {
            properties = PropertiesLoaderUtils.loadAllProperties("conf/message.properties");
        } catch (IOException e) {
            // TODO 自动生成 catch 块
            logger.error("load Config failed!",e);
        }
    }

    public synchronized static MessageLoader getInstance()
    {
        if(messageLoader == null)
        {
            messageLoader = new MessageLoader();
        }
        return messageLoader;
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
            properties = PropertiesLoaderUtils.loadAllProperties("conf/message.properties");
        } catch (IOException e) {
            logger.error("load Config failed!",e);
        }
    }
}
