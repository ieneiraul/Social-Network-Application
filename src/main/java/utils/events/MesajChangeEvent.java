package utils.events;

import socialnetwork.domain.CerereDePrietenie;
import socialnetwork.domain.Message;

public class MesajChangeEvent implements Event {
    private ChangeEventType type;
    private Message data, oldData;

    public MesajChangeEvent(ChangeEventType type, Message data) {
        this.type = type;
        this.data = data;
    }

    public ChangeEventType getType() {
        return type;
    }

    public Message getData() {
        return data;
    }

    public Message getOldData() {
        return oldData;
    }
}
