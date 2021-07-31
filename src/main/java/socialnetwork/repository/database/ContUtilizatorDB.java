package socialnetwork.repository.database;

import socialnetwork.domain.ContUtilizator;
import socialnetwork.repository.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.*;
import java.util.*;

public class ContUtilizatorDB {
    private String url;
    private String username;
    private String password;

    public ContUtilizatorDB(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
    public Long findAccount(String username, String password) {

        String SQL = "SELECT id_utilizator "
                + "FROM contutilizator "
                + "WHERE username = ? AND parola = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1,username);
            pstmt.setString(2,Hashing.PasswordHashing(password));
            ResultSet rs = pstmt.executeQuery();

            while(rs.next()) {
                Long id = rs.getLong("id_utilizator");
                return id;
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        return null;
    }

    public boolean existUser(String username) {
        String SQL = "SELECT id_utilizator "
                + "FROM contutilizator "
                + "WHERE username = ? ";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1,username);
            ResultSet rs = pstmt.executeQuery();

            while(rs.next()) {
                Long id = rs.getLong("id_utilizator");
                return true;
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        return false;
    }

    public void createAccount(Long id, String username, String password) {
        String SQL = "INSERT INTO contutilizator(id_utilizator,username,parola) VALUES(?,?,?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL,
                     Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, Math.toIntExact(id));
            pstmt.setString(2, username);
            pstmt.setString(3, Hashing.PasswordHashing(password));

            int affectedRows = pstmt.executeUpdate();
            // check the affected rows
            if (affectedRows > 0) {
                // get the ID back
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        id = rs.getLong(1);
                    }
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
