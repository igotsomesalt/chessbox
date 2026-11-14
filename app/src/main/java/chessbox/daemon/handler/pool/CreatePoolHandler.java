package chessbox.daemon.handler.pool;

import org.jspecify.annotations.NonNull;

import chessbox.daemon.DaemonMain;
import chessbox.daemon.handler.RequestHandler;
import chessbox.engine.EnginePool;
import chessbox.protocol.req.pool.CreatePoolRequest;
import chessbox.protocol.res.pool.CreatePoolResponse;

public class CreatePoolHandler implements RequestHandler<@NonNull CreatePoolRequest, CreatePoolResponse>  {

    @Override
    public Class<@NonNull CreatePoolRequest> id() {
        return CreatePoolRequest.class;
    }

    @Override
    public CreatePoolResponse handle(@NonNull CreatePoolRequest request) throws Exception {
        EnginePool pool = DaemonMain.ctx.engines().createPool(request.engineId(), request.threads());
        return new CreatePoolResponse(pool.id(), pool.engineName(), pool.size(), pool.available());
    }

}
