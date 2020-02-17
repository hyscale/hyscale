/**
 * Copyright 2019 Pramati Prism, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.hyscale.commons.logger;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Create Formatted table for display
 * Use {@link Builder} to create TableFormatter with fields
 * Allows row addition
 * Print Table
 */
//TODO Table Field Constraint
public class TableFormatter {

	private static final String NEW_LINE = "\n";

	private static final String BLANK_STRING = "···";

	private static final String DASH = "---";

	private final String PERCENTAGE = "%";

	private final String formatter;

	private List<TableField> fields = new ArrayList<TableField>();

	private List<TableRow> tableRows = new ArrayList<TableRow>();

	private TableFormatter(List<TableField> fields) {
		this.fields = fields;
		StringBuilder formatBuilder = new StringBuilder();

		// Create table format
		for (int i = 0; i < fields.size(); i++) {
			formatBuilder.append(PERCENTAGE).append(Integer.toString(i + 1))
					.append(getformatPadString(fields.get(i).getLength()));
		}
		formatBuilder.append(NEW_LINE);
		this.formatter = formatBuilder.toString();
	}

	public TableFormatter addRow(TableRow row) {
		if (row == null) {
			return this;
		}
		this.tableRows.add(row);
		return this;
	}

	public TableFormatter addRow(String[] rowData) {
		if (rowData == null) {
			return this;
		}
		TableRow tableRow = new TableRow();
		tableRow.setRowData(rowData);
		this.tableRows.add(tableRow);
		return this;
	}
	
	private String getformatPadString(int padding) {
		return "$-" + padding + "s\t";
	}
	
	
	@Override
	public String toString() {
		StringBuilder table = new StringBuilder();
		table.append(getFormattedFields());
		table.append(NEW_LINE);
		this.tableRows.stream().forEach(each -> {
			table.append(getFormattedRow(each.getRowData()));
			table.append(NEW_LINE);
		});
		return table.toString();
	}
	
	/**
	 * 
	 * @return Formatted fields as String in format
	 * {field1 field2
	 * 	 ---	---}
	 * 
	 */
	public String getFormattedFields() {
		String[] fieldArray = fields.stream().map(each -> each.getName()).toArray(String[]::new);
		String[] fieldDemarcater = fields.stream().map(each -> DASH).toArray(String[]::new);
		
		StringBuilder sb = new StringBuilder();
		sb.append(getFormattedString(fieldArray)).append(getFormattedString(fieldDemarcater));
		
		return sb.toString();
	}

	private String getFormattedString(String[] toFormat) {
		return String.format(formatter, toFormat);
	}

	/**
	 * Format row data for number of fields
	 * Replace null with empty string
	 * Ignore rows which doesnot have fields
	 * 
	 * @param originalRow - String array
	 * @return formatted row as String
	 */
	public String getFormattedRow(String[] originalRow) {
		int length = fields.size();
		String[] updatedRow = new String[length ];

		for (int i = 0; i < length; i++) {
			if (i >= originalRow.length || originalRow[i] == null) {
				updatedRow[i] = BLANK_STRING;
			} else {
				updatedRow[i] = originalRow[i];
			}
		}

		return getFormattedString(updatedRow);
	}

	/**
	 * Builder class to create TableFormatter Instance
	 *
	 */
	public static class Builder {

		private List<TableField> fields;

		public Builder() {
			fields = new ArrayList<TableField>();
		}

		public Builder addField(String name) {
			if (StringUtils.isBlank(name)) {
				return this;
			}
			TableField field = new TableField();
			field.setName(name);
			fields.add(field);
			return this;
		}

		public Builder addField(String name, Integer length) {
			if (StringUtils.isBlank(name)) {
				return this;
			}
			TableField field = new TableField();
			field.setName(name);
			field.setLength(length);
			fields.add(field);
			return this;
		}

		public Builder addField(TableField field) {
			if (field == null) {
				return this;
			}
			fields.add(field);
			return this;
		}

		public TableFormatter build() {
			return new TableFormatter(this.fields);
		}
	}

}
