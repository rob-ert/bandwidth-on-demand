/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.db;

import javax.sql.DataSource;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.googlecode.flyway.core.Flyway;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/spring/appCtx.xml", "/spring/appCtx-jpa-test.xml", "/spring/appCtx-nbi-client.xml",
    "/spring/appCtx-idd-client.xml" })
@TransactionConfiguration(defaultRollback = true, transactionManager = "transactionManager")
public class DatabaseMigrationTestIntegration extends AbstractTransactionalJUnit4SpringContextTests {

  @Autowired
  private DataSource dataSource;

  @Test
  public void preventError() {
    //
  }

  @Ignore("should run against a specific integration test postgres database, because of incompatible sql statemens")
  public void shouldInsertInstitutes() {
    Flyway flyway = new Flyway();
    flyway.setDataSource(dataSource);
    flyway.setBasePackage("nl.surfnet.bod.db.migration");

    flyway.clean();
    flyway.init();
    flyway.migrate();
  }
}
