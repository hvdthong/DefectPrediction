package org.apache.log4j.lf5.viewer;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;

/**
 * Provides methods to accomplish common yet non-trivial tasks
 * with Swing. Obvious implementations of these methods have been
 * tried and failed.
 *
 * @author Richard Wan
 */


public class LF5SwingUtils {





  /**
   * Selects a the specified row in the specified JTable and scrolls
   * the specified JScrollpane to the newly selected row. More importantly,
   * the call to repaint() delayed long enough to have the table
   * properly paint the newly selected row which may be offscre
   * @param table should belong to the specified JScrollPane
   */
  public static void selectRow(int row, JTable table, JScrollPane pane) {
    if (table == null || pane == null) {
      return;
    }
    if (contains(row, table.getModel()) == false) {
      return;
    }
    moveAdjustable(row * table.getRowHeight(), pane.getVerticalScrollBar());
    selectRow(row, table.getSelectionModel());
    repaintLater(table);
  }

  /**
   * Makes the specified Adjustable track if the view area expands and
   * the specified Adjustable is located near the of the view.
   */
  public static void makeScrollBarTrack(Adjustable scrollBar) {
    if (scrollBar == null) {
      return;
    }
    scrollBar.addAdjustmentListener(new TrackingAdjustmentListener());
  }

  /**
   * Makes the vertical scroll bar of the specified JScrollPane
   * track if the view expands (e.g. if rows are added to an underlying
   * table).
   */
  public static void makeVerticalScrollBarTrack(JScrollPane pane) {
    if (pane == null) {
      return;
    }
    makeScrollBarTrack(pane.getVerticalScrollBar());
  }

  protected static boolean contains(int row, TableModel model) {
    if (model == null) {
      return false;
    }
    if (row < 0) {
      return false;
    }
    if (row >= model.getRowCount()) {
      return false;
    }
    return true;
  }

  protected static void selectRow(int row, ListSelectionModel model) {
    if (model == null) {
      return;
    }
    model.setSelectionInterval(row, row);
  }

  protected static void moveAdjustable(int location, Adjustable scrollBar) {
    if (scrollBar == null) {
      return;
    }
    scrollBar.setValue(location);
  }

  /**
   * Work around for JTable/viewport bug.
   */
  protected static void repaintLater(final JComponent component) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        component.repaint();
      }
    });
  }

}
