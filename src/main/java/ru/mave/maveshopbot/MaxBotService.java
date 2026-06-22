package ru.mave.maveshopbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class MaxBotService {

    private static final Logger log = LoggerFactory.getLogger(MaxBotService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DeepSeekService deepSeekService;


    @Autowired
    private ObjectMapper objectMapper;

    @Value("${max.bot.token}")
    private String botToken;

    public boolean sendMessage(long chatId, String text) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", botToken);

            String url = "https://platform-api.max.ru/messages?chat_id=" + chatId;

            ObjectNode body = objectMapper.createObjectNode();
            body.put("text", text);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Сообщение отправлено в чат {}", chatId);
                return true;
            } else {
                log.error("Ошибка отправки: {}", response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения", e);
            return false;
        }
    }


    public String getReplyForMessage(long chatId, String message) {
        return deepSeekService.getAIResponse(chatId, message);
    }
}