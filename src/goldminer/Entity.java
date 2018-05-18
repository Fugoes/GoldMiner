package goldminer;

public abstract class Entity implements Cloneable, GUI.Paintable {
    int x;
    int y;
    int radius;

    Entity() {

    }

    Entity(int x, int y, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    @Override
    protected Entity clone() throws CloneNotSupportedException {
        Entity result = (Entity) super.clone();
        result.x = this.x;
        result.y = this.y;
        result.radius = this.radius;
        return result;
    }
}
