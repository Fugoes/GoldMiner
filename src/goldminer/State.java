package goldminer;

import util.FP;
import util.Tuple2;

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
        synchronized (this) {
            Hook hook = this.hooks[playerID];
            Hook anotherHook = this.hooks[1 - playerID];
            if (time >= hook.pendingEndTime + 200) {
                hook.pendingRad = hook.getRadByTime(time);
                hook.pendingBeginTime = time;
                Vector<Tuple2<Long, Integer>> rs = this.getPossibleEntities(hook);
                if (rs.size() == 0) {
                    this.moveEmpty(hook);
                } else {
                    if (anotherHook.pendingEntityId == rs.get(0).t2) {
                        if (anotherHook.pendingIntersectTime < rs.get(0).t1) {
                            if (rs.size() > 1) {
                                this.moveNonEmpty(hook, rs.get(1));
                            } else {
                                this.moveEmpty(hook);
                            }
                        } else {
                            this.moveNonEmpty(hook, rs.get(0));
                            Vector<Tuple2<Long, Integer>> rsp = this.getPossibleEntities(anotherHook);
                            assert rsp.size() >= 1;
                            if (rsp.size() == 1) {
                                this.moveEmpty(anotherHook);
                            } else {
                                this.moveNonEmpty(anotherHook, rsp.get(1));
                            }
                        }
                    } else {
                        this.moveNonEmpty(hook, rs.get(0));
                    }
                }
            }
        }
    }

    private void moveEmpty(Hook hook) {
        int distance = hook.getMaxDistance(hook.pendingRad);
        long delta = (long) (distance / Hook.DOWN_SPEED);
        hook.pendingIntersectTime = hook.pendingBeginTime + 200 + delta;
        hook.pendingEndTime = hook.pendingIntersectTime + 200 + delta;
        hook.pendingEntityId = -1;
    }

    private void moveNonEmpty(Hook hook, Tuple2<Long, Integer> t) {
        long delta = t.t1 - 200 - hook.pendingBeginTime;
        hook.pendingIntersectTime = hook.pendingBeginTime + 200 + delta;
        hook.pendingEndTime = hook.pendingIntersectTime + 200 +
                delta * this.entities.get(t.t2).speedFactor;
        hook.pendingEntityId = t.t2;
    }

    /**
     * assume hook.zeroTime and hook.pendingBeginTime are set.
     *
     * @param hook
     * @return
     */
    private Vector<Tuple2<Long, Integer>> getPossibleEntities(Hook hook) {
        Vector<Tuple2<Long, Integer>> rs = new Vector<>();
        for (int i = 0; i < this.entities.size(); i++) {
            Entity entity = this.entities.get(i);
            if (!entity.taken) {
                int distance = entity.getDistance(hook, hook.pendingRad);
                if (distance != -1) {
                    rs.add(new Tuple2<>(hook.pendingBeginTime + 200 + (int) (distance / Hook.DOWN_SPEED), i));
                }
            }
        }
        rs.sort((o1, o2) -> {
            if (o1.t1 < o2.t1) {
                return -1;
            } else if (o1.t1 > o2.t1) {
                return 1;
            } else {
                return 0;
            }
        });
        return rs;
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
