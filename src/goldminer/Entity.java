package goldminer;

import java.awt.image.BufferedImage;

public abstract class Entity implements Cloneable, GUI.Paintable {
    int x;
    int y;
    int radius;
    int speedFactor;
    long takenTime;

    Entity() {

    }

    Entity(int x, int y, int speedFactor, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.speedFactor = speedFactor;
        this.takenTime = Long.MAX_VALUE;
    }

    @Override
    protected Entity clone() throws CloneNotSupportedException {
        Entity result = (Entity) super.clone();
        result.x = this.x;
        result.y = this.y;
        result.radius = this.radius;
        result.speedFactor = this.speedFactor;
        result.takenTime = this.takenTime;
        return result;
    }

    abstract int getDistance(Hook hook, double rad);

    abstract BufferedImage getIMAGE();
}
