   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at


   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hssf.eventusermodel;

import java.io.InputStream;
import java.io.IOException;

import org.apache.poi.hssf.eventusermodel.HSSFUserException;
import org.apache.poi.hssf.record.RecordFormatException;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.RecordFactory;
import org.apache.poi.hssf.record.ContinueRecord;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Low level event based HSSF reader.  Pass either a DocumentInputStream to
 * process events along with a request object or pass a POIFS POIFSFileSystem to
 * processWorkbookEvents along with a request.
 *
 * This will cause your file to be processed a record at a time.  Each record with
 * a static id matching one that you have registed in your HSSFRequest will be passed
 * to your associated HSSFListener.
 *
 * @see org.apache.poi.hssf.dev.EFHSSF
 *
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Carey Sublette  (careysub@earthling.net)
 */

public class HSSFEventFactory
{
    /** Creates a new instance of HSSFEventFactory */

    public HSSFEventFactory()
    {
    }

    /**
     * Processes a file into essentially record events.
     *
     * @param req       an Instance of HSSFRequest which has your registered listeners
     * @param fs        a POIFS filesystem containing your workbook
     */

    public void processWorkbookEvents(HSSFRequest req, POIFSFileSystem fs)
        throws IOException
    {
        InputStream in = fs.createDocumentInputStream("Workbook");

        processEvents(req, in);
    }

    /**
	 * Processes a file into essentially record events.
	 *
	 * @param req       an Instance of HSSFRequest which has your registered listeners
	 * @param fs        a POIFS filesystem containing your workbook
	 * @return 			numeric user-specified result code.
	 */

	public short abortableProcessWorkbookEvents(HSSFRequest req, POIFSFileSystem fs)
		throws IOException, HSSFUserException
	{
		InputStream in = fs.createDocumentInputStream("Workbook");
		return abortableProcessEvents(req, in);
    }

    /**
     * Processes a DocumentInputStream into essentially Record events.
     *
     * If an <code>AbortableHSSFListener</code> causes a halt to processing during this call
     * the method will return just as with <code>abortableProcessEvents</code>, but no
     * user code or <code>HSSFUserException</code> will be passed back.
     *
     * @see org.apache.poi.poifs.filesystem.POIFSFileSystem#createDocumentInputStream(String)
     * @param req       an Instance of HSSFRequest which has your registered listeners
     * @param in        a DocumentInputStream obtained from POIFS's POIFSFileSystem object
     */

    public void processEvents(HSSFRequest req, InputStream in)
        throws IOException
	{
		try
		{
			genericProcessEvents(req, new RecordInputStream(in));
		}
		catch (HSSFUserException hue)
		{/*If an HSSFUserException user exception is thrown, ignore it.*/ }
	}


    /**
     * Processes a DocumentInputStream into essentially Record events.
     *
     * @see org.apache.poi.poifs.filesystem.POIFSFileSystem#createDocumentInputStream(String)
     * @param req       an Instance of HSSFRequest which has your registered listeners
     * @param in        a DocumentInputStream obtained from POIFS's POIFSFileSystem object
	 * @return 			numeric user-specified result code.
     */

    public short abortableProcessEvents(HSSFRequest req, InputStream in)
        throws IOException, HSSFUserException
    {
		return genericProcessEvents(req, new RecordInputStream(in));
    }

     /**
	 * Processes a DocumentInputStream into essentially Record events.
	 *
	 * @see org.apache.poi.poifs.filesystem.POIFSFileSystem#createDocumentInputStream(String)
	 * @param req       an Instance of HSSFRequest which has your registered listeners
	 * @param in        a DocumentInputStream obtained from POIFS's POIFSFileSystem object
	 * @return 			numeric user-specified result code.
	 */

	protected short genericProcessEvents(HSSFRequest req, RecordInputStream in)
		throws IOException, HSSFUserException
	{
		short userCode = 0;

		short sid = 0;
		process:
		{
                  
			Record rec       = null;

			while (in.hasNextRecord())
			{
                          in.nextRecord();

				sid = in.getSid();;
                
                if ( sid == 0 )
                    break;


				if ((rec != null) && (sid != ContinueRecord.sid))
				{
					userCode = req.processRecord(rec);
					if (userCode != 0) break process;
				}
				if (sid != ContinueRecord.sid)
				{
					Record[] recs = RecordFactory.createRecord(in);

					if (recs.length > 1)
						for (int k = 0; k < (recs.length - 1); k++)
							userCode = req.processRecord(
							if (userCode != 0) break process;
						}
					}

				}
				else
					{
                                  throw new RecordFormatException("Records should handle ContinueRecord internally. Should not see this exception");
				}
			}
			if (rec != null)
			{
				userCode = req.processRecord(rec);
				if (userCode != 0) break process;
			}
		}
		
		return userCode;

    }
}
