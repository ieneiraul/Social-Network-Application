package utils.events;

import socialnetwork.domain.Eveniment;
import socialnetwork.domain.Message;

public class EventChangeEvent implements Event {
    private ChangeEventType type;
    private Eveniment data, oldData;

    public EventChangeEvent(ChangeEventType type, Eveniment data) {
        this.type = type;
        this.data = data;
    }

    public ChangeEventType getType() {
        return type;
    }

    public Eveniment getData() {
        return data;
    }

    public Eveniment getOldData() {
        return oldData;
    }
}
