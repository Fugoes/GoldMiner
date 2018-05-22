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

    int getDistance(Hook hook, double rad) {
        int deltaX = this.x - hook.x;
        int deltaY = this.y - hook.y;
        double distance = Math.abs((deltaX + Math.tan(rad) * deltaY) * Math.cos(rad));
        if (distance < this.radius + Hook.IMAGE.getWidth() / 3) {
            int r = (int) Math.sqrt((double) (deltaX * deltaX + deltaY * deltaY));
            return r - Hook.IMAGE.getHeight() / 2;
        } else {
            return -1;
        }
    }

    abstract BufferedImage getImage(long time);
}
