package goldminer;

import util.ResTools;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class Entities implements Cloneable, GUI.Paintable {
    public static abstract class EntityBase implements Cloneable, GUI.Paintable {
        int x;
        int y;
        int radius;
        int speedFactor;
        int score;
        int playerID;
        long pendingEndTime;
        long takenTime;

        EntityBase() {
        }

        EntityBase(int x, int y, int speedFactor, int radius, int score) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.speedFactor = speedFactor;
            this.score = score;
            this.playerID = -1;
            this.takenTime = Long.MAX_VALUE;
        }

        @Override
        protected EntityBase clone() throws CloneNotSupportedException {
            Entities.EntityBase result = (Entities.EntityBase) super.clone();
            result.x = this.x;
            result.y = this.y;
            result.radius = this.radius;
            result.speedFactor = this.speedFactor;
            result.score = this.score;
            result.playerID = this.playerID;
            result.pendingEndTime = this.pendingEndTime;
            result.takenTime = this.takenTime;
            return result;
        }

        int getDistance(Hook hook) {
            double rad = hook.getRadByTime(hook.pendingBeginTime);
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

        int getX(long time) {
            return this.x;
        }

        int getY(long time) {
            return this.y;
        }

        abstract int getRadius();

        abstract String getName();

        abstract BufferedImage getImage(long time);
    }

    public static class GoldMax extends Entities.EntityBase {
        static final int RADIUS = 75;
        static final int SPEED_FACTOR = 3;
        static final int SCORE = 500;
        static final String NAME = "GoldMax";
        static final BufferedImage IMAGE = ResTools.shrinkTo(ResTools.getImageFromRes(
                "/gold.png"), 150, 150);

        GoldMax(int x, int y) {
            super(x, y, GoldMax.SPEED_FACTOR, GoldMax.RADIUS, GoldMax.SCORE);
        }

        private GoldMax() {
        }

        @Override
        protected GoldMax clone() throws CloneNotSupportedException {
            return (GoldMax) super.clone();
        }

        @Override
        public void paint(Graphics g, State state, long time) {
            if (time < this.takenTime) {
                g.drawImage(GoldMax.IMAGE,
                        this.x - GoldMax.IMAGE.getWidth() / 2, this.y - GoldMax.IMAGE.getHeight() / 2, null);
            }
        }

        @Override
        public BufferedImage getImage(long time) {
            return GoldMax.IMAGE;
        }

        @Override
        int getRadius() {
            return GoldMax.RADIUS;
        }

        @Override
        String getName() {
            return GoldMax.NAME;
        }
    }

    public static class GoldMid extends Entities.EntityBase {
        static final int RADIUS = 50;
        static final int SPEED_FACTOR = 2;
        static final int SCORE = 200;
        static final String NAME = "GoldMid";
        static final BufferedImage IMAGE = ResTools.shrinkTo(ResTools.getImageFromRes(
                "/gold.png"), 100, 100);

        GoldMid(int x, int y) {
            super(x, y, GoldMid.SPEED_FACTOR, GoldMid.RADIUS, GoldMid.SCORE);
        }

        private GoldMid() {
        }

        @Override
        protected GoldMid clone() throws CloneNotSupportedException {
            return (GoldMid) super.clone();
        }

        @Override
        public void paint(Graphics g, State state, long time) {
            if (time < this.takenTime) {
                g.drawImage(GoldMid.IMAGE,
                        this.x - GoldMid.IMAGE.getWidth() / 2, this.y - GoldMid.IMAGE.getHeight() / 2, null);
            }
        }

        @Override
        public BufferedImage getImage(long time) {
            return GoldMid.IMAGE;
        }

        @Override
        int getRadius() {
            return GoldMid.RADIUS;
        }

        @Override
        String getName() {
            return GoldMid.NAME;
        }
    }

    public static class GoldMin extends Entities.EntityBase {
        static final int RADIUS = 25;
        static final int SPEED_FACTOR = 1;
        static final int SCORE = 100;
        static final String NAME = "GoldMin";
        static final BufferedImage IMAGE = ResTools.shrinkTo(ResTools.getImageFromRes(
                "/gold.png"), 50, 50);

        GoldMin(int x, int y) {
            super(x, y, GoldMin.SPEED_FACTOR, GoldMin.RADIUS, GoldMin.SCORE);
        }

        private GoldMin() {
        }

        @Override
        protected GoldMin clone() throws CloneNotSupportedException {
            return (GoldMin) super.clone();
        }

        @Override
        public void paint(Graphics g, State state, long time) {
            if (time < this.takenTime) {
                g.drawImage(GoldMin.IMAGE,
                        this.x - GoldMin.IMAGE.getWidth() / 2, this.y - GoldMin.IMAGE.getHeight() / 2, null);
            }
        }

        @Override
        public BufferedImage getImage(long time) {
            return GoldMin.IMAGE;
        }

        @Override
        int getRadius() {
            return GoldMin.RADIUS;
        }

        @Override
        String getName() {
            return GoldMin.NAME;
        }
    }

    public static class Pig extends Entities.EntityBase {
        static final int RADIUS = 50;
        static final int SPEED_FACTOR = 1;
        static final int SCORE = 5;
        static final String NAME = "Pig";
        static final BufferedImage IMAGE_LEFT = ResTools.shrinkTo(ResTools.getImageFromRes(
                "/pig.png"), 75, 50);
        static final BufferedImage IMAGE_RIGHT = ResTools.flipByY(ResTools.shrinkTo(ResTools.getImageFromRes(
                "/pig.png"), 75, 50));
        static final int PERIOD = 8000;

        Pig(int x, int y) {
            super(x, y, Pig.SPEED_FACTOR, Pig.RADIUS, Pig.SCORE);
        }

        private Pig() {
        }

        @Override
        protected Pig clone() throws CloneNotSupportedException {
            return (Pig) super.clone();
        }

        @Override
        public void paint(Graphics g, State state, long time) {
            if (time < this.takenTime) {
                BufferedImage image = this.getImage(time);
                g.drawImage(
                        image,
                        this.getX(time) - image.getWidth() / 2,
                        this.getY(time) - image.getHeight() / 2,
                        null
                );
            }
        }

        @Override
        public BufferedImage getImage(long time) {
            time += this.x * Pig.PERIOD / 1800 / 2;
            time %= Pig.PERIOD;
            if (time > Pig.PERIOD / 2) {
                return Pig.IMAGE_LEFT;
            } else {
                return Pig.IMAGE_RIGHT;
            }
        }

        @Override
        int getX(long time) {
            time += this.x * Pig.PERIOD / 1800 / 2;
            time %= Pig.PERIOD;
            time = time > Pig.PERIOD / 2 ? Pig.PERIOD - time : time;
            return (int) (time * 1800 * 2 / Pig.PERIOD + 60);
        }

        @Override
        int getY(long time) {
            return this.y;
        }

        @Override
        int getDistance(Hook hook) {
            double rad = hook.getRadByTime(hook.pendingBeginTime);
            int deltaY = this.y - hook.y;
            double rad0 = Math.atan((double) (hook.x - 60) / (double) deltaY);
            double rad1 = Math.atan((double) (1800 - hook.x) / (double) deltaY);
            if (rad > rad0 || rad < -rad1) {
                return -1;
            } else {
                int deltaX = (int) (deltaY * Math.tan(rad));
                int x0 = hook.x - deltaX;
                int distance = (int) Math.sqrt(deltaX * deltaX + deltaY * deltaY) - Hook.IMAGE.getHeight() / 2;
                long timeDelta = (long) (distance / Hook.DOWN_SPEED);
                int x1 = this.getX(hook.pendingBeginTime + 200 + timeDelta);
                if (Math.abs(x0 - x1) < Pig.RADIUS + Hook.IMAGE.getWidth() / 3) {
                    return distance;
                } else {
                    return -1;
                }
            }
        }

        @Override
        int getRadius() {
            return Pig.RADIUS;
        }

        @Override
        String getName() {
            return Pig.NAME;
        }
    }

    public static class Pocket extends Entities.EntityBase {
        static final int RADIUS = 50;
        static final int SPEED_FACTOR = 2;
        static final String NAME = "Pocket";
        static final BufferedImage IMAGE = ResTools.shrinkTo(ResTools.getImageFromRes(
                "/pocket.png"), 100, 100);

        Pocket(int x, int y, int score) {
            super(x, y, Pocket.SPEED_FACTOR, Pocket.RADIUS, score);
        }

        private Pocket() {
        }

        @Override
        protected Pocket clone() throws CloneNotSupportedException {
            return (Pocket) super.clone();
        }

        @Override
        public void paint(Graphics g, State state, long time) {
            if (time < this.takenTime) {
                g.drawImage(Pocket.IMAGE,
                        this.x - GoldMid.IMAGE.getWidth() / 2, this.y - GoldMid.IMAGE.getHeight() / 2, null);
            }
        }

        @Override
        public BufferedImage getImage(long time) {
            return Pocket.IMAGE;
        }

        @Override
        int getRadius() {
            return Pocket.RADIUS;
        }

        @Override
        String getName() {
            return Pocket.NAME;
        }
    }

    public static class Rock extends Entities.EntityBase {
        static final int RADIUS = 60;
        static final int SPEED_FACTOR = 2;
        static final int SCORE = 20;
        static final String NAME = "Rock";
        static final BufferedImage IMAGE = ResTools.shrinkTo(ResTools.getImageFromRes("/rock.png"), 140, 100);

        Rock(int x, int y) {
            super(x, y, Rock.SPEED_FACTOR, Rock.RADIUS, Rock.SCORE);
        }

        private Rock() {
        }

        @Override
        protected Rock clone() throws CloneNotSupportedException {
            return (Rock) super.clone();
        }

        @Override
        public void paint(Graphics g, State state, long time) {
            if (time < this.takenTime) {
                g.drawImage(Rock.IMAGE, this.x - Rock.IMAGE.getWidth() / 2, this.y - Rock.IMAGE.getHeight() / 2, null);
            }
        }

        @Override
        public BufferedImage getImage(long time) {
            return Rock.IMAGE;
        }

        @Override
        int getRadius() {
            return Rock.RADIUS;
        }

        @Override
        String getName() {
            return Rock.NAME;
        }
    }
}
