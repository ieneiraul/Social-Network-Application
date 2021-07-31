package socialnetwork.service;

import controller.UtilizatorController;
import socialnetwork.domain.*;
import socialnetwork.domain.validators.RepoException;
import socialnetwork.domain.validators.ServiceException;
import socialnetwork.repository.Repository;
import socialnetwork.repository.database.ContUtilizatorDB;
import socialnetwork.repository.database.StatusNotificariDB;
import utils.events.ChangeEventType;
import utils.events.PrietenieChangeEvent;
import utils.events.UtilizatorChangeEvent;
import utils.observers.Observable;
import utils.observers.Observer;
//import sun.nio.ch.Util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.*;

public class UtilizatorService implements Observable<UtilizatorChangeEvent> {
    private Repository<Long, Utilizator> repo;
    private Repository<Tuple<Long,Long>,Prietenie> prietenieRepository;
    private Repository<Long,CerereDePrietenie> cerereDePrietenieRepository;
    private Repository<Long, Message> messageRepository;
    private ContUtilizatorDB contRepository;
    private StatusNotificariDB statusRepository;


    /**
     * Constructor Utilizator Service
     * @param repo ->repo de utilizatori
     * @param prietenieRepository ->repo de prietenie
     * @param cerereDePrietenieRepository->repo de cereri de prietenie
     * @param messageRepository ->repo de mesaje
     */
    public UtilizatorService(Repository<Long, Utilizator> repo,
                             Repository<Tuple<Long,Long>,Prietenie> prietenieRepository,
                             Repository<Long,CerereDePrietenie> cerereDePrietenieRepository,
                             Repository<Long,Message> messageRepository, ContUtilizatorDB contRepository, StatusNotificariDB statusRepository) {
        this.repo = repo;
        this.prietenieRepository=prietenieRepository;
        this.cerereDePrietenieRepository = cerereDePrietenieRepository;
        this.messageRepository=messageRepository;
        this.contRepository= contRepository;
        this.statusRepository=statusRepository;
    }

    /**
     * Sterge manual toate mesajele in care apare un id dat
     * Se sterg si mesajele trimise de el si mesajele pentru el
     * Se ia in vedere si cazul in care utilizatorul isi trimite mesaj lui
     * @param id ->un id de utilizator
     */
    public void stergeMesajeFrom(long id) {
        int nu=0;
        List<Long> list = new ArrayList<>();
        for (Message pr : messageRepository.findAll()) {
           if(pr.getFrom().getId()==id) list.add(pr.getId());
           for(Utilizator el : pr.getTo()){
               nu=0;
               for(Long r: list){
                   if(r == el.getId())
                       nu=1;
               }
               if(el.getId()==id && nu==0) list.add(pr.getId());

           }
        }
        list.forEach(x -> messageRepository.delete(x));
    }

        /**
        sterge un prieten din lista prietenilor
         @param id  - un utilizator(ce se va sterge din lista de prieteni)
         */
    public void stergePrieten(long id){
        List<Prietenie> list =new ArrayList<>();
        for(Prietenie pr: prietenieRepository.findAll()){
           if(pr.getId().getRight() == id || pr.getId().getLeft() ==id)
               list.add(pr);
       }
        list.forEach(x-> prietenieRepository.delete(new Tuple<>(x.getId().getLeft(),x.getId().getRight())));

    }

    /**
     * Sterge manual cererile de prietenie trimise de id si catre id
     * @param id ->un id de utilizator
     */
    public void stergeCereri(long id) {
        List<CerereDePrietenie> list = new ArrayList<>();
        for (CerereDePrietenie pr : cerereDePrietenieRepository.findAll()){
           if(pr.getPrimeste().getId()== id || pr.getTrimite().getId() == id)
                list.add(pr);
        }
        list.forEach(x -> cerereDePrietenieRepository.delete(x.getId()));
    }


        /**
         Adauga un nou utilizator
         @param messageTask  - un utilizator
         */
    public Utilizator addUtilizator(Utilizator messageTask) {
        long nr=(long) nrE()+1;
        while(repo.findOne(nr)!=null){
            nr++;
        }
        messageTask.setId(nr);
        Utilizator task = repo.save(messageTask);
        if(task == null) {
            notifyObservers(new UtilizatorChangeEvent(ChangeEventType.ADD,task));
        }

        return task;
    }

    /**
    Returneaza numarul de elemente din map
     */
    public int nrE(){
        return repo.nrElem();
    }

    /**
    Actualizeaza datele unui utlizator, dat fiind id-ul sau
     @param id  - id-ul existent al utilizatorului
     @param messageTask  -datele cu care se inlocuieste
     */
    public Utilizator updateUtilizator(long id, Utilizator messageTask) {
        messageTask.setId(id);
        Utilizator task = repo.update(messageTask);
        setFriends();
        if(task!=null) {
            notifyObservers(new UtilizatorChangeEvent(ChangeEventType.UPDATE, task));
        }
        return task;

    }

    /**
     *
     * @param id ->un id de utilizator
     * @return un utilizator care are id-ul dat
     */
    public Utilizator findOne(long id){
        return repo.findOne(id);
    }

    /**
    Sterg un utilizator
     @param id  - id-ul utilizatorului de sters
     */
    public Utilizator removeUtilizator(long id){
       /* for(Prietenie p: prietenieRepository.findAll()){
            if(p.getId().getRight() == id || p.getId().getLeft() ==id)
                stergePrieten(id);
        }
        setFriends();

        */
        Utilizator u = repo.findOne(id);
        if(repo.findOne(id) == null) throw  new ServiceException("Nu exista acest utilizator.");
        stergePrieten(id);
        stergeCereri(id);
        stergeMesajeFrom(id);
        setFriends();

        repo.delete(id);
        if(u!=null) {
            notifyObservers(new UtilizatorChangeEvent(ChangeEventType.DELETE, u));
        }
        return  u;

    }

    /**
    Returnez toti utilizatorii
     */
    public Iterable<Utilizator> getAll(){
        //setFriends();
        return repo.findAll();
    }

    public Iterable<Utilizator> getFromOffset(Long idUtilizatorCurent,Long offset, Long limit){
        return repo.findFromOffset(idUtilizatorCurent,offset, limit);
    }

    /**
    Functia ce afiseaza la utilizatori prietenii sai
     */
    public void setFriends(){

        for(Utilizator u: getAll()){
            u.setList0();
        }
        for(Prietenie pr : prietenieRepository.findAll()){
            Utilizator u1=repo.findOne(pr.getId().getRight());
            Utilizator u2=repo.findOne(pr.getId().getLeft());
            u1.setFriends(u2);
            u2.setFriends(u1);
        }


    }

    /**
    Afiseaza numarul de comunitati
     */
    public  int NrComunitati(){

        int nr= 0;
        for(Utilizator u: getAll()){
            nr++;
        }
        int noduri = nr;
        int muchii=prietenieRepository.nrElem();
        ComponenteConexe ccc= new ComponenteConexe(noduri);
        for(Prietenie  p: prietenieRepository.findAll()){
            ccc.adaugaMuchie(p.getId().getLeft().intValue(), p.getId().getRight().intValue());
        }
        int rez=ComponenteConexe.NrComponente(noduri,muchii,ccc );
        return rez;
    }

    /**
     * Neapelata, incompleta
     * Functie ce incearca sa afiseze comunitatea cea mai sociabila
     * @return lista cu id-uri de utlizatori
     */
    public  List<Long> ComunitateSociabila (){
        int nr = 0,aux,ma=0;
        List<Long> lista =new ArrayList<Long>();
        Utilizator remarcabil=new Utilizator(" "," ");
        for(Utilizator u:getAll()){
            aux=u.nrFriends();
            if(aux > ma){
                ma=aux;
                remarcabil = u;
            }

        }

        lista.add(remarcabil.getId());
        for(int  i= 0;i<remarcabil.nrFriends();i++){
          for(Utilizator u: remarcabil.getFriends()){

              if(!lista.contains(u.getId()))

                  lista.add(u.getId());
              for(Utilizator u2: u.getFriends()){
                  if(!lista.contains(u2.getId()))

                      lista.add(u2.getId());
              }

          }
           /* String numele=remarcabil.getFriends().get(i).getFirstName()+ " "+
                    remarcabil.getFriends().get(i).getLastName();
            lista.add(numele);

            */
        }


        return lista;
    }


    /**
     * Afiseaza toti utilizatorii si prietenii acestora
     */
    public String[] afisarePrieteni(){

        int nr = 0,i=0;
        for(Utilizator u: repo.findAll()) nr++;
        String[] pri = new String[nr+1];

        for(i =0; i< pri.length;i++) pri[i] = " ";


        for(Prietenie pr : prietenieRepository.findAll()){
            Utilizator u =repo.findOne(pr.getId().getLeft());
            Utilizator u1= repo.findOne(pr.getId().getRight());
            String nume= " |"+u1.getFirstName()+" "+u1.getLastName();
            String util1 = u.getFirstName()+" "+u.getLastName();
            String util2 = u1.getFirstName()+" "+u1.getLastName();
            pri[Math.toIntExact(u.getId())]+=nume;
            String nume1=" |"+ u.getFirstName()+" "+u.getLastName();
            pri[Math.toIntExact(u1.getId())]+=nume1;


        }
        return pri;

    }


    /**
     * Se adauga intr-o lista toti prietenii unui user dat(doar id-urile lor)
     * @param id ->id-ul unui utilizator existent
     * @return lista de prieteni a utilizatorului
     */
    public List<Long> getFriend2(long id){
        List<Long> deReturnat= new ArrayList<>();
        for(Prietenie p: prietenieRepository.findAll()){
            if(p.getId().getRight() == id)
                deReturnat.add(p.getId().getLeft());
            if(p.getId().getLeft()==id)
                deReturnat.add(p.getId().getRight());
        }
        return  deReturnat;
    }

    public List<Long> getFriend2ByOffset(long id, long offset, long limit){
        List<Long> deReturnat= new ArrayList<>();
        for(Prietenie p: prietenieRepository.findFromOffset(id,offset, limit)){
            if(p.getId().getRight() == id)
                deReturnat.add(p.getId().getLeft());
            if(p.getId().getLeft()==id)
                deReturnat.add(p.getId().getRight());
        }
        return  deReturnat;
    }

    public Iterable<Utilizator> getUsersByOffset(long id, long offset, long limit) {
        return repo.findFromOffset(id, offset, limit);
    }


    /**
     * Cauta un utilizator dupa nume si prenume
     * @param nume ->numele dat
     * @param prenume ->prenumele dat
     * @return primul utilizator ce are numele si prenumele dat
     */
    public Utilizator cautaUser(String nume, String prenume){
        for(Utilizator u : getAll()){
            if(u.getFirstName().equals(prenume) && u.getLastName().equals(nume))
                return u;
        }
        return null;
    }



    private List<Observer<UtilizatorChangeEvent>> observers=new ArrayList<>();

    /**
     * Adauga un observer
     * @param e
     */
    @Override
    public void addObserver(Observer<UtilizatorChangeEvent> e) {
        observers.add(e);

    }

    @Override
    public void removeObserver(Observer<UtilizatorChangeEvent> e) {

    }

    /**
     * Notifica observers atunci cand are loc o modificare
     * @param t
     */
    @Override
    public void notifyObservers(UtilizatorChangeEvent t) {
        observers.stream().forEach(x->x.update(t));


    }

    /**
     * Returneaza intr-o lista de stringuri niste propozitii ce contin:
     *   +cu cine s-a imprietenit userul in perioada data
     *   +ce mesaje a trimis userul in perioada data
     * @param id ->id-ul unui utilizator
     * @param inceput ->data de inceput
     * @param sfarsit ->data de sfarsit
     * Pentru raport1 -pdf
     * @return
     */
    public List<String> getMessagesAndFriendships(long id, LocalDateTime inceput, LocalDateTime sfarsit){
        List<String> rez= new ArrayList<>();
        rez.add("Prietenii noi in aceasta perioada:");
        for(Prietenie  p: prietenieRepository.findAll()){
            if(p.getDate().isBefore(sfarsit) && p.getDate().isAfter(inceput)) {
                if (p.getId().getLeft() == id) {
                    Utilizator prieten = findOne(p.getId().getRight());
                    rez.add("Prieten: " + prieten.getFirstName() + " " + prieten.getLastName() +"  in data "+
                             p.getDate().getDayOfMonth()+"-"+p.getDate().getMonth()+"-"+p.getDate().getYear()+"  "+p.getDate().getHour()+":"+p.getDate().getMinute());
                }
                else{
                    if (p.getId().getRight() == id) {
                        Utilizator prieten = findOne(p.getId().getLeft());
                        rez.add(String.format("Prieten: " + prieten.getFirstName() + " " + prieten.getLastName() + "  in data " +
                                p.getDate().getDayOfMonth() + "-" + p.getDate().getMonth() + "-" + p.getDate().getYear() + "  " + p.getDate().getHour() + ":" + p.getDate().getMinute()));
                    }
                }
            }
        }
        rez.add("Mesaje noi in aceasta perioada:");
        for(Message m: messageRepository.findAll()){

            if(m.getDate().isBefore(sfarsit) &&
                    m.getDate().isAfter(inceput)){

                    for(Utilizator persoana: m.getTo()){
                        if(persoana.getId()  == id){
                            rez.add("Mesaj: "+m.getMessage()+
                                    "; de la : "+m.getFrom().getFirstName()+" "+m.getFrom().getLastName());
                        }
                    }
                }
            }


        Utilizator deCautat=findOne(id);
        if(rez.isEmpty()) rez.add("Nicio activitate a userului: "+deCautat.getFirstName() +" "+deCautat.getLastName());
        return  rez;
    }


    /**
     * Se afiseaza toate mesajele primite de u1 de la u2
     * @param u1 ->un user
     * @param u2 ->un user
     * @param inceput ->data de inceput
     * @param sfarsit ->data de final
     * Pentru raport 2-pdf
     * @return
     */
    public List<String> getMesajeDelaPrieten(Utilizator u1, Utilizator u2,LocalDateTime inceput,LocalDateTime sfarsit){
        List<String> rez= new ArrayList<>();
        rez.add("Mesajele primite de "+u1.getFirstName()+" "+u1.getLastName()+
                " de la "+u2.getFirstName()+" "+u2.getLastName()+":");
        for(Message m: messageRepository.findAll()){

            if(m.getDate().isBefore(sfarsit) &&
                    m.getDate().isAfter(inceput)){

                if(m.getFrom().getId()==u2.getId()) {

                    for (Utilizator persoana : m.getTo()) {
                        if (persoana.getId() == u1.getId()) {
                            rez.add("Mesaj: " + m.getMessage() + " in data: " + m.getDate().getDayOfMonth()+"-"+m.getDate().getMonth()+"-"+m.getDate().getYear()+"  "+m.getDate().getHour()+":"+m.getDate().getMinute());
                        }
                    }
                }
                }
        }
        return  rez;
    }

    public Utilizator loginUtilizator(String username, String password) {
        try{
            Long id = contRepository.findAccount(username, password);
            Utilizator utilizator= repo.findOne(id);
            return utilizator;
        } catch (Exception e) {
            System.out.println("nu exista un utilizator cu aceste informatii");
        }
        return null;
    }

    public boolean existUsername(String username) {
        return contRepository.existUser(username);
    }

    public void createUserAccount(Long id, String username, String password) {
        contRepository.createAccount(id,username,password);
    }

    public void createUserFromRegister(String firstname, String lastname, String username, String password) {
        Utilizator u= new Utilizator(firstname, lastname);
        Utilizator utilizatorNou= addUtilizator(u);
        System.out.println(utilizatorNou.getId());
        createUserAccount(utilizatorNou.getId(), username, password);
        createStatusNotificari(utilizatorNou.getId());
    }
    public void createStatusNotificari(Long idUtilizator) {
        statusRepository.saveStatus(idUtilizator);
    }

    public boolean getStatusNotificari(Long idUtilizator) {
        if(statusRepository.findStatus(idUtilizator)==(long) 1) return true;
        return false;
    }
    public void setStatusNotificari(Long idUtilizator, Long status) {
        statusRepository.updateStatus(idUtilizator, status);
    }





}
