package nl.surfnet.bod.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;

import java.util.Collection;

import nl.surfnet.bod.domain.Institution;
import nl.surfnet.bod.service.InstitutionService;

import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

public class InstitutionControllerTest {

    private InstitutionService institutionServiceMock = Mockito.mock(InstitutionService.class);

    private InstitutionController subject = new InstitutionController(institutionServiceMock);

    @Test
    public void instutionsShouldBeFilteredBySearchParamIgnoringCase() {
        Collection<Institution> unfilteredInstitutions = Lists.newArrayList(
                new Institution("Universiteit Utrecht"),
                new Institution("Universiteit Amsterdam"));
        when(institutionServiceMock.getInstitutions()).thenReturn(unfilteredInstitutions);

        Collection<Institution> institutionsInAmsterdam = subject.jsonList("amsterdam");

        assertThat(institutionsInAmsterdam, hasSize(1));
        assertThat(institutionsInAmsterdam.iterator().next().getName(), containsString("Amsterdam"));
    }

}
