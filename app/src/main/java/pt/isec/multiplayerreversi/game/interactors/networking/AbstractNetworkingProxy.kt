package pt.isec.multiplayerreversi.game.interactors.networking

import android.util.JsonReader
import android.util.JsonWriter
import android.util.Log
import pt.isec.multiplayerreversi.App.Companion.OURTAG
import pt.isec.multiplayerreversi.game.interactors.JsonTypes
import java.io.*
import java.lang.Exception
import java.net.Socket
import java.util.concurrent.ArrayBlockingQueue
import kotlin.concurrent.thread

abstract class AbstractNetworkingProxy(private val socket: Socket) : Closeable {

    private val osw = OutputStreamWriter(BufferedOutputStream(socket.getOutputStream()))
    private val isr = InputStreamReader(BufferedInputStream(socket.getInputStream()))
    private var jsonWriter: JsonWriter = JsonWriter(osw)
    private var jsonReader: JsonReader = JsonReader(isr)

    data class RunnableBlock(val type: String, val block: (JsonWriter) -> Unit)

    private val queuedActions = ArrayBlockingQueue<RunnableBlock>(10)
    private lateinit var receivingThread: Thread
    private val senderThread: Thread

    @Volatile
    protected var shouldExit = false

    init {
        jsonWriter.beginArray().flush()
        jsonReader.beginArray()

        senderThread = thread {
            while (!shouldExit) {
                try {
                    val block = queuedActions.take()
                    sendThrough(block.type, block.block)
                } catch (e: InterruptedException) {
                    shouldExit = true
                } catch (e: IOException) {
                    this.close()
                    Log.e(OURTAG, "IOException, socket closed")
                } catch (e: Throwable) {
                    Log.e(OURTAG, "Exception", e)
                }
            }
            while (queuedActions.isNotEmpty()) {
                val block = queuedActions.take()
                sendThrough(block.type, block.block)
            }
        }
    }

    protected fun sendThrough(type: String, block: (jsonWriter: JsonWriter) -> Unit) {
        beginSendWithType(type)
        block(jsonWriter)
        endSend()
    }

    protected fun receiveThrough(block: (type: String, jsonReader: JsonReader) -> Boolean) {
        val type = beginReadAndGetType()
        val readSomething = block(type, jsonReader)
        if (!readSomething) jsonReader.skipValue()
        endRead()
    }

    protected fun queueJsonWrite(type: String, block: (jsonReader: JsonWriter) -> Unit) {
        queuedActions.put(RunnableBlock(type, block))
    }

    protected fun queueClose() {
        queuedActions.put(RunnableBlock(JsonTypes.Setup.DATA) {
            jsonWriter.nullValue()
            thread { close() }
        })
    }

    protected fun setReceiving(
        threadName: String, reader: (type: String, jsonReader: JsonReader) -> Boolean,
    ) {
        receivingThread = thread {
            try {
                while (!shouldExit)
                    receiveThrough(reader)
            } catch (e: InterruptedException) {
                shouldExit = true
            } catch (e: IOException) {
                Log.e(OURTAG, "Lost connection : IOException")
            } catch (e: Throwable) {
                Log.e(OURTAG, "Throwable in Thread with name : $threadName", e)
            }
        }
    }

    private fun beginSendWithType(type: String) {
        jsonWriter.beginObject()
        jsonWriter.name("type").value(type)
        jsonWriter.name(JsonTypes.Setup.DATA)
    }

    private fun beginReadAndGetType(): String {
        jsonReader.beginObject()
        jsonReader.nextName() // "type" :
        val type = jsonReader.nextString()
        jsonReader.nextName() // "data" :
        return type
    }

    private fun endSend() {
        jsonWriter.endObject()
        jsonWriter.flush()
    }

    private fun endRead() {
        jsonReader.endObject()
    }

    override fun close() {
        shouldExit = true
        try {
            osw.close()
            isr.close()
            socket.close()
        } catch (e: Exception) {
        }
    }
}