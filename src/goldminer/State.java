package goldminer;

import util.FP;
import util.Tuple2;

import java.util.Calendar;
import java.util.Random;
import java.util.Vector;
import java.util.function.Consumer;

public class State {
    private final static State instance = new State();

    private State() {
    }

    private long zeroTime;
    private long pauseTime = Long.MAX_VALUE;

    Vector<Entities.EntityBase> entities = new Vector<>();
    Hook[] hooks = new Hook[2];

    public void init() {
        this.hooks[0] = new Hook(0, 860, 200);
        this.hooks[1] = new Hook(1, 1060, 200);
    }

    public void randomInit() {
        Random random = new Random(Calendar.getInstance().getTimeInMillis());
        int count = 0;
        Entities.EntityBase newEntity = null;
        while (true) {
            if (this.entities.size() == 14) {
                return;
            }
            switch (this.entities.size()) {
                case 0:
                    newEntity = new Entities.Pig(
                            this.randomX(Entities.Pig.RADIUS, random),
                            320
                    );
                    break;
                case 1:
                    newEntity = new Entities.Pig(
                            this.randomX(Entities.Pig.RADIUS, random),
                            1080 - Entities.Pig.RADIUS - 25
                    );
                    break;
                case 2:
                    if (random.nextBoolean()) {
                        newEntity = new Entities.Pig(
                                this.randomX(Entities.Pig.RADIUS, random),
                                1080 - 3 * Entities.Pig.RADIUS - 30
                        );
                    } else {
                        newEntity = new Entities.Pig(
                                this.randomX(Entities.Pig.RADIUS, random),
                                320 + 2 * Entities.Pig.RADIUS + 6
                        );
                    }
                    break;
                case 3:
                case 4:
                case 5:
                case 6:
                    newEntity = new Entities.Rock(
                            this.randomX(Entities.Rock.RADIUS, random),
                            this.randomY(Entities.Rock.RADIUS, random)
                    );
                    break;
                case 7:
                    newEntity = new Entities.GoldMax(
                            this.randomX(Entities.GoldMax.RADIUS, random),
                            this.randomY(Entities.GoldMax.RADIUS, random)
                    );
                    break;
                case 8:
                case 9:
                    newEntity = new Entities.GoldMid(
                            this.randomX(Entities.GoldMid.RADIUS, random),
                            this.randomY(Entities.GoldMid.RADIUS, random)
                    );
                    break;
                case 10:
                case 11:
                    newEntity = new Entities.GoldMin(
                            this.randomX(Entities.GoldMin.RADIUS, random),
                            this.randomY(Entities.GoldMin.RADIUS, random)
                    );
                    break;
                case 12:
                case 13:
                    newEntity = new Entities.Pocket(
                            this.randomX(Entities.Pocket.RADIUS, random),
                            this.randomY(Entities.Pocket.RADIUS, random),
                            random.nextInt(1000)
                    );
                    break;
            }
            if (this.isEntitiesConflict(newEntity)) {
                count++;
                if (count == 500) {
                    this.entities.remove(this.entities.size() - 1);
                    count = 0;
                }
            } else {
                this.entities.add(newEntity);
                count = 0;
            }
        }
    }

    public String dumpEntities() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ENTITIES");
        for (Entities.EntityBase entity : this.entities) {
            stringBuilder.append("," + entity.getName() + "," + entity.x + "," + entity.y);
            if (entity instanceof Entities.Pocket) {
                stringBuilder.append("," + entity.score);
            }
        }
        return stringBuilder.toString();
    }

    public void loadEntities(String[] strings) {
        int i = 1;
        Entities.EntityBase entity = null;
        while (i < strings.length) {
            switch (strings[i]) {
                case Entities.GoldMax.NAME:
                    entity = new Entities.GoldMax(
                            Integer.valueOf(strings[i + 1]),
                            Integer.valueOf(strings[i + 2])
                    );
                    i += 3;
                    break;
                case Entities.GoldMid.NAME:
                    entity = new Entities.GoldMid(
                            Integer.valueOf(strings[i + 1]),
                            Integer.valueOf(strings[i + 2])
                    );
                    i += 3;
                    break;
                case Entities.GoldMin.NAME:
                    entity = new Entities.GoldMin(
                            Integer.valueOf(strings[i + 1]),
                            Integer.valueOf(strings[i + 2])
                    );
                    i += 3;
                    break;
                case Entities.Pig.NAME:
                    entity = new Entities.Pig(
                            Integer.valueOf(strings[i + 1]),
                            Integer.valueOf(strings[i + 2])
                    );
                    i += 3;
                    break;
                case Entities.Pocket.NAME:
                    entity = new Entities.Pocket(
                            Integer.valueOf(strings[i + 1]),
                            Integer.valueOf(strings[i + 2]),
                            Integer.valueOf(strings[i + 3])
                    );
                    i += 4;
                    break;
                case Entities.Rock.NAME:
                    entity = new Entities.Rock(
                            Integer.valueOf(strings[i + 1]),
                            Integer.valueOf(strings[i + 2])
                    );
                    i += 3;
                    break;
            }
            this.entities.add(entity);
        }
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
                        if (anotherHook.pendingIntersectTime < rs.get(0).t1 ||
                                (anotherHook.pendingIntersectTime == rs.get(0).t1 && playerID == 0)) {
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

    public static State getSnapshot() {
        State result = new State();
        synchronized (State.instance) {
            for (Entities.EntityBase entity : State.instance.entities) {
                result.entities.add(FP.liftExp(entity::clone).get().get());
            }
            result.hooks[0] = FP.liftExp(() -> State.instance.hooks[0].clone()).get().get();
            result.hooks[1] = FP.liftExp(() -> State.instance.hooks[1].clone()).get().get();
            result.zeroTime = State.instance.zeroTime;
            result.pauseTime = State.instance.pauseTime;
        }
        return result;
    }

    public static State getInstance() {
        return State.instance;
    }

    public int[] getScores(long time) {
        int[] rs = new int[2];
        rs[0] = 0;
        rs[1] = 0;
        for (Entities.EntityBase entity : this.entities) {
            if (entity.playerID != -1) {
                if (entity.pendingEndTime <= time) {
                    rs[entity.playerID] += entity.score;
                }
            }
        }
        return rs;
    }

    public void traverseHook(Consumer<Hook> func) {
        for (Hook hook : this.hooks) {
            func.accept(hook);
        }
    }

    public void traverseEntities(Consumer<Entities.EntityBase> func) {
        for (Entities.EntityBase entity : this.entities) {
            func.accept(entity);
        }
    }

    public void start() {
        this.zeroTime = Calendar.getInstance().getTimeInMillis();
    }

    public static long getTimeSync() {
        long time;
        synchronized (State.instance) {
            time = State.instance.getTime();
        }
        return time;
    }

    public static void pause(long time) {
        synchronized (State.instance) {
            if (State.instance.pauseTime == Long.MAX_VALUE) {
                State.instance.pauseTime = time;
            }
        }
    }

    public static void resume() {
        synchronized (State.instance) {
            State.instance.zeroTime = Calendar.getInstance().getTimeInMillis() - State.instance.pauseTime;
            State.instance.pauseTime = Long.MAX_VALUE;
        }
    }

    private long getTime() {
        long time = Calendar.getInstance().getTimeInMillis() - this.zeroTime;
        return time < this.pauseTime ? time : this.pauseTime;
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
        hook.pendingEndTime = hook.pendingIntersectTime + 200 + delta * this.entities.get(t.t2).speedFactor;
        hook.pendingEntityId = t.t2;
        this.entities.get(hook.pendingEntityId).playerID = hook.playerID;
        this.entities.get(hook.pendingEntityId).pendingEndTime = hook.pendingEndTime;
        this.entities.get(hook.pendingEntityId).takenTime = hook.pendingIntersectTime;
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
            Entities.EntityBase entity = this.entities.get(i);
            if (!(entity.takenTime < hook.pendingBeginTime)) {
                int distance = entity.getDistance(hook);
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

    private boolean isEntitiesConflict(Entities.EntityBase newEntity) {
        for (Entities.EntityBase entity : this.entities) {
            if (this.isTwoEntitiesConflict(entity, newEntity)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTwoEntitiesConflict(Entities.EntityBase a, Entities.EntityBase b) {
        if (a instanceof Entities.Pig || b instanceof Entities.Pig) {
            return Math.abs(a.y - b.y) < a.getRadius() + b.getRadius() + 5;
        } else {
            return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y))
                    < a.getRadius() + b.getRadius() + 5;
        }
    }

    private int randomX(int radius, Random random) {
        radius += 10;
        return random.nextInt(1920 - 2 * radius) + radius;
    }

    private int randomY(int radius, Random random) {
        radius += 10;
        return random.nextInt(1080 - 2 * radius - 320) + 320;
    }
}
