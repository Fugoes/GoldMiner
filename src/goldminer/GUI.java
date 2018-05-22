package goldminer;

import util.ResTools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class GUI {
    final static Font FONT = ResTools.getFontFromRes("/xkcd.otf");

    final Dimension vDim = new Dimension(1920, 1080);
    final Dimension rDim = new Dimension(1920, 1080);
    final BufferedImage image = new BufferedImage(vDim.width, vDim.height, BufferedImage.TYPE_INT_ARGB);
    final java.util.Timer timer = new java.util.Timer();
    int playerID;
    Connections.ConnectionBase connection;

    Frame frame;

    interface Paintable {
        void paint(Graphics g, State state, long time);
    }

    void startTimerTask(int FPS) {
        this.timer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                GUI.this.frame.repaint();
            }
        }, 0, 1000 / FPS);
    }

    GUI(int playerID) {
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
        this.playerID = playerID;
    }

    void beginWelcomeScreen() {
        final long zeroTime = Calendar.getInstance().getTimeInMillis();
        this.frame.setPaintFunction(g -> {
            int width, height;
            Rectangle2D geom;
            synchronized (GUI.this.rDim) {
                width = GUI.this.rDim.width;
                height = GUI.this.rDim.height;
                Graphics bufferedG = GUI.this.image.getGraphics();
                bufferedG.setColor(Color.WHITE);
                bufferedG.fillRect(0, 0, GUI.this.vDim.width, GUI.this.vDim.height);
                bufferedG.setFont(GUI.FONT.deriveFont(Font.BOLD, 100));
                long time = Calendar.getInstance().getTimeInMillis() - zeroTime;
                if (time < 3000) {
                    bufferedG.setColor(Color.BLACK);
                } else {
                    int t = (int) (255 - 255 * (5000 - time) / 2000.0);
                    t = t < 256 ? t : 255;
                    t = t >= 0 ? t : 0;
                    bufferedG.setColor(new Color(t, t, t));
                }
                geom = bufferedG.getFontMetrics().getStringBounds("Gold  Miner", bufferedG);
                bufferedG.drawString(
                        "Gold  Miner",
                        GUI.this.vDim.width / 2 - (int) (geom.getWidth() / 2),
                        GUI.this.vDim.height / 2
                );
                bufferedG.setFont(GUI.FONT.deriveFont(Font.BOLD, 40));
                geom = bufferedG.getFontMetrics().getStringBounds("Created  by  Fugoes  with  Love", bufferedG);
                bufferedG.drawString(
                        "Created  by  Fugoes  with  Love",
                        GUI.this.vDim.width - (int) geom.getWidth() - 10,
                        GUI.this.vDim.height - 10
                );
                g.drawImage(GUI.this.image, 0, 0, width, height, this.frame);
            }
        });
    }

    void beginWaitingConnectionScreen() {
        this.frame.setPaintFunction(g -> {
            int width, height;
            Rectangle2D geom;
            synchronized (GUI.this.rDim) {
                width = GUI.this.rDim.width;
                height = GUI.this.rDim.height;
                Graphics bufferedG = GUI.this.image.getGraphics();
                bufferedG.setColor(Color.WHITE);
                bufferedG.fillRect(0, 0, GUI.this.vDim.width, GUI.this.vDim.height);
                bufferedG.setFont(GUI.FONT.deriveFont(Font.BOLD, 100));
                bufferedG.setColor(Color.BLACK);
                geom = bufferedG.getFontMetrics().getStringBounds("Waiting  for  Connection...", bufferedG);
                bufferedG.drawString(
                        "Waiting  for  Connection...",
                        GUI.this.vDim.width / 2 - (int) (geom.getWidth() / 2),
                        GUI.this.vDim.height / 2
                );
                bufferedG.setFont(GUI.FONT.deriveFont(Font.BOLD, 40));
                geom = bufferedG.getFontMetrics().getStringBounds("Created  by  Fugoes  with  Love", bufferedG);
                bufferedG.drawString(
                        "Created  by  Fugoes  with  Love",
                        GUI.this.vDim.width - (int) geom.getWidth() - 10,
                        GUI.this.vDim.height - 10
                );
                g.drawImage(GUI.this.image, 0, 0, width, height, this.frame);
            }
        });
    }

    void beginGameScreen() {
        this.frame.setPaintFunction(g -> {
            int width, height;
            synchronized (GUI.this.rDim) {
                width = GUI.this.rDim.width;
                height = GUI.this.rDim.height;
            }
            Graphics bufferedG = GUI.this.image.getGraphics();
            bufferedG.setColor(Color.WHITE);
            bufferedG.fillRect(0, 0, GUI.this.vDim.width, GUI.this.vDim.height);
            long time = State.getTimeSync();
            State state = State.getSnapshot();
            state.traverseEntities(entity -> entity.paint(bufferedG, state, time));
            state.traverseHook(hook -> hook.paint(bufferedG, state, time));
            bufferedG.setFont(GUI.FONT.deriveFont(Font.BOLD, 45));
            bufferedG.setColor(Color.BLACK);
            bufferedG.drawString("Hello", 20, 60);
            g.drawImage(GUI.this.image, 0, 0, width, height, this.frame);
        });
        this.frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                long time = State.getTimeSync();
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    State.getInstance().move(GUI.this.playerID, time);
                    GUI.this.connection.send("MOVE," + GUI.this.playerID + "," + time);
                }
            }
        });
    }

    class Frame extends JFrame {
        AtomicReference<Consumer<Graphics>> paintFunctionRef = new AtomicReference<>();

        Frame() {
            super("Gold Miner");
            this.paintFunctionRef.set(g -> {
            });
        }

        public void setPaintFunction(Consumer<Graphics> paintFunction) {
            this.paintFunctionRef.set(paintFunction);
        }

        @Override
        public void paint(Graphics g) {
            this.paintFunctionRef.get().accept(g);
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
