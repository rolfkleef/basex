package org.basex.data;

import static org.junit.Assert.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.junit.*;
import org.junit.Test;

/**
 * This class tests the stability of the data storage.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Dimitar Popov
 */
public final class TableTest extends SandboxTest {
  /** Database XML file. */
  private static final String DBFILE = "src/test/resources/factbook.zip";
  /** Select Germany. */
  private static final String SELECT = "//country[@name='Germany']";
  /** Delete Germany. */
  private static final String DELETE = "delete node " + SELECT;
  /** Re-insert Germany. */
  private static final String INSERT = "insert node %1$s "
      + "after //country[@name='France']";

  /** Table file. */
  private IOFile tbl;

  /**
   * Set up method.
   */
  @Before
  public void setUp() {
    execute(new CreateDB(NAME, DBFILE));
    tbl = context.data().meta.dbfile(DataText.DATATBL);
  }

  /**
   * Drops the JUnitTest database.
   */
  @After
  public void tearDown() {
    execute(new DropDB(NAME));
  }

  /**
   * Test if the size of the table remains constant after insertion, deletion,
   * and re-insertion of the same record.
   */
  @Test
  public void tableSize() {
    final long s = tbl.length();

    final String n = query(SELECT);
    query(DELETE);
    query(String.format(INSERT, n));
    execute(new Close());

    assertEquals("Database size changed: ", s, tbl.length());
  }
}
