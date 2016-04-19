package com.ken.base.properties;

import org.apache.log4j.Logger;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * Created by Ken on 2015/12/18.
 */

public class ConfigLoader {

    private static Logger logger = Logger.getLogger(ConfigLoader.class);

    private Properties properties;

    public ConfigLoader(String fileName){
        try {
            properties = PropertiesLoaderUtils.loadAllProperties(fileName);
        } catch (IOException e) {
            // TODO 自动生成 catch 块
            logger.error("load Config failed!",e);
        }
    }


    public String getProperty(String key){
        try {
            return new String(this.properties.getProperty(key).getBytes("ISO-8859-1"), "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            logger.error(e);
            return null;
        }
    }

    public void Reloader(String fileName)
    {
        properties.clear();
        try {
            properties = PropertiesLoaderUtils.loadAllProperties(fileName);
        } catch (IOException e) {
            logger.error("load Config failed!",e);
        }
    }

}
