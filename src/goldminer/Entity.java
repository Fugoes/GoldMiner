package goldminer;

import util.Copyable;

public abstract class Entity implements Copyable {
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
}
