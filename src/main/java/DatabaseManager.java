import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:habits.db";
    private Connection connection;

    public DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            createTable();
            System.out.println("База данных подключена успешно");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Метод для создания таблицы привычек
    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS habits (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "name TEXT NOT NULL, " +
                "description TEXT, " +
                "created_date TEXT NOT NULL, " +
                "completed_days INTEGER DEFAULT 0, " +
                "total_days INTEGER DEFAULT 0)";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean addHabit(long userId, String name, String description) {
        String sql = "INSERT INTO habits (user_id, name, description, created_date) VALUES (?, ?, ?, datetime('now'))";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.setString(2, name);
            pstmt.setString(3, description);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Habit> getUserHabits(long userId) {
        List<Habit> habits = new ArrayList<>();
        String sql = "SELECT * FROM habits WHERE user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Habit habit = new Habit(
                        rs.getInt("id"),
                        rs.getLong("user_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("created_date"),
                        rs.getInt("completed_days"),
                        rs.getInt("total_days")
                );
                habits.add(habit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return habits;
    }

    public boolean completeHabit(int habitId, long userId) {
        String sql = "UPDATE habits SET completed_days = completed_days + 1, total_days = total_days + 1 " +
                "WHERE id = ? AND user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, habitId);
            pstmt.setLong(2, userId);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteHabit(int habitId, long userId) {
        String sql = "DELETE FROM habits WHERE id = ? AND user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, habitId);
            pstmt.setLong(2, userId);
            int rowsDeleted = pstmt.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getUserStats(long userId) {
        String sql = "SELECT COUNT(*) as total_habits, " +
                "SUM(completed_days) as total_completed, " +
                "SUM(total_days) as total_days " +
                "FROM habits WHERE user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int totalHabits = rs.getInt("total_habits");
                int totalCompleted = rs.getInt("total_completed");
                int totalDays = rs.getInt("total_days");
                double successRate = totalDays > 0 ? (double) totalCompleted / totalDays * 100 : 0;

                return String.format("📊 Ваша статистика:\n\n" +
                                "📝 Всего привычек: %d\n" +
                                "✅ Выполнено дней: %d\n" +
                                "📅 Всего дней: %d\n" +
                                "🎯 Успешность: %.1f%%",
                        totalHabits, totalCompleted, totalDays, successRate);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "📊 У вас пока нет привычек для статистики";
    }

    public Habit getHabitById(int habitId, long userId) {
        String sql = "SELECT * FROM habits WHERE id = ? AND user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, habitId);
            pstmt.setLong(2, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Habit(
                        rs.getInt("id"),
                        rs.getLong("user_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("created_date"),
                        rs.getInt("completed_days"),
                        rs.getInt("total_days")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean updateHabitDescription(int habitId, long userId, String description) {
        String sql = "UPDATE habits SET description = ? WHERE id = ? AND user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, description);
            pstmt.setInt(2, habitId);
            pstmt.setLong(3, userId);

            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}