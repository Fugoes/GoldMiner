package goldminer;

public abstract class Entity implements Cloneable, GUI.Paintable {
    int x;
    int y;
    int radius;
    int speedFactor;
    boolean taken;

    Entity() {

    }

    Entity(int x, int y, int speedFactor, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.speedFactor = speedFactor;
        this.taken = false;
    }

    @Override
    protected Entity clone() throws CloneNotSupportedException {
        Entity result = (Entity) super.clone();
        result.x = this.x;
        result.y = this.y;
        result.radius = this.radius;
        result.speedFactor = this.speedFactor;
        result.taken = this.taken;
        return result;
    }

    abstract long getIntersectTime(Hook hook, double rad);
}
