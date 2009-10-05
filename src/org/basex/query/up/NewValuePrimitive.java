package org.basex.query.up;

import static org.basex.query.QueryText.*;

import org.basex.query.QueryException;
import org.basex.query.item.Nod;
import org.basex.query.util.Err;

/**
 * Abstract udpate primitive which holds an aditional 'name' attribute to for
 * updating values, names, etc.. 
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public abstract class NewValuePrimitive extends UpdatePrimitive {
  /** New name. */
  final byte[] name;

  /**
   * Constructor.
   * @param n target node
   * @param newName new name
   */
  public NewValuePrimitive(final Nod n, final byte[] newName) {
    super(n);
    name = newName;
  }

  @SuppressWarnings("unused")
  @Override
  public void check() throws QueryException {
  }

  @Override
  public void merge(UpdatePrimitive p) throws QueryException {
    // [LK] throw CORRECT query exception: multiple renames on same node
    Err.or(UPTRGMULT, node);
  }

  @Override
  public Type type() {
    return null;
  }
}