package io.hyscale.commons.models;

import java.util.ArrayList;

public class DecoratedArrayList<E> extends ArrayList<E> {

	public boolean isNotEmpty() {
		return !this.isEmpty();
	}
}