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


//    public String getReplyForMessage(String message) {
//        String lowerMsg = message.toLowerCase().trim();
//
//        // ======== 1. ПРИВЕТСТВИЕ ========
//        if (lowerMsg.contains("привет") || lowerMsg.equals("здравствуйте") || lowerMsg.equals("добрый день") ||
//                lowerMsg.equals("старт") || lowerMsg.contains("приветствую") || lowerMsg.equals("хей") ||
//                lowerMsg.equals("ку") || lowerMsg.equals("здарова") || lowerMsg.equals("start")) {
//            return "Здравствуйте! Это виртуальный помощник MAVE. Мы — небольшой авторский проект, создающий футболки с уникальными принтами. Я помогу вам выбрать модель, оформить заказ или отвечу на частые вопросы. Что вас интересует?";
//        }
//
//        // ======== 2. МОДЕЛИ И РАЗМЕРЫ ========
//        if (lowerMsg.contains("модели") || lowerMsg.contains("размер") || lowerMsg.contains("мужской") ||
//                lowerMsg.contains("женский") || lowerMsg.contains("какой размер") || lowerMsg.contains("размерная сетка") ||
//                lowerMsg.contains("крои") || lowerMsg.contains("как выбрать размер") || lowerMsg.contains("у вас есть размер")) {
//            return "Мы заботимся о том, чтобы вещь MAVE сидела на вас идеально. Мы выпускаем футболки в двух вариантах кроя — мужском и женском.\n\nРазмеры: В наличии модели M и L.";
//        }
//
//        // ======== 3. ЦЕНА И КАТАЛОГ ========
//        if (lowerMsg.contains("сколько стоит") || lowerMsg.contains("цена") || lowerMsg.contains("стоимость") ||
//                lowerMsg.contains("каталог") || lowerMsg.contains("где посмотреть") || lowerMsg.contains("показать модели") ||
//                lowerMsg.contains("ассортимент") || lowerMsg.contains("что есть") || lowerMsg.contains("выбор") ||
//                lowerMsg.contains("расценки") || lowerMsg.contains("прайс")) {
//            return "Стоимость любой футболки MAVE — 4500 рублей. Все актуальные модели можно посмотреть в нашем каталоге по ссылке: https://themave.ru";
//        }
//
//        // ======== 4. ОПЛАТА ========
//        if (lowerMsg.contains("как оплатить") || lowerMsg.contains("оплата") || lowerMsg.contains("перевод") ||
//                lowerMsg.contains("наличные") || lowerMsg.contains("карта") || lowerMsg.contains("способ оплаты") ||
//                lowerMsg.contains("как заплатить") || lowerMsg.contains("реквизиты")) {
//            return "Сейчас оплата происходит при личном оформлении заказа через перевод или наличными при получении. Мы работаем напрямую с каждым покупателем, поэтому просто напишите нам в чат, и мы поможем оформить доставку!";
//        }
//
//        // ======== 5. ДОСТАВКА ========
//        if (lowerMsg.contains("доставка") || lowerMsg.contains("отправка") || lowerMsg.contains("сроки") ||
//                lowerMsg.contains("почта") || lowerMsg.contains("сдэк") || lowerMsg.contains("курьер") ||
//                lowerMsg.contains("регион") || lowerMsg.contains("когда придет") || lowerMsg.contains("сколько ждать") ||
//                lowerMsg.contains("получение")) {
//            return "Мы доставляем футболки MAVE по всей России. Стоимость и сроки зависят от выбранного способа (СДЭК или Почта России) и вашего региона. Напишите нам в чат, куда нужно отправить посылку, и мы сориентируем вас по условиям.";
//        }
//
//        // ======== 6. СОСТАВ И УХОД ========
//        if (lowerMsg.contains("ткань") || lowerMsg.contains("хлопок") || lowerMsg.contains("состав") ||
//                lowerMsg.contains("качество") || lowerMsg.contains("стирка") || lowerMsg.contains("уход") ||
//                lowerMsg.contains("принт") || lowerMsg.contains("материал") || lowerMsg.contains("гипоаллергенно") ||
//                lowerMsg.contains("как стирать") || lowerMsg.contains("гладить") || lowerMsg.contains("из чего")) {
//            return "Мы выбираем органический хлопок за его чистоту и мягкость. Он гипоаллергенный, отлично пропускает воздух и невероятно приятен к коже. Без вредных химикатов при выращивании — только бережное отношение к природе и максимальный комфорт для вас.\n\n" +
//                    "Чтобы принт и качество ткани радовали вас как можно дольше:\n" +
//                    "• Стирайте при температуре не выше 30-40°C.\n" +
//                    "• Используйте деликатный режим стирки.\n" +
//                    "• Гладьте футболку с изнаночной стороны, чтобы сохранить уникальность принта.";
//        }
//
//        // ======== 7. ПОМОЩЬ / КОНСУЛЬТАЦИЯ ========
//        if (lowerMsg.contains("помогите") || lowerMsg.contains("не знаю") || lowerMsg.contains("посоветуйте") ||
//                lowerMsg.contains("что выбрать") || lowerMsg.contains("подберите") || lowerMsg.contains("какой вариант") ||
//                lowerMsg.contains("как заказать") || lowerMsg.contains("как купить") || lowerMsg.contains("не могу определиться")) {
//            return "С удовольствием ответим на все вопросы, поможем определиться с размером и оформить доставку.";
//        }
//
//        // ======== 8. КОНТАКТЫ ========
//        if (lowerMsg.contains("контакты") || lowerMsg.contains("связаться") || lowerMsg.contains("позвонить") ||
//                lowerMsg.contains("написать") || lowerMsg.contains("менеджер") || lowerMsg.contains("поддержка") ||
//                lowerMsg.contains("телефон") || lowerMsg.contains("почта") || lowerMsg.contains("telegram") ||
//                lowerMsg.contains("tg") || lowerMsg.contains("email") || lowerMsg.contains("как с вами связаться") ||
//                lowerMsg.contains("куда позвонить")) {
//            return "Контакты для связи с нами:\n\n" +
//                    "• Telegram: @masharachkova, @ElyaRachkova\n" +
//                    "• Почта: alia85_07@mail.ru, masharachkovaa@gmail.com\n\n" +
//                    "Или просто продолжайте диалог — я помогу оформить заказ!";
//        }
//
//        // ======== 9. СБОР КОНТАКТА (заказ / согласие) ========
//        if (lowerMsg.equals("да") || lowerMsg.contains("хочу заказать") || lowerMsg.contains("согласен") ||
//                lowerMsg.contains("оформить") || lowerMsg.contains("заказ") || lowerMsg.contains("купить") ||
//                lowerMsg.equals("ок") || lowerMsg.equals("давай") || lowerMsg.contains("хочу купить") ||
//                lowerMsg.contains("оставьте контакты")) {
//            return "Пожалуйста, напишите здесь ваш номер телефона или ник, по которому нам удобнее всего связаться с вами.";
//        }
//
//        // ======== СТАНДАРТНЫЙ ОТВЕТ (если ничего не подошло) ========
//        return "Я не совсем понял ваш вопрос. Я могу помочь вам с:\n" +
//                "• выбором модели и размеров\n" +
//                "• ценой и каталогом\n" +
//                "• оплатой и доставкой\n" +
//                "• составом и уходом за футболками\n\n" +
//                "Напишите, что вас интересует, или посмотрите каталог на сайте: https://themave.ru";
//    }
}