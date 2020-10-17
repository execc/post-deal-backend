package com.wavesplatform.we.app.deals.service

import java.lang.IllegalStateException
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.Signature
import java.security.interfaces.ECPrivateKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECPrivateKeySpec
import org.postgresql.util.Base64
import org.springframework.stereotype.Service

const val KEY_ECDSA = "ECDSA"
const val SIG_ECDSA = "SHA256withECDSA"

@Service
class CryptoService {

    fun loadPrivateKey(privateKeyString: String, curveName: String): ECPrivateKey {
        val s = BigInteger(privateKeyString, 16)
        val ecParameterSpec = ECNamedCurveTable.getParameterSpec(curveName)
        val privateKeySpec = ECPrivateKeySpec(s, ecParameterSpec)
        return try {
            val keyFactory = KeyFactory.getInstance(KEY_ECDSA, BouncyCastleProvider())
            keyFactory.generatePrivate(privateKeySpec)
        } catch (e: NoSuchAlgorithmException) {
            throw IllegalStateException("NoSuchAlgorithm", e)
        } catch (e: InvalidKeySpecException) {
            throw IllegalStateException("InvalidKeySpec", e)
        } as ECPrivateKey
    }

    fun verify(signature: String, data: String, publicKey: String) =
            verify(
                signature = Base64.decode(signature),
                data = data.toByteArray(),
                key = Base64.decode(publicKey),
                curveName = "secp256k1"
            )

    fun verify(signature: ByteArray, data: ByteArray, key: ByteArray, curveName: String): Boolean {
        val keyFactory = KeyFactory.getInstance(KEY_ECDSA, BouncyCastleProvider())
        val publicKey = keyFactory.generatePublic(X509EncodedKeySpec(key)) as PublicKey
        val sig = Signature.getInstance(SIG_ECDSA, BouncyCastleProvider())
        sig.initVerify(publicKey)
        sig.update(data)
        return sig.verify(signature)
    }

    fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = input.toByteArray(StandardCharsets.UTF_8)
        return bytesToHex(digest.digest(bytes))
    }

    private fun bytesToHex(hash: ByteArray): String {
        val hexString = StringBuffer()
        for (i in hash.indices) {
            val hex = Integer.toHexString(0xff and hash[i].toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }
        return hexString.toString()
    }
}
