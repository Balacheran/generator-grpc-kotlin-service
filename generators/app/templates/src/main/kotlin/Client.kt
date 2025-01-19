package <%= packageName %>

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.runBlocking
import java.io.Closeable
import java.util.concurrent.TimeUnit

class GrpcClient(private val channel: ManagedChannel) : Closeable {
    private val stub = GreeterServiceGrpcKt.GreeterServiceCoroutineStub(channel)

    suspend fun greet(name: String) {
        val request = HelloRequest.newBuilder()
            .setName(name)
            .build()
        try {
            val response = stub.sayHello(request)
            println("Server response: ${response.message}")
        } catch (e: Exception) {
            println("RPC failed: ${e.message}")
        }
    }

    override fun close() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }
}

fun main() = runBlocking {
    val port = 50051
    val channel = ManagedChannelBuilder.forAddress("localhost", port)
        .usePlaintext()
        .build()

    val client = GrpcClient(channel)
    client.greet("World")
    client.close()
}