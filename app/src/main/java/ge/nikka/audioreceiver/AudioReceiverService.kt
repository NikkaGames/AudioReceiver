package ge.nikka.audioreceiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.IBinder
import android.os.Process
import androidx.core.app.NotificationCompat
import java.net.DatagramPacket
import java.net.DatagramSocket

class AudioReceiverService : Service() {
    private var thread: Thread? = null
    private var socket: DatagramSocket? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notif = NotificationCompat.Builder(this, "audio_receiver")
            .setContentTitle("Audio Receiver")
            .setContentText("Receiving audio stream…")
            .setOngoing(true)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notif)
        startReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        socket?.close()
        thread?.interrupt()
        thread = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    private fun createChannel() {
        val ch = NotificationChannel(
            "audio_receiver",
            "Audio Receiver",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
    }

    private fun startReceiver() {
        thread = object : Thread() {
            override fun run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
                val opus = Opus()
                opus.decoderInit(Constants.SampleRate._48000(), Constants.Channels.stereo())
                socket = DatagramSocket(8888)
                socket?.receiveBufferSize = 65536
                val audioTrack = AudioTrack.Builder()
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setSampleRate(48000)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                            .build()
                    )
                    .setBufferSizeInBytes(2048)
                    .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
                audioTrack.play()
                val queue = ArrayDeque<ByteArray>()
                val buf = ByteArray(1024)
                val packet = DatagramPacket(buf, buf.size)
                val frameNs = 2_500_000L
                var next = System.nanoTime()
                try {
                    while (!interrupted()) {
                        if (queue.size < 2) {
                            try {
                                socket?.receive(packet)
                            } catch (_: Exception) {
                                break
                            }
                            queue.add(buf.copyOf(packet.length))
                            continue
                        }
                        val now = System.nanoTime()
                        val diff = next - now
                        if (diff > 0) {
                            try {
                                sleep(diff / 1_000_000, (diff % 1_000_000).toInt())
                            } catch (_: InterruptedException) {
                                break
                            }
                        }
                        next += frameNs
                        if (next < now - 500_000) next = now
                        val frame = queue.removeFirstOrNull() ?: continue
                        val decoded = opus.decode(frame, Constants.FrameSize._120()) ?: continue
                        val pcm = opus.convert(decoded) ?: continue
                        audioTrack.write(pcm, 0, pcm.size)
                    }
                } finally {
                    audioTrack.stop()
                    audioTrack.release()
                    opus.decoderRelease()
                    socket?.close()
                }
            }
        }
        thread?.start()
    }
}
