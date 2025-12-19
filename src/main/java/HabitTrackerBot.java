import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;

public class HabitTrackerBot extends TelegramLongPollingBot {

    private DatabaseManager dbManager;

    private java.util.Map<Long, UserState> userStates = new java.util.HashMap<>();

    private class UserState {
        String state;
        String tempData;
        Integer tempHabitId;

        UserState(String state) {
            this.state = state;
        }

        UserState(String state, String tempData) {
            this.state = state;
            this.tempData = tempData;
        }

        UserState(String state, String tempData, Integer tempHabitId) {
            this.state = state;
            this.tempData = tempData;
            this.tempHabitId = tempHabitId;
        }
    }

    public HabitTrackerBot() {
        dbManager = new DatabaseManager();
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Проверяем, что получено текстовое сообщение
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            long userId = update.getMessage().getFrom().getId();

            switch (messageText) {
                case "/start":
                    sendWelcomeMessage(chatId);
                    break;
                case "/help":
                    sendHelpMessage(chatId);
                    break;
                case "/newhabit":
                    startCreatingHabit(chatId, userId);
                    break;
                case "/myhabits":
                    showUserHabits(chatId, userId);
                    break;
                case "/complete":
                    askForHabitToComplete(chatId, userId);
                    break;
                case "/deletehabit":
                    askForHabitToDelete(chatId, userId);
                    break;
                case "/stats":
                    showStats(chatId, userId);
                    break;
                case "/adddescription":
                    askForHabitToAddDescription(chatId, userId);
                    break;
                default:
                    handleUserInput(chatId, userId, messageText);
            }
        }
    }

    private void sendWelcomeMessage(long chatId) {
        String welcomeText = "🎯 Добро пожаловать в трекер привычек!\n\n" +
                "Я помогу вам формировать полезные привычки!\n" +
                "Нажмите /help чтобы увидеть все команды\n\n"+
                "Начните с создания своей первой привычки!";

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(welcomeText);
        sendMessageWithKeyboard(message);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendHelpMessage(long chatId) {
        String helpText = "🎯 Добро пожаловать в трекер привычек!\n\n" +
                "С помощью этого бота вы можете:\n" +
                "📝 Создавать привычки с описанием\n" +
                "📋 Просматривать свои привычки\n" +
                "✅ Отмечать выполнение привычек\n" +
                "🗑️ Удалять привычки\n" +
                "📊 Отслеживать прогресс\n" +
                "✏️ Добавлять/изменять описания\n\n" +
                "Доступные команды:\n" +
                "/newhabit - Создать новую привычку\n" +
                "/myhabits - Показать мои привычки\n" +
                "/complete - Отметить выполнение привычки\n" +
                "/deletehabit - Удалить привычку\n" +
                "/adddescription - Добавить описание к привычке\n" +
                "/stats - Показать статистику\n" +
                "/help - Помощь";

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(helpText);
        sendMessageWithKeyboard(message);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void startCreatingHabit(long chatId, long userId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("📝 Введите название новой привычки:");

        userStates.put(userId, new UserState("waiting_for_habit_name"));

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void showUserHabits(long chatId, long userId) {
        List<Habit> habits = dbManager.getUserHabits(userId);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);

        if (habits.isEmpty()) {
            message.setText("📭 У вас пока нет привычек. Создайте первую с помощью /newhabit");
        } else {
            StringBuilder habitsText = new StringBuilder("📋 Ваши привычки:\n\n");
            for (Habit habit : habits) {
                habitsText.append(habit.toString()).append("\n\n");
            }
            message.setText(habitsText.toString());
        }

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void askForHabitToComplete(long chatId, long userId) {
        List<Habit> habits = dbManager.getUserHabits(userId);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);

        if (habits.isEmpty()) {
            message.setText("📭 У вас нет привычек для отметки");
        } else {
            StringBuilder habitsList = new StringBuilder("✅ Отметить выполнение привычки:\n\n");
            for (Habit habit : habits) {
                habitsList.append(String.format("#%d - %s\n", habit.getId(), habit.getName()));
            }
            habitsList.append("\nВведите ID привычки: (только число)");
            message.setText(habitsList.toString());
            userStates.put(userId, new UserState("waiting_for_complete_id"));
        }

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void askForHabitToDelete(long chatId, long userId) {
        List<Habit> habits = dbManager.getUserHabits(userId);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);

        if (habits.isEmpty()) {
            message.setText("📭 У вас нет привычек для удаления");
        } else {
            StringBuilder habitsList = new StringBuilder("🗑️ Удалить привычку:\n\n");
            for (Habit habit : habits) {
                habitsList.append(String.format("#%d - %s\n", habit.getId(), habit.getName()));
            }
            habitsList.append("\nВведите ID привычки для удаления:(только число)");
            message.setText(habitsList.toString());
            userStates.put(userId, new UserState("waiting_for_delete_id"));
        }

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void askForHabitToAddDescription(long chatId, long userId) {
        List<Habit> habits = dbManager.getUserHabits(userId);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);

        if (habits.isEmpty()) {
            message.setText("📭 У вас нет привычек для добавления описания");
        } else {
            StringBuilder habitsList = new StringBuilder("✏️ Добавить описание к привычке:\n\n");
            for (Habit habit : habits) {
                String currentDesc = habit.getDescription();
                if (currentDesc == null || currentDesc.isEmpty()) {
                    habitsList.append(String.format("#%d - %s (нет описания)\n", habit.getId(), habit.getName()));
                } else {
                    habitsList.append(String.format("#%d - %s\n", habit.getId(), habit.getName()));
                }
            }
            habitsList.append("\nВведите ID привычки для добавления/изменения описания:(только число)");
            message.setText(habitsList.toString());
            userStates.put(userId, new UserState("waiting_for_description_habit_id"));
        }

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void showStats(long chatId, long userId) {
        String stats = dbManager.getUserStats(userId);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(stats);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleUserInput(long chatId, long userId, String input) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);

        UserState userState = userStates.get(userId);

        if (userState == null) {
            message.setText("Неизвестная команда. Используйте /help для просмотра доступных команд.");
        } else {
            switch (userState.state) {
                case "waiting_for_habit_name":
                    userStates.put(userId, new UserState("waiting_for_habit_description", input));
                    message.setText("📝 Теперь введите описание для привычки \"" + input + "\":\n" +
                            "(Если не хотите добавлять описание, отправьте '-' )");
                    break;

                case "waiting_for_habit_description":
                    String habitName = userState.tempData;
                    String description = input.equals("-") ? "" : input;

                    boolean success = dbManager.addHabit(userId, habitName, description);
                    if (success) {
                        message.setText("✅ Привычка \"" + habitName + "\" успешно создана!\n" +
                                (description.isEmpty() ? "Описание не добавлено" : "Описание: " + description));
                    } else {
                        message.setText("❌ Ошибка при создании привычки. Попробуйте еще раз.");
                    }
                    userStates.remove(userId);
                    break;

                case "waiting_for_complete_id":
                    try {
                        int habitId = Integer.parseInt(input);
                        boolean completed = dbManager.completeHabit(habitId, userId);

                        if (completed) {
                            message.setText("🎉 Привычка отмечена как выполненная сегодня!");
                        } else {
                            message.setText("❌ Не удалось найти привычку с таким ID");
                        }
                    } catch (NumberFormatException e) {
                        message.setText("❌ Пожалуйста, введите число (ID привычки)");
                    }
                    userStates.remove(userId);
                    break;

                case "waiting_for_delete_id":
                    try {
                        int habitId = Integer.parseInt(input);
                        boolean deleted = dbManager.deleteHabit(habitId, userId);

                        if (deleted) {
                            message.setText("🗑️ Привычка успешно удалена!");
                        } else {
                            message.setText("❌ Не удалось найти привычку с таким ID");
                        }
                    } catch (NumberFormatException e) {
                        message.setText("❌ Пожалуйста, введите число (ID привычки)");
                    }
                    userStates.remove(userId);
                    break;

                case "waiting_for_description_habit_id":
                    try {
                        int habitId = Integer.parseInt(input);
                        Habit habit = dbManager.getHabitById(habitId, userId);

                        if (habit != null) {
                            userStates.put(userId, new UserState("waiting_for_description_text", null, habitId));

                            String currentDesc = habit.getDescription();
                            if (currentDesc != null && !currentDesc.isEmpty()) {
                                message.setText("✏️ Текущее описание привычки \"" + habit.getName() + "\":\n" +
                                        currentDesc + "\n\n" +
                                        "Введите новое описание (или '-' чтобы оставить текущее):");
                            } else {
                                message.setText("✏️ Введите описание для привычки \"" + habit.getName() + "\":\n" +
                                        "(Отправьте '-' если не хотите добавлять описание)");
                            }
                        } else {
                            message.setText("❌ Не удалось найти привычку с таким ID");
                            userStates.remove(userId);
                        }
                    } catch (NumberFormatException e) {
                        message.setText("❌ Пожалуйста, введите число (ID привычки)");
                        userStates.remove(userId);
                    }
                    break;

                case "waiting_for_description_text":
                    Integer habitId = userState.tempHabitId;
                    String newDescription = input.equals("-") ? "" : input;

                    boolean updated = dbManager.updateHabitDescription(habitId, userId, newDescription);
                    if (updated) {
                        message.setText("✅ Описание привычки успешно " +
                                (newDescription.isEmpty() ? "удалено" : "обновлено") + "!");
                    } else {
                        message.setText("❌ Ошибка при обновлении описания");
                    }
                    userStates.remove(userId);
                    break;

                default:
                    message.setText("Неизвестное состояние. Используйте /help для просмотра команд.");
                    userStates.remove(userId);
            }
        }

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageWithKeyboard(SendMessage message) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("/newhabit");
        row1.add("/myhabits");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("/complete");
        row2.add("/adddescription");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("/deletehabit");
        row3.add("/stats");

        KeyboardRow row4 = new KeyboardRow();
        row4.add("/help");

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);
        keyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(keyboardMarkup);
    }

    @Override
    public String getBotUsername() {
        return ""; //имя бота
    }

    @Override
    public String getBotToken() {
        return ""; //токен бота
    }

    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new HabitTrackerBot());
            System.out.println("🎯 Бот трекера привычек запущен!");
            System.out.println("📊 База данных: habits.db");
            System.out.println("✏️ Добавлена возможность добавлять описания к привычкам!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClosing() {
        dbManager.close();
        super.onClosing();
    }
}