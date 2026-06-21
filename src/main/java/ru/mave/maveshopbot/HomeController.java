package ru.mave.maveshopbot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MaxBotService maxBotService;

    @Value("${notifications.chat.id}")
    private long notificationsChatId;

    @GetMapping("/api/hello")
    public String sayHello() {
        return "Shop bot is live!";
    }

    @PostMapping("/api/max/webhook")
    public ResponseEntity<String> handleMaxWebhook(@RequestBody String payload) {
        log.info("Получен webhook от MAX: {}", payload);

        try {
            JsonNode json = objectMapper.readTree(payload);
            String updateType = json.has("update_type") ? json.get("update_type").asText() : "";

            if ("message_created".equals(updateType)) {
                JsonNode message = json.get("message");
                JsonNode recipient = message.get("recipient");
                long chatId = recipient.get("chat_id").asLong();
                String chatType = recipient.has("chat_type") ? recipient.get("chat_type").asText() : "dialog";
                String text = message.get("body").has("text") ? message.get("body").get("text").asText() : "";
                String senderName = message.get("sender").has("first_name") ?
                        message.get("sender").get("first_name").asText() : "Пользователь";

                if ("chat".equals(chatType) || "group".equals(chatType)) {
                    log.info("Сообщение из группы {}, игнорируем", chatId);
                    return ResponseEntity.ok("OK");
                }

                String reply = maxBotService.getReplyForMessage(chatId, text);
                maxBotService.sendMessage(chatId, reply);

                String groupMessage = "🛍 **Магазин MAVE**\n" +
                        "👤 От: " + senderName + "\n" +
                        "💬 Вопрос: " + text + "\n" +
                        "🤖 Ответ бота: " + reply;
                maxBotService.sendMessage(notificationsChatId, groupMessage);

            } else if ("bot_started".equals(updateType)) {
                long chatId = json.get("chat_id").asLong();
                String userName = json.get("user").has("first_name") ?
                        json.get("user").get("first_name").asText() : "Пользователь";
                String reply = "Здравствуйте! Я — виртуальный помощник бренда MAVE. Мы создаем стильные и качественные футболки из натурального хлопка.\n\n" +
                        "Я могу помочь вам:\n" +
                        "• Узнать цены и наличие футболок\n" +
                        "• Подобрать размер по вашим параметрам\n" +
                        "• Рассказать о способах оплаты и доставки\n" +
                        "• Ответить на другие вопросы о заказе";
                maxBotService.sendMessage(chatId, reply);

                String notification = "🟢 **Новый клиент магазина**: " + userName + " (chat_id: " + chatId + ")";
                maxBotService.sendMessage(notificationsChatId, notification);
            }

            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            log.error("Ошибка обработки webhook", e);
            return ResponseEntity.badRequest().body("Error");
        }
    }
}