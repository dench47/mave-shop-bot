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

    public String getReplyForMessage(String message) {
        String lowerMsg = message.toLowerCase().trim();

        // Приветствие
        if (lowerMsg.contains("привет") || lowerMsg.equals("здравствуйте") || lowerMsg.equals("start")) {
            return "Здравствуйте! 👕\n\nЯ — консультант бренда MAVE.\n\n" +
                    "Расскажу вам о наших футболках, ценах, размерах, доставке и помогу с выбором.\n\n" +
                    "Напишите, что вас интересует: цены, размеры, доставка, оплата.";
        }

        // Цены
        if (lowerMsg.contains("цен") || lowerMsg.contains("стои") || lowerMsg.contains("сколько")) {
            return "💰 Стоимость одной футболки MAVE — 4 500 ₽.\n\n" +
                    "🚚 Доставка оплачивается отдельно по тарифам почты или курьерской службы.\n\n" +
                    "Примерная стоимость доставки по России — от 300 до 800 ₽, точную сумму рассчитает почта при оформлении.\n\n" +
                    "Скидок нет, цена фиксированная.";
        }

        // Размеры
        if (lowerMsg.contains("размер") || lowerMsg.contains("малом") || lowerMsg.contains("габарит") ||
                lowerMsg.contains("рост") || lowerMsg.contains("как подобрать") || lowerMsg.contains("таблиц")) {
            return "📏 В наличии только два размера:\n\n" +
                    "S — рост до 170 см, грудь до 92 см\n" +
                    "M — рост до 178 см, грудь до 100 см\n\n" +
                    "Если ваш размер не подходит — к сожалению, других размеров пока нет.";
        }

        // Доставка
        if (lowerMsg.contains("доставк") || lowerMsg.contains("отправк") || lowerMsg.contains("курьер") ||
                lowerMsg.contains("срок") || lowerMsg.contains("когда придет") || lowerMsg.contains("почт")) {
            return "🚚 Мы отправляем заказы по всей России.\n\n" +
                    "Срок доставки — от 3 до 7 рабочих дней.\n\n" +
                    "Стоимость доставки зависит от вашего региона:\n" +
                    "• обычно от 300 до 800 ₽\n" +
                    "• точную сумму рассчитает почта при оформлении заказа\n\n" +
                    "Отправка происходит в течение 1-2 дней после оплаты.";
        }

        // Оплата
        if (lowerMsg.contains("оплат") || lowerMsg.contains("карт") || lowerMsg.contains("сбп") ||
                lowerMsg.contains("нал") || lowerMsg.contains("как оплатить")) {
            return "💳 Мы принимаем оплату:\n\n" +
                    "• банковскими картами (Visa, Mastercard, МИР)\n" +
                    "• через СБП\n" +
                    "• наличными при самовывозе (если вы в Выборге)\n\n" +
                    "Ссылка на оплату придёт после оформления заказа.";
        }

        // тест
        if (lowerMsg.contains("тест")) {
            return "тест работает";
        }


        // Состав / качество
        if (lowerMsg.contains("ткань") || lowerMsg.contains("хлопок") || lowerMsg.contains("материал") ||
                lowerMsg.contains("качеств") || lowerMsg.contains("стирк") || lowerMsg.contains("уход")) {
            return "👕 Мы используем 100% натуральный хлопок высокого качества.\n\n" +
                    "Футболки мягкие, дышащие и хорошо держат форму.\n\n" +
                    "Рекомендации по уходу:\n" +
                    "• стирка при 30°C\n" +
                    "• не отбеливать\n" +
                    "• гладить с изнаночной стороны\n\n" +
                    "При правильном уходе принт сохраняет яркость долгое время.";
        }

        // Контакты
        if (lowerMsg.contains("контакт") || lowerMsg.contains("связаться") || lowerMsg.contains("позвонить") ||
                lowerMsg.contains("написать") || lowerMsg.contains("менеджер") || lowerMsg.contains("поддержк") ||
                lowerMsg.contains("телефон") || lowerMsg.contains("почт") || lowerMsg.contains("tg") ||
                lowerMsg.contains("telegram")) {
            return "📱 Связаться с нами можно так:\n\n" +
                    "Telegram: @masharachkova, @ElyaRachkova\n" +
                    "Почта: alia85_07@mail.ru, masharachkovaa@gmail.com\n\n" +
                    "Или просто продолжайте диалог со мной — я отвечу на все вопросы.";
        }

        // Каталог
        if (lowerMsg.contains("каталог") || lowerMsg.contains("ассортимент") || lowerMsg.contains("какие футболки") ||
                lowerMsg.contains("выбор") || lowerMsg.contains("модель") || lowerMsg.contains("расцветк") ||
                lowerMsg.contains("цвет")) {
            return "🛍️ Весь ассортимент футболок MAVE представлен на нашем сайте:\n\n" +
                    "👉 https://themave.ru\n\n" +
                    "Размеры: только S и M.";
        }

        // Бренд / О нас
        if (lowerMsg.contains("бренд") || lowerMsg.contains("о вас") || lowerMsg.contains("кто вы")) {
            return "👕 MAVE — это бренд качественной одежды из натурального хлопка.\n\n" +
                    "Мы создаём футболки, в которых комфортно в любой ситуации.\n\n" +
                    "Каждая модель продумана до мелочей — от ткани до принта.\n\n" +
                    "Простота, стиль и качество — вот что такое MAVE.";
        }



        // Если ничего не подошло
        return "Спасибо за ваш вопрос! Я не совсем понял, что вы ищете.\n\n" +
                "Я могу рассказать вам о:\n" +
                "• ценах (4 500 ₽)\n" +
                "• размерах (только S и M)\n" +
                "• доставке (по всей России)\n" +
                "• оплате (карта, СБП, наличные)\n" +
                "• уходе за футболкой\n" +
                "Напишите, что вас интересует, или посмотрите каталог на сайте: https://themave.ru";
    }
}