package <%= packageName %>.interceptors

import io.grpc.*
import org.slf4j.LoggerFactory

class LoggingInterceptor : ServerInterceptor {
    private val logger = LoggerFactory.getLogger(LoggingInterceptor::class.java)

    override fun <ReqT : Any, RespT : Any> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        logger.info("Receiving call to ${call.methodDescriptor.fullMethodName}")
        return next.startCall(LoggingServerCall(call), headers)
    }

    private class LoggingServerCall<ReqT, RespT>(
        delegate: ServerCall<ReqT, RespT>
    ) : ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(delegate)
}