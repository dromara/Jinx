
package com.happylifeplat.netty.http;

import com.google.common.primitives.Ints;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>Description: .</p>
 * <p>Company: 深圳市旺生活互联网科技有限公司</p>
 * <p>Copyright: 2015-2017 happylifeplat.com All Rights Reserved</p>
 *
 * @author yu.xiao@happylifeplat.com
 * @version 1.0
 * @date 2017/5/12 16:46
 * @since JDK 1.8
 */
public class HttpContentInputStream extends ServletInputStream {

    private final Channel                    channel;
    private AtomicBoolean                    closed;
    private final BlockingQueue<HttpContent> queue;
    private HttpContent                      current;
    private ReadListener                     readListener;

    public HttpContentInputStream(Channel channel) {
        this.channel = checkNotNull(channel);
        this.closed = new AtomicBoolean();
        queue = new LinkedBlockingQueue<>();
    }

    public void addContent(HttpContent httpContent) {
        checkNotClosed();
        queue.offer(httpContent.retain());
        // TODO limit the number of queued inputs, stop handler from reading from channel
    }

    @Override
    public int readLine(byte[] b, int off, int len) throws IOException {
        checkNotNull(b);
        // TODO use ByteBuf native approach, i.e. bytesBefore, ByteBufProcessor
        return super.readLine(b, off, len);
    }

    @Override
    public boolean isFinished() {
        checkNotClosed();
        return isLastContent() && current.content().readableBytes() == 0;
    }

    private boolean isLastContent() {
        return current instanceof LastHttpContent;
    }

    @Override
    public boolean isReady() {
        checkNotClosed();
        return (current != null && current.content().readableBytes() > 0) || !queue.isEmpty();
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        checkNotClosed();
        checkNotNull(readListener);
        this.readListener = readListener;
    }

    @Override
    public long skip(long n) throws IOException {
        checkNotClosed();
        // TODO implement skip that doesn't read bytes
        return readContent(Ints.checkedCast(n)).readableBytes();
    }

    @Override
    public int available() throws IOException {
        return null == current ? 0 : current.content().readableBytes();
    }

    @Override
    public void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            // FIXME release the non-written HttpContents?
            queue.clear();
            current = null;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        checkNotNull(b);
        if (0 == len) {
            return 0;
        }
        poll();
        if (isFinished()) {
            return -1;
        }
        ByteBuf byteBuf = readContent(len);
        int readableBytes = byteBuf.readableBytes();
        byteBuf.readBytes(b, off, readableBytes);
        return readableBytes - byteBuf.readableBytes();
    }

    @Override
    public int read() throws IOException {
        poll();
        if (isFinished()) {
            return -1;
        }
        return readContent(1).getByte(0);
    }

    public ByteBuf readContent() throws IOException {
        poll();
        if (isFinished()) {
            return null;
        }
        ByteBuf content = current.content();
        // append request data if at readable
        if (current.content().readableBytes() > 0) {// readable
            if (content == null) {
                content = Unpooled.buffer();
            }
            content.writeBytes(current.content());
            current.release();
        }
        return content;
    }

    private ByteBuf readContent(int length) throws IOException {
        ByteBuf content = current.content();
        if (length < content.readableBytes()) {
            return content.readSlice(length);
        } else {
            return content;
        }
    }

    private void poll() throws IOException {
        checkNotClosed();
        if (null == current || current.content().readableBytes() == 0) {
            boolean blocking = null == readListener;
            while (!isLastContent()) {
                try {
                    // FIXME add appropriate timeout value
                    current = queue.poll(0, TimeUnit.NANOSECONDS);
                } catch (InterruptedException ignored) {
                }
                if (current != null || !blocking) {
                    break;
                }
                if (!channel.isActive()) {
                    throw new IOException("Channel is not active");
                }
            }
        }
    }

    private void checkNotClosed() {
        if (closed.get()) {
            throw new IllegalStateException("Stream is closed");
        }
    }
}
