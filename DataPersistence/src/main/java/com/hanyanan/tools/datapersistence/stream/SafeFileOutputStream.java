package com.hanyanan.tools.datapersistence.stream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by hanyanan on 2014/7/14.
 */
class SafeFileOutputStream extends FileOutputStream {
    public SafeFileOutputStream(File file) throws FileNotFoundException {
        super(file);
    }
}
