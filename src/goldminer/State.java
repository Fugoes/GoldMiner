package goldminer;

import util.FP;
import util.Tuple2;

import java.util.Comparator;
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
        this.entities.add(new Rock(400, 800));
    }

    public void move(int playerID, long time) {
        Vector<Tuple2<Long, Integer>> vector = new Vector<>();
        for (int i = 0; i < this.entities.size(); i++) {
            Entity entity = this.entities.get(i);
            if (!entity.taken) {
                long intersectTime = entity.getIntersectTime(this.hooks[playerID], time);
                if (intersectTime >= 0) {
                    vector.add(new Tuple2<>(intersectTime, i));
                }
            }
        }
        vector.sort(Comparator.comparing(o -> o.t1));
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

    public void traverseEntities(Consumer<Entity> func) {
        for (Entity entity : this.entities) {
            func.accept(entity);
        }
    }
}
