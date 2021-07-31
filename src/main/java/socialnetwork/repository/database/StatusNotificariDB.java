package socialnetwork.repository.database;

import java.sql.*;

public class StatusNotificariDB {
    private String url;
    private String username;
    private String password;

    public StatusNotificariDB(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public Long findStatus(Long idUtilizator) {
        String SQL = "SELECT status "
                + "FROM statusnotificari "
                + "WHERE idutilizator = ? ";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, Math.toIntExact(idUtilizator));
            ResultSet rs = pstmt.executeQuery();

            while(rs.next()) {
                Long status = rs.getLong("status");
                return status;
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        return null;
    }

    public void updateStatus(Long idUtilizator, Long status) {
        String SQL = "UPDATE statusnotificari "
                + "SET status = ? "
                + "WHERE idutilizator = ?";

        int affectedrows = 0;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, Math.toIntExact(status));
            pstmt.setInt(2, Math.toIntExact(idUtilizator));

            affectedrows = pstmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void saveStatus(Long idUtilizator) {
        String SQL = "INSERT INTO statusnotificari(idutilizator,status) VALUES(?,?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL,
                     Statement.RETURN_GENERATED_KEYS)) {
            Long status=(long)1;
            pstmt.setInt(1, Math.toIntExact(idUtilizator));
            pstmt.setInt(2, Math.toIntExact(status));

            int affectedRows = pstmt.executeUpdate();
            // check the affected rows
            if (affectedRows > 0) {
                // get the ID back
                //System.out.println("exista deja acest id");
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
