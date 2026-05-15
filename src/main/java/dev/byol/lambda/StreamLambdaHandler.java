package dev.byol.lambda;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;

import dev.byol.Application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamLambdaHandler implements RequestStreamHandler {

    private static final SpringBootLambdaContainerHandler<HttpApiV2ProxyRequest, ?> handler;

    static {
        try {
            handler = SpringBootLambdaContainerHandler.getHttpApiV2ProxyHandler(Application.class);
        } catch (ContainerInitializationException e) {
            throw new RuntimeException("Unable to load Spring Boot application", e);
        }
    }

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        handler.proxyStream(input, output, context);
    }
}
