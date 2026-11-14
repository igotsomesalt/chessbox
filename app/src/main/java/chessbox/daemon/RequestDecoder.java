package chessbox.daemon;

import java.io.IOException;

import com.fasterxml.jackson.databind.*;

import chessbox.protocol.req.DaemonRequest;

public class RequestDecoder {

    private final RequestRegistry requests;
    private final ObjectMapper mapper = new ObjectMapper();

    public RequestDecoder(RequestRegistry requests) {
        this.requests = requests;
    }

    public DaemonRequest decode(String json) throws IOException {
        JsonNode root = mapper.readTree(json);

        String type = root.path("type").asText();
        JsonNode payload = root.path("payload");

        Class<? extends DaemonRequest> requestClass = requests.get(type);

        return mapper.treeToValue(payload, requestClass);
    }
}