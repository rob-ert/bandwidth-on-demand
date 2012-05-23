/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
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
