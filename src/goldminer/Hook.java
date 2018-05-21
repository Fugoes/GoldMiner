package goldminer;

import util.Coordinate;
import util.ImageTools;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

public class Hook implements Cloneable, GUI.Paintable {
    static final int MAX_DEGREE = 88;
    static final BufferedImage IMAGE
            = ImageTools.shrinkTo(ImageTools.getImageFromRes("/hook.png"), 70, 50);
    static final double DOWN_SPEED = 0.5;

    int x;
    int y;
    long zeroTime;
    long pendingIntersectTime;
    long pendingBeginTime;

    Entity pendingEntity;

    Hook(int x, int y) {
        this.x = x;
        this.y = y;
        this.zeroTime = 0;
        this.pendingBeginTime = -1;
        this.pendingIntersectTime = -1;
    }

    @Override
    public void paint(Graphics g, long time) {
        if (this.pendingBeginTime == -1 || time <= this.pendingBeginTime) {
            Coordinate c = new Coordinate(Hook.IMAGE.getWidth() / 2, 0);
            BufferedImage image = ImageTools.rotateByRad(Hook.IMAGE, this.getRadByTime(time - this.zeroTime), c);
            g.drawImage(image, this.x - c.x, this.y - c.y, null);
        } else {
            double rad = this.getRadByTime(this.pendingBeginTime - this.zeroTime);
            Coordinate c = new Coordinate(Hook.IMAGE.getWidth() / 2, 0);
            BufferedImage image = ImageTools.rotateByRad(Hook.IMAGE, rad, c);
            if (time <= this.pendingBeginTime + 200) {
                g.drawImage(image, this.x - c.x, this.y - c.y, null);
            } else if (time <= this.pendingBeginTime + 200 + this.pendingIntersectTime) {
                int len = (int) ((time - this.pendingBeginTime - 200) * Hook.DOWN_SPEED);
                int deltaX = (int) (len * Math.sin(-rad));
                int deltaY = (int) (len * Math.cos(-rad));
                g.drawImage(image, this.x - c.x + deltaX, this.y - c.y + deltaY, null);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(5));
                g2.draw(new Line2D.Float(this.x, this.y, this.x + deltaX, this.y + deltaY));
            } else {
                int len = (int) (this.pendingIntersectTime * Hook.DOWN_SPEED);
                int deltaX = (int) (len * Math.sin(-rad));
                int deltaY = (int) (len * Math.cos(-rad));
                g.drawImage(image, this.x - c.x + deltaX, this.y - c.y + deltaY, null);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(5));
                g2.draw(new Line2D.Float(this.x, this.y, this.x + deltaX, this.y + deltaY));
            }
        }
    }

    @Override
    protected Hook clone() throws CloneNotSupportedException {
        Hook result = (Hook) super.clone();
        result.x = this.x;
        result.y = this.y;
        result.zeroTime = this.zeroTime;
        result.pendingBeginTime = this.pendingBeginTime;
        result.pendingIntersectTime = this.pendingIntersectTime;
        return result;
    }

    double getRadByTime(long time) {
        time %= 2000;
        time = time > 1000 ? 2000 - time : time;
        return ((Hook.MAX_DEGREE * time * time * (1500 - time) / 250000 - Hook.MAX_DEGREE * 1000) / 180000.0) * Math.PI;
    }
}
