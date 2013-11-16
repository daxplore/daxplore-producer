package org.daxplore.producer.gui.groups;

import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.JPanel;

/**
 * A {@link JPanel} that can grow, but not shrink, in height.
 * 
 * <p>Useful for preventing visual glitches in Panels that resize regularly due
 * to continuously changing content.</p>
 * 
 * {@see JPanel}
 */
@SuppressWarnings("serial")
public class VerticallyGrowingJPanel extends JPanel {
	/** Tracks the biggest height that has either been set or read */
	private int minHeight = -1;

	/**
	 * {@see JPanel#JPanel()}
	 */
	public VerticallyGrowingJPanel() {
		super();
	}

	/**
	 * {@see JPanel#JPanel(boolean)}
	 */
	public VerticallyGrowingJPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	/**
	 * {@see JPanel#JPanel(LayoutManager)}
	 */
	public VerticallyGrowingJPanel(LayoutManager layoutManager) {
		super(layoutManager);
	}

	/**
	 * {@see JPanel#JPanel(LayoutManager, boolean)}
	 */
	public VerticallyGrowingJPanel(LayoutManager layoutManager, boolean isDoubleBuffered) {
		super(layoutManager, isDoubleBuffered);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dimension getSize() {
		return getForcedHeightDimension(super.getSize());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dimension getPreferredSize() {
		return getForcedHeightDimension(super.getPreferredSize());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dimension getMinimumSize() {
		return getForcedHeightDimension(super.getMinimumSize());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dimension getMaximumSize() {
		Dimension expectedDimension = super.getMaximumSize();
		if (expectedDimension.height >= minHeight) {
			return expectedDimension;
		}
		return new Dimension(expectedDimension.width, minHeight);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dimension getSize(Dimension rv) {
		return getForcedHeightDimension(super.getSize(rv));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMinimumSize(Dimension minimumSize) {
		if (minimumSize.height >= minHeight) {
			minHeight = minimumSize.height;
		}
		super.setMinimumSize(minimumSize);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPreferredSize(Dimension preferredSize) {
		if (preferredSize.height >= minHeight) {
			minHeight = preferredSize.height;
		}
		super.setPreferredSize(preferredSize);
	}

	/**
	 * {@inheritDoc}
	 */
	public void resetForcedSize() {
		minHeight = -1;
	}

	/**
	 * {@inheritDoc}
	 */
	private Dimension getForcedHeightDimension(Dimension expectedDimension) {
		if (expectedDimension.height > minHeight) {
			minHeight = expectedDimension.height;
			return expectedDimension;
		} else if (expectedDimension.height == minHeight) {
			return expectedDimension;
		}
		return new Dimension(expectedDimension.width, minHeight);
	}
}