/**
 * Copyright (c) 2012, 2013 SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.nbi.onecontrol;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.Notify;
import org.tmforum.mtop.fmw.xsd.notmsg.v1.Notify.Message;
import org.tmforum.mtop.sb.xsd.soc.v1.ObjectFactory;
import org.tmforum.mtop.sb.xsd.soc.v1.ServiceObjectCreationType;


public class NotificationConsumerHttpTest {

  private static final ObjectFactory OBJECT_FACTORY = new org.tmforum.mtop.sb.xsd.soc.v1.ObjectFactory();

  @Test
  public void should_limit_number_of_saved_notifications() {
    NotificationConsumerHttp subject = new NotificationConsumerHttp();
    for (int i = 0; i < NotificationConsumerHttp.NOTIFICATION_LIMIT + 5; ++i) {
      @SuppressWarnings("unchecked")
      Notify notification = new Notify().withMessage(new Message().withCommonEventInformation(
          OBJECT_FACTORY.createServiceObjectCreation(new ServiceObjectCreationType().withNotificationId("" + i))));
      subject.notify(null, notification);
    }

    assertThat(subject.getServiceObjectCreations(), hasSize(NotificationConsumerHttp.NOTIFICATION_LIMIT));
    assertThat(subject.getServiceObjectCreations().get(0), Matchers.is(new ServiceObjectCreationType().withNotificationId("5")));
    assertThat(
        subject.getServiceObjectCreations().get(NotificationConsumerHttp.NOTIFICATION_LIMIT - 1),
        is(new ServiceObjectCreationType().withNotificationId(String.valueOf(NotificationConsumerHttp.NOTIFICATION_LIMIT + 5 - 1))));
  }
}
