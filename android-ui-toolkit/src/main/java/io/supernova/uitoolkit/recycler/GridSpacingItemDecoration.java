package io.supernova.uitoolkit.recycler;


import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Implementation of RecyclerView.ItemDecoration that adds consistent spacings between items in RecyclerView
 * with GridLayoutManager preserving equal size of all children across non-scrolling axis.
 */
public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

	private final int spacing;

	private List<Pair<Integer, Integer>> calculatedSpacings;
	private int parentSizeBase;
	private int spanCountBase;


	/**
	 * Creates spacing decoration without applying screen density to the spacing argument.
	 *
	 * @param spacing - space between items in RecyclerView
	 */
	public GridSpacingItemDecoration(int spacing) {
		this.spacing = spacing;
	}


	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

		if (!(parent.getLayoutManager() instanceof GridLayoutManager)) {
			throw new UnsupportedOperationException("GridSpacingItemDecoration can only be used in combination with GridLayoutManager.");
		}

		GridLayoutManager manager = ((GridLayoutManager) parent.getLayoutManager());

		switch(manager.getOrientation()) {
			case GridLayoutManager.VERTICAL:
				this.getItemOffsetVertical(outRect, parent, view);
				break;

			case GridLayoutManager.HORIZONTAL:
				this.getItemOffsetHorizontal(outRect, parent, view);
				break;
		}
	}

	private void getItemOffsetVertical(Rect outRect, RecyclerView parent, View child) {

		int parentSize = parent.getWidth();
		GridLayoutManager manager = ((GridLayoutManager) parent.getLayoutManager());
		GridLayoutManager.LayoutParams layoutParams = ((GridLayoutManager.LayoutParams) child.getLayoutParams());
		int childAdapterPosition = parent.getChildAdapterPosition(child);

		List<Pair<Integer, Integer>> calculatedSpacings = this.getCalculatedSpacings(parentSize, manager.getSpanCount());
		Pair<Integer, Integer> spacing = calculatedSpacings.get(layoutParams.getSpanIndex());

		if (childAdapterPosition < manager.getSpanCount()) {
			// Apply horizontal spacing only to the first row
			outRect.set(spacing.first, 0, spacing.second, 0);
		} else {
			// Add vertical spacing in addition to horizontal spacings
			outRect.set(spacing.first, this.spacing, spacing.second, 0);
		}
	}

	private void getItemOffsetHorizontal(Rect outRect, RecyclerView parent, View child) {

		int parentSize = parent.getHeight();
		GridLayoutManager manager = ((GridLayoutManager) parent.getLayoutManager());
		GridLayoutManager.LayoutParams layoutParams = ((GridLayoutManager.LayoutParams) child.getLayoutParams());
		int childAdapterPosition = parent.getChildAdapterPosition(child);

		List<Pair<Integer, Integer>> calculatedSpacings = this.getCalculatedSpacings(parentSize, manager.getSpanCount());
		Pair<Integer, Integer> spacing = calculatedSpacings.get(layoutParams.getSpanIndex());

		if (childAdapterPosition < manager.getSpanCount()) {
			// Apply vertical spacing only to the first column
			outRect.set(0, spacing.first, 0, spacing.second);
		} else {
			// Add horizontal spacing in addition to vertical spacings
			outRect.set(this.spacing, spacing.first, 0, spacing.second);;
		}
	}

	private List<Pair<Integer, Integer>> getCalculatedSpacings(int parentSize, int spanCount) {

		if (this.parentSizeBase == parentSize && this.spanCountBase == spanCount) {
			// Cached value is valid here
			return this.calculatedSpacings;
		} else {
			// Recalculation needs to happen
			List<Pair<Integer, Integer>> calculatedSpacings = this.calculateSpacings(parentSize, spanCount);
			this.parentSizeBase = parentSize;
			this.spanCountBase = spanCount;
			this.calculatedSpacings = calculatedSpacings;
			return calculatedSpacings;
		}
	}

	private List<Pair<Integer, Integer>> calculateSpacings(float parentSize, int spanCount) {

		// We need at least 2 children next to each other to place spacing between them
		if (spanCount < 2) {
			return Collections.singletonList(Pair.create(0, 0));
		}

		List<Pair<Integer, Integer>> newSpacings = new ArrayList<>();

		float normalSpanSize = parentSize / spanCount;
		float spacedSpanSize = (parentSize - (spanCount - 1) * this.spacing) / spanCount;

		int firstItemSpacing = Math.round(normalSpanSize - spacedSpanSize);

		// First item in a row will have right spacing only
		newSpacings.add(Pair.create(0, firstItemSpacing));

		// Remember how much spacing left to consume
		int remainingSpacingFromLastPass = this.spacing - firstItemSpacing;

		for (int i = 1; i < spanCount; i++) {

			// Calculate spacing for this span position
			int left = remainingSpacingFromLastPass;
			int right = Math.round(normalSpanSize - left - spacedSpanSize);
			newSpacings.add(Pair.create(left, right));

			// Write remaining spacing
			remainingSpacingFromLastPass = this.spacing - right;
		}

		return newSpacings;
	}
}