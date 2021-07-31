package socialnetwork.domain;

import java.time.LocalDateTime;
import java.util.List;

public class Eveniment extends Entity<Long>{
    private String nume;
    private String descriere;
    private LocalDateTime data;
    private Utilizator utilizatorCreator;
    private List<Utilizator> utilizatoriInscrisi;

    public Eveniment(String nume, String descriere, Utilizator utilizatorCreator, List<Utilizator> utilizatoriInscrisi,LocalDateTime data) {
        this.nume = nume;
        this.descriere=descriere;
        this.utilizatorCreator = utilizatorCreator;
        this.utilizatoriInscrisi = utilizatoriInscrisi;
        this.data=data;
    }

    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public String getDescriere() {
        return descriere;
    }

    public void setDescriere(String descriere) {
        this.descriere = descriere;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public Utilizator getUtilizatorCreator() {
        return utilizatorCreator;
    }

    public void setUtilizatorCreator(Utilizator utilizatorCreator) {
        this.utilizatorCreator = utilizatorCreator;
    }

    public List<Utilizator> getUtilizatoriInscrisi() {
        return utilizatoriInscrisi;
    }

    public void setUtilizatoriInscrisi(List<Utilizator> utilizatoriInscrisi) {
        this.utilizatoriInscrisi = utilizatoriInscrisi;
    }

    @Override
    public String toString() {
        String toAsString="";
        for(Utilizator u:utilizatoriInscrisi){

            toAsString+=u.toString()+" ";
        }

        return "Eveniment{" +
                " id = "+ this.getId()+
                " nume = "+ this.nume+
                " descriere = "+ this.descriere+
                "  utilizator creator='" + utilizatorCreator + '\'' +
                ", utilizatori inscrisi=" + toAsString +
                ", date=" + data +
                '}';
    }
}
