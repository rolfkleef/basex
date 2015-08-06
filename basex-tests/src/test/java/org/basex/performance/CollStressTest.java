package org.basex.performance;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.junit.*;
import org.junit.Test;

/**
 * This class adds and retrieves documents in a collection.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class CollStressTest extends SandboxTest {
  /** Number of documents to be added. */
  private static final int SIZE = 4000;

  /**
   * Initializes the tests.
   */
  @BeforeClass
  public static void init() {
    execute(new CreateDB(NAME));
    // Speed up updates and add documents
    set(MainOptions.AUTOFLUSH, false);
    set(MainOptions.INTPARSE, true);
    for(int i = 0; i < SIZE; i++) execute(new Add(Integer.toString(i), "<xml/>"));
    set(MainOptions.AUTOFLUSH, true);
  }

  /**
   * Requests specific documents.
   */
  @Test
  public void specificOpened() {
    execute(new Open(NAME));
    for(int i = 0; i < SIZE; i++) query("collection('" + NAME + '/' + i + "')");
  }

  /**
   * Requests specific documents from closed database.
   */
  @Test
  public void specificClosed() {
    execute(new Close());
    for(int i = 0; i < SIZE; i++) query("collection('" + NAME + '/' + i + "')");
  }

  /**
   * Requests all documents.
   */
  @Test
  public void allOpened() {
    execute(new Open(NAME));
    query("for $i in 0 to " + (SIZE - 1) + " return collection(concat('" + NAME + "/', $i))");
  }

  /**
   * Requests all documents from closed database.
   */
  @Test
  public void allClosed() {
    execute(new Close());
    query("for $i in 0 to " + (SIZE - 1) + " return collection(concat('" + NAME + "/', $i))");
  }
}
