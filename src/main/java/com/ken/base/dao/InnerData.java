package com.ken.base.dao;

import com.ken.base.common.JsonUtils;

import java.io.IOException;
import java.util.Map;

/**
 * Created by ken on 19/4/16.
 */
public class InnerData {

    private String code =null;
    private String comment = null;
    private long timestamp;
    private Map<String,String> content = null;

    public InnerData()
    {
        timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Map<String, String> getContent() {
        return content;
    }

    public String getContentString()
    {
        try {
            return JsonUtils.toJson(this.content);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setContent(Map<String, String> content) {
        this.content = content;
    }

    public void setContent(String content) {
        try {
            this.content = JsonUtils.fromJson(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
