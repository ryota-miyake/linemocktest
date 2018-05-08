package com.example.linebot;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.DatetimePickerAction;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;


@LineMessageHandler
public class LinebotController {
	@Autowired
    private LineMessagingClient lineMessagingClient;
    // 改行コード
    private static final String CODE = "\n";

    private LineTest lineTest = new LineTest();

    // リッチメニュー選択時テキスト
	private static final String JIKO_TEXT = "事故受付";
	private static final Integer MENU_NO_JIKO = 1;
	private static final String IRYOKIKAN_ANNA_TEXT = "医療機関案内";
	private static final Integer MENU_NO_IRYOKIKAN = 2;
	private static final String KEIYAKU_HENKOU_TEXT = "契約内容変更";
	private static final Integer MENU_NO_HENKOU = 3;
	private static final String KEIYAKU_TORIKESHI_TEXT = "契約取消";
	private static final Integer MENU_NO_TORIKESHI = 4;
	private static final String SUPPORTLINE_ANNAI_TEXT = "サポートライン連絡先案内";
	private static final Integer MENU_NO_SUPPORTLINE = 5;

	/**
	 * テキスト
	 * @param event
	 * @throws Exception
	 */
	@EventMapping
    public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws Exception {
    	TextMessageContent message = event.getMessage();
        handleTextContent(event.getReplyToken(), event, message);
        System.out.println("event: " + event);
    }
	/**
	 * 位置情報
	 * @param event
	 */
    @EventMapping
    public void handleLocationMessageEvent(MessageEvent<LocationMessageContent> event) {
        LocationMessageContent locationMessage = event.getMessage();
        System.out.println("event: " + event);
    }

    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        System.out.println("event: " + event);
    }

    /**
     * フォロー（友だち追加・ブロック解除）
     * @param event
     */
    @EventMapping
    public void handleFollowEvent(FollowEvent event) {
        String replyToken = event.getReplyToken();
    }

    @EventMapping
    public void handlePostbackEvent(PostbackEvent event){
    	String[] data = event.getPostbackContent().getData().split(",");
    	// リッチメニュー判定
        System.out.println("event: " + event);

    }
    /**
     *
     * @param replyToken 返信先判別トークン
     * @param event メッセージイベント
     * @param content メッセージオブジェクト
     * @throws Exception
     */
    private void handleTextContent(String replyToken, Event event, TextMessageContent content)
            throws Exception {
    	String text = content.getText();
    	// プッシュメッセージ用：ユーザID
        String userId = event.getSource().getUserId();

    	// リッチメニュー判定
    	if(JIKO_TEXT.equals(text)){
    		// 事故受付
            this.replyText(replyToken,
            		"今回ご連絡いただいている保険事故について、該当の保険契約を以下からお選び下さい。");
            // プッシュメッセージ（カルーセルテンプレート）
            this.pushMessage(userId, this.createJikouketsukeKeiyakuCarouselTemplate());
            lineTest.setMenuNo("1");
    	}else if(IRYOKIKAN_ANNA_TEXT.equals(text)){
    		// 医療機関案内
    		// 位置情報の送信を依頼
            this.replyText(replyToken,
            		"医療機関の案内を行います。" + CODE
            		+ "現在地の位置情報を送信してください。");

            lineTest.setMenuNo("2");
    	}else if(KEIYAKU_HENKOU_TEXT.equals(text)){
    		// 契約変更
    		// 応答メッセージ
            this.replyText(replyToken,
            		"契約内容の変更を行います。" + CODE
            		+ "変更する契約を選択してください。");
            // プッシュメッセージ（カルーセルテンプレート）
            lineTest.setMenuNo("3");
    	}else if(KEIYAKU_TORIKESHI_TEXT.equals(text)){
    		// 契約取消
            this.replyText(replyToken,
            		"契約内容の取消を行います。" + CODE
            		+ "変更する契約を選択してください。");
            // プッシュメッセージ（カルーセルテンプレート）
            lineTest.setMenuNo("4");
    	}else if(SUPPORTLINE_ANNAI_TEXT.equals(text)){
    		// サポートライン連絡先案内
    		// 位置情報の送信を依頼
            this.replyText(replyToken,
            		"サポートライン連絡先の案内を行います。" + CODE
            		+ "現在地の位置情報を送信してください。");
            lineTest.setMenuNo("5");
    	}else{
            this.replyText(replyToken,"メニュー番号"+lineTest.getMenuNo());
    	}
    }

    private void replyText(@NonNull String replyToken, @NonNull String message) {
        if (replyToken.isEmpty()) {
            throw new IllegalArgumentException("replyToken must not be empty");
        }
        if (message.length() > 1000) {
            message = message.substring(0, 1000 - 2) + "……";
        }
        this.reply(replyToken, new TextMessage(message));
    }
    private void reply(@NonNull String replyToken, @NonNull Message message) {
        reply(replyToken, Collections.singletonList(message));
    }
    private void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
        try {
            BotApiResponse apiResponse = lineMessagingClient
                    .replyMessage(new ReplyMessage(replyToken, messages))
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
    private Message createJikouketsukeKeiyakuCarouselTemplate(){
    	CarouselTemplate carouselTemplate = new CarouselTemplate(
    			Arrays.asList(
    					new CarouselColumn(null,"契約1","保険期間:20160401-20170401" + CODE + "旅行先：台湾" + CODE + "契約証番号：TEST000001",
    							Arrays.asList(new PostbackAction("この保険を選択する","jiko,TEST000001"))),
    					new CarouselColumn(null,"契約2","保険期間:20170401-20180401" + CODE + "旅行先：中国" + CODE + "契約証番号：TEST000002",
    							Arrays.asList(new PostbackAction("この保険を選択する","jiko,TEST000002"))),
    					new CarouselColumn(null,"契約3","保険期間:20180401-20190401" + CODE + "旅行先：香港" + CODE + "契約証番号：TEST000003",
    							Arrays.asList(new PostbackAction("この保険を選択する","jiko,TEST000003"))),
    					new CarouselColumn(null,"契約4","保険期間:20190401-20200401" + CODE + "旅行先：韓国" + CODE + "契約証番号：TEST000004",
    							Arrays.asList(new PostbackAction("この保険を選択する","jiko,TEST000004"))),
    					new CarouselColumn(null,"契約4","保険期間:20190401-20200401" + CODE + "旅行先：韓国" + CODE + "契約証番号：TEST000004",
    							Arrays.asList(new DatetimePickerAction("Datetime",
                                        "action=sel",
                                        "datetime",
                                        "2017-06-18T06:15",
                                        "2100-12-31T23:59",
                                        "1900-01-01T00:00")))));
    	TemplateMessage templateMessage = new TemplateMessage("事故受付契約選択", carouselTemplate);
    	return templateMessage;
    }
    /**
     * プッシュメッセージ
     * @param to 送信先ユーザID
     * @param message メッセージ
     */
    private void pushMessage(@NonNull String to, @NonNull Message message){
    	try {
			BotApiResponse apiResponse = lineMessagingClient.pushMessage(new PushMessage(to, message)).get();

		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
    }
}
