package com.adaptris.core.es5;

/**
 * What to do with this document.
 */
// These are references by ordinal from the
// "Delta_Status" column so the ordering in this enum is important!
public enum DocumentAction {
  /** Delete the document */
  DELETE,
  /** Update a document */
  UPDATE,
  /** Index a document */
  INDEX,
  /** Update or insert the document */
  UPSERT
}