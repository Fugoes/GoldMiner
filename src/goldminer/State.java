package goldminer;

import util.FP;

import java.util.Vector;
import java.util.function.Consumer;

public class State {
    private final static State instance = new State();

    private State() {
    }

    Vector<Entity> entities = new Vector<>();
    Hook[] hooks = new Hook[2];

    public void init() {
        this.hooks[0] = new Hook(860, 200);
        this.hooks[1] = new Hook(1060, 200);
    }

    public void move(int playerID, long time) {
    }

    public static State getSnapshot() {
        State result = new State();
        synchronized (State.instance) {
            for (Entity entity : State.instance.entities) {
                result.entities.add(FP.liftExp(entity::clone).get().get());
            }
            result.hooks[0] = FP.liftExp(() -> State.instance.hooks[0].clone()).get().get();
            result.hooks[1] = FP.liftExp(() -> State.instance.hooks[1].clone()).get().get();
        }
        return result;
    }

    public static State getInstance() {
        return State.instance;
    }

    public void traverseHook(Consumer<Hook> func) {
        for (Hook hook : this.hooks) {
            func.accept(hook);
        }
    }
}
