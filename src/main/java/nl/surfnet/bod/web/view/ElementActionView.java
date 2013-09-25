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
package nl.surfnet.bod.web.view;

import org.springframework.context.MessageSource;


/**
 * View responsible for holding state related to an element in the user
 * interface. E.g. if the deletion of a row is enabled. The {@link #isAllowed()}
 * indicates if the action is allowed, the {@link #getReason()} contains the
 * message key for the reason why or why not. The convention is that the
 * reasonKey is retrieved from a {@link MessageSource} and shown as a tooltip.
 *
 */
public class ElementActionView {

  private final boolean allowed;
  private final String reasonKey;

  public ElementActionView(boolean actionAllowed) {
    this(actionAllowed, null);
  }

  public ElementActionView(boolean actionAllowed, String actionReasonKey) {
    this.allowed = actionAllowed;
    this.reasonKey = actionReasonKey;
  }

  public boolean isAllowed() {
    return allowed;
  }

  public String getReasonKey() {
    return reasonKey;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (allowed ? 1231 : 1237);
    result = prime * result + ((reasonKey == null) ? 0 : reasonKey.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ElementActionView other = (ElementActionView) obj;
    if (allowed != other.allowed) {
      return false;
    }
    if (reasonKey == null) {
      if (other.reasonKey != null) {
        return false;
      }
    }
    else if (!reasonKey.equals(other.reasonKey)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "ElementActionView [allowed=" + allowed + ", reasonKey=" + reasonKey + "]";
  }

}
