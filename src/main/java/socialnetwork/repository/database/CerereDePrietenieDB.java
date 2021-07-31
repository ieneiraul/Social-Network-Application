package socialnetwork.repository.database;

import socialnetwork.domain.CerereDePrietenie;

import socialnetwork.domain.Eveniment;
import socialnetwork.domain.Message;
import socialnetwork.domain.Utilizator;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.domain.validators.Validator;
import socialnetwork.repository.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;



public class CerereDePrietenieDB implements Repository<Long, CerereDePrietenie> {
    private String url;
    private String username;
    private String password;
    private Validator<CerereDePrietenie> validator;
    private Repository<Long,Utilizator> repository;

    public CerereDePrietenieDB(String url, String username, String password, Validator<CerereDePrietenie> validator,
                               Repository<Long,Utilizator> repository) {
        this.url = url;
        this.repository=repository;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }

    /**
     * Cauta o cerere de prietenie
     * @param aLong ->id-ul cererii cautate
     * @return cererea de prietenie
     */
    @Override
    public CerereDePrietenie findOne(Long aLong) {
//        String SQL = "SELECT id,id_1,id_2,status,datac"
//                + "FROM cereredeprietenie "
//                + "WHERE id = ?";
//
//        try (Connection conn = connect();
//             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
//
//            pstmt.setInt(1, Math.toIntExact(aLong));
//            ResultSet rs = pstmt.executeQuery();
//
//            while(rs.next()) {
//                Long id = rs.getLong("id");
//                Long id_1 = rs.getLong("id_1");
//                Long id_2 = rs.getLong("id_2");
//
//                String status  = rs.getString("status");
//                LocalDateTime data = rs.getObject( 5,LocalDateTime.class);
//
//                Utilizator u1=repository.findOne(id_1);
//                Utilizator u2=repository.findOne(id_2);
//
//                CerereDePrietenie u =new CerereDePrietenie(u1,u2);
//                u.setId(id);
//                u.setStatus(status);
//                u.setData(data);
//                return u;
//            }
//
//
//        } catch (SQLException ex) {
//            System.out.println(ex.getMessage());
//        }
//
//        return null;
        List<CerereDePrietenie> list=new ArrayList<>();
        findAll().forEach(list::add);
        for(CerereDePrietenie cerere: list){
            if(cerere.getId() == aLong)
                return cerere;
        }
        return  null;
    }


    /**
     *
     * @return toate cererile de prietenie
     */
    @Override
    public Iterable<CerereDePrietenie> findAll() {
        Set<CerereDePrietenie> cereri = new HashSet<>();

        try {

            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("SELECT * from cereredeprietenie");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long id_1 = resultSet.getLong("id_1");
                Long id_2 = resultSet.getLong("id_2");
                String status= resultSet.getString("status");
                LocalDateTime data = resultSet.getObject( 5,LocalDateTime.class);


                Utilizator u1=repository.findOne(id_1);
                Utilizator u2=repository.findOne(id_2);
                CerereDePrietenie c = new CerereDePrietenie(u1,u2);
                c.setId(id);
                c.setStatus(status);
                c.setData(data);
                cereri.add(c);

            }
            return cereri;
        }
        catch(SQLException e){
            e.printStackTrace();}
        catch (ClassNotFoundException e){
            e.printStackTrace();
        }

        return cereri;

    }

    @Override
    public Iterable<CerereDePrietenie> findFromOffset(Long idUtilizatorCurent ,Long offset, Long limit) {
        Set<CerereDePrietenie> cereri = new HashSet<>();
        try {

            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, username, password);
            //PreparedStatement statement = connection.prepareStatement("SELECT * from cereredeprietenie WHERE id_1="+idUtilizatorCurent+" OR id_2="+idUtilizatorCurent+" LIMIT "+limit+" OFFSET "+offset+";");
            PreparedStatement statement = connection.prepareStatement("SELECT * from cereredeprietenie WHERE id_1="+idUtilizatorCurent+" OR id_2="+idUtilizatorCurent+" ORDER BY datac LIMIT "+limit+" OFFSET "+offset+";");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long id_1 = resultSet.getLong("id_1");
                Long id_2 = resultSet.getLong("id_2");
                String status= resultSet.getString("status");
                LocalDateTime data = resultSet.getObject( 5,LocalDateTime.class);


                Utilizator u1=repository.findOne(id_1);
                Utilizator u2=repository.findOne(id_2);
                CerereDePrietenie c = new CerereDePrietenie(u1,u2);
                c.setId(id);
                c.setStatus(status);
                c.setData(data);
                cereri.add(c);

            }
            return cereri;
        }
        catch(SQLException e){
            e.printStackTrace();}
        catch (ClassNotFoundException e){
            e.printStackTrace();
        }

        return cereri;
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }


    /**
     * Salveaza o noua cerere de prietenie
     * @param entity ->cererea de prietenie de salvat
     *         entity must be not null
     * @return ->cererea de prietenie salvata
     */
    @Override
    public CerereDePrietenie save(CerereDePrietenie entity) {

        String SQL = "INSERT INTO cereredeprietenie(id, id_1,id_2,status,datac) VALUES(?,?,?,?,?)";

        long id = 0;


        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL,
                     Statement.RETURN_GENERATED_KEYS)) {


            pstmt.setInt(1, Math.toIntExact(entity.getId()));
            pstmt.setInt(2, Math.toIntExact(entity.getTrimite().getId()));
            pstmt.setInt(3, Math.toIntExact(entity.getPrimeste().getId()));
            pstmt.setString(4, entity.getStatus());
            pstmt.setObject(5, entity.getData());


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

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM cereredeprietenie WHERE id_1= "+id+" OR id_2= "+id+" ;");
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

    @Override
    public Eveniment deleteUserOfAnEvent(Long iduser, Long ideveniment) {
        return null;
    }


    /**
     * Sterge o cerere de prietenie
     * @param aLong ->id-ul unei cereri de prietenie
     * @return null
     */
    @Override
    public CerereDePrietenie delete(Long aLong) {

        String SQL = "DELETE FROM cereredeprietenie WHERE id = ?";
        if(findOne(aLong) == null ) throw  new ValidationException("Nu exista cererea.");

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
     * Modifica statusul unei cereri de prietenie
     * @param c -> o cerere de prietenie
     * @return cererea de prietenie modificata
     */
    @Override
    public CerereDePrietenie update(CerereDePrietenie c ){
        String SQL = "UPDATE cerereDePrietenie "
                + "SET status = ? "
                + "WHERE id = ?";

        int affectedrows = 0;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {


            pstmt.setString(1, c.getStatus());
            pstmt.setInt(2, Math.toIntExact(c.getId()));

            affectedrows = pstmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return c;

    }


    /**
     *
     * @return numarul total de cereri de prietenie
     */
    @Override
    public int nrElem() {
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM cereredeprietenie ;");
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
}
