package chessbox.daemon.handler;

import chessbox.protocol.req.DaemonRequest;
import chessbox.protocol.res.global.DaemonResponse;
import chessbox.util.MappingRegistry;

public class RequestHandlerRegistry extends MappingRegistry<
    Class<? extends DaemonRequest>,
    RequestHandler<? extends DaemonRequest, ? extends DaemonResponse>
> {

    public <Q extends DaemonRequest> DaemonResponse dispatch(Q request)
        throws Exception {

        RequestHandler<?, ?> handler = get(request.getClass());

        if (handler == null) {
            throw new IllegalArgumentException(
                "No handler for: " + request.getClass().getName()
            );
        }

        return invoke(handler, request);
    }

    @SuppressWarnings("unchecked")
    private <Q extends DaemonRequest, R extends DaemonResponse>
    R invoke(RequestHandler<?, ?> handler, Q request) throws Exception {

        return ((RequestHandler<Q, R>) handler).handle(request);
    }
}
