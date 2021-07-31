package socialnetwork.service;

import jdk.vm.ci.meta.Local;
import socialnetwork.domain.Message;
import socialnetwork.domain.ReplyMessage;
import socialnetwork.domain.Utilizator;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.repository.Repository;
import utils.events.CerereDePrietenieChangeEvent;
import utils.events.ChangeEventType;
import utils.events.MesajChangeEvent;
import utils.observers.Observable;
import utils.observers.Observer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MessageService implements Observable<MesajChangeEvent> {
    private Repository<Long, Message> messageRepository ;
    private  Repository<Long, Utilizator> utilizatorRepository;
    private List<Observer<MesajChangeEvent>> observers=new ArrayList<>();

    /**
     * Constructor MessageService
     * @param messageRepository ->repo de mesaje
     * @param utilizatorRepository ->repo de utilizatori
     */
    public MessageService(Repository<Long, Message> messageRepository, Repository<Long, Utilizator> utilizatorRepository) {
        this.messageRepository = messageRepository;
        this.utilizatorRepository = utilizatorRepository;
    }

    /**
     *
     * @return toate mesajele
     */
    public Iterable<Message> getMessages(){ return  messageRepository.findAll();}


    /**
     * Adauga un nou mesaj in tabela messages
     * @param id_u ->id-ul utilizatorului ce trimite mesajul
     * @param to ->lista ce contine id-urile utilizatorilor catre care se trimite mesajul
     * @param msg->mesajul de trimis
     * @return mesajul adaugat
     */
    public Message addMessage(long id_u, List<Long> to, String msg){
        long nr= messageRepository.nrElem()+1;
        while(messageRepository.findOne(nr)!=null) nr++;
        LocalDateTime date= LocalDateTime.now();
        Utilizator u =utilizatorRepository.findOne(id_u);
        List<Utilizator> lista =new ArrayList<>();
        for(Long el : to){
            lista.add(utilizatorRepository.findOne(el));
        }
        Message m=new Message(u,lista,msg);
        m.setId(nr);
        m.setDate(date);
        messageRepository.save(m);
        notifyObservers(new MesajChangeEvent(ChangeEventType.ADD,m));
        return m;
    }


    /**
     * Reprezinta un raspuns la un mesaj primit
     * Se verifica sa fi primit un mesaj inainte de a raspunde
     * Raspunsul va fi salvat tot ca un mesaj in tabela messages
     * In coloana reply_to din tabela messages se va pune id-ul mesajului primit,
     * acela la care se ofera raspunsul
     * @param id ->id-ul mesajului la care se raspunde
     * @param id_u ->id user ce raspunde la mesaj
     * @param msg ->mesajul de raspuns
     * @return mesajul nou de reply
     */
    public Message replyto(long id, long id_u, String msg){
        long i= messageRepository.nrElem()+1;
        while(messageRepository.findOne(i)!=null) i++; //id mesaj nou de raspuns
        LocalDateTime date= LocalDateTime.now();
        Message m= messageRepository.findOne(id);
        if(m==null) throw new ValidationException("Nu exista acest mesaj.Incercati sa il trimiteti intai.");
        int gasit=0;
        for(Utilizator to:m.getTo()){
            if(to.getId()==id_u){
                gasit=1; break;
            }
        }
        if(gasit==0) throw new ValidationException("Nu puteti da reply daca nu ati primit un mesaj.");
        List<Utilizator> to =new ArrayList<Utilizator>();
        to.add(m.getFrom());
        Utilizator u= utilizatorRepository.findOne(id_u);
       // Message reply=new Message(id_u,to,msg);
        Message newM= new Message(u,to,msg);
        newM.setId(i);
        newM.setDate(date);
        newM.setReply(m.getId());
        messageRepository.save(newM);
        return newM;
    }


    /**
     * Se vor afisa conversatiile dintre cei doi utilizatori, dati prin id-urile lor
     * Conversatiile se afiseaza in ordine cronologica
     * @param id1 ->primul utilizator
     * @param id2 ->al doilea utilizator
     * @return lista cu mesajele dintre ei
     */
    public List<Message> AfisareConversatii(long id1, long id2){
        List<Message> lis=new ArrayList<>();
        for(Message m: messageRepository.findAll()){
            if(m.getFrom().getId()==id1){
                int bun=0;
                for(Utilizator to: m.getTo()){
                    if(to.getId() == id2)
                    { bun=1; break;}
                }
                if(bun==1) lis.add(m);
            }
            else{
                if(m.getFrom().getId()==id2){
                    int bun=0;
                    for(Utilizator to: m.getTo()){
                        if(to.getId() == id1)
                        { bun=1; break;}
                    }
                    if(bun==1) lis.add(m);

                }
            }

        }
//        lis.sort(new Comparator<Message>() {
//            @Override
//            public int compare(Message o1, Message o2) {
//               if(o1.getDate().isBefore(o2.getDate()))
//                   return 0;
//               return 1;
//            }
//        });
        Comparator<Message> ComparatorM
           = Comparator.comparing(
                Message::getDate, (s1, s2) -> {
                    return s1.compareTo(s2);
                });

        lis.sort(ComparatorM);
        return lis;
    }

    public int nrE() {
        return messageRepository.nrElem();
    }
    public int nrEOf2Users(long id1, long id2) {
        return messageRepository.nrElemOf2Users(id1,id2);
    }

    public Iterable<Message> getMessagesByOffset(long idUtilizator, long idPrieten, long offset, long limit) {
        return messageRepository.find2UsersMessagesFromOffset(idUtilizator, idPrieten,offset, limit);
    }


    @Override
    public void addObserver(Observer<MesajChangeEvent> e) {
        observers.add(e);
    }

    @Override
    public void removeObserver(Observer<MesajChangeEvent> e) {
    }

    @Override
    public void notifyObservers(MesajChangeEvent t) {
        observers.stream().forEach(x->x.update(t));
    }
}
