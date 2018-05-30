package goldminer;

import util.FP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * master: send state
 */
public class Connections {
    public static abstract class ConnectionBase {
        boolean isMaster;
        BiConsumer<Integer, Long> receiveMoveCallback;
        Runnable gameStartFunction;
        Supplier<String> stateSupplier;
        Consumer<String> stateConsumer;
        Consumer<Long> pauseFunction;
        Runnable resumeFunction;

        ConnectionBase(boolean isMaster,
                       BiConsumer<Integer, Long> receiveMoveCallback,
                       Runnable gameStartFunction,
                       Supplier<String> stateSupplier,
                       Consumer<String> stateConsumer,
                       Consumer<Long> pauseFunction,
                       Runnable resumeFunction) {
            this.isMaster = isMaster;
            this.receiveMoveCallback = receiveMoveCallback;
            this.gameStartFunction = gameStartFunction;
            this.stateSupplier = stateSupplier;
            this.stateConsumer = stateConsumer;
            this.pauseFunction = pauseFunction;
            this.resumeFunction = resumeFunction;
        }

        abstract void sendMove(int playerID, long time);

        abstract void sendPause(long time);

        abstract void sendResume();

        abstract String receiveOneLine();

        void mainLoop() {
            while (true) {
                String msg = this.receiveOneLine();
                System.err.println(msg);
                String[] args = msg.split(",");
                switch (args[0]) {
                    case "MOVE":
                        this.receiveMoveCallback.accept(Integer.valueOf(args[1]), Long.valueOf(args[2]));
                        break;
                    case "PAUSE":
                        this.pauseFunction.accept(Long.valueOf(args[1]));
                        break;
                    case "RESUME":
                        this.resumeFunction.run();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public static class TCPServer extends ConnectionBase {
        ServerSocket serverSocket;
        Socket socket;
        BufferedReader in;
        PrintWriter out;
        int port;

        TCPServer(int port,
                  BiConsumer<Integer, Long> receiveMoveCallback,
                  Runnable gameStartFunction,
                  Supplier<String> stateSupplier,
                  Consumer<String> stateConsumer,
                  Consumer<Long> pauseFunction,
                  Runnable resumeFunction) {
            super(
                    true,
                    receiveMoveCallback,
                    gameStartFunction,
                    stateSupplier,
                    stateConsumer,
                    pauseFunction,
                    resumeFunction
            );
            this.port = port;
            new Thread(() -> {
                this.waitForUp();
                String state = this.stateSupplier.get();
                this.out.println(state);
                String answer = FP.liftExp(() -> this.in.readLine()).get().get();
                if (answer.equals("START")) {
                    this.gameStartFunction.run();
                    System.err.println("start");
                    this.mainLoop();
                } else {
                    System.exit(-1);
                }
            }).start();
        }

        @Override
        void sendMove(int playerID, long time) {
            this.out.println("MOVE," + playerID + "," + time);
        }

        @Override
        void sendPause(long time) {
            this.out.println("PAUSE," + time);
        }

        @Override
        void sendResume() {
            this.out.println("RESUME");
        }

        @Override
        String receiveOneLine() {
            return FP.liftExp(() -> this.in.readLine()).get().get();
        }

        private void waitForUp() {
            try {
                this.serverSocket = new ServerSocket(this.port);
                this.socket = this.serverSocket.accept();
                this.out = new PrintWriter(this.socket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            } catch (IOException e) {
                System.exit(-1);
            }
        }
    }

    public static class TCPClient extends ConnectionBase {
        Socket socket;
        BufferedReader in;
        PrintWriter out;
        String addr;
        int port;

        TCPClient(
                String addr,
                int port,
                BiConsumer<Integer, Long> receiveMoveCallback,
                Runnable gameStartFunction,
                Supplier<String> stateSupplier,
                Consumer<String> stateConsumer,
                Consumer<Long> pauseFunction,
                Runnable resumeFunction) {
            super(
                    false,
                    receiveMoveCallback,
                    gameStartFunction,
                    stateSupplier,
                    stateConsumer,
                    pauseFunction,
                    resumeFunction
            );
            this.addr = addr;
            this.port = port;
            new Thread(() -> {
                this.waitForUp();
                String state = this.receiveOneLine();
                this.stateConsumer.accept(state);
                this.out.println("START");
                this.gameStartFunction.run();
                this.mainLoop();
            }).start();
        }

        @Override
        void sendMove(int playerID, long time) {
            this.out.println("MOVE," + playerID + "," + time);
        }

        @Override
        void sendPause(long time) {
            this.out.println("PAUSE," + time);
        }

        @Override
        void sendResume() {
            this.out.println("RESUME");
        }

        @Override
        String receiveOneLine() {
            return FP.liftExp(() -> this.in.readLine()).get().get();
        }

        private void waitForUp() {
            while (true) {
                try {
                    this.socket = new Socket(this.addr, this.port);
                    break;
                } catch (IOException e) {
                    FP.liftExp(() -> Thread.sleep(100)).run();
                }
            }
            try {
                this.out = new PrintWriter(this.socket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            } catch (IOException e) {
                System.exit(-1);
            }
        }
    }
}
