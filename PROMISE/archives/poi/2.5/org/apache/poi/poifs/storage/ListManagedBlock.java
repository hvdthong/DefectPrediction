   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at


   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        

package org.apache.poi.poifs.storage;

import java.io.IOException;

/**
 * An interface for blocks managed by a list that works with a
 * BlockAllocationTable to keep block sequences straight
 *
 * @author Marc Johnson (mjohnson at apache dot org
 */

public interface ListManagedBlock
{

    /**
     * Get the data from the block
     *
     * @return the block's data as a byte array
     *
     * @exception IOException if there is no data
     */

    public byte [] getData()
        throws IOException;
