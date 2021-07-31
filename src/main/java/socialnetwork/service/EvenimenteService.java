package socialnetwork.service;

import socialnetwork.domain.Eveniment;
import socialnetwork.domain.Message;
import socialnetwork.domain.Utilizator;
import socialnetwork.repository.Repository;
import utils.events.ChangeEventType;
import utils.events.EventChangeEvent;
import utils.events.MesajChangeEvent;
import utils.observers.Observable;
import utils.observers.Observer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class EvenimenteService implements Observable<EventChangeEvent> {
    private Repository<Long, Eveniment> evenimentRepository;
    private Repository<Long, Utilizator> utilizatorRepository;
    private List<Observer<EventChangeEvent>> observers=new ArrayList<>();

    public EvenimenteService(Repository<Long, Eveniment> evenimentRepository, Repository<Long, Utilizator> utilizatorRepository) {
        this.evenimentRepository = evenimentRepository;
        this.utilizatorRepository = utilizatorRepository;
        stergeEvenimenteVechi();
    }

    public Iterable<Eveniment> getEvents() { return evenimentRepository.findAll();}

    public Iterable<Eveniment> getEventsOfAnUser(long id) {
        Iterable<Eveniment> events=evenimentRepository.findAll();
        List<Eveniment> list  = StreamSupport.stream(events.spliterator(), false)
                .filter(x->x.getUtilizatorCreator().getId()==id || x.getUtilizatoriInscrisi().contains(utilizatorRepository.findOne(id)))
                .collect(Collectors.toList());
        return list;
    }

    public Eveniment addEvent(long id_u, List<Long> to, String nume, String descriere, LocalDateTime data){
        long nr= evenimentRepository.nrElem()+1;
        while(evenimentRepository.findOne(nr)!=null) nr++;
        Utilizator u =utilizatorRepository.findOne(id_u);
        List<Utilizator> lista =new ArrayList<>();
        for(Long el : to){
            lista.add(utilizatorRepository.findOne(el));
        }
        Eveniment e= new Eveniment(nume,descriere,u,lista,data);
        e.setId(nr);
        evenimentRepository.save(e);
        notifyObservers(new EventChangeEvent(ChangeEventType.ADD,e));
        return e;
    }

    public void stergeEvenimenteVechi() {
        Iterable<Eveniment> events=evenimentRepository.findAll();
        for (Eveniment e:events) {
            if(e.getData().isBefore(LocalDateTime.now())) evenimentRepository.delete(e.getId());
        }
    }

    public Iterable<Eveniment> getEventsByOffset(long id, long offset, long limit) {
        return evenimentRepository.findFromOffset(id,offset,limit);
    }

    public Iterable<Eveniment> getUsersEventsByOffset(long id, long offset, long limit) {
        return evenimentRepository.findUsersEventsFromOffset(id,offset,limit);
    }

    public void joinUserAtEvent(long id, Eveniment e) {
        evenimentRepository.addUtilizatorInscris(e.getId(),id);
    }


    public int nrE() {
        return evenimentRepository.nrElem();
    }

    public int nrEOfUser(long id) { return evenimentRepository.nrElemOfAnUser(id);}

    public void deleteUserFromEvent(long id, long idEveniment) { evenimentRepository.deleteUserOfAnEvent(id,idEveniment);}

    @Override
    public void addObserver(Observer<EventChangeEvent> e) {
        observers.add(e);
    }

    @Override
    public void removeObserver(Observer<EventChangeEvent> e) { }

    @Override
    public void notifyObservers(EventChangeEvent t) {
        observers.stream().forEach(x->x.update(t));
    }
}
