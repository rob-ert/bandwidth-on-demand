package nl.surfnet.bod;

import org.jasypt.intf.service.JasyptStatelessService

object EncryptedString {

  implicit def stringToEncryptedString(str: String) = new {

    val service = new JasyptStatelessService();
    val password = sys.env.getOrElse("BOD_ENCRYPTION_PASSWORD", sys.error("Encryption password is not set"))
    val algo = "PBEWithMD5AndTripleDES";

    def encrypt(): String = service.encrypt(str, password, null, null, algo, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null);

    def decrypt(): String = service.decrypt(str, password, null, null, algo, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null);
  }
}
