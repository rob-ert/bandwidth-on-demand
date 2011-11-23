package nl.surfnet.bod.domain;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import nl.surfnet.bod.repo.PhysicalPortRepo;
import nl.surfnet.bod.service.PhysicalPortService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class PhysicalPortDataOnDemand {

    private final Random rnd = new SecureRandom();

    private List<PhysicalPort> data;

    @Autowired
    private PhysicalResourceGroupDataOnDemand physicalResourceGroupDataOnDemand;

    @Autowired
    @Qualifier("physicalPortServiceRepoImpl")
    private PhysicalPortService physicalPortServiceRepoImpl;

    @Autowired
    private PhysicalPortRepo physicalPortRepo;

    public PhysicalPort getNewTransientPhysicalPort(final int index) {
        PhysicalPort obj = new PhysicalPort();
        setName(obj, index);
        setPhysicalResourceGroup(obj, index);
        return obj;
    }

    public void setName(final PhysicalPort obj, final int index) {
        String name = "name_" + index;
        obj.setName(name);
    }

    public void setPhysicalResourceGroup(final PhysicalPort obj, final int index) {
        PhysicalResourceGroup physicalResourceGroup = physicalResourceGroupDataOnDemand
                .getRandomPhysicalResourceGroup();
        obj.setPhysicalResourceGroup(physicalResourceGroup);
    }

    public PhysicalPort getSpecificPhysicalPort(int index) {
        init();
        if (index < 0)
            index = 0;
        if (index > (data.size() - 1))
            index = data.size() - 1;
        PhysicalPort obj = data.get(index);
        java.lang.Long id = obj.getId();
        return physicalPortServiceRepoImpl.find(id);
    }

    public PhysicalPort getRandomPhysicalPort() {
        init();
        PhysicalPort obj = data.get(rnd.nextInt(data.size()));
        java.lang.Long id = obj.getId();
        return physicalPortServiceRepoImpl.find(id);
    }

    public boolean modifyPhysicalPort(final PhysicalPort obj) {
        return false;
    }

    public void init() {
        int from = 0;
        int to = 10;
        data = physicalPortServiceRepoImpl.findEntries(from, to);
        if (data == null)
            throw new IllegalStateException("Find entries implementation for 'PhysicalPort' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<nl.surfnet.bod.domain.PhysicalPort>();
        for (int i = 0; i < 10; i++) {
            PhysicalPort obj = getNewTransientPhysicalPort(i);
            try {
                physicalPortServiceRepoImpl.save(obj);
            } catch (ConstraintViolationException e) {
                StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> it = e.getConstraintViolations().iterator(); it.hasNext();) {
                    ConstraintViolation<?> cv = it.next();
                    msg.append("[").append(cv.getConstraintDescriptor()).append(":").append(cv.getMessage())
                            .append("=").append(cv.getInvalidValue()).append("]");
                }
                throw new RuntimeException(msg.toString(), e);
            }
            physicalPortRepo.flush();
            data.add(obj);
        }
    }
}
