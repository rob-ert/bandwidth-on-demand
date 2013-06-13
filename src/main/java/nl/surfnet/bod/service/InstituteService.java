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
package nl.surfnet.bod.service;

import java.util.Collection;

import nl.surfnet.bod.domain.Institute;

public interface InstituteService {

  /**
   * Finds the {@link Institute} related to the given Id.
   * 
   * @param id
   *          Id to search for.
   * @return {@link Institute} related to the id
   */
  Institute find(Long id);

  /**
   * Finds the {@link Institute} related to the given shortName
   * 
   * @param shortName
   *          ShortName to search for
   * @return {@link Institute} related to the shortName ofr null when not found
   */
  Institute findByShortName(String shortName);

  /**
   * Finds all {@link Institute}s which are aligned with IDD.
   * 
   * @return
   */
  Collection<Institute> findAlignedWithIDD();

  /**
   * Retrieves all {@link Institute}s from the external IDD system, updates and
   * persist them. All {@link Institute}s that are present in BoD but not in the
   * IDD system anymore are marked as <strong>not aligned</strong> with IDD
   * {@link Institute#isAlignedWithIDD()}. All other institutes are marked
   * alignedWithIDD.
   */
  void refreshInstitutes();

}
