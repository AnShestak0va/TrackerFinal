public class Habit {
    private int id;
    private long userId;
    private String name;
    private String description;
    private String createdDate;
    private int completedDays;
    private int totalDays;

    public Habit(int id, long userId, String name, String description,
                 String createdDate, int completedDays, int totalDays) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.createdDate = createdDate;
        this.completedDays = completedDays;
        this.totalDays = totalDays;
    }

    public int getId() { return id; }
    public long getUserId() { return userId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCreatedDate() { return createdDate; }
    public int getCompletedDays() { return completedDays; }
    public int getTotalDays() { return totalDays; }

    @Override
    public String toString() {
        return String.format("📌 Привычка #%d\n" +
                        "🎯 Название: %s\n" +
                        "📝 Описание: %s\n" +
                        "📅 Создана: %s\n" +
                        "✅ Выполнено дней: %d/%d",
                id, name, description, createdDate.substring(0, 10), completedDays, totalDays);
    }
}