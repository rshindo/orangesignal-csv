/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.orangesignal.csv;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * {@link CsvResultSet} クラスの単体テストです。
 * 
 * @author Koji Sugisawa
 */
public class CsvResultSetTest {

	private static CsvConfig cfg;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		cfg = new CsvConfig(',', '"', '\\');
		cfg.setNullString("NULL");
		cfg.setBreakString("\n");
		cfg.setIgnoreTrailingWhitespaces(true);
		cfg.setIgnoreLeadingWhitespaces(true);
		cfg.setIgnoreEmptyLines(true);
		cfg.setIgnoreLinePatterns(Pattern.compile("^#.*$"));
	}

	@Test
	public void testCsvResultSet() throws IOException {
		final CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("field_name\r\nxxx")));
		rs.close();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCsvResultSetIllegalArgumentException() throws IOException {
		new CsvResultSet(null);
	}

	@Test(expected = IOException.class)
	public void testCsvResultSetIOException() throws IOException {
		new CsvResultSet(new CsvReader(new StringReader(""), cfg));
	}

	@Test(expected = SQLException.class)
	public void testEnsureOpen() throws Exception {
		final CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader(
				"code, market, name, price, date, time, datetime, active \r\n" +
				"9999, T1, OrangeSignal CSV test, NULL, 2009-01-01, 12:00:00, 2009-01-01 12:00:00, 0 \r\n" +
				"9999, T1, OrangeSignal CSV test, 500.05, 2009-01-01, 12:00:00, 2009-01-01 12:00:00, 1 \r\n"
			), cfg));
		rs.close();
		rs.next();
	}

	@Test
	public void test() throws Exception {
		final CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader(
				"code, market, name, price, date, time, datetime, active \r\n" +
				"9999, T1, OrangeSignal CSV test, NULL, 2009-01-01, 12:00:00, 2009-01-01 12:00:00, 0 \r\n" +
				"9999, T1, OrangeSignal CSV test, 500.05, 2009-01-01, 12:00:00, 2009-01-01 12:00:00, 1 \r\n"
			), cfg));
		try {
			assertThat(rs.getStatement(), nullValue());
			assertThat(rs.isClosed(), is(false));
			assertThat(rs.getFetchDirection(), is(ResultSet.FETCH_FORWARD));
			assertThat(rs.getFetchSize(), is(0));
			assertThat(rs.getType(), is(ResultSet.TYPE_FORWARD_ONLY));
			assertThat(rs.getConcurrency(), is(ResultSet.CONCUR_READ_ONLY));
			assertThat(rs.getHoldability(), is(ResultSet.HOLD_CURSORS_OVER_COMMIT));

			assertThat(rs.next(), is(true));
			assertThat(rs.getRow(), is(1));

			assertThat(rs.getString(1), is("9999"));
			assertThat(rs.wasNull(), is(false));
			assertThat(rs.getString("code"), is("9999"));
			assertThat(rs.wasNull(), is(false));

			assertThat(rs.getShort(1), is((short) 9999));
			assertThat(rs.getShort("code"), is((short) 9999));
			assertThat(rs.getInt(1), is(9999));
			assertThat(rs.getInt("code"), is(9999));

			assertThat(rs.getLong(4), is(0L));
			assertThat(rs.wasNull(), is(true));
			assertThat(rs.getLong("price"), is(0L));
			assertThat(rs.wasNull(), is(true));

			assertThat(rs.getBoolean(8), is(false));
			assertThat(rs.getBoolean("active"), is(false));

			assertThat(rs.getString(4), nullValue());
			assertThat(rs.wasNull(), is(true));
//			assertThat(rs.getString(1), is("aaa"));
//			assertThat(rs.getString("col1"), is("aaa"));

		} finally {
			rs.close();
			assertThat(rs.isClosed(), is(true));
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testGetBigDecimalIntInt() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.getBigDecimal(1, 0);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testGetUnicodeStreamInt() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.getUnicodeStream(1);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testGetBigDecimalStringInt() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.getBigDecimal("id", 0);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testGetUnicodeStreamString() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.getUnicodeStream("id");
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testGetCursorName() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.getCursorName();
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testIsBeforeFirst() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.isBeforeFirst();
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testIsAfterLast() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.isAfterLast();
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testIsFirst() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.isFirst();
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testIsLast() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.isLast();
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testBeforeFirst() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.beforeFirst();
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testAfterLast() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.afterLast();
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testFirst() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.first();
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testLast() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.last();
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testAbsolute() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.absolute(0);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testRelative() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.relative(0);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testPrevious() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.previous();
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testSetFetchDirection() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.setFetchDirection(ResultSet.FETCH_FORWARD);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testSetFetchSize() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.setFetchSize(0);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testRowUpdated() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.rowUpdated();
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testRowInserted() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.rowInserted();
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testRowDeleted() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.rowDeleted();
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateNullInt() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateNull(1);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateBooleanIntBoolean() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateBoolean(1, false);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateByteIntByte() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateByte(1, (byte) 0);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateShortIntShort() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateShort(1, (short) 0);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateIntIntInt() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateInt(1, 0);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateLongIntLong() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateLong(1, 0L);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateFloatIntFloat() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateFloat(1, 0F);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateDoubleIntDouble() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateDouble(1, 0D);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateBigDecimalIntBigDecimal() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateBigDecimal(1, null);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateStringIntString() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateString(1, null);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateBytesIntByteArray() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateBytes(1, null);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateDateIntDate() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateDate(1, null);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateTimeIntTime() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateTime(1, null);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateTimestampIntTimestamp() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateTimestamp(1, null);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateAsciiStreamIntInputStreamInt() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateAsciiStream(1, null, 0);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateBinaryStreamIntInputStreamInt() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateBinaryStream(1, null, 0);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateCharacterStreamIntReaderInt() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateCharacterStream(1, null, 0);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateObjectIntObjectInt() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateObject(1, null, 0);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateObjectIntObject() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateObject(1, null);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateNullString() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateNull("id");
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateBooleanStringBoolean() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateBoolean("id", false);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateByteStringByte() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateByte("id", (byte) 0);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateShortStringShort() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateShort("id", (short) 0);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateIntStringInt() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateInt("id", 0);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateLongStringLong() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateLong("id", 0L);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateFloatStringFloat() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateFloat("id", 0F);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateDoubleStringDouble() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateDouble("id", 0D);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateBigDecimalStringBigDecimal() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateBigDecimal("id", null);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateStringStringString() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateString("id", null);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateBytesStringByteArray() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateBytes("id", null);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateDateStringDate() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateDate("id", null);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateTimeStringTime() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateTime("id", null);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateTimestampStringTimestamp() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateTimestamp("id", null);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateAsciiStreamStringInputStreamInt() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateAsciiStream("id", null, 0);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateBinaryStreamStringInputStreamInt() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateBinaryStream("id", null, 0);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateCharacterStreamStringReaderInt() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateCharacterStream("id", new StringReader(""), 0);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateObjectStringObjectInt() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateObject("id", null, 0);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateObjectStringObject() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateObject("id", null);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testInsertRow() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.insertRow();
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateRow() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateRow();
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testDeleteRow() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.deleteRow();
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testRefreshRow() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.refreshRow();
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testCancelRowUpdates() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.cancelRowUpdates();
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testMoveToInsertRow() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.moveToInsertRow();
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testMoveToCurrentRow() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.moveToCurrentRow();
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testGetObjectIntMapOfStringClassOfQ() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.getObject(1, (Map<String, Class<?>>) null);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testGetRefInt() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.getRef(1);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testGetArrayInt() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.getArray(1);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testGetObjectStringMapOfStringClassOfQ() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.getObject("id", (Map<String, Class<?>>) null);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testGetRefString() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.getRef("id");
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateRefIntRef() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateRef(1, null);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateRefStringRef() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateRef("id", null);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateBlobIntBlob() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			final Blob blob = null;
			rs.updateBlob(1, blob);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateBlobStringBlob() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			final Blob blob = null;
			rs.updateBlob("id", blob);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateClobIntClob() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			final Clob clob = null;
			rs.updateClob(1, clob);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateClobStringClob() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			final Clob clob = null;
			rs.updateClob("id", clob);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateArrayIntArray() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateArray(1, null);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateArrayStringArray() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateArray("id", null);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testGetRowIdInt() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.getRowId(1);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testGetRowIdString() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.getRowId("id");
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateRowIdIntRowId() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateRowId(1, null);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateRowIdStringRowId() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateRowId("id", null);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateNStringIntString() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateNString(1, null);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateNStringStringString() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateNString("id", null);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateNClobIntNClob() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			final NClob nclob = null;
			rs.updateNClob(1, nclob);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateNClobStringNClob() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			final NClob nclob = null;
			rs.updateNClob("id", nclob);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testGetSQLXMLInt() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.getSQLXML(1);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testGetSQLXMLString() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.getSQLXML("id");
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateSQLXMLIntSQLXML() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateSQLXML(1, null);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateSQLXMLStringSQLXML() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateSQLXML("id", null);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateNCharacterStreamIntReaderLong() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateNCharacterStream(1, new StringReader(""), 0L);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateNCharacterStreamStringReaderLong() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateNCharacterStream("id", new StringReader(""), 0L);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateAsciiStreamIntInputStreamLong() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateAsciiStream(1, new ByteArrayInputStream("".getBytes()), 0L);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateBinaryStreamIntInputStreamLong() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateBinaryStream(1, new ByteArrayInputStream("".getBytes()), 0L);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateCharacterStreamIntReaderLong() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateCharacterStream(1, new StringReader(""), 0L);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateAsciiStreamStringInputStreamLong() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateAsciiStream("id", new ByteArrayInputStream("".getBytes()), 0L);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateBinaryStreamStringInputStreamLong() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateBinaryStream("id", new ByteArrayInputStream("".getBytes()), 0L);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateCharacterStreamStringReaderLong() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateCharacterStream("id", new StringReader(""), 0L);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateBlobIntInputStreamLong() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateBlob(1, new ByteArrayInputStream("".getBytes()), 0L);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateBlobStringInputStreamLong() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateBlob("id", new ByteArrayInputStream("".getBytes()), 0L);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateClobIntReaderLong() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateClob(1, new StringReader(""), 0L);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateClobStringReaderLong() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateClob("id", new StringReader(""), 0L);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateNClobIntReaderLong() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateNClob(1, new StringReader(""), 0L);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateNClobStringReaderLong() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateNClob("id", new StringReader(""), 0L);
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateNCharacterStreamIntReader() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateNCharacterStream(1, new StringReader(""));
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateNCharacterStreamStringReader() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateNCharacterStream("id", new StringReader(""));
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateAsciiStreamIntInputStream() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateAsciiStream(1, new ByteArrayInputStream("".getBytes()));
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateBinaryStreamIntInputStream() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateBinaryStream(1, new ByteArrayInputStream("".getBytes()));
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateCharacterStreamIntReader() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateCharacterStream(1, new StringReader(""));
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateAsciiStreamStringInputStream() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateAsciiStream("id", new ByteArrayInputStream("".getBytes()));
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateBinaryStreamStringInputStream() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateBinaryStream("id", new ByteArrayInputStream("".getBytes()));
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateCharacterStreamStringReader() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateCharacterStream("id", new StringReader(""));
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateBlobIntInputStream() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateBlob(1, new ByteArrayInputStream("".getBytes()));
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateBlobStringInputStream() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateBlob("id", new ByteArrayInputStream("".getBytes()));
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateClobIntReader() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateClob(1, new StringReader(""));
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateClobStringReader() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateClob("id", new StringReader(""));
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateNClobIntReader() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateNClob(1, new StringReader(""));
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUpdateNClobStringReader() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.updateNClob("id", new StringReader(""));
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testUnwrap() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.unwrap(this.getClass());
		}
	}

	@Test(expected = SQLFeatureNotSupportedException.class)
	public void testIsWrapperFor() throws Exception {
		try (CsvResultSet rs = new CsvResultSet(new CsvReader(new StringReader("id\r\nNULL"), cfg))) {
			rs.isWrapperFor(this.getClass());
		}
	}

}