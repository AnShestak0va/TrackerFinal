import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

// Главный класс бота, который наследуется от TelegramLongPollingBot
// Этот класс будет обрабатывать входящие сообщения
public class SimpleHelpBot extends TelegramLongPollingBot {

    // Метод, который вызывается при получении нового сообщения
    @Override
    public void onUpdateReceived(Update update) {
        // Проверяем, содержит ли обновление сообщение от пользователя
        if (update.hasMessage() && update.getMessage().hasText()) {

            // Получаем текст сообщения от пользователя
            String messageText = update.getMessage().getText();
            // Получаем ID чата для отправки ответа
            long chatId = update.getMessage().getChatId();

            // Проверяем, если пользователь отправил команду "/помощь"
            if (messageText.equals("/помощь")) {
                // Отправляем ответ с помощью
                sendHelpMessage(chatId);
            }
        }
    }

    // Метод для отправки сообщения с помощью
    private void sendHelpMessage(long chatId) {
        // Создаем объект SendMessage для отправки сообщения
        SendMessage message = new SendMessage();

        // Устанавливаем ID чата, куда отправить сообщение
        message.setChatId(chatId);

        // Текст сообщения с помощью
        String helpText = "Это простой бот-помощник.\n" +
                "Доступные команды:\n" +
                "/помощь - показать это сообщение";

        // Устанавливаем текст сообщения
        message.setText(helpText);

        try {
            // Выполняем отправку сообщения
            execute(message);
        } catch (TelegramApiException e) {
            // Обрабатываем возможные ошибки при отправке
            e.printStackTrace();
        }
    }

    // Метод, который возвращает имя бота (то, что следует после @)
    @Override
    public String getBotUsername() {
        return "treker_privichek_bot"; // Замените на имя вашего бота
    }

    // Метод, который возвращает токен бота
    // Токен получается у @BotFather в Telegram
    @Override
    public String getBotToken() {
        return "8234577299:AAElrwziWdME-CXXjYeFjcqSSKb32BacfsU";
    }

    // Основной метод для запуска бота
    public static void main(String[] args) {
        try {
            // Создаем объект для работы с API Telegram
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

            // Регистрируем нашего бота
            botsApi.registerBot(new SimpleHelpBot());

            // Выводим сообщение об успешном запуске
            System.out.println("Бот запущен и готов к работе!");

        } catch (TelegramApiException e) {
            // Обрабатываем ошибки при запуске бота
            e.printStackTrace();
        }
    }
}