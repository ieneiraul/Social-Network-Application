package socialnetwork.repository.database;

import socialnetwork.domain.Eveniment;
import socialnetwork.domain.Message;

import socialnetwork.domain.Utilizator;
import socialnetwork.domain.validators.RepoException;
import socialnetwork.domain.validators.Validator;
import socialnetwork.repository.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;



public class MessageDB implements Repository<Long, Message> {
    private String url;
    private String username;
    private String password;
    private Validator<Message> validator;
    Repository<Long,Utilizator> repository ;

    public MessageDB(String url, String username, String password, Validator<Message> validator,
                     Repository<Long,Utilizator> repository) {
        this.url = url;
        this.repository=repository;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }

    /**
     * Cauta un mesaj
     * @param aLong ->mesajul de cautat
     * @return mesajul gasit /null
     */
    @Override
    public Message findOne(Long aLong) {
        String SQL = "SELECT id,from_u,mesaj,datac,reply_to "
                + "FROM message "
                + "WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, Math.toIntExact(aLong));
            ResultSet rs = pstmt.executeQuery();

            while(rs.next()) {
                Long id = rs.getLong("id");
                Long from_u = rs.getLong("from_u");
                String mesaj = rs.getString("mesaj");
                LocalDateTime data = rs.getObject( 4,LocalDateTime.class);
                Long reply_to=rs.getLong("reply_to");

                Utilizator from= repository.findOne(from_u);
                List<Utilizator> to = MessageReceiver(id);
                Message m= new Message(from,to,mesaj);
                m.setReply(reply_to);
                m.setId(id);
                m.setDate(data);

                return m;
            }


        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        return null;
    }

    /**
     * Ia toate datele din tabela messagereceiver
     * Creeaza o lista de long unde pune toti utilizatorii catre care se adreseaza
     * mesajul cu id-ul id
     * @param id ->mesajul a carui destinatarii ii caut
     * @return lista cu destinatarii
     */
    public List<Utilizator> MessageReceiver(long id){

        List<Utilizator> to  = new ArrayList<>();
        try {

            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("SELECT * from messagereceiver");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Long id_mesaj = resultSet.getLong("id_mesaj");
                Long id_user = resultSet.getLong("id_user");
                if(id_mesaj == id) {
                    Utilizator u = repository.findOne(id_user);
                    to.add(u);
                }
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


    /**
     *
     * @return toate mesajele din tabela message
     * De asemenea, vor fi setati si utilizatorii carora le sunt adresate mesajele
     */
    @Override
    public Iterable<Message> findAll() {
        Set<Message> messages = new HashSet<>();

        try {

            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("SELECT * from message");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long id_u = resultSet.getLong("from_u");
                String mesaj= resultSet.getString("mesaj");
                LocalDateTime date=resultSet.getObject(4,LocalDateTime.class);
                Long reply_ornot=resultSet.getLong("reply_to");


                Utilizator u = repository.findOne(id_u);
                List<Utilizator> to = MessageReceiver(id);
                Message m =new Message(u,to,mesaj);
                m.setDate(date);
                m.setId(id);
                m.setReply(reply_ornot);


                messages.add(m);

            }

            return messages;
        }
        catch(SQLException e){
            e.printStackTrace();}
        catch (ClassNotFoundException e){
            e.printStackTrace();
        }

        return messages;

    }

    @Override
    public Iterable<Message> findFromOffset(Long idUtilizatorCurent,Long offset, Long limit) {
        return null;
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Va adauga in tabela messagereceiver un nou id_mesaj si id_user
     * @param id_m ->un id de mesaj
     * @param id_u -> un id de user
     */
    public void addReceiver(long id_m, long id_u){
        String SQL = "INSERT INTO messagereceiver(id_mesaj,id_user) VALUES(?,?)";

        long id =0;
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL,
                     Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, Math.toIntExact(id_m));
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


    /**
     * Salveaza un nou mesaj
     * @param entity ->mesajul de salvat
     *         entity must be not null
     * @return mesajul salvat
     */
    @Override
    public Message save(Message entity) {

        String SQL = "INSERT INTO message(id,from_u,mesaj,datac,reply_to) VALUES(?,?,?,?,?)";

        long id = 0;


        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL,
                     Statement.RETURN_GENERATED_KEYS)) {


            pstmt.setInt(1, Math.toIntExact(entity.getId()));
            pstmt.setInt(2, Math.toIntExact(entity.getFrom().getId()));
            pstmt.setString(3, entity.getMessage());
            pstmt.setObject(4, entity.getDate());
            pstmt.setInt(5, Math.toIntExact(entity.getReply()));


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
                entity.getTo().forEach(x->addReceiver(entity.getId(),x.getId()));

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
        Set<Message> messages = new HashSet<>();
        try {

            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, username, password);
            //PreparedStatement statement = connection.prepareStatement("SELECT * from message WHERE (from_u="+idUtilizatorCurent+" AND id = (SELECT id_mesaj from messagereceiver WHERE id_user = "+idPrieten+" AND id_mesaj=id)) OR (from_u="+idPrieten+" AND id = (SELECT id_mesaj from messagereceiver WHERE id_user = "+idUtilizatorCurent+" AND id_mesaj=id)) ORDER BY id LIMIT "+limit+" OFFSET "+offset+";");
            PreparedStatement statement = connection.prepareStatement("SELECT M.* " +
                    " FROM message M where M.from_u = "+idUtilizatorCurent+" and  " +
                    " exists (select * from messagereceiver MR  where MR.id_user= "+idPrieten+" and MR.id_mesaj = M. id  ) or " +
                    " M.from_u= "+idPrieten+" and exists (select * from messagereceiver where id_user= "+idUtilizatorCurent+" and id_mesaj=M.id) order by datac limit "+limit+" offset "+offset+" ;");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long id_u = resultSet.getLong("from_u");
                String mesaj= resultSet.getString("mesaj");
                LocalDateTime date=resultSet.getObject(4,LocalDateTime.class);
                Long reply_ornot=resultSet.getLong("reply_to");


                Utilizator u = repository.findOne(id_u);
                List<Utilizator> to = MessageReceiver(id);
                Message m =new Message(u,to,mesaj);
                m.setDate(date);
                m.setId(id);
                m.setReply(reply_ornot);


                messages.add(m);

            }

            return messages;
        }
        catch(SQLException e){
            e.printStackTrace();}
        catch (ClassNotFoundException e){
            e.printStackTrace();
        }

        return messages;

    }

    @Override
    public int nrElemOf2Users(long id1, long id2) {
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM message WHERE (from_u="+id1+" AND id = (SELECT id_mesaj from messagereceiver WHERE id_user = "+id2+" AND id_mesaj=id)) OR (from_u="+id2+" AND id = (SELECT id_mesaj from messagereceiver WHERE id_user = "+id1+" AND id_mesaj=id));");
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
    public int nrElemOfAnUser(long id) {
        return 0;
    }

    @Override
    public Eveniment deleteUserOfAnEvent(Long iduser, Long ideveniment) {
        return null;
    }


    /**
     * Sterge atat din messagereceiver, cat si din message
     * @param aLong ->se sterge mesajul cu acest id
     * @return null
     */
    @Override
    public Message delete(Long aLong) {
        String SQL2 = "DELETE FROM messagereceiver WHERE id_user = ?";

        int affectedrows2 = 0;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL2)) {

            pstmt.setInt(1, Math.toIntExact(aLong));

            affectedrows2 = pstmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        String SQL = "DELETE FROM message WHERE id = ?";
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
    public Message update(Message entity) {

        return entity;
    }

    /**
     *
     * @return numarul total de mesaje
     */
    @Override
    public int nrElem() {
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM message ;");
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
