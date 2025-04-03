package com.waxd.pos.fcmb.datastore

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import com.waxd.pos.fcmb.app.FcmbApp
import java.lang.Exception
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec


object KeyStore {

    const val USER_TOKEN = "com.waxd.pos.fcmb.userAccessToken"
    const val USER_UID = "com.waxd.pos.fcmb.uid"
    private val tag = KeyStore::class.java.simpleName

    private fun getKeyStore(): java.security.KeyStore {
        val keystore = java.security.KeyStore.getInstance("AndroidKeyStore")
        keystore.load(null)
        return keystore
    }

    fun generateKey(key: String): SecretKey {
        val keyGenerator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            key,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(false)
            .build()
        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    private fun generateIv(): ByteArray {
        val iv = ByteArray(12)
        SecureRandom().nextBytes(iv)
        return iv
    }

    fun Context.encryptData(key: String, data: String) {
        val secretKey = getKeyStore().getKey(key, null) as SecretKey?
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = generateIv()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        val encryptedData = cipher.doFinal(data.toByteArray())
        saveEncryptedData(encryptedData, key, iv)
    }

    fun Context.decryptData(key: String): String {
        val encryptedData = getEncryptedData(Pair(key, "$key.iv"))
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = getKeyStore().getKey(key, null) as SecretKey? ?: return ""
        if (encryptedData.second == null) return ""
        return try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, encryptedData.second))
            val decryptedData = cipher.doFinal(encryptedData.first)
            String(decryptedData)
        } catch (e: AEADBadTagException) {
            Log.e("DecryptionError", "Authentication tag mismatch", e)
            ""
        } catch (e: Exception) {
            Log.e("DecryptionError", "Decryption failed", e)
            ""
        }
    }

    private fun Context.saveEncryptedData(encryptedData: ByteArray, key: String, iv: ByteArray) {
        val sharedPrefs = getSharedPreferences(
            "${FcmbApp.instance.packageName}.EncryptedDataPrefs",
            Context.MODE_PRIVATE
        )
        with(sharedPrefs.edit()) {
            putString(key, Base64.encodeToString(encryptedData, Base64.DEFAULT))
            putString("$key.iv", Base64.encodeToString(iv, Base64.DEFAULT))
            apply()
        }
    }

    private fun Context.getEncryptedData(pair: Pair<String, String>): Pair<ByteArray?, ByteArray?> {
        val sharedPrefs = getSharedPreferences(
            "${FcmbApp.instance.packageName}.EncryptedDataPrefs",
            Context.MODE_PRIVATE
        )
        val encryptedDataString = sharedPrefs.getString(pair.first, null)
        val encryptedIvString = sharedPrefs.getString(pair.second, null)
        val dataByte = encryptedDataString?.let {
            Base64.decode(it, Base64.DEFAULT)
        }

        val ivByte = encryptedIvString?.let {
            Base64.decode(it, Base64.DEFAULT)
        }
        return Pair(dataByte, ivByte)
    }

    fun deleteAllKeys() {
        try {
            val aliases = getKeyStore().aliases()
            while (aliases.hasMoreElements()) {
                val alias = aliases.nextElement()
                getKeyStore().deleteEntry(alias)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}