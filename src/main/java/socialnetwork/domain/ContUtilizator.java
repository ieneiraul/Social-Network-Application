package socialnetwork.domain;

public class  ContUtilizator extends Entity<Long>{
    private Long idUtilizator;
    private String username;
    private String password;

    public ContUtilizator(Long idUtilizator, String username, String password) {
        this.idUtilizator = idUtilizator;
        this.username = username;
        this.password = password;
    }

    public Long getIdUtilizator() {
        return idUtilizator;
    }

    public void setIdUtilizator(Long idUtilizator) {
        this.idUtilizator = idUtilizator;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
