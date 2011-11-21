package nl.surfnet.bod.domain;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

@Configurable
@Entity
public class PhysicalResourceGroup {

	@NotNull
	private String name;

	@NotNull
	private String institutionName;


	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("InstitutionName: ").append(getInstitutionName()).append(", ");
        sb.append("Name: ").append(getName()).append(", ");
        sb.append("Version: ").append(getVersion());
        return sb.toString();
    }

	@PersistenceContext
    transient EntityManager entityManager;

	@Transactional
    public void persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }

	@Transactional
    public void remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            PhysicalResourceGroup attached = PhysicalResourceGroup.findPhysicalResourceGroup(this.id);
            this.entityManager.remove(attached);
        }
    }

	@Transactional
    public void flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }

	@Transactional
    public void clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }

	@Transactional
    public PhysicalResourceGroup merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        PhysicalResourceGroup merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public static final EntityManager entityManager() {
        EntityManager em = new PhysicalResourceGroup().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countPhysicalResourceGroups() {
        return entityManager().createQuery("SELECT COUNT(o) FROM PhysicalResourceGroup o", Long.class).getSingleResult();
    }

	public static List<PhysicalResourceGroup> findAllPhysicalResourceGroups() {
        return entityManager().createQuery("SELECT o FROM PhysicalResourceGroup o", PhysicalResourceGroup.class).getResultList();
    }

	public static PhysicalResourceGroup findPhysicalResourceGroup(Long id) {
        if (id == null) return null;
        return entityManager().find(PhysicalResourceGroup.class, id);
    }

	public static List<PhysicalResourceGroup> findPhysicalResourceGroupEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM PhysicalResourceGroup o", PhysicalResourceGroup.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@Version
    @Column(name = "version")
    private Integer version;

	public Long getId() {
        return this.id;
    }

	public void setId(Long id) {
        this.id = id;
    }

	public Integer getVersion() {
        return this.version;
    }

	public void setVersion(Integer version) {
        this.version = version;
    }

	public String getName() {
        return this.name;
    }

	public void setName(String name) {
        this.name = name;
    }

	public String getInstitutionName() {
        return this.institutionName;
    }

	public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }
}
