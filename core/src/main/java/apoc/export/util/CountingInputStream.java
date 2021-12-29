package apoc.export.util;

import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.channels.SeekableByteChannel;

/**
 * @author mh
 * @since 22.05.16
 */
public class CountingInputStream extends FilterInputStream implements SizeCounter {
    public static final int BUFFER_SIZE = 1024 * 1024;
    private final long total;
    private long count=0;
    private long newLines;

    public CountingInputStream(File file) throws FileNotFoundException {
        super(new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE));
        this.total = file.length();
    }
    public CountingInputStream(InputStream stream, long total) throws FileNotFoundException {
        super(new BufferedInputStream(stream, BUFFER_SIZE));
        this.total = total;
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        int read = super.read(buf, off, len);
        count+=read;
        for (int i=off;i<off+len;i++) {
            if (buf[i] == '\n') newLines++;
        }
        return read;
    }

    @Override
    public int read() throws IOException {
        count++;
        int read = super.read();
        if (read == '\n') newLines++;
        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        count += n;
        return super.skip(n);
    }

    public long getCount() {
        return count;
    }

    public long getNewLines() {
        return newLines;
    }

    public long getTotal() {
        return total;
    }

    @Override
    public long getPercent() {
        if (total <= 0) return 0;
        return count*100 / total;
    }

    public InputStream getStream() {
	   return in;
    }

	public CountingReader asReader() throws IOException {
		Reader reader = new InputStreamReader(in,"UTF-8");
        return new CountingReader(reader,total);
	}

	public SeekableByteChannel asChannel() throws IOException {
        return new SeekableInMemoryByteChannel(this.readAllBytes());
    }
}