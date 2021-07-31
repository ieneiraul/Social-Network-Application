package socialnetwork.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Utilizator extends Entity<Long>{
    private String firstName;
    private String lastName;
    private List<Utilizator> friends;

    public Utilizator(String firstName, String lastName) {
        this.friends=new ArrayList<>();
        this.firstName = firstName;
        this.lastName = lastName;

    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<Utilizator> getFriends() {
        return friends;
    }

    /**
     *
     * @param u1 - un alt utilizator
     *          adauga un prieten nou
     */
    public void setFriends(Utilizator u1){
        friends.add(u1);

    }

    /**
     * lista de prieteni devine vida
     */
    public void setList0(){
        friends.clear();
    }

    //
    /**returneaza numarul de prieteni
    */
    public int nrFriends(){
        return friends.size();
    }

    @Override
    public String toString() {
        String friendsAsString="";
        for (Utilizator u:friends) {
            friendsAsString+=u.getLastName() +" "+u.getFirstName()+ " ;";

        }


        return  firstName + ' '  + lastName ;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Utilizator)) return false;
        Utilizator that = (Utilizator) o;
        return getFirstName().equals(that.getFirstName()) &&
                getLastName().equals(that.getLastName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFirstName(), getLastName());
    }
}