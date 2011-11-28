package nl.surfnet.bod.service;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.Collection;

import nl.surfnet.bod.domain.Institution;

import org.junit.Test;

public class InstitutionStaticServiceTest {

    private InstitutionStaticService subject = new InstitutionStaticService();

    @Test
    public void fetchAllInstitutions() {
        Collection<Institution> institutions = subject.getInstitutions();

        assertThat(institutions, hasSize(greaterThan(0)));
    }

    @Test
    public void asdf() {
        subject.setStaticResponseFile("/idd_response_with_newline.xml");

        Collection<Institution> institutions = subject.getInstitutions();

        assertThat(getOnlyElement(institutions).getName(), is("Unitversiteit Utrecht"));
    }

}
