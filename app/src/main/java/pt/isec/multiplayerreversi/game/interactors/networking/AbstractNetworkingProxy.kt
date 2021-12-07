package pt.isec.multiplayerreversi.game.interactors.networking

import android.util.JsonReader
import android.util.JsonWriter
import android.util.Log
import pt.isec.multiplayerreversi.App.Companion.OURTAG
import pt.isec.multiplayerreversi.game.interactors.JsonTypes
import java.io.*
import java.net.Socket
import java.util.concurrent.ArrayBlockingQueue
import kotlin.concurrent.thread

abstract class AbstractNetworkingProxy(socket: Socket) : Closeable {

    private val osw = OutputStreamWriter(BufferedOutputStream(socket.getOutputStream()))
    private val isr = InputStreamReader(BufferedInputStream(socket.getInputStream()))
    private var jsonWriter: JsonWriter = JsonWriter(osw)
    private var jsonReader: JsonReader = JsonReader(isr)

    data class RunnableJSONBlock(val type: String, val block: (JsonWriter) -> Unit)

    private val queuedActions = ArrayBlockingQueue<RunnableJSONBlock>(10)
    private lateinit var receivingThread: Thread
    private val senderThread: Thread

    @Volatile
    protected var shouldExit = false

    init {
        jsonWriter.beginArray().flush()
        jsonReader.beginArray()

        senderThread = thread {
            while (!shouldExit) {
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

    protected fun writeJson(type: String, block: (jsonReader: JsonWriter) -> Unit) {
        queuedActions.put(RunnableJSONBlock(type, block))
    }

    protected fun setReceiving(
        threadName: String,
        reader: (type: String, jsonReader: JsonReader) -> Boolean,
    ) {
        receivingThread = thread {
            try {
                Log.i(OURTAG, "Started Thread with name : $threadName")
                while (!shouldExit) {
                    receiveThrough(reader)
                }
                Log.i(OURTAG, "Finished Thread with name : $threadName")
            } catch (e: InterruptedException) {
                Log.e(OURTAG, "Interrupted Thread with name : $threadName")
            } catch (e: Exception) {
                Log.e(OURTAG, "Exception in Thread with name : $threadName", e)
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

    private fun stopAllThreads() {
        senderThread.interrupt()
        receivingThread.interrupt()
    }

    override fun close() {
        synchronized(queuedActions) {
            shouldExit = true
            if (!queuedActions.isEmpty()) {
                senderThread.join()
            }
        }
        stopAllThreads()
        osw.close()
        isr.close()
        //TODO  quando se sai do jogo tem de fechar os sockets
    }
}