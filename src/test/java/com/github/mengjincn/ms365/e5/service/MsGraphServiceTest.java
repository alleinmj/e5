package com.github.mengjincn.ms365.e5.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.mengjincn.ms365.e5.config.MsGraphProperties;
import com.microsoft.graph.models.extensions.*;
import com.microsoft.graph.models.generated.BodyType;
import com.microsoft.graph.models.generated.Importance;
import com.microsoft.graph.requests.extensions.IMessageCollectionPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedList;

@SpringBootTest
class MsGraphServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(MsGraphServiceTest.class);
    @Autowired
    private MsGraphService msGraphService;
    private String file = "tmp/测试ApplyToken文件.txt";

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private MsGraphProperties msgraphProperties;
    @Autowired
    private ApplyTokenService applyTokenService;
    @BeforeEach
    void upload() {
        DriveItem driveItem = msGraphService.upload(file, "文件测试，已经被修改了...".getBytes());
        System.out.println("driveItem.id = " + driveItem.id);
        System.out.println("driveItem.name = " + driveItem.name);
    }


    @Test
    void mail(){
        getMailList();

        //sendMail();
        DriveItem item = msGraphService.get("httpRequests/msgraph appId，appSecret认证.docx");
        System.out.println("item.id = " + item.id);
    }


    /**
     * referenceAttachment目前还不成熟，先不使用
     */
    private void createMailWithReferenceAttachment(){

        Message message = new Message();
        message.subject = "Did you see last night's word document?";
        message.importance = Importance.LOW;
        ItemBody body = new ItemBody();
        body.contentType = BodyType.HTML;
        body.content = "They were <b>awesome document</b>!";
        message.body = body;
        LinkedList<Recipient> toRecipientsList = new LinkedList<Recipient>();
        Recipient toRecipients = new Recipient();
        EmailAddress emailAddress = new EmailAddress();
        emailAddress.address = "mengjin@alleinjin.onmicrosoft.com";
        toRecipients.emailAddress = emailAddress;
        toRecipientsList.add(toRecipients);
        message.toRecipients = toRecipientsList;

        message = msGraphService.getGraphClient().users(msgraphProperties.getUserId()).messages()
                .buildRequest()
                .post(message);
        System.out.println("message = " + message.id);
        ReferenceAttachment attachment = new ReferenceAttachment();
        attachment.oDataType = "#microsoft.graph.referenceAttachment";
        attachment.name = "msgraph.docx";
        attachment.id = "01HU6VT5DKMOKUCJT2ZFGLMMP6J6XTZLL5";
        attachment.isInline=false;
        attachment.size=1080;
        attachment.contentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document";

        Attachment attachment1 = msGraphService.getGraphClient().users(msgraphProperties.getUserId()).messages(message.id)
                .attachments()
                .buildRequest()
                .post(attachment);

        System.out.println("attachment.id = " + attachment1.id);


        msGraphService.getGraphClient().users(msgraphProperties.getUserId()).messages(message.id).send().buildRequest().post();
    }

    /**
     * 先创建邮件
     * 向邮件中添加附件
     * 发送邮件
     */
    private void createMail(){
        Message message = new Message();
        message.subject = "Did you see last night's game?";
        message.importance = Importance.LOW;
        ItemBody body = new ItemBody();
        body.contentType = BodyType.HTML;
        body.content = "They were <b>awesome</b>!";
        message.body = body;
        LinkedList<Recipient> toRecipientsList = new LinkedList<Recipient>();
        Recipient toRecipients = new Recipient();
        EmailAddress emailAddress = new EmailAddress();
        emailAddress.address = "mengjin@alleinjin.onmicrosoft.com";
        toRecipients.emailAddress = emailAddress;
        toRecipientsList.add(toRecipients);
        message.toRecipients = toRecipientsList;

        message = msGraphService.getGraphClient().users(msgraphProperties.getUserId()).messages()
                .buildRequest()
                .post(message);
        System.out.println("message = " + message.id);
        FileAttachment attachment = new FileAttachment();
        attachment.oDataType = "#microsoft.graph.fileAttachment";
        attachment.name = "smile";
        attachment.contentBytes = "R0lGODdhEAYEAA7".getBytes();

        Attachment attachment1 = msGraphService.getGraphClient().users(msgraphProperties.getUserId()).messages(message.id)
                .attachments()
                .buildRequest()
                .post(attachment);

        System.out.println("attachment.id = " + attachment1.id);


        msGraphService.getGraphClient().users(msgraphProperties.getUserId()).messages(message.id).send().buildRequest().post();
    }

    private void sendMail(){
        Message message = new Message();
        message.subject = "Meet for lunch?";
        ItemBody body = new ItemBody();
        body.contentType = BodyType.TEXT;
        body.content = "The new cafeteria is open.";
        message.body = body;
        LinkedList<Recipient> toRecipientsList = new LinkedList<Recipient>();
        Recipient toRecipients = new Recipient();
        EmailAddress emailAddress = new EmailAddress();
        emailAddress.address = "mengjin@alleinjin.onmicrosoft.com";
        toRecipients.emailAddress = emailAddress;
        toRecipientsList.add(toRecipients);
        message.toRecipients = toRecipientsList;

        LinkedList<JSONObject> attachmentsList = new LinkedList<>();
        JSONObject attachment = new JSONObject();
        attachment.put("name", "attachment.txt");
        attachment.put("contentType", "text/plain");
        attachment.put("contentBytes", "SGVsbG8gV29ybGQh".getBytes());
        attachment.put("@odata.type", "#microsoft.graph.fileAttachment");

        attachmentsList.add(attachment);


        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(message));
        jsonObject.put("attachments",attachmentsList);

        JSONObject requestBody = new JSONObject();
        requestBody.put("message", jsonObject);
        requestBody.put("saveToSentItems", true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(applyTokenService.getToken());
        HttpEntity<JSONObject> httpEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(String.format("https://graph.microsoft.com/v1.0/users/%s/sendMail", msgraphProperties.getUserId()),
                HttpMethod.POST,
                httpEntity, String.class);
        System.out.println(responseEntity.getStatusCode());

    }
    private void getMailList() {
        IMessageCollectionPage messages = msGraphService.getGraphClient().users(msgraphProperties.getUserId()).messages().buildRequest().select("sender,subject")
                .get();

        messages.getCurrentPage().forEach(message -> {
            logger.info(message.id);
            logger.info(message.subject);
            logger.info(message.sender.emailAddress.address);
        });
    }

    private void sendMailWithInternetMessageHeader(){
        Message message = new Message();
        message.subject = "9/9/2018: concert";
        ItemBody body = new ItemBody();
        body.contentType = BodyType.HTML;
        body.content = "The group represents Nevada.";
        message.body = body;
        LinkedList<Recipient> toRecipientsList = new LinkedList<Recipient>();
        Recipient toRecipients = new Recipient();
        EmailAddress emailAddress = new EmailAddress();
        emailAddress.address = "mengjin@alleinjin.onmicrosoft.com";
        toRecipients.emailAddress = emailAddress;
        toRecipientsList.add(toRecipients);
        message.toRecipients = toRecipientsList;
        LinkedList<InternetMessageHeader> internetMessageHeadersList = new LinkedList<InternetMessageHeader>();
        InternetMessageHeader internetMessageHeaders = new InternetMessageHeader();
        internetMessageHeaders.name = "x-custom-header-group-name";
        internetMessageHeaders.value = "Nevada";
        internetMessageHeadersList.add(internetMessageHeaders);
        InternetMessageHeader internetMessageHeaders1 = new InternetMessageHeader();
        internetMessageHeaders1.name = "x-custom-header-group-id";
        internetMessageHeaders1.value = "NV001";
        internetMessageHeadersList.add(internetMessageHeaders1);
        message.internetMessageHeaders = internetMessageHeadersList;

        msGraphService.getGraphClient().users(msgraphProperties.getUserId())
                .sendMail(message,true)
                .buildRequest()
                .post();
    }


    private void sendMailSimple() {
        Message message = new Message();
        message.subject = "Meet for lunch?";
        ItemBody body = new ItemBody();
        body.contentType = BodyType.TEXT;
        body.content = "The new cafeteria is open.";
        message.body = body;
        LinkedList<Recipient> toRecipientsList = new LinkedList<Recipient>();
        Recipient toRecipients = new Recipient();
        EmailAddress emailAddress = new EmailAddress();
        emailAddress.address = "mengjin@alleinjin.onmicrosoft.com";
        toRecipients.emailAddress = emailAddress;
        toRecipientsList.add(toRecipients);
        message.toRecipients = toRecipientsList;
        LinkedList<Recipient> ccRecipientsList = new LinkedList<Recipient>();
        Recipient ccRecipients = new Recipient();
        EmailAddress emailAddress1 = new EmailAddress();
        emailAddress1.address = "mengjin@sightp.com";
        ccRecipients.emailAddress = emailAddress1;
        ccRecipientsList.add(ccRecipients);
        message.ccRecipients = ccRecipientsList;

        boolean saveToSentItems = true;

        msGraphService.getGraphClient().users(msgraphProperties.getUserId())
                .sendMail(message,saveToSentItems)
                .buildRequest()
                .post();

    }

    @Test
    void get(){
        DriveItem driveItem = msGraphService.get(file+"1");
        System.out.println("driveItem.name = " + driveItem.name);
        System.out.println("driveItem.id = " + driveItem.id);
    }

    @Test
    void download() {
        try (Reader inputStream = new InputStreamReader(msGraphService.download(file))) {
            logger.info("receive: {}", FileCopyUtils.copyToString(inputStream));
        } catch (IOException e) {
            logger.error("download file {} with error", file, e);
        }
    }


    @AfterEach
    void delete() {
        msGraphService.delete(file);
    }
}