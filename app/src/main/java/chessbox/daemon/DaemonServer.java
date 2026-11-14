
package chessbox.daemon;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

import com.fasterxml.jackson.databind.*;

import chessbox.daemon.handler.RequestHandlerRegistry;
import chessbox.protocol.req.DaemonRequest;
import chessbox.protocol.req.global.ShutdownDaemonRequest;
import chessbox.protocol.res.global.DaemonResponse;

public class DaemonServer implements AutoCloseable {
    private final ServerSocket server;
    private final ExecutorService workers;
    private final ObjectMapper mapper = new ObjectMapper();
    private final RequestDecoder decoder;
    private final RequestHandlerRegistry requestHandlers;

    public DaemonServer(int port, RequestRegistry requests, RequestHandlerRegistry requestHandlers) throws IOException {

        this.server = new ServerSocket();
        server.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), port));

        this.workers = Executors.newVirtualThreadPerTaskExecutor();

        this.requestHandlers = requestHandlers;
        this.decoder = new RequestDecoder(requests);
    }

    public void start() throws IOException {
        System.out.println("Chessbox daemon listening on " + server.getLocalPort());

        while (!server.isClosed()) {
            try {
                Socket client = server.accept();
                workers.submit(() -> handle(client));
            } catch (SocketException e) {
                if (server.isClosed()) break;
                throw e;
            }
        }
    }

    private void handle(Socket client) {
        try (client;
            var in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
            var out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8))
        ) {

            String line = in.readLine();
            if (line == null) return;

            DaemonRequest request = null;
            ResponseEnvelope<? extends DaemonResponse> response;

            try {
                request = decoder.decode(line);
                DaemonResponse result = requestHandlers.dispatch(request);
                response = ResponseEnvelope.ok(result);
            } 
            
            catch (IllegalArgumentException e) {
                response = ResponseEnvelope.fail(
                    "INVALID_REQUEST",
                    e.getMessage()
                );
            } 
            
            catch (Exception e) {
                System.err.println("Request failed: " + e);

                response = ResponseEnvelope.fail(
                    "INTERNAL_ERROR",
                    String.format("An internal error occurred Test %s", e.getMessage())
                );
            }

            out.write(mapper.writeValueAsString(response));
            out.newLine();
            out.flush();


            if (request != null && request instanceof ShutdownDaemonRequest) {
                stop();
            }

        } catch (IOException e) {
            System.err.println(
                "Client connection error: " + e.getMessage()
            );
        }
    }

    public void stop() throws IOException {
        server.close();
    }

    @Override
    public void close() throws IOException {
        server.close();
        workers.shutdown();
    }
}
