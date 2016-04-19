package com.ken.dairy.service.platform;

import com.cloopen.rest.sdk.CCPRestSmsSDK;
import com.ken.base.dao.InnerData;
import com.ken.base.properties.MessageLoader;
import com.ken.base.properties.PlatformLoader;
import com.ken.base.service.OperateService;
import org.apache.log4j.Logger;

/**
 * Created by ken on 19/4/16.
 */
public class SmsService implements OperateService{
    private static CCPRestSmsSDK restAPI = null;
    protected Logger logger = Logger.getLogger(this.getClass());
    private static SmsService smsService = null;

    private SmsService() {
        if(restAPI == null)
        {
            try{
                PlatformLoader platformLoader = PlatformLoader.getInstance();
                restAPI = new CCPRestSmsSDK();
                restAPI.init(platformLoader.getProperty("cloopen.sms.url"),
                        platformLoader.getProperty("cloopen.sms.port"));
                restAPI.setAccount(platformLoader.getProperty("cloopen.sms.accountSid"),
                        platformLoader.getProperty("cloopen.sms.accountToken"));
                restAPI.setAppId("cloopen.sms.appId");
            }
            catch (Exception e)
            {
                logger.error("SmsService:SmsService Init Failed!");
                logger.debug(e);
                restAPI = null;
            }
        }
    }

    public SmsService getInstance()
    {
        if(smsService == null)
        {
            smsService = new SmsService();
        }
        return  smsService;
    }

    @Override
    public InnerData run(InnerData in) {
        MessageLoader messageLoader = MessageLoader.getInstance();
        PlatformLoader platformLoader = PlatformLoader.getInstance();
        InnerData innerData = new InnerData();
        try
        {
            restAPI.sendTemplateSMS(in.getContent().get("receivers"),
                    platformLoader.getProperty("cloopen.sms."+in.getContent().get("template")),
                    in.getContent().get("params").split(";"));
            innerData.setCode(messageLoader.getProperty("success.code"));
            innerData.setComment(messageLoader.getProperty("success.desp"));
        }
        catch (Exception e)
        {
            innerData.setCode(messageLoader.getProperty("200001.code"));
            innerData.setComment(messageLoader.getProperty("200001.desp"));
        }
        return innerData;
    }
}
