package com.chen.common.utils;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.*;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * @author : goldgreat
 * @Description :
 * @Date :  2019/5/24 17:57
 */
public class MailUtil {
    public static String account = "18858113996";
    public static String password = "utJvQAq5XD9cWHT";
    public static String authPsd = "WVQGRITBIXUJQIFZ";
    public static String myAddress = "18858113996@163.com";
    public static final String MAIL_PROTOCOL = "smtp";
    public static final String MAIL_AUTH = "true";
    public static final String MAIL_HOST = "smtp.163.com";


    public static Properties getProperties() {
        Properties prop = new Properties();
        prop.setProperty("mail.host", MAIL_HOST);
        prop.setProperty("mail.transport.protocol", MAIL_PROTOCOL);
        prop.setProperty("mail.smtp.auth", MAIL_AUTH);
        return prop;
    }


    /**
     * @return
     * @throws Exception
     * @Method: createAttachMail
     * @Description: 创建一封带附件的邮件
     * @Anthor:孤傲苍狼
     */
    public static void sendAttachMail(String reci, String title, String content, String filePath) throws Exception {
        Properties prop = getProperties();
        //使用JavaMail发送邮件的5个步骤
        //1、创建session
        Session session = Session.getInstance(prop);
        //开启Session的debug模式，这样就可以查看到程序发送Email的运行状态
        session.setDebug(true);
        //2、通过session得到transport对象
        Transport ts = session.getTransport();
        //3、连上邮件服务器
        ts.connect(MAIL_HOST, account, authPsd);
        //4、创建邮件
        Message message = createAttachMail(session, title, content, reci, filePath);
        //5、发送邮件
        ts.sendMessage(message, message.getAllRecipients());
        ts.close();
    }

    public static MimeMessage createAttachMail(Session session,
                                               String title, String content, String reci, String path) throws Exception {
        MimeMessage message = new MimeMessage(session);

        //设置邮件的基本信息
        //发件人
        message.setFrom(new InternetAddress("18858113996@163.com"));
        //收件人
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(reci));
        //邮件标题
        message.setSubject(title);

        //创建邮件正文，为了避免邮件正文中文乱码问题，需要使用charset=UTF-8指明字符编码
        MimeBodyPart text = new MimeBodyPart();
        text.setContent(content, "text/html;charset=UTF-8");

        //创建邮件附件
        MimeBodyPart attach = new MimeBodyPart();
        DataHandler dh = new DataHandler(new FileDataSource(path));
        attach.setDataHandler(dh);
        attach.setFileName(MimeUtility.encodeText(title+".txt"));

        //创建容器描述数据关系
        MimeMultipart mp = new MimeMultipart();
        mp.addBodyPart(text);
        mp.addBodyPart(attach);
        mp.setSubType("mixed");

        message.setContent(mp);
        message.saveChanges();
        //将创建的Email写入到E盘存储
        message.writeTo(new FileOutputStream("attachMail.eml"));
        //返回生成的邮件
        return message;
    }

    public static void main(String[] args) throws Exception {
        sendAttachMail("18858113996@163.com", "nihao", "nihao", "pom.xml");
    }

}
