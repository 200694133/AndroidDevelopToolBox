package com.hanyanan.tools.datapersistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hanyanan on 2014/7/14.
 */
public class SafeFileInputStream extends FileInputStream {
    public SafeFileInputStream(File file) throws FileNotFoundException {
        super(file);
    }
}
