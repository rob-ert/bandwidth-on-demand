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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.googlecode.flyway.core.api.migration.spring.SpringJdbcMigration;

import org.apache.commons.lang.SerializationUtils;
import org.ogf.schemas.nsi._2011._10.connection.types.PathType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceParametersType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;

public class V1_16_0_0__JavaSerializationToXml implements SpringJdbcMigration {

    private static final nl.surfnet.bod.domain.ConnectionV1.PathTypeUserType PATH_TYPE = new nl.surfnet.bod.domain.ConnectionV1.PathTypeUserType();
    private static final nl.surfnet.bod.domain.ConnectionV1.ServiceParametersTypeUserType SERVICE_PARAMETERS_TYPE = new nl.surfnet.bod.domain.ConnectionV1.ServiceParametersTypeUserType();

    @Override
    public void migrate(final JdbcTemplate jdbcTemplate) throws Exception {
        jdbcTemplate.execute("ALTER TABLE connection ADD COLUMN path_xml TEXT, ADD COLUMN service_parameters_xml TEXT");

        jdbcTemplate.query("SELECT id, path, service_parameters FROM connection", new RowCallbackHandler() {

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                final long id = rs.getLong("id");
                final PathType path = deserialize(PathType.class, rs.getBytes("path"));
                final ServiceParametersType serviceParameters = deserialize(ServiceParametersType.class, rs.getBytes("service_parameters"));

                jdbcTemplate.update("UPDATE connection SET path_xml = ?, service_parameters_xml = ? WHERE id = ?", new PreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement st) throws SQLException {
                        PATH_TYPE.nullSafeSet(st, path, 1, null);
                        SERVICE_PARAMETERS_TYPE.nullSafeSet(st, serviceParameters, 2, null);
                        st.setLong(3, id);
                    }
                });
            }

            private <T> T deserialize(Class<T> type, byte[] bytes) {
                return type.cast(SerializationUtils.deserialize(bytes));
            }
        });

        jdbcTemplate.execute("ALTER TABLE connection ALTER COLUMN path_xml SET NOT NULL, ALTER COLUMN service_parameters_xml SET NOT NULL");
        jdbcTemplate.execute("ALTER TABLE connection drop column path, drop column service_parameters");
        jdbcTemplate.execute("ALTER TABLE connection RENAME COLUMN path_xml TO path");
        jdbcTemplate.execute("ALTER TABLE connection RENAME COLUMN service_parameters_xml TO service_parameters");
    }
}
