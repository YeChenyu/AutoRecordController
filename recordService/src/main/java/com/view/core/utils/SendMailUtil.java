package com.view.core.utils;


import java.io.File;


public class SendMailUtil {
    private static final String HOST = "smtp.126.com";
    private static final String PORT = "25";///"587";
    private static final String FROM_ADD = "shabixixi111@126.com";//发件箱
    private static final String FROM_PSW = "shabixixiha1";//授权码
    private static final String FROM_TO =  "shabixixi111@126.com";//收件箱


    public static void send(final File file,final String num){
        final MailInfo mailInfo = creatMail(FROM_TO);
        final MailSender sms = new MailSender();
        new Thread(new Runnable() {
            @Override
            public void run() {
                sms.sendFileMail(mailInfo,file,num);
            }
        }).start();
    }


    //@NonNull
    private static MailInfo creatMail(String toAdd) {
        final MailInfo mailInfo = new MailInfo();
        mailInfo.setMailServerHost(HOST);
        mailInfo.setMailServerPort(PORT);
        mailInfo.setValidate(true);
        mailInfo.setUserName(FROM_ADD); // 你的邮箱地址
        mailInfo.setPassword(FROM_PSW);// 您的邮箱密码
        mailInfo.setFromAddress(FROM_ADD); // 发送的邮箱
        mailInfo.setToAddress(toAdd); // 发到哪个邮件去
        mailInfo.setSubject("电话"); // 邮件主题
        mailInfo.setContent("A"); // 邮件文本
        return mailInfo;
    }
}
