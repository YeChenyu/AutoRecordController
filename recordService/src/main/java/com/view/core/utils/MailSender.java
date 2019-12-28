package com.view.core.utils;

import android.util.Log;

import java.io.File;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.view.core.utils.FileUtils;

public class MailSender {

    public boolean sendFileMail(MailInfo info, File file, String num) {
        Message attachmentMail = createAttachmentMail(info,file,num);
        try {
            Transport.send(attachmentMail);

            FileUtils.delete(file);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 创建带有附件的邮件
     *
     * @return
     */
    private Message createAttachmentMail(final MailInfo info, final File file, final String num) {
        //创建邮件
        MimeMessage message = null;
        Properties pro = info.getProperties();
        try {

            Session sendMailSession = Session.getInstance(pro, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(info.getUserName(), info.getPassword());
                }
            });

            message = new MimeMessage(sendMailSession);
            // 设置邮件的基本信息
            //创建邮件发送者地址
            Address from = new InternetAddress(info.getFromAddress());
            //设置邮件消息的发送者
            message.setFrom(from);
            //创建邮件的接受者地址，并设置到邮件消息中
            Address to = new InternetAddress(info.getToAddress());
            //设置邮件消息的接受者, Message.RecipientType.TO属性表示接收者的类型为TO
            message.setRecipient(Message.RecipientType.TO, to);
            //邮件标题
            message.setSubject(info.getSubject());

            // 创建邮件正文，为了避免邮件正文中文乱码问题，需要使用CharSet=UTF-8指明字符编码
            MimeBodyPart text = new MimeBodyPart();
            text.setContent(info.getContent(), "text/html;charset=UTF-8");


            // 图片
            MimeBodyPart image = new MimeBodyPart();
            DataHandler dh = new DataHandler(new FileDataSource(file));
            image.setDataHandler(dh);
            image.setContentID("wcy.jpg");
            image.setHeader("Content-ID", "<image>");
            image.setHeader("Content-Type", "image/png");
            image.setDisposition(MimeBodyPart.INLINE);
            image.setFileName(num+".3gp");


            // 创建容器描述数据关系
            MimeMultipart mp = new MimeMultipart();
            mp.addBodyPart(text);
            mp.addBodyPart(image);
            mp.setSubType("related");


            message.setContent(mp);
            message.saveChanges();
        } catch (Exception e) {
            Log.e("TAG", "创建带附件的邮件失败");
            e.printStackTrace();
        }
        // 返回生成的邮件
        return message;
    }
}
