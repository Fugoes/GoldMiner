package goldminer;

import util.FP;
import util.Tuple2;

import java.nio.file.attribute.AclEntryType;
import java.util.Calendar;
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
        this.entities.add(new Rock(600, 500));
    }

    public void move(int playerID, long time) {
        //       0 ms ~     200 ms : hook freeze
        //     200 ms ~ 200 + k ms : hook down
        // 200 + k ms ~ 400 + k ms : hook freeze
        // 400 + k ms ~            : hook up
        synchronized (this) {
            if (this.hooks[playerID].pendingBeginTime == -1) {
                if (this.calcPossibleEntityIntersectTime(playerID, time)) {
                    this.hooks[playerID].pendingBeginTime = time;
                } else {
                    this.hooks[playerID].pendingBeginTime = -1;
                }
            }
        }
    }

    public boolean calcPossibleEntityIntersectTime(int playerID, long time) {
        long min = Integer.MAX_VALUE;
        Entity minEntity = null;
        Hook hook = this.hooks[playerID];
        for (Entity entity : this.entities) {
            if (!entity.taken) {
                long intersectTime = entity.getIntersectTime(hook, hook.getRadByTime(time - hook.zeroTime));
                if (intersectTime != -1 && intersectTime < min) {
                    min = intersectTime;
                    minEntity = entity;
                }
            }
        }
        if (min < Integer.MAX_VALUE) {
            this.hooks[playerID].pendingIntersectTime = min;
            this.hooks[playerID].pendingEntity = minEntity;
            return true;
        } else {
            this.hooks[playerID].pendingIntersectTime = -1;
            this.hooks[playerID].pendingEntity = null;
            return false;
        }
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
