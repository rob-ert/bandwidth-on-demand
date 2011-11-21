package nl.surfnet.bod.domain;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceContext;
import javax.persistence.Version;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

@Entity
@Configurable
public class PhysicalPort {

	private String name;

	@ManyToOne
	private PhysicalResourceGroup physicalResourceGroup;


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
            PhysicalPort attached = PhysicalPort.findPhysicalPort(this.id);
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
    public PhysicalPort merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        PhysicalPort merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

	public static final EntityManager entityManager() {
        EntityManager em = new PhysicalPort().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countPhysicalPorts() {
        return entityManager().createQuery("SELECT COUNT(o) FROM PhysicalPort o", Long.class).getSingleResult();
    }

	public static List<PhysicalPort> findAllPhysicalPorts() {
        return entityManager().createQuery("SELECT o FROM PhysicalPort o", PhysicalPort.class).getResultList();
    }

	public static PhysicalPort findPhysicalPort(Long id) {
        if (id == null) return null;
        return entityManager().find(PhysicalPort.class, id);
    }

	public static List<PhysicalPort> findPhysicalPortEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM PhysicalPort o", PhysicalPort.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Id: ").append(getId()).append(", ");
        sb.append("Name: ").append(getName()).append(", ");
        sb.append("PhysicalResourceGroup: ").append(getPhysicalResourceGroup()).append(", ");
        sb.append("Version: ").append(getVersion());
        return sb.toString();
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

	public PhysicalResourceGroup getPhysicalResourceGroup() {
        return this.physicalResourceGroup;
    }

	public void setPhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup) {
        this.physicalResourceGroup = physicalResourceGroup;
    }
}
