package com.ken.dairy.service.platform;

import com.ken.base.dao.InnerData;
import com.ken.base.properties.MessageLoader;
import com.ken.base.properties.PlatformLoader;
import com.ken.base.service.OperateService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.*;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by ken on 19/4/16.
 */
@Service
public class EmailService implements OperateService{

    protected Logger logger=Logger.getLogger(this.getClass());
    private String from = null;
    private String host = null;
    private String port = "25";
    private String username=null;
    private String password = null;
    private String title = null;
    private String content = null;
    private String type = null;

    private static EmailService emailService = null;

    public EmailService getInstance()
    {
        if(emailService == null)
        {
            emailService = new EmailService();
        }
        return emailService;
    }

    @SuppressWarnings("static-access")
    @Override
    public InnerData run(InnerData in)
    {
        MessageLoader messageLoader = MessageLoader.getInstance();
        InnerData out = new InnerData();
        try {
            Map<String,String> map = in.getContent();
            getSenderInfo(map.get("sender"), map.get("message"));
            String contentReal = setContent(content, map);
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.starttls.enable", "true");// 使用 STARTTLS安全连接
            props.put("mail.smtp.port", port); //google使用465或587端口
            props.put("mail.smtp.auth", "true"); // 使用验证
            // props.put("mail.debug", "true");

            // 根据邮件会话属性和密码验证器构造一个发送邮件的session
            Session sendMailSession = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username,password);
                }
            });


            // 创建邮件对象
            Message msg = new MimeMessage(sendMailSession);

            // 发件人
            msg.setFrom(new InternetAddress(from));

            msg.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(map.get("receiver")));

            msg.setSubject(title);

            if (type == "txt") {
                msg.setText(contentReal);
            } else {
                msg.setContent(contentReal, "text/html;charset=utf-8");
            }

            Transport.send(msg);
            out.setCode(messageLoader.getProperty("success.code"));
            out.setComment(messageLoader.getProperty("success.desp"));
        }
        catch (MessagingException e)
        {
            out.setCode(messageLoader.getProperty("200005.code"));
            out.setComment(messageLoader.getProperty("200005.desp"));
        }
        return out;
    }

    private String setContent(String content,Map<String,String> params)
    {
        for ( Map.Entry<String, String> entry:params.entrySet()
                ) {
            content.replace("{"+entry.getKey()+"}",entry.getValue());
        }
        String result = content;
        return result;
    }

    private HashMap<String,String> getSenderInfo(String sender, String messager) {
        HashMap<String, String> result = new HashMap<String, String>();
        try {
            PlatformLoader configLoader = PlatformLoader.getInstance();
            host = configLoader.getProperty(sender + ".host");
            from = configLoader.getProperty(sender + ".from");
            port = configLoader.getProperty(sender + ".port");
            username = configLoader.getProperty(sender + ".username");
            password = configLoader.getProperty(sender + ".password");
            title = configLoader.getProperty(messager + ".title");
            content = configLoader.getProperty(messager + ".content");
            type = configLoader.getProperty(messager + ".type");
        } finally {
            result.put("host", host);
            result.put("from", from);
            result.put("port", port);
            result.put("username", username);
            result.put("password", password);
            result.put("title", title);
            result.put("content", content);
            result.put("type", type);
        }
        return result;
    }
}
