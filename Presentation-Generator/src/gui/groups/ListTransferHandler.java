package gui.groups;

import gui.widget.QuestionWidget;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

public class ListTransferHandler extends TransferHandler {
	private int[] indices = null;
	private int addIndex = -1; // Location where items were added
	private int addCount = 0; // Number of items added.

	private DataFlavor dataFlavor = new DataFlavor(QuestionWidget.class,
			"QuestionWidget");

	static class QuestionWidgetsTransferable implements Transferable {
		DataFlavor flavor = new DataFlavor(QuestionWidget.class,
				"QuestionWidget");
		List<QuestionWidget> list;

		public QuestionWidgetsTransferable(List<QuestionWidget> componentList) {
			list = componentList;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			DataFlavor[] a = { flavor };
			return a;
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return flavor.equals(this.flavor);
		}

		@Override
		public Object getTransferData(DataFlavor flavor)
				throws UnsupportedFlavorException, IOException {
			return list;
		}

	}

	/**
	 * We only support importing strings (although widget support is sorely
	 * needed).
	 */
	public boolean canImport(TransferHandler.TransferSupport info) {
		// Check for String flavor
		if (!info.isDataFlavorSupported(dataFlavor)) {
			return false;
		}
		return true;
	}

	/**
	 * Bundle up the selected items in a single list for export. Each line is
	 * separated by a newline.
	 */
	protected Transferable createTransferable(JComponent c) {
		JList<QuestionWidget> list = (JList<QuestionWidget>) c;
		indices = list.getSelectedIndices();
		List<QuestionWidget> widgets = new LinkedList<QuestionWidget>();
		for (int i : indices) {
			widgets.add((QuestionWidget) list.getComponent(i));
		}
		return new QuestionWidgetsTransferable(widgets);
	}

	/**
	 * We support both copy and move actions.
	 */
	public int getSourceActions(JComponent c) {
		return TransferHandler.COPY;
	}

	/**
	 * Perform the actual import.
	 */
	public boolean importData(TransferHandler.TransferSupport info) {
		if (!info.isDrop()) {
			return false;
		}

		JList list = (JList) info.getComponent();
		DefaultListModel listModel = (DefaultListModel) list.getModel();
		JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
		int index = dl.getIndex();
		boolean insert = dl.isInsert();

		// Get the string that is being dropped.
		Transferable t = info.getTransferable();
		String data;
		try {
			data = (String) t.getTransferData(DataFlavor.stringFlavor);
		} catch (Exception e) {
			return false;
		}

		// Wherever there is a newline in the incoming data,
		// break it into a separate item in the list.
		String[] values = data.split("\n");

		addIndex = index;
		addCount = values.length;

		// Perform the actual import.
		for (int i = 0; i < values.length; i++) {
			if (insert) {
				listModel.add(index++, values[i]);
			} else {
				// If the items go beyond the end of the current
				// list, add them in.
				if (index < listModel.getSize()) {
					listModel.set(index++, values[i]);
				} else {
					listModel.add(index++, values[i]);
				}
			}
		}
		return true;
	}

	/**
	 * Remove the items moved from the list.
	 */
	protected void exportDone(JComponent c, Transferable data, int action) {
		JList source = (JList) c;
		DefaultListModel listModel = (DefaultListModel) source.getModel();

		if (action == TransferHandler.MOVE) {
			for (int i = indices.length - 1; i >= 0; i--) {
				listModel.remove(indices[i]);
			}
		}

		indices = null;
		addCount = 0;
		addIndex = -1;
	}
}