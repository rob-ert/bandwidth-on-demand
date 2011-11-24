package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

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

}
