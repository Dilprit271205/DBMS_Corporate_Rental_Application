import java.sql.*;

public class StudentDAO {

    public static void addStudent(String name, int age, String course) {
        try {
            Connection conn = DBConnection.getConnection();

            String query = "INSERT INTO students (name, age, course) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(query);

            ps.setString(1, name);
            ps.setInt(2, age);
            ps.setString(3, course);

            ps.executeUpdate();

            System.out.println("✅ Student Added!");

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void viewStudents() {
        try {
            Connection conn = DBConnection.getConnection();

            String query = "SELECT * FROM students";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            System.out.println("\nID | Name | Age | Course");

            while (rs.next()) {
                System.out.println(
                    rs.getInt("id") + " | " +
                    rs.getString("name") + " | " +
                    rs.getInt("age") + " | " +
                    rs.getString("course")
                );
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateStudent(int id, String name) {
        try {
            Connection conn = DBConnection.getConnection();

            String query = "UPDATE students SET name=? WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(query);

            ps.setString(1, name);
            ps.setInt(2, id);

            ps.executeUpdate();

            System.out.println("✅ Student Updated!");

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteStudent(int id) {
        try {
            Connection conn = DBConnection.getConnection();

            String query = "DELETE FROM students WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(query);

            ps.setInt(1, id);

            ps.executeUpdate();

            System.out.println("✅ Student Deleted!");

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}