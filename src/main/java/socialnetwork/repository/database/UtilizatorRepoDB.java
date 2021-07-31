package socialnetwork.repository.database;

import socialnetwork.domain.Eveniment;
import socialnetwork.domain.Message;
import socialnetwork.domain.Utilizator;
import socialnetwork.domain.validators.RepoException;
import socialnetwork.domain.validators.Validator;
import socialnetwork.repository.Repository;

import java.sql.*;
import java.util.*;



public class UtilizatorRepoDB implements Repository<Long, Utilizator> {
    private String url;
    private String username;
    private String password;
    private Validator<Utilizator> validator;

    public UtilizatorRepoDB(String url, String username, String password, Validator<Utilizator> validator) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }

    /**
     * Va returna utilizator dat de id-ul sau
     * @param aLong ->un id de utilizator
     * @return utilizator gasit(sau null, daca nu exista)
     */
    @Override
    public Utilizator findOne(Long aLong) {

        String SQL = "SELECT id,first_name,last_name "
                + "FROM utilizatori "
                + "WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, Math.toIntExact(aLong));
            ResultSet rs = pstmt.executeQuery();

            while(rs.next()) {
                Long id = rs.getLong("id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");

                Utilizator u = new Utilizator(firstName, lastName);
                u.setId(id);
                return u;
            }


        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        return null;
    }


    /**
     *
     * @return toti utilizatorii din tabela utilizatori
     */
    @Override
    public Iterable<Utilizator> findAll() {
        Set<Utilizator> users = new HashSet<>();

        try {

            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("SELECT * from utilizatori ORDER BY last_name");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");

                Utilizator utilizator = new Utilizator(firstName, lastName);
                utilizator.setId(id);
                users.add(utilizator);

            }
            return users;
        }
         catch(SQLException e){
                e.printStackTrace();}
        catch (ClassNotFoundException e){
            e.printStackTrace();
        }

            return users;

    }

    @Override
    public Iterable<Utilizator> findFromOffset(Long idUtilizatorCurent,Long offset, Long limit) {
        Set<Utilizator> users = new HashSet<>();
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM utilizatori ORDER BY last_name LIMIT "+limit+" OFFSET "+offset+";");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");

                Utilizator utilizator = new Utilizator(firstName, lastName);
                utilizator.setId(id);
                users.add(utilizator);

            }
            return users;
        }
        catch(SQLException e){
            e.printStackTrace();}
        catch (ClassNotFoundException e){
            e.printStackTrace();
        }

        return users;

    }


    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }


    /**
     * Se introduce o noua entitate in tabel
     * @param entity ->un utilizator de adaugat in tabela
     *         entity must be not null
     * @return utilizatorul adaugat
     */
    @Override
    public Utilizator save(Utilizator entity) {

        String SQL = "INSERT INTO utilizatori(id,first_name,last_name) VALUES(?,?,?)";

        long id = 0;
        long idaux=entity.getId();
        while(findOne(idaux)!=null){
            idaux ++;
        }

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL,
                     Statement.RETURN_GENERATED_KEYS)) {


            pstmt.setInt(1, Math.toIntExact(idaux));
            pstmt.setString(2, entity.getFirstName());
            pstmt.setString(3, entity.getLastName());

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


        return  entity;

    }

    @Override
    public void addUtilizatorInscris(long id_e, long id_u) {

    }

    @Override
    public Iterable<Eveniment> findUsersEventsFromOffset(Long idUtilizatorCurent, Long offset, Long limit) {
        return null;
    }

    @Override
    public Iterable<Message> find2UsersMessagesFromOffset(Long idUtilizatorCurent, Long idPrieten, Long offset, Long limit) {
        return null;
    }

    @Override
    public int nrElemOf2Users(long id1, long id2) {
        return 0;
    }

    @Override
    public int nrElemOfAnUser(long id) {
        return 0;
    }

    @Override
    public Eveniment deleteUserOfAnEvent(Long iduser, Long ideveniment) {
        return null;
    }


    /**
     * Se sterge un utilizator, dat prin id-ul sau
     * @param aLong ->id utilizator de sters
     * @return null
     */
    @Override
    public Utilizator delete(Long aLong) {

        String SQL = "DELETE FROM utilizatori WHERE id = ?";
        if(findOne(aLong) == null ) throw  new RepoException("Nu exista utilizatorul.");

        int affectedrows = 0;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, Math.toIntExact(aLong));

            affectedrows = pstmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }


    /**
     * Modifica prenumele unui utilizator
     * @param id ->id-ul unui utilizator existent
     * @param firstname ->prenumele ce inlocuieste prenumele curent
     */
    public  void setBDFirstName(long id, String firstname ){
        String SQL = "UPDATE utilizatori "
                + "SET first_name = ? "
                + "WHERE id = ?";

        int affectedrows = 0;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {


            pstmt.setString(1, firstname);
            pstmt.setInt(2, Math.toIntExact(id));

            affectedrows = pstmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

    }


    /**
     * Modifica numele de familie a unui utilizator
     * @param id ->id-ul unui utilizator existent
     * @param lastname -> numele ce inlocuieste numele curent

     */
    public void setBDLastName(long id, String lastname){
        String SQL = "UPDATE utilizatori "
                + "SET last_name = ? "
                + "WHERE id = ?";

        int affectedrows = 0;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setString(1, lastname);
            pstmt.setInt(2, Math.toIntExact(id));

            affectedrows = pstmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }


    /**
     * Se modifica numele si prenumele unui utilizator existent
     * @param entity ->datele cu care se inlocuiesc cele curente
     *          entity must not be null
     * @return utilizatorul modificat
     */
    @Override
    public Utilizator update(Utilizator entity) {
        setBDFirstName(entity.getId(), entity.getFirstName());
        setBDLastName(entity.getId(), entity.getLastName());
        return findOne(entity.getId());
    }

    /**
     *
     * @return numarul de elemente din tabela
     */
    @Override
    public int nrElem() {
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM utilizatori ;");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Integer nr = resultSet.getInt(1);
                return nr;
            }
        }
        catch(SQLException e){
            e.printStackTrace();}
        catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        return 0;

    }

    public Long findAccount(String username, String password) {

        String SQL = "SELECT id_utilizator "
                + "FROM contutilizator "
                + "WHERE username = ? AND parola = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setString(1,username);
            pstmt.setString(2,password);
            ResultSet rs = pstmt.executeQuery();

            while(rs.next()) {
                Long id = rs.getLong("id");
                return id;
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        return null;
    }
}
