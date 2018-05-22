package goldminer;

import util.FP;

import java.util.Calendar;
import java.util.function.BiConsumer;

public class Main {
    public static void main(String[] argv) {
        int playerID;
        State.getInstance().init();
        GUI gui = new GUI();
        Connections.ConnectionBase connection;
        switch (argv.length) {
            case 1:
                connection = new Connections.TCPServer(Integer.valueOf(argv[0]), new TCPServerBiConsumer(gui));
                playerID = 0;
                break;
            case 2:
                connection = new Connections.TCPClient(argv[0], Integer.valueOf(argv[1]), new TCPClientBiConsumer(gui));
                playerID = 1;
                break;
            default:
                System.exit(-1);
                return;
        }
        gui.startTimerTask(10);

        long time = Calendar.getInstance().getTimeInMillis();
        gui.beginWelcomeScreen();
        FP.liftExp(() -> Thread.sleep(1000)).run();

        gui.beginWaitingConnectionScreen();
        while (!connection.waitForUp(200)) ;

        if (playerID == 0) {
            connection.send("START");
        }
    }
}

class TCPServerBiConsumer implements BiConsumer<Connections.ConnectionBase, String> {
    GUI gui;

    TCPServerBiConsumer(GUI gui) {
        this.gui = gui;
    }

    @Override
    public void accept(Connections.ConnectionBase connectionBase, String s) {
        System.err.println(s);
        String[] args = s.split(",");
        switch (args[0]) {
            case "START":
                gui.beginGameScreen();
                State.getInstance().start();
                break;
        }
    }
}

class TCPClientBiConsumer implements BiConsumer<Connections.ConnectionBase, String> {
    GUI gui;

    TCPClientBiConsumer(GUI gui) {
        this.gui = gui;
    }

    @Override
    public void accept(Connections.ConnectionBase connectionBase, String s) {
        System.err.println(s);
        String[] args = s.split(",");
        switch (args[0]) {
            case "START":
                connectionBase.send("START");
                gui.beginGameScreen();
                State.getInstance().start();
                break;
        }
    }
}
