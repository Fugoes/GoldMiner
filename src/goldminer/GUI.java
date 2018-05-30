package goldminer;

import util.FP;
import util.ResTools;
import util.Tuple2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class GUI {
    final static long END_TIME = 1 * 1000;
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
            }
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
        });
    }

    void beginWaitingConnectionScreen() {
        this.frame.setPaintFunction(g -> {
            int width, height;
            Rectangle2D geom;
            synchronized (GUI.this.rDim) {
                width = GUI.this.rDim.width;
                height = GUI.this.rDim.height;
            }
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
            if (time >= GUI.END_TIME) {
                int[] scores = state.getScores(GUI.END_TIME);
                if (scores[0] > scores[1]) {
                    GUI.this.beginResultScreen(0, scores[this.playerID], scores[1 - this.playerID]);
                } else if (scores[0] < scores[1]) {
                    GUI.this.beginResultScreen(1, scores[this.playerID], scores[1 - this.playerID]);
                } else {
                    GUI.this.beginResultScreen(-1, scores[this.playerID], scores[1 - this.playerID]);
                }
            } else {
                state.traverseEntities(entity -> entity.paint(bufferedG, state, time));
                state.traverseHook(hook -> hook.paint(bufferedG, state, time));
                bufferedG.setFont(GUI.FONT.deriveFont(Font.BOLD, 45));
                bufferedG.setColor(Color.BLACK);
                int[] scores = state.getScores(time);
                bufferedG.drawString("Score: " + scores[0], 20, 60);
                bufferedG.drawString("Score: " + scores[1], 1600, 60);
                String s = Long.toString(time / 1000) + " S";
                Rectangle2D geom = bufferedG.getFontMetrics().getStringBounds(s, bufferedG);
                bufferedG.drawString(s, 1920 / 2 - (int) geom.getWidth() / 2, 60);
                g.drawImage(GUI.this.image, 0, 0, width, height, this.frame);
            }
        });
        this.frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                long time = State.getTimeSync();
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    State.getInstance().move(GUI.this.playerID, time);
                    GUI.this.connection.sendMove(GUI.this.playerID, time);
                }
            }
        });
    }

    void beginResultScreen(int playerID, int score, int theOtherScore) {
        StringBuilder stringBuilder = new StringBuilder();
        this.frame.setPaintFunction(g -> {
            Rectangle2D geom;
            String s;
            int width, height;
            synchronized (GUI.this.rDim) {
                width = GUI.this.rDim.width;
                height = GUI.this.rDim.height;
            }
            Graphics bufferedG = GUI.this.image.getGraphics();
            bufferedG.setColor(Color.WHITE);
            bufferedG.fillRect(0, 0, GUI.this.vDim.width, GUI.this.vDim.height);
            bufferedG.setFont(GUI.FONT.deriveFont(Font.BOLD, 45));
            bufferedG.setColor(Color.BLACK);
            if (playerID == -1) {
                s = "Draw !";
            } else {
                if (playerID == GUI.this.playerID) {
                    s = "You  won !";
                } else {
                    s = "You  lose !";
                }
            }
            geom = bufferedG.getFontMetrics().getStringBounds(s, bufferedG);
            bufferedG.drawString(s, GUI.this.vDim.width / 2 - (int) (geom.getWidth() / 2),
                    GUI.this.vDim.height / 2 - 3 * (int) geom.getHeight());
            s = "Opponent  player's  Score:  " + Integer.toString(theOtherScore);
            geom = bufferedG.getFontMetrics().getStringBounds(s, bufferedG);
            bufferedG.drawString(s, GUI.this.vDim.width / 2 - (int) (geom.getWidth() / 2),
                    GUI.this.vDim.height / 2 - (int) geom.getHeight());
            s = "Your  Score:  " + Integer.toString(score);
            geom = bufferedG.getFontMetrics().getStringBounds(s, bufferedG);
            bufferedG.drawString(s, GUI.this.vDim.width / 2 - (int) (geom.getWidth() / 2),
                    GUI.this.vDim.height / 2 + (int) geom.getHeight());
            synchronized (stringBuilder) {
                s = "Your  Name:  " + stringBuilder.toString();
            }
            geom = bufferedG.getFontMetrics().getStringBounds(s, bufferedG);
            bufferedG.drawString(s, GUI.this.vDim.width / 2 - (int) (geom.getWidth() / 2),
                    GUI.this.vDim.height / 2 + 3 * (int) (geom.getHeight()));
            g.drawImage(GUI.this.image, 0, 0, width, height, this.frame);
        });
        this.frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                synchronized (stringBuilder) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        GUI.this.beginBillboardScreen(stringBuilder.toString(), score);
                    } else {
                        char c = e.getKeyChar();
                        if ((c >= 'a' && c <= 'z')
                                || (c >= 'A' && c <= 'Z')) {
                            stringBuilder.append(e.getKeyChar());
                        } else if (c == ' ') {
                            stringBuilder.append("  ");
                        }
                    }
                }
            }
        });
    }

    void beginBillboardScreen(String name, int score) {
        FP.liftExp(() -> {
            BufferedWriter bw = new BufferedWriter(new FileWriter("billboard.txt", true));
            bw.write(name + "," + score + "\n");
            bw.flush();
            bw.close();
        }).run();
        BufferedReader br = FP.liftExp(() -> new BufferedReader(new FileReader("billboard.txt"))).get().get();
        Vector<Tuple2<String, Integer>> history = new Vector<>();
        while (true) {
            Optional<String> l = FP.liftExp(br::readLine).get();
            l.ifPresent(line -> {
                String[] ss = line.split(",");
                history.add(new Tuple2<>(ss[0], Integer.valueOf(ss[1])));
            });
            if (!l.isPresent()) {
                break;
            }
        }
        FP.liftExp(br::close).run();
        // Collections.sort is stable
        Collections.sort(history, (o1, o2) -> {
            if (o1.t2 > o2.t2) {
                return -1;
            } else if (o1.t2 < o2.t2) {
                return 1;
            } else {
                return 0;
            }
        });
        Map<String, Integer> playerToScore = new HashMap<>();
        for (Tuple2<String, Integer> s : history) {
            if (playerToScore.containsKey(s.t1)) {
                playerToScore.replace(s.t1, playerToScore.get(s.t1) + s.t2);
            } else {
                playerToScore.put(s.t1, s.t2);
            }
        }
        Vector<Tuple2<String, Integer>> historyOfPlayer = new Vector<>();
        playerToScore.forEach((player, s) -> {
            historyOfPlayer.add(new Tuple2<>(player, s));
        });
        Collections.sort(historyOfPlayer, (o1, o2) -> {
            if (o1.t2 > o2.t2) {
                return -1;
            } else if (o1.t2 < o2.t2) {
                return 1;
            } else {
                return 0;
            }
        });

        StringBuilder sb;

        sb = new StringBuilder("Player\n\n\n");
        for (int i = 0; i < 5; i++) {
            if (i < history.size()) {
                Tuple2<String, Integer> entry = history.get(i);
                sb.append(entry.t1 + "\n");
            } else {
                break;
            }
        }
        String historyPlayers = sb.toString();

        sb = new StringBuilder("Score\n\n\n");
        for (int i = 0; i < 5; i++) {
            if (i < history.size()) {
                Tuple2<String, Integer> entry = history.get(i);
                sb.append(entry.t2 + "\n");
            } else {
                break;
            }
        }
        String historyScores = sb.toString();

        sb = new StringBuilder("Player\n\n\n");
        for (int i = 0; i < 5; i++) {
            if (i < historyOfPlayer.size()) {
                Tuple2<String, Integer> entry = historyOfPlayer.get(i);
                sb.append(entry.t1 + "\n");
            } else {
                break;
            }
        }
        String historyOfPlayerPlayers = sb.toString();

        sb = new StringBuilder("Total\nScore\n\n");
        for (int i = 0; i < 5; i++) {
            if (i < historyOfPlayer.size()) {
                Tuple2<String, Integer> entry = historyOfPlayer.get(i);
                sb.append(entry.t2 + "\n");
            } else {
                break;
            }
        }
        String historyOfPlayerScores = sb.toString();

        this.frame.setPaintFunction(g -> {
            Rectangle2D geom;
            String s;
            int width, height;
            synchronized (GUI.this.rDim) {
                width = GUI.this.rDim.width;
                height = GUI.this.rDim.height;
            }
            Graphics bufferedG = GUI.this.image.getGraphics();
            bufferedG.setColor(Color.WHITE);
            bufferedG.fillRect(0, 0, GUI.this.vDim.width, GUI.this.vDim.height);
            bufferedG.setFont(GUI.FONT.deriveFont(Font.BOLD, 60));
            bufferedG.setColor(Color.BLACK);

            s = "Billboard";
            geom = bufferedG.getFontMetrics().getStringBounds(s, bufferedG);
            bufferedG.drawString(s, GUI.this.vDim.width / 2 - (int) (geom.getWidth() / 2),
                    40 + 2 * ((int) geom.getHeight()));

            Tuple2<Integer, Integer> t1;
            Tuple2<Integer, Integer> t2;

            t1 = GUI.getStringBounds(historyPlayers, bufferedG);
            t2 = GUI.getStringBounds(historyScores, bufferedG);
            GUI.drawString(bufferedG, historyPlayers,
                    GUI.this.vDim.width / 4 - ((t1.t1 + t2.t1 + 50) / 2),
                    GUI.this.vDim.height / 2 - ((int) geom.getHeight()) * 5 / 2);
            GUI.drawString(bufferedG, historyScores,
                    GUI.this.vDim.width / 4 - ((t1.t1 + t2.t1 + 50) / 2) + t1.t1 + 50,
                    GUI.this.vDim.height / 2 - ((int) geom.getHeight()) * 5 / 2);

            t1 = GUI.getStringBounds(historyOfPlayerPlayers, bufferedG);
            t2 = GUI.getStringBounds(historyOfPlayerScores, bufferedG);
            GUI.drawString(bufferedG, historyOfPlayerPlayers,
                    GUI.this.vDim.width * 3 / 4 - ((t1.t1 + t2.t1 + 50) / 2),
                    GUI.this.vDim.height / 2 - ((int) geom.getHeight()) * 5 / 2);
            GUI.drawString(bufferedG, historyOfPlayerScores,
                    GUI.this.vDim.width * 3 / 4 - ((t1.t1 + t2.t1 + 50) / 2) + t1.t1 + 50,
                    GUI.this.vDim.height / 2 - ((int) geom.getHeight()) * 5 / 2);

            g.drawImage(GUI.this.image, 0, 0, width, height, this.frame);
        });
        this.frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
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

    private static void drawString(Graphics g, String s, int x, int y) {
        for (String line : s.split("\n")) {
            g.drawString(line, x, y);
            y += g.getFontMetrics().getHeight();
        }
    }

    private static Tuple2 getStringBounds(String s, Graphics g) {
        double width = 0, height = 0;
        for (String line : s.split("\n")) {
            Rectangle2D geom = g.getFontMetrics().getStringBounds(line, g);
            if (geom.getWidth() > width) {
                width = geom.getWidth();
            }
            height += geom.getHeight();
        }
        return new Tuple2<>((int) width, (int) height);
    }
}
