package nl.surfnet.bod.opendrac;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nortel.appcore.app.drac.common.types.DracService;
import com.nortel.appcore.app.drac.common.types.Facility;
import com.nortel.appcore.app.drac.common.types.NetworkElementHolder;
import com.nortel.appcore.app.drac.common.types.Schedule;
import com.nortel.appcore.app.drac.common.types.TaskType;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper;
import com.nortel.appcore.app.drac.common.utility.CryptoWrapper.CryptedString;
import com.nortel.appcore.app.drac.security.ClientLoginType;
import com.nortel.appcore.app.drac.security.LoginToken;
import com.nortel.appcore.app.drac.server.nrb.NrbInterface;
import com.nortel.appcore.app.drac.server.requesthandler.RemoteConnectionProxy;
import com.nortel.appcore.app.drac.server.requesthandler.RequestHandlerException;

/**
 * A wrapper 'service' around OpenDRAC's {@link NrbInterface}. The main
 * difference is that the methods in this class use a {@link LoginToken} instead
 * of a clear text password.
 * 
 * @author robert
 * 
 */
@Service("nrbService")
public class NrbService {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final RemoteConnectionProxy nrbProxy = new RemoteConnectionProxy();

  private NrbInterface getNrbInterface() {
    try {
      return nrbProxy.getNrbInterface();
    }
    catch (RequestHandlerException e) {
      log.error("Error: ", e);
      return null;
    }
  }

  /**
   * 
   * @param loginToken
   *          a valid {@link LoginToken}
   * @return a list of all currently available network elements or
   *         <code>null</code> whenever an exception occurs
   */
  public List<NetworkElementHolder> getAllNetworkElements(final LoginToken loginToken) {
    try {
      return getNrbInterface().getAllNetworkElements(loginToken);
    }
    catch (Exception e) {
      log.error("Error: ", e);
      return null;
    }

  }

  /**
   * 
   * @param loginToken
   *          a valid {@link LoginToken}
   * @return a list of all currently available UNI network facilities or
   *         <code>null</code> whenever an exception occurs
   */
  public List<Facility> getAllUniFacilities(final LoginToken loginToken) {

    try {
      final List<Facility> facilities = new ArrayList<Facility>();
      for (NetworkElementHolder holder : getAllNetworkElements(loginToken)) {
        final List<Facility> facilitiesPerNetworkElementHolder = getNrbInterface().getFacilities(loginToken,
            holder.getId());
        for (final Facility facility : facilitiesPerNetworkElementHolder) {
          if ("UNI".equals(facility.get("interfaceType"))) {
            facilities.add(facility);
          }
        }
      }
      return facilities;
    }
    catch (Exception e) {
      log.error("Error: ", e);
      return null;
    }
  }

  /**
   * 
   * @param username
   * @param encryptedPassword
   *          an an encrypted username
   * @return a valid {@link LoginToken} or <code>null</code> whenever an
   *         exception occurs
   */
  public LoginToken getLoginToken(final String username, final String encryptedPassword) {

    final String password = CryptoWrapper.INSTANCE.decrypt(new CryptedString(encryptedPassword));
    try {
      return getNrbInterface()
          .login(ClientLoginType.INTERNAL_LOGIN, username, password.toCharArray(), null, null, null);
    }
    catch (Exception e) {
      log.error("Error: ", e);
      return null;
    }
  }

  /**
   * 
   * @param loginToken
   *          a valid {@link LoginToken}
   * @param schedule
   *          the {@link Schedule} to be executed
   * @return the id of the created schedule or <code>null</code> whenever an
   *         exception occurs
   */
  public String createSchedule(final LoginToken loginToken, final Schedule schedule) {
    try {
      return getNrbInterface().asyncCreateSchedule(loginToken, schedule);
    }
    catch (Exception e) {
      log.error("Error: ", e);
      return null;
    }
  }

  /**
   * 
   * @param loginToken
   *          a valid {@link LoginToken}
   * @param scheduleId
   *          the id of the schedule of interest
   * @return
   */
  public TaskType getScheduleStatus(final LoginToken loginToken, final String scheduleId) {
    try {
      return getNrbInterface().getTaskInfo(loginToken, scheduleId);
    }
    catch (Exception e) {
      log.error("Error: ", e);
      return null;
    }
  }

  /**
   * 
   * @param loginToken
   * @param scheduleId
   */
  public void cancelSchedule(final LoginToken loginToken, final String scheduleId) {
    try {
      getNrbInterface().cancelSchedule(loginToken, scheduleId);
    }
    catch (Exception e) {
      log.error("Error: ", e);
    }
  }

  /**
   * 
   * @param loginToken
   * @param scheduleId
   * @param minutes
   */
  public void extendSchedule(final LoginToken loginToken, final String scheduleId, int minutes) {
    try {
      final DracService dracService = getNrbInterface().getCurrentlyActiveServiceByScheduleId(loginToken, scheduleId);
      getNrbInterface().extendServiceTime(loginToken, dracService, minutes);
    }
    catch (Exception e) {
      log.error("Error: ", e);
    }
  }

}
