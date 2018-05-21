package goldminer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Calendar;

public class GUI {
    final Dimension vDim = new Dimension(1920, 1080);
    final Dimension rDim = new Dimension(1920, 1080);
    final BufferedImage image = new BufferedImage(vDim.width, vDim.height, BufferedImage.TYPE_INT_ARGB);
    final java.util.Timer timer = new java.util.Timer();
    Frame frame;

    interface Paintable {
        void paint(Graphics g, long time);
    }

    GUI(int FPS) {
        this.frame = new Frame();
        this.frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension dim = GUI.this.frame.getSize();
                if (!GUI.this.adjustToVDimRatio(dim)) {
                    GUI.this.frame.setSize(dim);
                }
                synchronized (GUI.this.rDim) {
                    GUI.this.rDim.width = dim.width;
                    GUI.this.rDim.height = dim.height;
                }
            }
        });
        this.frame.setSize(1000, 1000);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setVisible(true);
        this.timer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                GUI.this.frame.repaint();
            }
        }, 0, 1000 / FPS);
        this.frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    long time = Calendar.getInstance().getTimeInMillis();
                    State.getInstance().move(0, time);
                }
            }
        });
    }

    class Frame extends JFrame {
        Frame() {
            super("Gold Miner");
        }

        @Override
        public void paint(Graphics g) {
            int width, height;
            synchronized (GUI.this.rDim) {
                width = GUI.this.rDim.width;
                height = GUI.this.rDim.height;
            }
            Graphics bufferedG = GUI.this.image.getGraphics();
            bufferedG.setColor(Color.WHITE);
            bufferedG.fillRect(0, 0, GUI.this.vDim.width, GUI.this.vDim.height);
            long time = Calendar.getInstance().getTimeInMillis();
            State state = State.getSnapshot();
            state.traverseEntities(entity -> entity.paint(bufferedG, time));
            state.traverseHook(hook -> hook.paint(bufferedG, time));
            g.drawImage(GUI.this.image, 0, 0, width, height, this);
        }
    }

    private boolean adjustToVDimRatio(Dimension dim) {
        if (dim.width * vDim.height < dim.height * vDim.width) {
            int height;
            if (dim.width * vDim.height % vDim.width == 0) {
                height = dim.width * vDim.height / vDim.width;
            } else {
                height = dim.width * vDim.height / vDim.width + 1;
            }
            if (dim.height == height) {
                return true;
            } else {
                dim.height = height;
                return false;
            }
        } else if (dim.width * vDim.height > dim.height * vDim.width) {
            if (dim.width == dim.height * vDim.width / vDim.height) {
                return true;
            } else {
                dim.width = dim.height * vDim.width / vDim.height;
                return false;
            }
        } else {
            return true;
        }
    }
}
