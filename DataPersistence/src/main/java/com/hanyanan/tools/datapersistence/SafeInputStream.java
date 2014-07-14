package com.hanyanan.tools.datapersistence;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hanyanan on 2014/7/14.
 */
public class SafeInputStream extends InputStream {
    @Override
    public int read() throws IOException {
        return 0;
    }
}
