package chessbox.daemon;

import chessbox.protocol.req.DaemonRequest;
import chessbox.util.MappingRegistry;

public class RequestRegistry extends MappingRegistry<String, Class<? extends DaemonRequest>> {

}
