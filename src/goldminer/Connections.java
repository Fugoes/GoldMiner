package goldminer;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * master: send state
 */
public class Connections {
    public abstract class ConnectionBase {
        boolean isMaster;
        BiConsumer<Integer, Long> receiveMoveCallback;
        Runnable gameStartFunction;
        Supplier<String> stateSupplier;
        Consumer<String> stateConsumer;

        ConnectionBase(boolean isMaster,
                       BiConsumer<Integer, Long> receiveMoveCallback,
                       Runnable gameStartFunction,
                       Supplier<String> stateSupplier,
                       Consumer<String> stateConsumer) {
            this.isMaster = isMaster;
            this.receiveMoveCallback = receiveMoveCallback;
            this.gameStartFunction = gameStartFunction;
            this.stateSupplier = stateSupplier;
            this.stateConsumer = stateConsumer;
        }

        abstract void sendMove(int playerID, long time);
    }

    public class TCPServer extends ConnectionBase {
        ServerSocket serverSocket;
        Socket socket;

        TCPServer(BiConsumer<Integer, Long> receiveMoveCallback,
                  Runnable gameStartFunction,
                  Supplier<String> stateSupplier,
                  Consumer<String> stateConsumer) {
            super(true, receiveMoveCallback, gameStartFunction, stateSupplier, stateConsumer);
        }

        @Override
        void sendMove(int playerID, long time) {
        }
    }
}
