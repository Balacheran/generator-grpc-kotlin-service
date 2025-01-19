package <%= packageName %>

import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver

class GreeterService : GreeterServiceGrpcKt.GreeterServiceCoroutineImplBase() {
    override suspend fun sayHello(request: HelloRequest): HelloResponse {
        return HelloResponse.newBuilder()
            .setMessage("Hello, ${request.name}!")
            .build()
    }
}

class GrpcServer(private val port: Int) {
    private val server: Server = ServerBuilder
        .forPort(port)
        .addService(GreeterService())
        .build()

    fun start() {
        server.start()
        println("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("*** shutting down gRPC server since JVM is shutting down")
                this@GrpcServer.stop()
                println("*** server shut down")
            }
        )
    }

    private fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }
}

fun main() {
    val port = 50051
    val server = GrpcServer(port)
    server.start()
    server.blockUntilShutdown()
}