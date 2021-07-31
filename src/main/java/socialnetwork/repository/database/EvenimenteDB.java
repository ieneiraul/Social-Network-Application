package socialnetwork.repository.database;

import socialnetwork.domain.Eveniment;

import socialnetwork.domain.Message;
import socialnetwork.domain.Utilizator;
import socialnetwork.domain.validators.RepoException;
import socialnetwork.domain.validators.Validator;
import socialnetwork.repository.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EvenimenteDB implements Repository<Long, Eveniment> {
    private String url;
    private String username;
    private String password;
    private Validator<Eveniment> validator;
    Repository<Long, Utilizator> repository ;

    public EvenimenteDB(String url, String username, String password, Validator<Eveniment> validator, Repository<Long, Utilizator> repository) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
        this.repository = repository;
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public Eveniment findOne(Long aLong) {
        String SQL = "SELECT * "
                + "FROM evenimente "
                + "WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, Math.toIntExact(aLong));
            ResultSet rs = pstmt.executeQuery();

            while(rs.next()) {
                Long id = rs.getLong("id");
                Long idcreator = rs.getLong("idcreator");
                String nume = rs.getString("nume");
                String descriere = rs.getString("descriere");
                LocalDateTime data = rs.getObject( 5,LocalDateTime.class);

                Utilizator creator= repository.findOne(idcreator);
                List<Utilizator> to = getUtilizatoriInscrisiLaUnEveniment(id);
                Eveniment e= new Eveniment(nume,descriere,creator,to,data);
                e.setId(id);
                return e;
            }


        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        return null;
    }

    @Override
    public Iterable<Eveniment> findAll() {
        Set<Eveniment> eveniments = new HashSet<>();

        try {

            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("SELECT * from evenimente");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long idcreator = resultSet.getLong("idcreator");
                String nume= resultSet.getString("nume");
                String descriere= resultSet.getString("descriere");
                LocalDateTime date=resultSet.getObject(5,LocalDateTime.class);


                Utilizator u = repository.findOne(idcreator);
                List<Utilizator> to = getUtilizatoriInscrisiLaUnEveniment(id);
                Eveniment e=new Eveniment(nume,descriere,u,to,date);
                e.setId(id);



                eveniments.add(e);

            }

            return eveniments;
        }
        catch(SQLException e){
            e.printStackTrace();}
        catch (ClassNotFoundException e){
            e.printStackTrace();
        }

        return eveniments;
    }
    /**
     * Ia toate datele din tabela messagereceiver
     * Creeaza o lista de long unde pune toti utilizatorii catre care se adreseaza
     * mesajul cu id-ul id
     * @param id ->mesajul a carui destinatarii ii caut
     * @return lista cu destinatarii
     */
    public List<Utilizator> getUtilizatoriInscrisiLaUnEveniment(long id){
        List<Utilizator> to  = new ArrayList<>();
        try {

            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("SELECT * from eventenrolledusers WHERE ideveniment="+id+" ;");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Long id_eveniment = resultSet.getLong("ideveniment");
                Long id_user = resultSet.getLong("iduser");
                    Utilizator u = repository.findOne(id_user);
                    to.add(u);
            }

            return to;
        }
        catch(SQLException e){
            e.printStackTrace();}
        catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        return to;
    }

    @Override
    public Iterable<Eveniment> findFromOffset(Long idUtilizatorCurent, Long offset, Long limit) {
        Set<Eveniment> eveniments = new HashSet<>();
        try {

            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("SELECT * from evenimente ORDER BY data LIMIT "+limit+" OFFSET "+offset+";");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long idcreator = resultSet.getLong("idcreator");
                String nume= resultSet.getString("nume");
                String descriere= resultSet.getString("descriere");
                LocalDateTime date=resultSet.getObject(5,LocalDateTime.class);

                Utilizator u = repository.findOne(idcreator);
                List<Utilizator> to = getUtilizatoriInscrisiLaUnEveniment(id);
                Eveniment e=new Eveniment(nume,descriere,u,to,date);
                e.setId(id);
                eveniments.add(e);
            }

            return eveniments;
        }
        catch(SQLException e){
            e.printStackTrace();}
        catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        return eveniments;
    }

    @Override
    public Iterable<Eveniment> findUsersEventsFromOffset(Long idUtilizatorCurent, Long offset, Long limit) {
        Set<Eveniment> eveniments = new HashSet<>();
        try {

            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("SELECT * from evenimente WHERE id IN (SELECT ideveniment FROM eventenrolledusers WHERE iduser="+idUtilizatorCurent+" ) ORDER BY data LIMIT "+limit+" OFFSET "+offset+";");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long idcreator = resultSet.getLong("idcreator");
                String nume= resultSet.getString("nume");
                String descriere= resultSet.getString("descriere");
                LocalDateTime date=resultSet.getObject(5,LocalDateTime.class);

                Utilizator u = repository.findOne(idcreator);
                List<Utilizator> to = getUtilizatoriInscrisiLaUnEveniment(id);
                Eveniment e=new Eveniment(nume,descriere,u,to,date);
                e.setId(id);
                eveniments.add(e);
            }

            return eveniments;
        }
        catch(SQLException e){
            e.printStackTrace();}
        catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        return eveniments;
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
            PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM evenimente WHERE id IN (SELECT ideveniment FROM eventenrolledusers WHERE iduser="+id+" );");
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
    public Eveniment save(Eveniment entity) {
        String SQL = "INSERT INTO evenimente(id,idcreator,nume,descriere,data) VALUES(?,?,?,?,?)";

        long id = 0;


        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL,
                     Statement.RETURN_GENERATED_KEYS)) {


            pstmt.setInt(1, Math.toIntExact(entity.getId()));
            pstmt.setInt(2, Math.toIntExact(entity.getUtilizatorCreator().getId()));
            pstmt.setString(3, entity.getNume());
            pstmt.setString(4, entity.getDescriere());
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
                entity.getUtilizatoriInscrisi().forEach(x->addUtilizatorInscris(entity.getId(), x.getId()));
                //entity.getTo().forEach(x->addReceiver(entity.getId(),x.getId()));

            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return  entity;
    }
    @Override
    public void addUtilizatorInscris(long id_e, long id_u){
        String SQL = "INSERT INTO eventenrolledusers(ideveniment,iduser) VALUES(?,?)";

        long id =0;
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL,
                     Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, Math.toIntExact(id_e));
            pstmt.setInt(2, Math.toIntExact(id_u));


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

    @Override
    public Eveniment delete(Long aLong) {
        String SQL = "DELETE FROM evenimente WHERE id = ?";
        if(findOne(aLong) == null ) throw  new RepoException("Nu exista mesajul.");

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

    @Override
    public Eveniment deleteUserOfAnEvent(Long iduser, Long ideveniment) {
        String SQL = "DELETE FROM eventenrolledusers WHERE iduser = ? AND ideveniment = ?";

        int affectedrows = 0;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, Math.toIntExact(iduser));
            pstmt.setInt(2, Math.toIntExact(ideveniment));

            affectedrows = pstmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    @Override
    public Eveniment update(Eveniment entity) {
        return entity;
    }

    @Override
    public int nrElem() {
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM evenimente ;");
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
