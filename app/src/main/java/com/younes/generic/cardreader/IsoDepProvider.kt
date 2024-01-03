package com.younes.generic.cardreader

import android.nfc.tech.IsoDep
import com.github.devnied.emvnfccard.parser.IProvider

/**
 * IProvider implementation using IsoDep as a communication channel
 */
class IsoDepProvider(private val isoDep: IsoDep) : IProvider {

    override fun transceive(pCommand: ByteArray?): ByteArray {
        return isoDep.transceive(pCommand)
    }

    /**
     * This method is not called if your EMV template config has readAt set to false
     */
    override fun getAt(): ByteArray {
        return byteArrayOf()
    }
}