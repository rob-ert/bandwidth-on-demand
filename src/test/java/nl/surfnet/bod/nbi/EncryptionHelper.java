/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
