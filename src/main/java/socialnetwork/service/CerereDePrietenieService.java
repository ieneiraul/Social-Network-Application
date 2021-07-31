package socialnetwork.service;

import socialnetwork.domain.CerereDePrietenie;
import socialnetwork.domain.Prietenie;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.Utilizator;
import socialnetwork.domain.validators.ServiceException;
import socialnetwork.domain.validators.RepoException;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.repository.Repository;
import utils.events.CerereDePrietenieChangeEvent;
import utils.events.ChangeEventType;
import utils.events.UtilizatorChangeEvent;
import utils.observers.Observable;
import utils.observers.Observer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CerereDePrietenieService implements Observable<CerereDePrietenieChangeEvent> {
    private Repository<Tuple<Long, Long>, Prietenie> prietenieRepository;
    private Repository<Long, Utilizator> repo;
    private Repository<Long, CerereDePrietenie> cerereDePrietenieRepository;


    /**
     * Constructor CerereDePrietenieService
     * @param repo ->repo de utilizatori
     * @param pRepo ->repo de prietenie
     * @param cerereDePrietenieRepository ->repo de cerere de prietenie
     */
    public CerereDePrietenieService(Repository<Long, Utilizator> repo, Repository<Tuple<Long, Long>, Prietenie> pRepo,
                           Repository<Long, CerereDePrietenie> cerereDePrietenieRepository) {
        prietenieRepository = pRepo;
        this.repo = repo;
        this.cerereDePrietenieRepository = cerereDePrietenieRepository;


    }

    /**
     *
     * @return toate cererile de prietenie
     */
    public Iterable<CerereDePrietenie> getCereri() {
        return cerereDePrietenieRepository.findAll();
    }


    /**
     * Se dau 2 id uri de utilizator
     * id1 trimite o cerere de prietenie catre id2
     * Statusul cererii e, by default, 'pending'
     * @param id1 ->id-ul utilizatorului ce trimite cererea
     * @param id2 ->id-ul utilizatorului catre care e trimisa cererea
     * @throws RepoException daca nu exista un utilizator
     * @throws ServiceException daca deja sunt prieteni
     * @throws ValidationException daca cererea s-a trimis deja
     */
    public void addCerere(long id1, long id2) {
        if(id1==id2) throw new ValidationException("Nu iti poti trimite o cerere de prietenie tie.");
        Utilizator u1 = repo.findOne(id1);
        Utilizator u2 = repo.findOne(id2);
        if (u1 == null || u2 == null)
            throw new ServiceException("Un utilizator e null!");

        for(CerereDePrietenie prietenie: cerereDePrietenieRepository.findAll()){
            if(prietenie.getTrimite().getId() == id1 && prietenie.getPrimeste().getId() ==id2)
                throw new ValidationException("S-a trimis cererea!Asteptati.");
            if(prietenie.getPrimeste().getId()==id1 && prietenie.getTrimite().getId()==id2)
                throw new ValidationException("Ati primit deja o cerere de prietenie. Acceptati-o.");
        }

        if(prietenieRepository.findOne(new Tuple<>(id1,id2))!=null ||
           prietenieRepository.findOne(new Tuple<>(id2,id1))!=null)
            throw new ServiceException("Exista deja prietenia. Nu trebuie sa trimiti cerere de prietenie.");
        CerereDePrietenie cerereDePrietenie = new CerereDePrietenie(u1,u2);
        long nr=(long)cerereDePrietenieRepository.nrElem()+1;
        while(cerereDePrietenieRepository.findOne(nr)!=null){
            nr++;
        }
        cerereDePrietenie.setId(nr);
        cerereDePrietenie.setStatus("pending");
        cerereDePrietenie.setData(LocalDateTime.now());
        cerereDePrietenieRepository.save(cerereDePrietenie);
        if(cerereDePrietenie!=null) {
            notifyObservers(new CerereDePrietenieChangeEvent(ChangeEventType.ADD, cerereDePrietenie));
        }
    }


    /**
     * Se va modifica statusul unei cereri de prietenie existente
     * Statusurile disponibile sunt approved/rejected
     * Daca statusul e approved, se va adauga o noua prietenie
     * @param id ->id-ul unei prietenii existente
     * @param status ->noul status
     */
    public void modificaCerere(long id, String status) {

      CerereDePrietenie c = cerereDePrietenieRepository.findOne(id);
      if(c== null) throw new ValidationException("Nu exista aceasta cerere");
        if (status.equals("approved")) {
            Prietenie pr = new Prietenie();
            pr.setDate(LocalDateTime.now());
            pr.setId(new Tuple<>(c.getTrimite().getId(), c.getPrimeste().getId()));
            prietenieRepository.save(pr);

            c.setStatus("approved");

            cerereDePrietenieRepository.update(c);
        }
        if (status.equals("rejected")) {
            c.setStatus("rejected");

            cerereDePrietenieRepository.update(c);
        }


    }

    private List<Observer<CerereDePrietenieChangeEvent>> observers=new ArrayList<>();


    public  void stergeCerere(long id){
        CerereDePrietenie cerereDePrietenie = cerereDePrietenieRepository.findOne(id);
        if(cerereDePrietenie == null){
            throw  new  ValidationException("nu exista cererea");
        }
        cerereDePrietenieRepository.delete(id);
        if(cerereDePrietenie!=null) {
            notifyObservers(new CerereDePrietenieChangeEvent(ChangeEventType.DELETE, cerereDePrietenie));
        }
    }

    public Iterable<CerereDePrietenie> getRequestsFromOffset(Long idUtilizatorCurent, Long offset, Long limit) {
        return cerereDePrietenieRepository.findFromOffset(idUtilizatorCurent, offset, limit);
    }

    public int nrE() {
        return cerereDePrietenieRepository.nrElem();
    }

    public int nrEOfUser(Long id) {
        return cerereDePrietenieRepository.nrElemOfAnUser(id);
    }

    @Override
    public void addObserver(Observer<CerereDePrietenieChangeEvent> e) {
        observers.add(e);
    }

    @Override
    public void removeObserver(Observer<CerereDePrietenieChangeEvent> e) {
    }

    @Override
    public void notifyObservers(CerereDePrietenieChangeEvent t) {
        observers.stream().forEach(x->x.update(t));
    }
}