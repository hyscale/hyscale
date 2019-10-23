package io.hyscale.servicespec.model;

import java.lang.reflect.Field;

import io.hyscale.servicespec.annotations.StrategicMergePatch;

/**
 * Get field metadata details based on annotation
 * @author tushart
 *
 */
public class AnnotationFieldDataProvider implements FieldMetaDataProvider {

	private Class klazz;

	public AnnotationFieldDataProvider(Class klazz) {
		super();
		this.klazz = klazz;
	}

	@Override
	public FieldMetaData getMetaData(String field) {
		// For given field, return merge key information
		FieldMetaData fieldMetaData = new FieldMetaData();
		try {
			Field keyObjectField = klazz.getDeclaredField(field);
			StrategicMergePatch patchMergeKey = keyObjectField.getAnnotation(StrategicMergePatch.class);
			fieldMetaData.setKey(patchMergeKey.key());
		} catch (NoSuchFieldException | SecurityException e) {
			return null;
		}
		return fieldMetaData;
	}

}
