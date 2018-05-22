package goldminer;

import util.FP;

import java.util.function.BiConsumer;

public class Main {
    public static void main(String[] argv) {
        int playerID;
        State.getInstance().init();
        Connections.ConnectionBase connection;
        GUI gui;
        switch (argv.length) {
            case 1:
                gui = new GUI(0);
                connection = new Connections.TCPServer(Integer.valueOf(argv[0]), new TCPServerBiConsumer(gui));
                playerID = 0;
                break;
            case 2:
                gui = new GUI(1);
                connection = new Connections.TCPClient(argv[0], Integer.valueOf(argv[1]), new TCPClientBiConsumer(gui));
                playerID = 1;
                break;
            default:
                System.exit(-1);
                return;
        }
        gui.connection = connection;
        gui.startTimerTask(60);

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
            case "MOVE":
                State.getInstance().move(Integer.valueOf(args[1]), Long.valueOf(args[2]));
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
            case "MOVE":
                State.getInstance().move(Integer.valueOf(args[1]), Long.valueOf(args[2]));
                break;
        }
    }
}
