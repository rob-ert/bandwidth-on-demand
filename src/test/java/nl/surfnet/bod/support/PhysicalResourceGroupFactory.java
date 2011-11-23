package nl.surfnet.bod.support;

import nl.surfnet.bod.domain.PhysicalResourceGroup;

public class PhysicalResourceGroupFactory {

    private String name = "First group";
    private String institution = "SURFnet B.V.";
    private String adminGroup = null;

    public PhysicalResourceGroup create() {
        PhysicalResourceGroup group = new PhysicalResourceGroup();
        group.setName(name);
        group.setInstitutionName(institution);
        group.setAdminGroup(adminGroup);
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

    public PhysicalResourceGroupFactory setAdminGroupName(String adminGroup) {
        this.adminGroup = adminGroup;
        return this;
    }

}
