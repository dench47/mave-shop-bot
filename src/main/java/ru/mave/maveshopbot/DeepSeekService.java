package ru.mave.maveshopbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DeepSeekService {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekService.class);

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Хранилище истории: ключ - chatId, значение - список сообщений
    private final Map<Long, List<Map<String, String>>> chatHistory = new ConcurrentHashMap<>();

    // Максимум сообщений в истории
    private static final int MAX_HISTORY = 30;

    public String getAIResponse(long chatId, String userMessage) {
        try {
            String url = baseUrl + "/v1/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            // Системный промпт
            String systemText =
                    "Ты — виртуальный помощник бренда MAVE. Мы продаём футболки с уникальными принтами.\n" +
                            "\n" +
                            "ПРАВИЛА ОТВЕТОВ:\n" +
                            "1. Отвечай дружелюбно, по-человечески, коротко и по делу\n" +
                            "2. Цена любой футболки — 4500 ₽\n" +
                            "3. Размеры: только M и L\n" +
                            "4. Доставка через СДЭК или Почту России\n" +
                            "5. Оплата — переводом или наличными при получении\n" +
                            "6. Если не знаешь ответа — скажи, что передашь вопрос менеджеру\n" +
                            "7. Не выдумывай цены и условия\n" +
                            "\n" +
                            "КОНТАКТЫ ДЛЯ СВЯЗИ:\n" +
                            "• Telegram: @masharachkova, @ElyaRachkova\n" +
                            "• Почта: alia85_07@mail.ru, masharachkovaa@gmail.com\n" +
                            "\n" +
                            "ПРИНТЫ (всего три, официальные названия как на сайте themave.ru):\n" +
                            "\n" +
                            "1. «Спираль времени» — принт с черепахой, выполненный одной непрерывной линией-спиралью. Единственный во всей серии, сделанный вышивкой, а не печатью.\n" +
                            "Философия: символ стабильности, терпения и долголетия. Соединение черепахи и спирали напоминает о движении в своём ритме, сохранении спокойствия в потоке жизни. Черепаха не борется со временем — она растворена в нём. Её панцирь несёт кольца, как кольца деревьев, а её поступь настолько нетороплива, что между одним шагом и следующим успевает родиться и умереть целая человеческая мысль. Спираль, выведенная одной непрерывной линией — это графическое эхо самого времени. Время не линейно, оно свернуто в кольца, возвращается к себе, но каждый раз на новом витке. Одна линия означает неразрывность: от рождения до смерти, от первого вздоха до последнего, мы чертим свой узор, не отрывая пера от бумаги бытия. Спираль закручивается внутрь — это путь не вовне, а вглубь. Все великие путешествия ведут не к новым землям, а к новым слоям самого себя. Носить этот символ — значит помнить, что спешка отнимает годы, а покой их приумножает.\n" +
                            "\n" +
                            "2. «Геометрия чувств» — коллаж из эмоций, цветов и форм. Лицо разбито на геометрические фрагменты, как калейдоскоп состояний. Охра, синий, терракотовый — диалог цветов.\n" +
                            "Философия: в эпоху, где всё стремится к идеальной симметрии, этот принт — манифест иного пути. Он расщепляет лицо на геометрические фрагменты, как будто сама жизнь — это калейдоскоп случайностей и контрастов. Каждый сегмент принта — отдельное состояние: здесь — мимолетная грусть, там — дерзкая уверенность, а в центре — спокойное принятие. Мы не обязаны быть монолитными и предсказуемыми. Мы — сумма наших частей, даже если иногда кажется, что они не складываются в привычную картину. Этот принт — приглашение увидеть красоту в несовершенстве, в многогранности, в способности оставаться собой. Надевая эту вещь, вы выбираете право быть сложным, ярким и настоящим.\n" +
                            "\n" +
                            "3. «Код счастья» — генетический код 5-HTTLPR (ген-транспортер серотонина, «ген оптимизма»). Двойная спираль, закрученная в вечность.\n" +
                            "Философия: человечество прочитало текст, написанный внутри нас. Мы научились читать собственную архитектуру и обнаружили там инструкцию к радости. Участок 5-HTTLPR управляет транспортировкой серотонина — вещества, чей дефицит погружает мир в серые тона. Мы искали ключи к счастью вовне — в достижениях, накоплениях, признании, — а оно уже было записано в нас микроскопическими буквами генетического кода. Двойная спираль — это напоминание о фундаментальной двойственности: одна нить — это данность, доставшаяся от предков, вторая — наш жизненный опыт, люди, которых мы выбираем, мысли, которые мы допускаем. Счастье рождается в момент соприкосновения врожденной способности с реальностью. Носить этот символ — значит признавать сложность: мы — ходячая поэзия из аденина, тимина, гуанина и цитозина. И в этой поэме есть строфа, которая рифмует нас с миром, делая его выносимым и прекрасным.\n" +
                            "\n" +
                            "ЕСЛИ КЛИЕНТ ХОЧЕТ СДЕЛАТЬ ЗАКАЗ:\n" +
                            "— спроси его контакты (номер телефона или Telegram-ник)\n" +
                            "— скажи, что менеджер свяжется с ним в ближайшее время\n" +
                            "\n" +
                            "НИЧЕГО НЕ ВЫДУМЫВАЙ ПРО ТОВАРЫ, КОТОРЫХ НЕТ.";            // Получаем историю для этого пользователя
            List<Map<String, String>> history = chatHistory.computeIfAbsent(chatId, k -> new ArrayList<>());

            // Собираем сообщения для запроса
            ArrayNode messages = objectMapper.createArrayNode();

            // Системное сообщение
            ObjectNode systemMessage = objectMapper.createObjectNode();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemText);
            messages.add(systemMessage);

            // Добавляем историю
            for (Map<String, String> msg : history) {
                ObjectNode historyMsg = objectMapper.createObjectNode();
                historyMsg.put("role", msg.get("role"));
                historyMsg.put("content", msg.get("content"));
                messages.add(historyMsg);
            }

            // Добавляем текущее сообщение пользователя
            ObjectNode userMessageObj = objectMapper.createObjectNode();
            userMessageObj.put("role", "user");
            userMessageObj.put("content", userMessage);
            messages.add(userMessageObj);

            // Формируем тело запроса
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", model);
            body.set("messages", messages);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode json = objectMapper.readTree(response.getBody());
                String answer = json.get("choices").get(0).get("message").get("content").asText();

                // Сохраняем в историю
                Map<String, String> userEntry = Map.of("role", "user", "content", userMessage);
                Map<String, String> botEntry = Map.of("role", "assistant", "content", answer);
                history.add(userEntry);
                history.add(botEntry);

                // Ограничиваем размер истории
                if (history.size() > MAX_HISTORY) {
                    history.subList(0, history.size() - MAX_HISTORY).clear();
                }

                log.info("DeepSeek ответил на запрос от chatId {}", chatId);
                return answer;
            } else {
                log.error("Ошибка DeepSeek: {}", response.getStatusCode());
                return "Извините, я временно не могу ответить. Пожалуйста, напишите менеджеру.";
            }

        } catch (Exception e) {
            log.error("Ошибка при вызове DeepSeek", e);
            return "Извините, я временно не могу ответить. Пожалуйста, напишите менеджеру в личные сообщения.";
        }
    }
}