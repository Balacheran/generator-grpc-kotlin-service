package <%= packageName %>

import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat

class GreeterServiceTest {
    private val serverName = InProcessServerBuilder.generateName()
    private val server = InProcessServerBuilder
        .forName(serverName)
        .directExecutor()
        .addService(GreeterServiceImpl())
        .build()
    private val channel = InProcessChannelBuilder
        .forName(serverName)
        .directExecutor()
        .build()
    private val client = GrpcClient(channel)

    @BeforeEach
    fun setup() {
        server.start()
    }

    @AfterEach
    fun teardown() {
        channel.shutdown()
        server.shutdown()
    }

    @Test
    fun `should return hello message`() = runBlocking {
        // when
        val response = client.greet("Test")

        // then
        assertThat(response).isEqualTo("Hello, Test!")
    }
}