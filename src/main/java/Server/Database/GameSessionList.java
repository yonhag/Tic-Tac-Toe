package Server.Database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class GameSessionList extends ArrayList<GameSession> {

    public GameSessionList() {
        super();
    }

    public GameSessionList(Collection<? extends BaseEntity> lst) {
        super(lst.stream().map(item -> (GameSession) item).collect(Collectors.toList()));
    }

}
