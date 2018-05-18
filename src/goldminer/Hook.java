package goldminer;

import util.Coordinate;
import util.ImageTools;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Hook implements Cloneable, GUI.Paintable {
    static final int MAX_DEGREE = 88;
    static final BufferedImage IMAGE
            = ImageTools.shrinkTo(ImageTools.getImageFromRes("/hook.png"), 70, 50);

    int x;
    int y;
    long time;
    long beginTime;

    Hook(int x, int y) {
        this.x = x;
        this.y = y;
        this.time = 0;
        this.beginTime = -1;
    }

    @Override
    public void paint(Graphics g, long time) {
        Coordinate c = new Coordinate(Hook.IMAGE.getWidth() / 2, 0);
        BufferedImage image = ImageTools.rotateByRad(Hook.IMAGE, this.getRadByTime(time - this.time), c);
        g.drawImage(image, this.x - c.x, this.y - c.y, null);
    }

    @Override
    protected Hook clone() throws CloneNotSupportedException {
        Hook result = (Hook) super.clone();
        result.x = this.x;
        result.y = this.y;
        result.time = this.time;
        result.beginTime = this.beginTime;
        return result;
    }

    double getRadByTime(long time) {
        time %= 2000;
        time = time > 1000 ? 2000 - time : time;
        return ((Hook.MAX_DEGREE * time * time * (1500 - time) / 250000 - Hook.MAX_DEGREE * 1000) / 180000.0) * Math.PI;
    }
}
