package goldminer;

import util.ImageTools;

public class Rock extends Entity {
    static final int RADIUS = 20;

    Rock(int x, int y) {
        super(x, y, Rock.RADIUS);
    }
}
