package com.ken.dairy.service.platform;

import cn.jpush.api.JPushClient;
import cn.jpush.api.common.resp.APIConnectionException;
import cn.jpush.api.common.resp.APIRequestException;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;
import cn.jpush.api.report.ReceivedsResult;
import com.ken.base.dao.InnerData;
import com.ken.base.properties.MessageLoader;
import com.ken.base.properties.PlatformLoader;
import com.ken.base.service.OperateService;
import org.apache.commons.collections.map.HashedMap;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ken on 19/4/16.
 */

@Service
public class PushService implements OperateService{

    protected Logger logger=Logger.getLogger(this.getClass());

    private  static PushService pushService = null;
    protected static JPushClient jpushClient = null;

    private PushService()
    {
        if(jpushClient == null)
        {
            try
            {
                PlatformLoader platformLoader = PlatformLoader.getInstance();
                jpushClient = new JPushClient(platformLoader.getProperty("jpush.push.secert"),
                        platformLoader.getProperty("jpush.push.appkey"));
            }
            catch (Exception e)
            {
                logger.debug(e);
                logger.error("jpushService: jpush Client Init Failed!!!");
            }
        }
    }

    public PushService getInstance()
    {
        if(pushService == null)
        {
            pushService = new PushService();
        }
        return pushService;
    }

    @Override
    public InnerData run(InnerData in)
    {
        return parsePushResult(push(PushPayloadBuilder(in)));
    }

    private PushPayload PushPayloadBuilder(InnerData in)
    {
        PushPayload pushPayload = null;
        //String Message = null;
        Map<String,String> extras = new HashMap<String,String>();
        for (Map.Entry<String, String> entry:in.getContent().entrySet()
             ) {
            if((!entry.getKey().equals("receivers")) && (entry.getKey().equals("message")))
            {
                extras.put(entry.getKey(),entry.getValue());
            }
        }
        if(in.getCode().equals("ALL"))
        {
            pushPayload = PushPayload.newBuilder()
                    .setPlatform(Platform.android_ios())
                    .setAudience(Audience.all())
                    .setNotification(Notification.newBuilder()
                            .addPlatformNotification(IosNotification.newBuilder().setAlert(in.getContent().get("message")).addExtras(extras).build())
                            .addPlatformNotification(AndroidNotification.newBuilder().setAlert(in.getContent().get("message")).addExtras(extras).build()
                            ).build()
                    ).build();
        }
        else
        {
            pushPayload =
                    PushPayload.newBuilder()
                            .setPlatform(Platform.android_ios())
                            .setAudience(Audience.registrationId(in.getContent().get("receivers").split(";")))
                            .setNotification(
                                    Notification.newBuilder()
                                            .addPlatformNotification(IosNotification.newBuilder().setAlert(in.getContent().get("message")).addExtras(extras).build())
                                            .addPlatformNotification(AndroidNotification.newBuilder().setAlert(in.getContent().get("message")).addExtras(extras).build()
                                            ).build()
                            )
                            .build();
        }
        return pushPayload;
    }
    /*
    *  推送信息
    * */
    private PushResult push(PushPayload payload)
    {
        try {

            PushResult result = jpushClient.sendPush(payload);
            return result;

        } catch (APIConnectionException e) {
            logger.error("PushService : push error");
            logger.error("Connection error. Should retry later. ", e);
            return null;
        } catch (APIRequestException e) {
            logger.error("PushService : push error");
            logger.error("Error response from JPush server. Should review and fix it. ");
            logger.error(e);
            return null;
        }
    }

    /**
     * 解析推送返回值,获取接收客户端数量
     * */
    private InnerData parsePushResult(PushResult pushResult)
    {
        InnerData innerData = new InnerData();
        MessageLoader messageLoader = MessageLoader.getInstance();
        if(pushResult == null)
        {

            innerData.setCode(messageLoader.getProperty("200001.code"));
            innerData.setComment(messageLoader.getProperty("200001.desp"));
            innerData.setContent((String)null);
        }
        else
        {
            try {
                ReceivedsResult reportResult = jpushClient.getReportReceiveds(Long.toString(pushResult.msg_id));
                ReceivedsResult.Received reportReceived = reportResult.received_list.get(0);
                if (reportReceived.android_received == 0 &&
                        reportReceived.ios_apns_sent == 0) {
                    innerData.setCode(messageLoader.getProperty("200002.code"));
                    innerData.setComment(messageLoader.getProperty("200002.desp"));
                    innerData.setContent((String) null);
                } else {
                    Map<String, String> receiverMap = new HashMap<String, String>();
                    receiverMap.put("android_received", "" + reportReceived.android_received);
                    receiverMap.put("ios_apns_sent", "" + reportReceived.ios_apns_sent);
                    innerData.setCode(messageLoader.getProperty("success.code"));
                    innerData.setComment(messageLoader.getProperty("success.desp"));
                    innerData.setContent(receiverMap);
                }
            }
            catch (Exception e)
            {
                logger.error("PushService : parsePushResult error");
                logger.error(e);
                innerData.setCode(messageLoader.getProperty("200001.code"));
                innerData.setComment(messageLoader.getProperty("200001.desp"));
                innerData.setContent((String)null);
            }
        }
        return innerData;
    }
}
