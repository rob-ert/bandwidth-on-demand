package nl.surfnet.bod.support;

import nl.surfnet.bod.domain.PhysicalResourceGroup;

public class PhysicalResourceGroupFactory {

    private String name = "First group";
    private String institution = "SURFnet B.V.";

    public PhysicalResourceGroup create() {
        PhysicalResourceGroup group = new PhysicalResourceGroup();
        group.setName(name);
        group.setInstitutionName(institution);
        return group;
    }

    public PhysicalResourceGroupFactory setName(String name) {
        this.name = name;
        return this;
    }

    public PhysicalResourceGroupFactory setInstitution(String institution) {
        this.institution = institution;
        return this;
    }

}
