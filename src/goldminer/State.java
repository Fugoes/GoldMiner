package goldminer;

import util.Copyable;

import java.util.Vector;

public class State implements Copyable<State> {
    private static State instance = new State();

    private State() {
    }

    Vector<Entity> entities = new Vector<>();

    @Override
    public State copy() {
        State result = new State();
        for (Entity entity : this.entities) {
            result.entities.add(entity.copy());
        }
    }

    public static State getSnapshot() {
    }
}
