package goldminer;

public abstract class Entity implements Cloneable, GUI.Paintable {
    int x;
    int y;
    int radius;
    boolean taken;

    Entity() {

    }

    Entity(int x, int y, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.taken = false;
    }

    @Override
    protected Entity clone() throws CloneNotSupportedException {
        Entity result = (Entity) super.clone();
        result.x = this.x;
        result.y = this.y;
        result.radius = this.radius;
        result.taken = this.taken;
        return result;
    }

    abstract long getIntersectTime(Hook hook, double rad);
}
