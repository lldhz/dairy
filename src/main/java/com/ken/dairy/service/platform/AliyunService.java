package com.ken.dairy.service.platform;

import com.aliyun.oss.ClientException;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import com.ken.base.dao.InnerData;
import com.ken.base.properties.MessageLoader;
import com.ken.base.properties.PlatformLoader;
import com.ken.base.service.OperateService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ken on 19/4/16.
 */
@Service
public class AliyunService implements OperateService{
    protected Logger logger=Logger.getLogger(this.getClass());
    private static String Bucket;
    private static String Access_ID;
    private static String Secret_ID;
    private static String Endpoint;
    private static String domain;
    private static String roleArn;
    private static String region;
    private static String stsApiVersion;
    private static String roleSession;
    private static String armAccessID;
    private static String armSecretKey;
    private static String policy;
    private static String policyReal;
    private long updateTokenTime = 0;
    private AssumeRoleResponse roleResponse;

    private static AliyunService aliyunService = null;

    public AliyunService getInstance()
    {
        if(aliyunService == null)
            aliyunService = new AliyunService();
        return aliyunService;
    }

    private AliyunService()
    {

        try{
            PlatformLoader platformLoader = PlatformLoader.getInstance();
            Bucket = platformLoader.getProperty("aliyun.oss.Bucket");
            Access_ID = platformLoader.getProperty("aliyun.oss.AccessID");
            Secret_ID = platformLoader.getProperty("aliyun.oss.SecretID");
            Endpoint = platformLoader.getProperty("aliyun.oss.Endpoint");
            domain = platformLoader.getProperty("aliyun.oss.domain");
            roleArn = platformLoader.getProperty("aliyun.oss.roleArn");
            region = platformLoader.getProperty("aliyun.oss.region");
            stsApiVersion = platformLoader.getProperty("aliyun.oss.stsApiVersion");
            roleSession = platformLoader.getProperty("aliyun.oss.roleSession");
            armAccessID = platformLoader.getProperty("aliyun.oss.armAccessID");
            armSecretKey = platformLoader.getProperty("aliyun.oss.armSecretKey");
            policy = platformLoader.getProperty("aliyun.oss.policy");
            policyReal =  "{\n" +
                    "    \"Version\": \"1\", \n" +
                    "    \"Statement\": [\n" +
                    "        {\n" +
                    "            \"Action\": [\n" +
                    "                \"oss:GetObject\", \n" +
                    "                \"oss:PutObject\" \n" +
                    "            ], \n" +
                    "            \"Resource\": [\n" +
                    "                \"acs:oss:*:*:"+policy+"\",\n" +
                    "                \"acs:oss:*:*:"+policy+"\"/*\"\n" +
                    "            ], \n" +
                    "            \"Effect\": \"Allow\"\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}";
            roleResponse = null;
        }
        catch (Exception e)
        {
            logger.error(e);
            logger.error("AliyunService:get propertise Failed!!!");
        }
    }


    @Override
    public InnerData run(InnerData in) {
        MessageLoader messageLoader = MessageLoader.getInstance();
        InnerData innerData = new InnerData();
        if(in.getContent().get("command").equals("STS"))
        {
            if(roleResponse == null
                    || System.currentTimeMillis() - updateTokenTime > 6900000)
            {
                updateTokenTime = System.currentTimeMillis();
                try {
                    roleResponse = assumeRole(armAccessID, armSecretKey,
                            roleArn, roleSession, policyReal, ProtocolType.HTTPS);
                }
                catch (ClientException e)
                {
                    roleResponse = null;
                    innerData.setCode(messageLoader.getProperty("200003.code"));
                    innerData.setComment(messageLoader.getProperty("200003.desp"));
                }
            }

            if(roleResponse != null)
            {
                HashMap<String,String> content = new HashMap<String,String>();
                content.put("accessId",roleResponse.getCredentials().getAccessKeyId());
                content.put("accessSecret",roleResponse.getCredentials().getAccessKeySecret());
                content.put("securityToken",roleResponse.getCredentials().getSecurityToken());
                content.put("expiration",roleResponse.getCredentials().getExpiration());
                content.put("endpoint",Endpoint);
                content.put("domain",domain);
                content.put("fileUri",getFileUri(in.getContent().get("fileType"),in.getContent().get("dir")));
                innerData.setCode(messageLoader.getProperty("success.code"));
                innerData.setComment(messageLoader.getProperty("success.desp"));
                innerData.setContent(content);
            }
        }
        return innerData;
    }

    private String getFileUri(String fileType,String dir)
    {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return dir+"/"+sdf.format(date)+"."+getContentType(fileType);
    }

    private String getContentType(String fileType)
    {
        if(fileType.toLowerCase().equals("bmp")){return "image/bmp";}
        if(fileType.toLowerCase().equals("gif")){return "image/gif";}
        if(fileType.toLowerCase().equals("jpeg")||
                fileType.toLowerCase().equals("jpg")||
                fileType.toLowerCase().equals("png")){return "image/jpeg";}
        if(fileType.toLowerCase().equals("html")){return "text/html";}
        if(fileType.toLowerCase().equals("txt")){return "text/plain";}
        if(fileType.toLowerCase().equals("vsd")){return "application/vnd.visio";}
        if(fileType.toLowerCase().equals("pptx")||
                fileType.toLowerCase().equals("ppt")){return "application/vnd.ms-powerpoint";}
        if(fileType.toLowerCase().equals("docx")||
                fileType.toLowerCase().equals("doc")){return "application/msword";}
        if(fileType.toLowerCase().equals("xml")){return "text/xml";}
        return "text/html";
    }

    private AssumeRoleResponse assumeRole(String accessKeyId, String accessKeySecret,
                                          String roleArn, String roleSessionName, String policy,
                                          ProtocolType protocolType) throws ClientException {
        try {
            // 创建一个 Aliyun Acs Client, 用于发起 OpenAPI 请求
            IClientProfile profile = DefaultProfile.getProfile(stsApiVersion, accessKeyId, accessKeySecret);
            DefaultAcsClient client = new DefaultAcsClient(profile);

            // 创建一个 AssumeRoleRequest 并设置请求参数
            final AssumeRoleRequest request = new AssumeRoleRequest();
            request.setVersion(region);
            request.setMethod(MethodType.POST);
            request.setProtocol(protocolType);

            request.setRoleArn(roleArn);
            request.setRoleSessionName(roleSessionName);
            request.setPolicy(policy);

            // 发起请求，并得到response
            AssumeRoleResponse response = new AssumeRoleResponse();
            try {
                response = client.getAcsResponse(request);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return response;
        } catch (ClientException e) {
            throw e;
        }
    }
}
