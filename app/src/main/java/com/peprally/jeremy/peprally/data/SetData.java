package com.peprally.jeremy.peprally.data;

import android.os.Parcel;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SetData {
	
	private Set<String> values;

	SetData(Parcel in) {
		values = new HashSet<>(Arrays.asList(in.createStringArray()));
	}

	SetData(String item) {
		values = new HashSet<>(Collections.singleton(item));
	}

	void addItem(String item) {
		values.add(item);
	}

	void removeItem(String item) {
		values.remove(item);
	}

	boolean isEmpty() {
		return values == null || values.isEmpty();
	}

	public boolean contains(String item) {
        return values.contains(item);
    }

	String[] toArray() {
		return values.toArray(new String[values.size()]);
	}
	
	public Set<String> getValues() {
		return values;
	}
}
