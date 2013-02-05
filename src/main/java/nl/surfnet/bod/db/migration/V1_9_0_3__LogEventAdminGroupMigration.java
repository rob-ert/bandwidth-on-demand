/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.db.migration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.googlecode.flyway.core.api.migration.spring.SpringJdbcMigration;

public class V1_9_0_3__LogEventAdminGroupMigration implements SpringJdbcMigration {

  @Override
  public void migrate(JdbcTemplate jdbcTemplate) throws Exception {
    QueryCache cache = new QueryCache(jdbcTemplate);

    new SimpleEventsMigrator("PhysicalPort", jdbcTemplate).migrate();
    new SimpleEventsMigrator("PhysicalResourceGroup", jdbcTemplate).migrate();
    new ReservationLogEventsMigrator(jdbcTemplate, cache).migrate();
    new VirtualPortEventsMigrator(jdbcTemplate, cache).migrate();
    new VirtualPortRequestLinkEventsMigrator(jdbcTemplate, cache).migrate();

    jdbcTemplate.execute("ALTER TABLE log_event drop column admin_group");
  }

  private void insertAdminGroup(Long logEventId, String adminGroup, JdbcTemplate jdbcTemplate) {
    jdbcTemplate.update("insert into log_event_admin_groups (log_event, admin_group) values (?, ?)", logEventId, adminGroup);
  }

  private class VirtualPortRequestLinkEventsMigrator {
    private final JdbcTemplate jdbcTemplate;
    private final QueryCache queryCache;

    public VirtualPortRequestLinkEventsMigrator(JdbcTemplate jdbcTemplate, QueryCache queryCache) {
      this.jdbcTemplate = jdbcTemplate;
      this.queryCache = queryCache;
    }

    public void migrate() throws ExecutionException {
      List<VirtualPortRequestLinkLogEvent> virtualPortRequestLinkLogEvents = jdbcTemplate.query(
          "select le.id as le_id, vprl.* from log_event le, virtual_port_request_link vprl where le.domain_object_class = 'VirtualPortRequestLink' and "
          + "le.domain_object_id = vprl.id", new RowMapper<VirtualPortRequestLinkLogEvent>() {
        @Override
        public VirtualPortRequestLinkLogEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
          Long logEventId = rs.getLong("le_id");
          Long virtualResourceGroupId = rs.getLong("virtual_resource_group");
          Long physicalResourceGroupId = rs.getLong("physical_resource_group");

          return new VirtualPortRequestLinkLogEvent(logEventId, virtualResourceGroupId, physicalResourceGroupId);
        }
      });

      for (VirtualPortRequestLinkLogEvent virtualPortRequestLinkLogEvent : virtualPortRequestLinkLogEvents) {
        List<String> adminGroups = ImmutableList.of(
            queryCache.virtualResourceGroupAdminGroup.get(virtualPortRequestLinkLogEvent.getVirtualResourceGroupId()),
            queryCache.physicalResourceGroupAdminGroup.get(virtualPortRequestLinkLogEvent.getPhysicalResourceGroupId())
        );

        for (String adminGroup : adminGroups) {
          insertAdminGroup(virtualPortRequestLinkLogEvent.getLogEventId(), adminGroup, jdbcTemplate);
        }
      }
    }
  }

  private class VirtualPortEventsMigrator {

    private final JdbcTemplate jdbcTemplate;
    private final QueryCache queryCache;

    public VirtualPortEventsMigrator(JdbcTemplate jdbcTemplate, QueryCache queryCache) {
      this.jdbcTemplate = jdbcTemplate;
      this.queryCache = queryCache;
    }

    public void migrate() throws ExecutionException {
      List<VirtualPortLogEvent> virtualPortLogEvents = jdbcTemplate.query(
          "select le.id as le_id, vp.* from log_event le, virtual_port vp where le.domain_object_class = 'VirtualPort' and "
          + "le.domain_object_id = vp.id", new RowMapper<VirtualPortLogEvent>() {
        @Override
        public VirtualPortLogEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
          Long logEventId = rs.getLong("le_id");
          Long virtualResourceGroupId = rs.getLong("virtual_resource_group");
          Long physicalPortId = rs.getLong("physical_port");

          return new VirtualPortLogEvent(logEventId, virtualResourceGroupId, physicalPortId);
        }
      });

      for (VirtualPortLogEvent virtualPortLogEvent : virtualPortLogEvents) {
        List<String> adminGroups = ImmutableList.of(
            queryCache.virtualResourceGroupAdminGroup.get(virtualPortLogEvent.getVirtualResourceGroupId()),
            queryCache.physicalPortAdminGroup.get(virtualPortLogEvent.getPhysicalPortId())
        );

        for (String adminGroup : adminGroups) {
          insertAdminGroup(virtualPortLogEvent.getLogEventId(), adminGroup, jdbcTemplate);
        }
      }
    }
  }

  private class SimpleEventsMigrator {
    private final JdbcTemplate jdbcTemplate;
    private final String domainClass;

    public SimpleEventsMigrator(String domainClass, JdbcTemplate jdbcTemplate) {
      this.jdbcTemplate = jdbcTemplate;
      this.domainClass = domainClass;
    }

    public void migrate() {
      List<IdAdminGroup> idAdminGroups = jdbcTemplate.query(
        "select id, admin_group from log_event where domain_object_class = ?",
        new Object[] {domainClass},
        new RowMapper<IdAdminGroup>() {
          @Override
          public IdAdminGroup mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new IdAdminGroup(rs.getLong("id"), rs.getString("admin_group"));
          }
        }
      );

      for (IdAdminGroup idAdminGroup : idAdminGroups) {
        insertAdminGroup(idAdminGroup.getLogEventId(), idAdminGroup.getAdminGroup(), jdbcTemplate);
      }
    }
  }

  private class ReservationLogEventsMigrator {
    private final JdbcTemplate jdbcTemplate;
    private final QueryCache queryCache;

    public ReservationLogEventsMigrator(JdbcTemplate jdbcTemplate, QueryCache queryCache) {
      this.jdbcTemplate = jdbcTemplate;
      this.queryCache = queryCache;
    }

    public void migrate() throws ExecutionException {
      List<ReservationLogEvent> reservationLogEvents = jdbcTemplate.query(
          "select le.id as le_id, r.id as id, r.* from log_event le, reservation r where le.domain_object_class = 'Reservation' and "
          + "le.domain_object_id = r.id", new RowMapper<ReservationLogEvent>() {
        @Override
        public ReservationLogEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
          Long logEventId = rs.getLong("le_id");
          Long virtualResourceGroupId = rs.getLong("virtual_resource_group");
          Long sourcePort = rs.getLong("source_port");
          Long destinationPort = rs.getLong("destination_port");

          return new ReservationLogEvent(logEventId, virtualResourceGroupId, sourcePort, destinationPort);
        }
      });

      for (ReservationLogEvent res : reservationLogEvents) {
        List<String> adminGroups = ImmutableList.of(
          queryCache.virtualPortAdminGroup.get(res.getSourcePortId()),
          queryCache.virtualPortAdminGroup.get(res.getDestinationPortId()),
          queryCache.virtualResourceGroupAdminGroup.get(res.getVirtualResourceGroupId())
        );

        for (String adminGroup : adminGroups) {
          insertAdminGroup(res.getLogEventId(), adminGroup, jdbcTemplate);
        }
      }
    }
  }

  private class QueryCache {
    private final LoadingCache<Long, String> physicalResourceGroupAdminGroup = CacheBuilder.newBuilder()
      .build(
        new CacheLoader<Long, String>() {
          @Override
          public String load(Long vrgId) {
            return jdbcTemplate.queryForObject(
                "select admin_group from physical_resource_group where id = ?",
                String.class, vrgId);
          }
        }
      );

    private final LoadingCache<Long, String> virtualResourceGroupAdminGroup = CacheBuilder.newBuilder()
      .build(
        new CacheLoader<Long, String>() {
          @Override
          public String load(Long vrgId) {
            return jdbcTemplate.queryForObject(
                "select admin_group from virtual_resource_group where id = ?",
                String.class, vrgId);
          }
        }
      );

    private final LoadingCache<Long, String> physicalPortAdminGroup = CacheBuilder.newBuilder()
      .build(
        new CacheLoader<Long, String>() {
          @Override
          public String load(Long vrgId) {
            return jdbcTemplate.queryForObject(
                "select prg.admin_group from physical_resource_group prg, physical_port pp where pp.id = ? and "
                + "pp.physical_resource_group = prg.id",
                String.class, vrgId);
          }
        }
      );

    private final LoadingCache<Long, String> virtualPortAdminGroup = CacheBuilder.newBuilder()
      .build(
        new CacheLoader<Long, String>() {
          @Override
          public String load(Long vPortId) {
            return jdbcTemplate.queryForObject(
                "select prg.admin_group from physical_resource_group prg, virtual_port vp, physical_port pp where "
                + "vp.id = ? and "
                + "vp.physical_port = pp.id and "
                + "pp.physical_resource_group = prg.id", String.class, vPortId);
          }
        }
      );

    private final JdbcTemplate jdbcTemplate;

    public QueryCache(JdbcTemplate jdbcTemplate) {
      this.jdbcTemplate = jdbcTemplate;
    }
  }

  private static class VirtualPortRequestLinkLogEvent {
    private final Long logEventId;
    private final Long virtualResourceGroupId;
    private final Long physicalResourceGroupId;

    public VirtualPortRequestLinkLogEvent(Long logEventId, Long virtualResourceGroupId, Long physicalResourceGroupId) {
      this.logEventId = logEventId;
      this.virtualResourceGroupId = virtualResourceGroupId;
      this.physicalResourceGroupId = physicalResourceGroupId;
    }

    public Long getLogEventId() {
      return logEventId;
    }
    public Long getVirtualResourceGroupId() {
      return virtualResourceGroupId;
    }
    public Long getPhysicalResourceGroupId() {
      return physicalResourceGroupId;
    }
  }

  private static class VirtualPortLogEvent {
    private final Long logEventId;
    private final Long virtualResourceGroupId;
    private final Long physicalPortId;

    public VirtualPortLogEvent(Long logEventId, Long virtualResourceGroupId, Long physicalPortId) {
      this.logEventId = logEventId;
      this.virtualResourceGroupId = virtualResourceGroupId;
      this.physicalPortId = physicalPortId;
    }

    public Long getLogEventId() {
      return logEventId;
    }
    public Long getPhysicalPortId() {
      return physicalPortId;
    }
    public Long getVirtualResourceGroupId() {
      return virtualResourceGroupId;
    }
  }

  private static class IdAdminGroup {
    private final Long logEventId;
    private final String adminGroup;

    public IdAdminGroup(Long logEventId, String adminGroup) {
      this.logEventId = logEventId;
      this.adminGroup = adminGroup;
    }
    public Long getLogEventId() {
      return logEventId;
    }
    public String getAdminGroup() {
      return adminGroup;
    }
  }

  private static class ReservationLogEvent {
    private final Long logEventId;
    private final Long virtualResourceGroupId;
    private final Long sourcePortId;
    private final Long destinationPortId;

    public ReservationLogEvent(Long logEventId, Long virtualResourceGroupId, Long sourcePort, Long destinationPort) {
      this.logEventId = logEventId;
      this.virtualResourceGroupId = virtualResourceGroupId;
      this.sourcePortId = sourcePort;
      this.destinationPortId = destinationPort;
    }
    public Long getLogEventId() {
      return this.logEventId;
    }
    public Long getVirtualResourceGroupId() {
      return virtualResourceGroupId;
    }
    public Long getSourcePortId() {
      return sourcePortId;
    }
    public Long getDestinationPortId() {
      return destinationPortId;
    }
  }

}