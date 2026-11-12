
package chessbox.cli;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.*;

import chessbox.protocol.req.*;
import chessbox.protocol.res.*;

public class DaemonClient {
    private final ObjectMapper mapper = new ObjectMapper();
    private final String host;
    private final int port;

    public DaemonClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public <T extends DaemonResponse> T send(
        DaemonRequest request,
        Class<T> responseType
    ) throws IOException {

        String type = switch (request) {
            case CreatePoolRequest r -> "pool.create";
            case ListPoolsRequest r -> "pool.list";
        };

        try (Socket socket = new Socket()) {
            socket.connect(
                new InetSocketAddress(host, port),
                3000
            );
            socket.setSoTimeout(5000);

            var out = new BufferedWriter(
                new OutputStreamWriter(
                    socket.getOutputStream(),
                    StandardCharsets.UTF_8
                )
            );

            var in = new BufferedReader(
                new InputStreamReader(
                    socket.getInputStream(),
                    StandardCharsets.UTF_8
                )
            );

            var envelope = mapper.createObjectNode();
            envelope.put("type", type);
            envelope.set("payload", mapper.valueToTree(request));

            out.write(mapper.writeValueAsString(envelope));
            out.newLine();
            out.flush();

            String json = in.readLine();
            if (json == null) {
                throw new EOFException("Daemon closed connection");
            }

            JsonNode root = mapper.readTree(json);

            if (!root.path("success").asBoolean()) {
                JsonNode error = root.path("error");

                throw new DaemonException(
                    error.path("code").asText(),
                    error.path("message").asText()
                );
            }

            return mapper.treeToValue(
                root.path("data"),
                responseType
            );
        }
    }
}
