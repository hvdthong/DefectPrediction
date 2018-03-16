import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A {@link LockFactory} that wraps another {@link
 * LockFactory} and verifies that each lock obtain/release
 * is "correct" (never results in two processes holding the
 * lock at the same time).  It does this by contacting an
 * external server ({@link LockVerifyServer}) to assert that
 * at most one process holds the lock at a time.  To use
 * this, you should also run {@link LockVerifyServer} on the
 * host & port matching what you pass to the constructor.
 *
 * @see LockVerifyServer
 * @see LockStressTest
 */

public class VerifyingLockFactory extends LockFactory {

  LockFactory lf;
  byte id;
  String host;
  int port;

  private class CheckedLock extends Lock {
    private Lock lock;

    public CheckedLock(Lock lock) {
      this.lock = lock;
    }

    private void verify(byte message) {
      try {
        Socket s = new Socket(host, port);
        OutputStream out = s.getOutputStream();
        out.write(id);
        out.write(message);
        InputStream in = s.getInputStream();
        int result = in.read();
        in.close();
        out.close();
        s.close();
        if (result != 0)
          throw new RuntimeException("lock was double acquired");
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    public synchronized boolean obtain(long lockWaitTimeout)
      throws LockObtainFailedException, IOException {
      boolean obtained = lock.obtain(lockWaitTimeout);
      if (obtained)
        verify((byte) 1);
      return obtained;
    }

    public synchronized boolean obtain()
      throws LockObtainFailedException, IOException {
      return lock.obtain();
    }

    public synchronized boolean isLocked() {
      return lock.isLocked();
    }

    public synchronized void release() throws IOException {
      if (isLocked()) {
        verify((byte) 0);
        lock.release();
      }
    }
  }

  /**
   * @param id should be a unique id across all clients
   * @param lf the LockFactory that we are testing
   * @param host host or IP where {@link LockVerifyServer}
            is running
   * @param port the port {@link LockVerifyServer} is
            listening on
  */
  public VerifyingLockFactory(byte id, LockFactory lf, String host, int port) throws IOException {
    this.id = id;
    this.lf = lf;
    this.host = host;
    this.port = port;
  }

  public synchronized Lock makeLock(String lockName) {
    return new CheckedLock(lf.makeLock(lockName));
  }

  public synchronized void clearLock(String lockName)
    throws IOException {
    lf.clearLock(lockName);
  }
}