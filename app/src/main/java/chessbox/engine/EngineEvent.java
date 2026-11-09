package chessbox.engine;

import java.util.UUID;

public sealed interface EngineEvent {

    record PoolCreated(
        UUID poolId,
        String engineName,
        int size
    ) implements EngineEvent {}

    record PoolDestroyed(
        UUID poolId
    ) implements EngineEvent {}

    record EngineFailed(
        String engineId,
        Throwable cause
    ) implements EngineEvent {}
}
