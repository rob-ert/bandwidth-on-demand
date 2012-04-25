package nl.surfnet.bod.nbi;

import org.jasypt.intf.service.JasyptStatelessService;
import org.junit.Ignore;
import org.junit.Test;

public class EncryptionHelper {

  private static final String ALGO = "PBEWithMD5AndTripleDES";
  private static final String ENCRYPTION_PASSWORD = ""; // TO FILL

  @Test
  @Ignore("only used for quick decryption, not a real test")
  public void decryptAPassword() {
    final JasyptStatelessService service = new JasyptStatelessService();

    String input = "";

    String result = service.decrypt(input, ENCRYPTION_PASSWORD, null, null, ALGO, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null);

    System.err.println(result);
  }

  @Test
  @Ignore("only used for quick encryption, not a real test")
  public void encryptAPassword() {
    final JasyptStatelessService service = new JasyptStatelessService();

    String input = "";

    String result = service.encrypt(input, ENCRYPTION_PASSWORD, null, null, ALGO, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null);

    System.err.println(result);
  }

}
