package socialnetwork.domain;

import socialnetwork.service.MessageService;

import java.time.LocalDateTime;
import java.util.List;

public class ReplyMessage extends Message {

    Message reply;

    public ReplyMessage(Utilizator from, List<Utilizator> to, String message) {
        super(from, to, message);
    }
}
