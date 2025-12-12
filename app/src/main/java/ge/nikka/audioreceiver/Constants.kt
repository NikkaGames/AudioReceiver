package ge.nikka.audioreceiver

import androidx.annotation.IntRange

object Constants {

    class SampleRate private constructor(val v: Int) {
        companion object {
            fun _8000() = SampleRate(8000)
            fun _12000() = SampleRate(12000)
            fun _16000() = SampleRate(16000)
            fun _24000() = SampleRate(24000)
            fun _48000() = SampleRate(48000)
        }
    }

    class Channels private constructor(val v: Int) {
        companion object {
            fun mono() = Channels(1)
            fun stereo() = Channels(2)
        }
    }

    class Application private constructor(val v: Int) {
        companion object {
            fun voip() = Application(2048)
            fun audio() = Application(2049)
            fun lowdelay() = Application(2051)
        }
    }

    class Complexity private constructor(val v: Int) {
        companion object {
            fun instance(@IntRange(from = 0, to = 10) value: Int): Complexity {
                if (value < 0) return Complexity(0)
                if (value > 10) return Complexity(10)
                return Complexity(value)
            }
        }
    }

    class Bitrate private constructor(val v: Int) {
        companion object {
            fun instance(@IntRange(from = 500, to = 512000) value: Int): Bitrate {
                if (value < 500) return Bitrate(500)
                if (value > 512000) return Bitrate(512000)
                return Bitrate(value)
            }
            fun auto(): Bitrate {
                return Bitrate(-1000)
            }
            fun max(): Bitrate {
                return Bitrate(-1)
            }
        }
    }

    class FrameSize private constructor(val v: Int) {
        companion object {
            fun _120() = FrameSize(120)
            fun _160() = FrameSize(160)
            fun _240() = FrameSize(240)
            fun _320() = FrameSize(320)
            fun _480() = FrameSize(480)
            fun _640() = FrameSize(640)
            fun _960() = FrameSize(960)
            fun _1280() = FrameSize(1280)
            fun _1920() = FrameSize(1920)
            fun _2560() = FrameSize(2560)
            fun _2880() = FrameSize(2880)
            fun _custom(value: Int) = FrameSize(value)
            fun fromValue(value: Int): FrameSize {
                return when(value) {
                    120 -> _120()
                    160 -> _160()
                    240 -> _240()
                    320 -> _320()
                    480 -> _480()
                    640 -> _640()
                    960 -> _960()
                    1280 -> _1280()
                    1920 -> _1920()
                    2560 -> _2560()
                    2880 -> _2880()
                    else -> _custom(value)
                }
            }
        }
    }
}