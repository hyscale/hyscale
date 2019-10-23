package io.hyscale.servicespec.model;

/**
 * Defines metadata for a field
 * like key used to distinguish objects
 * 
 * @author tushart
 *
 */
public class FieldMetaData {

	// replace/ merge strategy can be added
	private String key;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
