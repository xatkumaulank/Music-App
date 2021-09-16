package com.naman14.timber.utils

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or

/**
 * Created by Christoph Walcher on 03.12.16.
 */
object LyricsExtractor {
    fun getLyrics(file: File): String? {
        val filename = file.name
        val fileending = filename.substring(filename.lastIndexOf('.') + 1, filename.length).toLowerCase()
        try {
            when (fileending) {
                "mp3" -> return getLyricsID3(file)
                "mp4", "m4a", "aac" -> return getLyricsMP4(file)
                "ogg", "oga" -> return getLyricsVorbis(file)
            }
        } catch (e: Exception) {
        }
        return null
    }

    @Throws(IOException::class)
    private fun readOgg(buf: ByteArray?, `in`: InputStream, bytesinpage: Int, skip: Int): Int {
        var bytesinpage = bytesinpage
        var toread = if (skip != -1) skip else buf!!.size
        var offset = 0
        while (toread > 0) {
            if (bytesinpage == 0) {
                val magic = ByteArray(4)
                `in`.read(magic)
                if (!Arrays.equals(magic, byteArrayOf('O'.toByte(), 'g'.toByte(), 'g'.toByte(), 'S'.toByte()))) {
                    `in`.close()
                    throw IOException()
                }
                val header = ByteArray(23)
                `in`.read(header)
                var count: Int = (header[22] and 0xFF.toByte()).toInt()
                while (count-- > 0) {
                    bytesinpage += `in`.read()
                }
            }
            var read = toread
            if (bytesinpage - toread < 0) read = bytesinpage
            if (skip != -1) `in`.skip(read.toLong()) else `in`.read(buf, offset, read)
            offset += read
            toread -= read
            bytesinpage -= read
        }
        return bytesinpage
    }

    @Throws(Exception::class)
    private fun getLyricsVorbis(file: File): String? {
        val `in` = FileInputStream(file)
        var bytesinpage = 0
        val buffer = ByteArray(7)
        bytesinpage = readOgg(buffer, `in`, bytesinpage, -1)
        if (!Arrays.equals(buffer, byteArrayOf(1, 'v'.toByte(), 'o'.toByte(), 'r'.toByte(), 'b'.toByte(), 'i'.toByte(), 's'.toByte()))) {
            `in`.close()
            return null
        }
        bytesinpage = readOgg(null, `in`, bytesinpage, 23)
        bytesinpage = readOgg(buffer, `in`, bytesinpage, -1)
        if (!Arrays.equals(buffer, byteArrayOf(3, 'v'.toByte(), 'o'.toByte(), 'r'.toByte(), 'b'.toByte(), 'i'.toByte(), 's'.toByte()))) {
            `in`.close()
            return null
        }
        val length = ByteArray(4)
        bytesinpage = readOgg(length, `in`, bytesinpage, -1)
        bytesinpage = readOgg(null, `in`, bytesinpage, byteArrayToInt(length))
        bytesinpage = readOgg(length, `in`, bytesinpage, -1)
        var count = byteArrayToIntLE(length)
        while (count-- > 0) {
            bytesinpage = readOgg(length, `in`, bytesinpage, -1)
            val comment_len = byteArrayToIntLE(length)
            val lyrics_tag = byteArrayOf('L'.toByte(), 'Y'.toByte(), 'R'.toByte(), 'I'.toByte(), 'C'.toByte(), 'S'.toByte(), '='.toByte())
            if (comment_len <= lyrics_tag.size) {
                bytesinpage = readOgg(null, `in`, bytesinpage, comment_len)
                continue
            }
            val comment_probe = ByteArray(lyrics_tag.size)
            bytesinpage = readOgg(comment_probe, `in`, bytesinpage, -1)
            bytesinpage = if (Arrays.equals(comment_probe, lyrics_tag)) {
                val lyrics = ByteArray(comment_len - lyrics_tag.size)
                readOgg(lyrics, `in`, bytesinpage, -1)
                `in`.close()
                return String(lyrics)
            } else {
                readOgg(null, `in`, bytesinpage, comment_len - lyrics_tag.size)
            }
        }
        `in`.close()
        return null
    }

    @Throws(Exception::class)
    private fun getLyricsMP4(file: File): String? {
        val `in` = FileInputStream(file)
        val head = ByteArray(4)
        `in`.read(head)
        var len = byteArrayToInt(head)
        `in`.read(head)
        if (!Arrays.equals(head, byteArrayOf('f'.toByte(), 't'.toByte(), 'y'.toByte(), 'p'.toByte()))) {
            `in`.close()
            return null
        }
        `in`.skip((len - 8).toLong())
        val path = arrayOf(byteArrayOf('m'.toByte(), 'o'.toByte(), 'o'.toByte(), 'v'.toByte()), byteArrayOf('u'.toByte(), 'd'.toByte(), 't'.toByte(), 'a'.toByte()), byteArrayOf('m'.toByte(), 'e'.toByte(), 't'.toByte(), 'a'.toByte()), byteArrayOf('i'.toByte(), 'l'.toByte(), 's'.toByte(), 't'.toByte()), byteArrayOf('©'.toByte(), 'l'.toByte(), 'y'.toByte(), 'r'.toByte()), byteArrayOf('d'.toByte(), 'a'.toByte(), 't'.toByte(), 'a'.toByte()))
        var atom_size = Int.MAX_VALUE
        outter@ for (atom in path) {
            while (`in`.available() > 0) {
                val buffer = ByteArray(4)
                `in`.read(buffer)
                len = byteArrayToInt(buffer)
                if (len == 0) continue
                `in`.read(buffer)
                if (len > atom_size) {
                    `in`.close()
                    return null
                }
                if (Arrays.equals(buffer, atom)) {
                    atom_size = len - 8
                    //Found Atom search next atom
                    continue@outter
                } else {
                    //Skip Atom
                    `in`.skip((len - 8).toLong())
                    atom_size -= len
                }
            }
            `in`.close()
            return null
        }
        `in`.skip(8)
        val buffer = ByteArray(atom_size - 8)
        `in`.read(buffer)
        `in`.close()
        return String(buffer)
    }

    @Throws(Exception::class)
    private fun getLyricsID3(file: File): String? {
        val `in` = FileInputStream(file)
        val buffer = ByteArray(4)
        `in`.read(buffer, 0, 3)
        if (!Arrays.equals(buffer, byteArrayOf('I'.toByte(), 'D'.toByte(), '3'.toByte(), 0))) {
            `in`.close()
            return null
        }
        `in`.read(buffer, 0, 3)
        val ext = (buffer[2].toInt() and 32) != 0
        `in`.read(buffer)
        var len: Int = (buffer[3] and 0x7F or (buffer[2] and 0x7F shl 7) or (buffer[1] and 0x7F shl 14) or (buffer[0] and 0x7F shl 21)).toInt()
        if (ext) {
            `in`.read(buffer)
            len -= 4
            val ext_len = byteArrayToInt(buffer)
            `in`.skip(ext_len.toLong())
            len -= ext_len
        }
        while (len > 0) {
            val tag_name = ByteArray(4)
            `in`.read(tag_name)
            len -= 4
            if (tag_name[0].toInt() == 0) break
            `in`.read(buffer)
            len -= 4
            var tag_len = byteArrayToInt(buffer)
            `in`.read(buffer, 0, 2)
            len -= 2
            if (Arrays.equals(tag_name, byteArrayOf('U'.toByte(), 'S'.toByte(), 'L'.toByte(), 'T'.toByte()))) {
                val head = ByteArray(4)
                `in`.read(head)
                len -= 4
                tag_len -= 4
                while (`in`.read() != 0) {
                    len--
                    tag_len--
                }
                if (head[0].toInt() == 1) `in`.read()
                val tag_value = ByteArray(tag_len)
                `in`.read(tag_value)
                len -= tag_len
                `in`.close()
                var charset: Charset? = null
                charset = when (head[0].toInt()) {
                    0 -> Charset.forName("ISO-8859-1")
                    1 -> Charset.forName("UTF-16")
                    2 -> Charset.forName("UTF-16BE")
                    3 -> Charset.forName("UTF-8")
                    else -> return null
                }
                return String(tag_value, charset)
            } else {
                `in`.skip(tag_len.toLong())
                len -= tag_len
            }
        }
        `in`.close()
        return null
    }

    private fun byteArrayToInt(b: ByteArray): Int {
        return (b[3] and 0xFF.toByte() or (b[2] and 0xFF.toByte() shl 8) or (b[1] and 0xFF.toByte() shl 16) or (b[0] and 0xFF.toByte() shl 24)).toInt()
    }

    private fun byteArrayToIntLE(b: ByteArray): Int {
        return (b[0] and 0xFF.toByte() or (b[1] and 0xFF.toByte() shl 8) or (b[2] and 0xFF.toByte() shl 16) or (b[3] and 0xFF.toByte() shl 24)).toInt()
    }
}