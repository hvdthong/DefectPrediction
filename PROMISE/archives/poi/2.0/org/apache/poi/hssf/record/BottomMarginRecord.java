package org.apache.poi.hssf.record;



import org.apache.poi.util.*;

/**
 * Record for the bottom margin.
 * NOTE: This source was automatically generated.
 * @author Shawn Laubach (slaubach at apache dot org)
 */
public class BottomMarginRecord
	 extends Record implements Margin
{
	 public final static short      sid                             = 0x29;
	 private  double     field_1_margin;


	 public BottomMarginRecord()
	 {

	 }

	 /**
	  * Constructs a BottomMargin record and sets its fields appropriately.
	  *
	  * @param id    id must be 0x29 or an exception
	  *              will be throw upon validation
	  * @param size  size the size of the data area of the record
	  * @param data  data of the record (should not contain sid/len)
	  */

	 public BottomMarginRecord(short id, short size, byte [] data)
	 {
		  super(id, size, data);
	 }

	 /**
	  * Constructs a BottomMargin record and sets its fields appropriately.
	  *
	  * @param id    id must be 0x29 or an exception
	  *              will be throw upon validation
	  * @param size  size the size of the data area of the record
	  * @param data  data of the record (should not contain sid/len)
	  * @param offset of the record's data
	  */

	 public BottomMarginRecord(short id, short size, byte [] data, int offset)
	 {
		  super(id, size, data, offset);
	 }

	 /**
	  * Checks the sid matches the expected side for this record
	  *
	  * @param id   the expected sid.
	  */
	 protected void validateSid(short id)
	 {
		  if (id != sid)
		  {
				throw new RecordFormatException("Not a BottomMargin record");
		  }
	 }

	 protected void fillFields(byte [] data, short size, int offset)
	 {
		  field_1_margin                  = LittleEndian.getDouble(data, 0x0 + offset);

	 }

	 public String toString()
	 {
		  StringBuffer buffer = new StringBuffer();

		  buffer.append("[BottomMargin]\n");

		  buffer.append("    .margin               = ")
				.append(" (").append(getMargin()).append(" )\n");

		  buffer.append("[/BottomMargin]\n");
		  return buffer.toString();
	 }

	 public int serialize(int offset, byte[] data)
	 {
		  LittleEndian.putShort(data, 0 + offset, sid);
		  LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

		  LittleEndian.putDouble(data, 4 + offset, field_1_margin);

		  return getRecordSize();
	 }

	 /**
	  * Size of record (exluding 4 byte header)
	  */
	 public int getRecordSize()
	 {
		  return 4  + 8;
	 }

	 public short getSid()
	 {
		  return this.sid;
	 }


	 /**
	  * Get the margin field for the BottomMargin record.
	  */
	 public double getMargin()
	 {
		  return field_1_margin;
	 }

	 /**
	  * Set the margin field for the BottomMargin record.
	  */
	 public void setMargin(double field_1_margin)
	 {
		  this.field_1_margin = field_1_margin;
	 }
    
	  public Object clone() {
		  BottomMarginRecord rec = new BottomMarginRecord();
		  rec.field_1_margin = this.field_1_margin;
		  return rec;
	 }

