package sk.upjs.jpaz2.inspector;

/**
 * The encapsulation for row separator. It encapsulates text and collapsed/expanded state of the separator.
 */
class OIRowSeparatorValue {
  /**
   * Displayed text
   */
  String text;
  
  /**
   * Indicates whether the separator is expanded or collapsed.
   */
  boolean collapsed = false;
  
  /**
   * Indicates whether the separator is active
   */
  boolean active = true;
}
