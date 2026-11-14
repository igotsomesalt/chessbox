package chessbox.daemon.handler.pool;

import java.util.Collection;

import org.jspecify.annotations.NonNull;

import chessbox.daemon.DaemonMain;
import chessbox.daemon.handler.RequestHandler;
import chessbox.engine.EnginePool;
import chessbox.protocol.req.pool.ListPoolsRequest;
import chessbox.protocol.res.pool.ListPoolsResponse;

public class ListPoolsHandler implements RequestHandler<@NonNull ListPoolsRequest, ListPoolsResponse> {

    @Override
    public Class<@NonNull ListPoolsRequest> id() {
        return ListPoolsRequest.class;
    }

    @Override
    public ListPoolsResponse handle(@NonNull ListPoolsRequest request) throws Exception {
        Collection<EnginePool> pools = DaemonMain.ctx.engines().listEnginePools();
        return new ListPoolsResponse(
            pools.stream()
                .map(p -> new ListPoolsResponse.Pool(p.id(), p.engineName(), p.size(), p.available()))
                .toList()
        );
    }

}
