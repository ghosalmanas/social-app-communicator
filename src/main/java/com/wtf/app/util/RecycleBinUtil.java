package com.wtf.app.util;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinError;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Interface for SHEmptyRecycleBin function
interface Shell32Ext extends Shell32 {
    Shell32Ext INSTANCE = Native.load("shell32", Shell32Ext.class, W32APIOptions.DEFAULT_OPTIONS);
    
    // SHEmptyRecycleBin function
    int SHEmptyRecycleBin(
        WinNT.HWND hwnd,
        String pszRootPath,
        int dwFlags
    );
    
    // SHQueryRecycleBin function
    int SHQueryRecycleBin(
        String pszRootPath,
        SHQUERYRBINFO pQueryInfo
    );
    
    // SHQUERYRBINFO structure
    public static class SHQUERYRBINFO extends com.sun.jna.Structure {
        public int cbSize;
        public long i64Size;
        public long i64NumItems;
        
        @Override
        protected java.util.List<String> getFieldOrder() {
            return java.util.Arrays.asList("cbSize", "i64Size", "i64NumItems");
        }
    }
}

/**
 * Utility class for managing the Windows Recycle Bin.
 */
public class RecycleBinUtil {
    private static final Logger logger = LoggerFactory.getLogger(RecycleBinUtil.class);
    private static final int SHERB_NOCONFIRMATION = 0x00000001;
    private static final int SHERB_NOPROGRESSUI = 0x00000002;
    private static final int SHERB_NOSOUND = 0x00000004;

    /**
     * Empties the Recycle Bin for all drives.
     * @return true if successful, false otherwise
     */
    public static boolean emptyRecycleBin() {
        try {
            int flags = SHERB_NOCONFIRMATION | SHERB_NOPROGRESSUI | SHERB_NOSOUND;
            int result = Shell32Ext.INSTANCE.SHEmptyRecycleBin(
                null, // No window handle
                null, // No root directory
                flags // Flags
            );
            
            if (result != 0) {  // S_OK is 0
                logger.warn("Failed to empty Recycle Bin. Error code: {}", result);
                return false;
            }
            
            logger.info("Successfully emptied Recycle Bin");
            return true;
        } catch (Exception e) {
            logger.error("Error while emptying Recycle Bin: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Gets the size of the Recycle Bin in bytes.
     * @return Size in bytes, or -1 if an error occurs
     */
    public static long getRecycleBinSize() {
        try {
            Shell32Ext.SHQUERYRBINFO queryInfo = new Shell32Ext.SHQUERYRBINFO();
            queryInfo.cbSize = queryInfo.size();
            
            int result = Shell32Ext.INSTANCE.SHQueryRecycleBin(
                null, // All drives
                queryInfo
            );
            
            if (result == 0) {  // S_OK is 0
                return queryInfo.i64Size;
            } else {
                logger.warn("Failed to get Recycle Bin size. Error code: {}", result);
                return -1;
            }
        } catch (Exception e) {
            logger.error("Error while getting Recycle Bin size: {}", e.getMessage(), e);
            return -1;
        }
    }
}
