package chessbox.daemon.handler;

import chessbox.protocol.req.DaemonRequest;
import chessbox.protocol.res.global.DaemonResponse;
import chessbox.util.Indentifiable;

public interface RequestHandler<Q extends DaemonRequest, R extends DaemonResponse> extends Indentifiable<Class<Q>> {
    R handle(Q request) throws Exception;
}