package nl.surfnet.bod;

import org.jasypt.intf.service.JasyptStatelessService
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor

object EncryptedString {

  val encryptor = {
    val password = sys.env.getOrElse("BOD_ENCRYPTION_PASSWORD", sys.error("Encryption password is not set"))

    val enc = new StandardPBEStringEncryptor()
    enc.setAlgorithm("PBEWithMD5AndTripleDES")
    enc.setPassword(password)
    enc
  }

  implicit def stringToEncryptedString(str: String) = new {

    def encrypt(): String = encryptor.encrypt(str)

    def decrypt(): String = encryptor.decrypt(str)
  }
}
