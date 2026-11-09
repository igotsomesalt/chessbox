package chessbox.cli.listener;

import java.io.PrintStream;
import java.util.function.Consumer;

import chessbox.engine.EngineEvent;

public class EngineEventPrinter implements Consumer<EngineEvent> {

    private final PrintStream out;
    private final PrintStream err;

    public EngineEventPrinter(PrintStream out, PrintStream err) {
        this.out = out;
        this.err = err;
    }


    @Override
    public synchronized void accept(EngineEvent event) {
        switch (event) {
            case EngineEvent.PoolCreated e -> out.printf("""
                Created pool:
                    %-8s [%s]
                    %-8s [%s] 
                    %-8s [%d]
                """.replace("\n", System.lineSeparator()), 
                "id", e.poolId(), "engine", e.engineName(), "size", e.size()
            );

            case EngineEvent.PoolDestroyed e -> out.printf("Removed pool %s%n", e.poolId());
            case EngineEvent.EngineFailed e -> err.printf("Engine failed: %s%n", e.cause().getMessage());
        }
    }

}
