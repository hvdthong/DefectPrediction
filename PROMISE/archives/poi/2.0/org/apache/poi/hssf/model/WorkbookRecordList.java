package org.apache.poi.hssf.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.Record;

public class WorkbookRecordList
{
    private List records = new ArrayList();



    public void setRecords( List records )
    {
        this.records = records;
    }

    public int size()
    {
        return records.size();
    }

    public Record get( int i )
    {
        return (Record) records.get(i);
    }

    public void add( int pos, Record r )
    {
        records.add(pos, r);
        if (getProtpos() >= pos) setProtpos( protpos + 1 );
        if (getBspos() >= pos) setBspos( bspos + 1 );
        if (getTabpos() >= pos) setTabpos( tabpos + 1 );
        if (getFontpos() >= pos) setFontpos( fontpos + 1 );
        if (getXfpos() >= pos) setXfpos( xfpos + 1 );
        if (getBackuppos() >= pos) setBackuppos( backuppos + 1 );
        if (getNamepos() >= pos) setNamepos(namepos+1);
        if (getSupbookpos() >= pos) setSupbookpos(supbookpos+1);
        if ((getPalettepos() != -1) && (getPalettepos() >= pos)) setPalettepos( palettepos + 1 );
        if (getExternsheetPos() >= pos) setExternsheetPos(getExternsheetPos() + 1);
    }

    public List getRecords()
    {
        return records;
    }

    public Iterator iterator()
    {
        return records.iterator();
    }

    public void remove( int pos )
    {
        records.remove(pos);
        if (getProtpos() >= pos) setProtpos( protpos - 1 );
        if (getBspos() >= pos) setBspos( bspos - 1 );
        if (getTabpos() >= pos) setTabpos( tabpos - 1 );
        if (getFontpos() >= pos) setFontpos( fontpos - 1 );
        if (getXfpos() >= pos) setXfpos( xfpos - 1 );
        if (getBackuppos() >= pos) setBackuppos( backuppos - 1 );
        if (getNamepos() >= pos) setNamepos(getNamepos()-1);
        if (getSupbookpos() >= pos) setSupbookpos(getSupbookpos()-1);
        if ((getPalettepos() != -1) && (getPalettepos() >= pos)) setPalettepos( palettepos - 1 );
        if (getExternsheetPos() >= pos) setExternsheetPos( getExternsheetPos() -1);
    }

    public int getProtpos()
    {
        return protpos;
    }

    public void setProtpos( int protpos )
    {
        this.protpos = protpos;
    }

    public int getBspos()
    {
        return bspos;
    }

    public void setBspos( int bspos )
    {
        this.bspos = bspos;
    }

    public int getTabpos()
    {
        return tabpos;
    }

    public void setTabpos( int tabpos )
    {
        this.tabpos = tabpos;
    }

    public int getFontpos()
    {
        return fontpos;
    }

    public void setFontpos( int fontpos )
    {
        this.fontpos = fontpos;
    }

    public int getXfpos()
    {
        return xfpos;
    }

    public void setXfpos( int xfpos )
    {
        this.xfpos = xfpos;
    }

    public int getBackuppos()
    {
        return backuppos;
    }

    public void setBackuppos( int backuppos )
    {
        this.backuppos = backuppos;
    }

    public int getPalettepos()
    {
        return palettepos;
    }

    public void setPalettepos( int palettepos )
    {
        this.palettepos = palettepos;
    }

	
	/**
	 * Returns the namepos.
	 * @return int
	 */
	public int getNamepos() {
		return namepos;
	}

	/**
	 * Returns the supbookpos.
	 * @return int
	 */
	public int getSupbookpos() {
		return supbookpos;
	}

	/**
	 * Sets the namepos.
	 * @param namepos The namepos to set
	 */
	public void setNamepos(int namepos) {
		this.namepos = namepos;
	}

	/**
	 * Sets the supbookpos.
	 * @param supbookpos The supbookpos to set
	 */
	public void setSupbookpos(int supbookpos) {
		this.supbookpos = supbookpos;
	}

	/**
	 * Returns the externsheetPos.
	 * @return int
	 */
	public int getExternsheetPos() {
		return externsheetPos;
	}

	/**
	 * Sets the externsheetPos.
	 * @param externsheetPos The externsheetPos to set
	 */
	public void setExternsheetPos(int externsheetPos) {
		this.externsheetPos = externsheetPos;
	}

}
