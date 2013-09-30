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
package nl.surfnet.bod.nsi.v2;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

@RunWith(MockitoJUnitRunner.class)
public class TransactionSoapHandlerTest {
  @Mock
  private PlatformTransactionManager transactionManager;

  @Mock
  private SOAPMessageContext context;

  @InjectMocks
  private TransactionSoapHandler subject = new TransactionSoapHandler();

  private SimpleTransactionStatus transactionStatus = new SimpleTransactionStatus();

  @Test
  public void shouldStartTransactionOnInboundMessage() {
    when(context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).thenReturn(false);
    when(transactionManager.getTransaction(null)).thenReturn(transactionStatus);

    assertThat(subject.handleMessage(context), is(true));

    verify(transactionManager).getTransaction(null);
    verify(context).put(TransactionSoapHandler.TRANSACTION_PROPERTY, transactionStatus);
    verify(context).setScope(TransactionSoapHandler.TRANSACTION_PROPERTY, Scope.HANDLER);
  }

  @Test
  public void shouldCommitTransactionOnOutboundMessage() {
    when(context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).thenReturn(true);
    when(context.get(TransactionSoapHandler.TRANSACTION_PROPERTY)).thenReturn(transactionStatus);

    assertThat(subject.handleMessage(context), is(true));

    verify(transactionManager).commit(transactionStatus);
  }

  @Test
  public void shouldRollbackTransactionOnFault() {
    when(context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).thenReturn(true);
    when(context.get(TransactionSoapHandler.TRANSACTION_PROPERTY)).thenReturn(transactionStatus);

    assertThat(subject.handleFault(context), is(true));

    verify(transactionManager).rollback(transactionStatus);
  }

  @Test
  public void shouldRollbackIncompleteTransactionsOnClose() {
    when(context.get(TransactionSoapHandler.TRANSACTION_PROPERTY)).thenReturn(transactionStatus);

    subject.close(context);

    verify(transactionManager).rollback(transactionStatus);
  }

  @Test
  public void shouldIgnoreCompletedTransactionOnClose() {
    transactionStatus.setCompleted();
    when(context.get(TransactionSoapHandler.TRANSACTION_PROPERTY)).thenReturn(transactionStatus);

    subject.close(context);

    verifyNoMoreInteractions(transactionManager);
  }
}
