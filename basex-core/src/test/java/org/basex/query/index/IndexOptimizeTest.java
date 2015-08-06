package org.basex.query.index;

import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.ft.*;
import org.basex.util.*;
import org.junit.*;
import org.junit.Test;

/**
 * This class tests if queries are rewritten for index access.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class IndexOptimizeTest extends AdvancedQueryTest {
  /**
   * Creates a test database.
   */
  @BeforeClass
  public static void start() {
    execute(new DropDB(NAME));
    set(MainOptions.FTINDEX, true);
    set(MainOptions.QUERYINFO, true);
  }

  /**
   * Checks the open command.
   * Test method.
   */
  @Test
  public void openDocTest() {
    createDoc();
    execute(new Open(NAME));
    check("//*[text() = '1']");
    check("data(//*[@* = 'y'])", "1");
    check("data(//@*[. = 'y'])", "y");
    check("//*[text() contains text '1']");
    check("//a[. = '1']");
    check("//xml[a = '1']");
    check(".[.//text() contains text '1']");
    check("for $s in ('x', '') return //*[text() = $s]", "");
  }

  /**
   * Checks the open command.
   * Test method.
   */
  @Test
  public void openCollTest() {
    createColl();
    execute(new Open(NAME));
    check("//*[text() = '1']");
    check("//*[text() contains text '1']");
    check("//a[. = '1']");
    check("//xml[a = '1']");
    check(".[.//text() contains text '1']");
    check("for $s in ('x', '') return //*[text() = $s]", "");
  }

  /**
   * Checks the XQuery doc() function.
   */
  @Test
  public void docTest() {
    createDoc();
    final String func = DOC.args(NAME);
    check(func + "//*[text() = '1']");
    check(func + "//*[text() contains text '2']");
    check(func + "//a[. = '1']");
    check(func + "//xml[a = '1']");
    check(func + "/.[.//text() contains text '1']");
    check(func + "[.//text() contains text '1']");
    check("for $s in ('x', '') return " + func + "//*[text() = $s]", "");
  }

  /**
   * Checks the XQuery collection() function.
   */
  @Test
  public void collTest() {
    createColl();
    final String func = COLLECTION.args(NAME);
    check(func + "//*[text() = '1']");
    check(func + "//*[text() contains text '2']");
    check(func + "//a[. = '1']");
    check(func + "//xml[a = '1']");
    check(func + "/.[.//text() contains text '1']");
    check(func + "[.//text() contains text '1']");
    check("for $s in ('x', '', string-join((1 to 513) ! 'a'))"
        + "return " + func + "//*[text() = $s]", "");
  }

  /**
   * Checks the XQuery db:open() function.
   */
  @Test
  public void dbOpenTest() {
    createColl();
    final String func = _DB_OPEN.args(NAME);
    check(func + "//*[text() = '1']");
    check(func + "//*[text() contains text '2']");
    check(func + "//a[. = '1']");
    check(func + "//xml[a = '1']");
    check("for $s in ('x', '', string-join((1 to 513) ! 'a'))"
        + "return " + func + "//*[text() = $s]", "");
  }

  /**
   * Checks the XQuery db:open() function, using a specific path.
   */
  @Test
  public void dbOpenExtTest() {
    createColl();
    final String func = _DB_OPEN.args(NAME, "two");
    check(func + "//*[text() = '1']", "");
    check(func + "//*[text() contains text '2']", "");
    check(func + "//a[. = '1']", "");
    check(func + "//xml[a = '1']", "");
    check(func + "//*[text() = '4']", "<a>4</a>");
    check("for $s in ('x', '', string-join((1 to 513) ! 'a'))"
        + "return " + func + "//*[text() = $s]", "");
  }

  /**
   * Checks full-text requests.
   */
  @Test
  public void ftTest() {
    createDoc();
    execute(new Open(NAME));
    check("data(//*[text() contains text '1'])", "1");
    check("data(//*[text() contains text '1 2' any word])", "1\n2 3");
    check("//*[text() contains text {'2','4'} all]", "");
    check("//*[text() contains text {'2','3'} all words]", "<a>2 3</a>");
    check("//*[text() contains text {'2','4'} all words]", "");
  }

  /**
   * Checks if a full-text index with language option is used.
   */
  @Test
  public void ftTestLang() {
    set(MainOptions.LANGUAGE, "de");
    createDoc();
    execute(new Open(NAME));
    check("//text()[. contains text '1']");
    check("//text()[. contains text '1' using language 'de']");
    check("//text()[. contains text '1' using language 'German']");
  }

  /**
   * Checks index optimizations inside functions.
   */
  @Test
  public void functionTest() {
    createColl();
    // document access after inlining
    check("declare function local:x($d) { collection($d)//text()[. = '1'] };"
        + "local:x('" + NAME + "')", "1");
    check("declare function local:x($d, $s) { collection($d)//text()[. = $s] };"
        + "local:x('" + NAME + "', '1')", "1");

    // text: search term must be string
    final String doc = _DB_OPEN.args(NAME);
    check("declare function local:x() {" + doc +
        "//text()[. = '1'] }; local:x()", "1");
    check("declare function local:x($x as xs:string) {" + doc +
        "//text()[. = $x] }; local:x('1')", "1");
    // full-text: search term may can have any type
    check("declare function local:x() {" + doc +
        "//text()[. contains text '1'] }; local:x()", "1");
    check("declare function local:x($x) {" + doc +
        "//text()[. contains text { $x }] }; local:x('1')", "1");
  }

  /**
   * Checks predicate tests for empty strings.
   */
  @Test
  public void empty() {
    createDoc();
    execute(new Open(NAME));
    query("//*[text() = '']", "");
    query("//text()[. = '']", "");
    query("//*[. = '']", "<a/>");
    query("//a[. = '']", "<a/>");
    query("//a[. = <x/>]", "<a/>");

    query("//a[not(text() = '')]/text()", "1\n2 3");
    query("//text()[not(. = '')]", "1\n2 3");
    query("//a[not(. = '')]/text()", "1\n2 3");
}

  /**
   * Creates a test database.
   */
  private static void createDoc() {
    execute(new CreateDB(NAME, "<xml><a x='y'>1</a><a>2 3</a><a/></xml>"));
    execute(new Close());
  }

  /**
   * Creates a test collection.
   */
  private static void createColl() {
    execute(new CreateDB(NAME));
    execute(new Add("one", "<xml><a>1</a><a>2 3</a></xml>"));
    execute(new Add("two", "<xml><a>4</a><a>5 6</a></xml>"));
    execute(new Optimize());
    execute(new Close());
  }

  /**
   * Check if specified query was rewritten for index access.
   * @param query query to be tested
   */
  private static void check(final String query) {
    check(query, null);
  }

  /**
   * Checks if specified query was rewritten for index access, and checks the query result.
   * @param query query to be tested
   * @param result expected query result
   */
  private static void check(final String query, final String result) {
    // compile query
    String plan = null;
    try {
      try(QueryProcessor qp = new QueryProcessor(query, context)) {
        final String string = qp.value().serialize().toString();
        if(result != null) assertEquals(result, normNL(string));

        // fetch query plan
        plan = qp.plan().serialize().toString();
      }

      // check if index is used
      try(QueryProcessor qp = new QueryProcessor(plan + "/descendant-or-self::*" +
            "[self::" + Util.className(ValueAccess.class) +
            "|self::" + Util.className(FTIndexAccess.class) + ']', context)) {
        final String string = qp.value().serialize().toString();
        assertFalse("No index used:\n- Query: " + query + "\n- Plan: " + plan + "\n- " +
            qp.info().trim(), string.isEmpty());
      }
    } catch(final QueryException ex) {
      fail(Util.message(ex) + "\n- Query: " + query + "\n- Plan: " + plan);
    } catch(final IOException ex) {
      fail(Util.message(ex));
    }
  }
}
