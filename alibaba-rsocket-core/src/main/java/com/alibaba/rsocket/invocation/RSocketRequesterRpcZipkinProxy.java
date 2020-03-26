package com.alibaba.rsocket.invocation;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.propagation.TraceContext;
import com.alibaba.rsocket.metadata.RSocketMimeType;
import com.alibaba.rsocket.metadata.TracingMetadata;
import com.alibaba.rsocket.upstream.UpstreamCluster;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.rsocket.Payload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;


/**
 * RSocket requester RPC proxy for remote service with zipkin tracing support
 *
 * @author leijuan
 */
public class RSocketRequesterRpcZipkinProxy extends RSocketRequesterRpcProxy {
    private Tracer tracer;

    public RSocketRequesterRpcZipkinProxy(@NotNull Tracing tracing, UpstreamCluster upstream,
                                          String group, Class<?> serviceInterface, @Nullable String service, String version,
                                          RSocketMimeType encodingType, @Nullable RSocketMimeType acceptEncodingType,
                                          Duration timeout, @Nullable String endpoint) {
        super(upstream, group, serviceInterface, service, version, encodingType, acceptEncodingType, timeout, endpoint);
        tracer = tracing.tracer();
    }

    @NotNull
    protected Mono<Payload> remoteRequestResponse(ByteBuf compositeMetadata, ByteBuf bodyBuf) {
        return Mono.deferWithContext(context -> {
            TraceContext traceContext = context.getOrDefault(TraceContext.class, null);
            if (traceContext != null) {
                CompositeByteBuf newCompositeMetadata = new CompositeByteBuf(PooledByteBufAllocator.DEFAULT, true, 2, compositeMetadata, tracingMetadata(traceContext).getContent());
                Span span = tracer.newChild(traceContext);
                return super.remoteRequestResponse(newCompositeMetadata, bodyBuf)
                        .doOnError(span::error)
                        .doOnSuccess(payload -> span.finish());
            }
            return super.remoteRequestResponse(compositeMetadata, bodyBuf);
        });
    }

    @Override
    protected Mono<Void> remoteFireAndForget(ByteBuf compositeMetadata, ByteBuf bodyBuf) {
        return Mono.deferWithContext(context -> {
            TraceContext traceContext = context.getOrDefault(TraceContext.class, null);
            if (traceContext != null) {
                CompositeByteBuf newCompositeMetadata = new CompositeByteBuf(PooledByteBufAllocator.DEFAULT, true, 2, compositeMetadata, tracingMetadata(traceContext).getContent());
                Span span = tracer.newChild(traceContext);
                return super.remoteFireAndForget(newCompositeMetadata, bodyBuf)
                        .doOnError(span::error)
                        .doOnSuccess(payload -> span.finish());
            }
            return super.remoteFireAndForget(compositeMetadata, bodyBuf);
        });
    }

    @Override
    protected Flux<Payload> remoteRequestStream(ByteBuf compositeMetadata, ByteBuf bodyBuf) {
        return Flux.deferWithContext(context -> {
            TraceContext traceContext = context.getOrDefault(TraceContext.class, null);
            if (traceContext != null) {
                CompositeByteBuf newCompositeMetadata = new CompositeByteBuf(PooledByteBufAllocator.DEFAULT, true, 2, compositeMetadata, tracingMetadata(traceContext).getContent());
                Span span = tracer.newChild(traceContext);
                return super.remoteRequestStream(newCompositeMetadata, bodyBuf)
                        .doOnError(span::error)
                        .doOnComplete(span::finish);
            }
            return super.remoteRequestStream(compositeMetadata, bodyBuf);
        });
    }

    public TracingMetadata tracingMetadata(TraceContext traceContext) {
        return new TracingMetadata(traceContext.traceIdHigh(), traceContext.traceId(), traceContext.spanId(), traceContext.parentId());
    }
}