package com.paymentchain.apigateway.filter;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class ApiDocsRewriteWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path != null && path.startsWith("/v3/api-docs")) {
            ServerHttpResponse originalResponse = exchange.getResponse();
            HttpHeaders headers = originalResponse.getHeaders();
            // only handle json responses
            return chain.filter(exchange.mutate().response(new ServerHttpResponseDecorator(originalResponse) {
                @Override
                public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                    MediaType contentType = headers.getContentType();
                    if (contentType != null && contentType.includes(MediaType.APPLICATION_JSON)) {
                        Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                        return super.writeWith(
                                fluxBody.collectList().flatMapMany(dataBuffers -> {
                                    StringBuilder sb = new StringBuilder();
                                    dataBuffers.forEach(db -> {
                                        byte[] bytes = new byte[db.readableByteCount()];
                                        db.read(bytes);
                                        DataBufferUtils.release(db);
                                        sb.append(new String(bytes, StandardCharsets.UTF_8));
                                    });
                                    String original = sb.toString();
                                    // Replace any "servers" array with a single relative server
                                    String replaced = original.replaceAll("\\\"servers\\\"\\s*:\\s*\\[.*?\\]", "\"servers\":[{\"url\":\"/\"}]");
                                    byte[] bytes = replaced.getBytes(StandardCharsets.UTF_8);
                                    DataBuffer buffer = bufferFactory().wrap(bytes);
                                    return Flux.just(buffer);
                                })
                        );
                    }
                    return super.writeWith(body);
                }

                @Override
                public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
                    return writeWith(Flux.from(body).flatMap(p -> p));
                }
            }).build()) ;
        }
        return chain.filter(exchange);
    }
}
