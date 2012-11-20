package nl.surfnet.bod.web.view;

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.domain.PhysicalResourceGroup;

public class PhysicalResourceGroupView {

  private Long id;
  private Institute institute;
  private String adminGroup;
  private String managerEmail;
  private Boolean active = false;

  private int physicalPortsAmount;
  private int virtualPortsAmount;
  private int reservationsAmount;

  public PhysicalResourceGroupView(final PhysicalResourceGroup physicalResourceGroup) {
    this.id = physicalResourceGroup.getId();
    this.institute = physicalResourceGroup.getInstitute();
    this.adminGroup = physicalResourceGroup.getAdminGroup();
    this.active = physicalResourceGroup.isActive();
    this.managerEmail = physicalResourceGroup.getManagerEmail();
  }
  
  public String getName() {
    return institute != null ? institute.getName() : null;
  }

  public final int getPhysicalPortsAmount() {
    return physicalPortsAmount;
  }

  public final void setPhysicalPortsAmount(int physicalPortsAmount) {
    this.physicalPortsAmount = physicalPortsAmount;
  }

  public final int getVirtualPortsAmount() {
    return virtualPortsAmount;
  }

  public final void setVirtualPortsAmount(int virtualPortsAmount) {
    this.virtualPortsAmount = virtualPortsAmount;
  }

  public final int getReservationsAmount() {
    return reservationsAmount;
  }

  public final void setReservationsAmount(int reservationsAmount) {
    this.reservationsAmount = reservationsAmount;
  }

  public final Long getId() {
    return id;
  }

  public final Institute getInstitute() {
    return institute;
  }

  public final String getAdminGroup() {
    return adminGroup;
  }

  public final String getManagerEmail() {
    return managerEmail;
  }

  public final Boolean getActive() {
    return active;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("PhysicalResourceGroupView [id=");
    builder.append(id);
    builder.append(", institute=");
    builder.append(institute);
    builder.append(", adminGroup=");
    builder.append(adminGroup);
    builder.append(", managerEmail=");
    builder.append(managerEmail);
    builder.append(", active=");
    builder.append(active);
    builder.append(", physicalPortsAmount=");
    builder.append(physicalPortsAmount);
    builder.append(", virtualPortsAmount=");
    builder.append(virtualPortsAmount);
    builder.append(", reservationsAmount=");
    builder.append(reservationsAmount);
    builder.append("]");
    return builder.toString();
  }

}
