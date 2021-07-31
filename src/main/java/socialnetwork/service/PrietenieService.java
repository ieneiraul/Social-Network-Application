package socialnetwork.service;

import socialnetwork.domain.EntityDTO;
import socialnetwork.domain.Prietenie;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.Utilizator;
import socialnetwork.domain.validators.ServiceException;
import socialnetwork.repository.Repository;
import utils.events.ChangeEventType;
import utils.events.PrietenieChangeEvent;
import utils.events.UtilizatorChangeEvent;
import utils.observers.Observable;
import utils.observers.Observer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PrietenieService implements Observable<PrietenieChangeEvent> {

    private Repository<Tuple<Long, Long>, Prietenie> prietenieRepository;
    private Repository<Long, Utilizator> repo;


    /**
     * Constructor PrietenieService
     *
     * @param repo  ->repo de utilizatori
     * @param pRepo ->repo de prietenie
     */
    public PrietenieService(Repository<Long, Utilizator> repo, Repository<Tuple<Long, Long>, Prietenie> pRepo) {
        prietenieRepository = pRepo;
        this.repo = repo;
    }

    public Prietenie findOne(long id1, long id2){
        if(prietenieRepository.findOne(new Tuple<>(id1, id2))==null &&
           prietenieRepository.findOne(new Tuple<>(id2,id1))!=null)
            return prietenieRepository.findOne(new Tuple<>(id2,id1));
        if(prietenieRepository.findOne(new Tuple<>(id2, id1))==null &&
                prietenieRepository.findOne(new Tuple<>(id1,id2))!=null)
            return prietenieRepository.findOne(new Tuple<>(id1,id2));
        return  null;

    }

    /**
     * @return toti prietenii
     */
    public Iterable<Prietenie> getPrieteni() {
        return prietenieRepository.findAll();
    }


    /**
     * Adauga o noua prietenie, fiind date 2 id-uri a 2 utilizatori
     *
     * @param id1 - un utilizator
     * @param id2 - un utilizator
     */
    public void addPrietenie(long id1, long id2) {
        Utilizator u1 = repo.findOne(id1);
        Utilizator u2 = repo.findOne(id2);

        if (u1 == null || u2 == null) {
            throw new ServiceException("Un utilizator nu exista!!");
        }

        if (prietenieRepository.findOne(new Tuple<>(id1, id2)) != null ||
                prietenieRepository.findOne(new Tuple<>(id2, id1)) != null) {
            throw new ServiceException("exista aceasta prietenie deja!!");
        }
        u1.setFriends(u2);
        u2.setFriends(u1);
        Prietenie pr = new Prietenie();

        pr.setId(new Tuple<>(u1.getId(), u2.getId()));
        pr.setDate(LocalDateTime.now());
        prietenieRepository.save(pr);
        if(pr == null) {
            notifyObservers(new PrietenieChangeEvent(ChangeEventType.ADD,pr));
        }


    }


    /**
     * Sterge o prietenie, date fiind id-urile utilizatorilor
     *
     * @param id1 - un utilizator
     * @param id2 - un utilizator
     */
    public void stergePrietenie(long id1, long id2) {
        Utilizator u1 = repo.findOne(id1);
        Utilizator u2 = repo.findOne(id2);
        if (u1 == null || u2 == null) {
            throw new ServiceException("Un utilizator nu exista!!");
        }
        Prietenie p;
        if (prietenieRepository.findOne(new Tuple<>(id1, id2)) == null &&
                prietenieRepository.findOne(new Tuple<>(id2, id1)) != null)
        {
            p =prietenieRepository.findOne(new Tuple<>(id2,id1));
            prietenieRepository.delete(new Tuple<>(id2, id1));
           }

        else {
            p =prietenieRepository.findOne(new Tuple<>(id1,id2));
            prietenieRepository.delete(new Tuple<>(id1, id2));

        }
        if(p!=null) {
            notifyObservers(new PrietenieChangeEvent(ChangeEventType.DELETE, p));
        }


    }


    /**
     * Construieste o lista ce contine toti prietenii utilizatorului dat
     * Format de afisare: Nume Prieten|Prenume Prieten |Data de la care sunt prieteni
     *
     * @return lista de prieteni a utilizatorului dat
     */
    public List<EntityDTO> AfisarePrieteniUser( Predicate<Prietenie> st, Predicate<Prietenie> dr) {

        List<Prietenie> list = new ArrayList<>();
        List<EntityDTO> result1;
        List<EntityDTO> result2;

        getPrieteni().forEach(list::add);

        result1 = list.stream()
                .filter(st)
                .map(x -> {
                    Utilizator u = repo.findOne(x.getId().getRight());
                    return new EntityDTO(u.getFirstName(),u.getLastName(),x.getDate());
                })
                .collect(Collectors.toList());

        result2 = list.stream()
                .filter(dr)
                .map(x -> {
                    Utilizator u = repo.findOne(x.getId().getLeft());
                    return new EntityDTO(u.getFirstName(),u.getLastName(),x.getDate());

                })
                .collect(Collectors.toList());

        List<EntityDTO> result = new ArrayList<>();
        result.addAll(result1);
        result.addAll(result2);
        return result;
    }

    private List<Observer<PrietenieChangeEvent>> observers=new ArrayList<>();

    public int nrE() {
        return prietenieRepository.nrElem();
    }


    @Override
    public void addObserver(Observer<PrietenieChangeEvent> e) {
        observers.add(e);
    }

    @Override
    public void removeObserver(Observer<PrietenieChangeEvent> e) {

    }

    @Override
    public void notifyObservers(PrietenieChangeEvent t) {
        observers.stream().forEach(x->x.update(t));

    }
}