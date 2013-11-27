package uk.ac.cam.gt319.fjava.tick0;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import uk.ac.cam.gt319.fjava.tick0.ExternalSort.Pair;

public class ExternalSort {
  
//  private final int CHUNK_SIZE_IN_BYTES = 1024;
  
	public static void sort(String a, String b) throws FileNotFoundException, IOException {
	  RandomAccessFile a1 = new RandomAccessFile(a, "rw");
    RandomAccessFile b1 = new RandomAccessFile(b, "rw");
    if (a1.length() == 0) return;
    
    a1.seek(0);
    DataInputStream in = buildDataInputStream(a1);
    DataOutputStream out = new DataOutputStream(
        new BufferedOutputStream(
        new FileOutputStream(b1.getFD())));
    Queue<Integer> readIn = new PriorityQueue<Integer>();
    List<Long> beginningPositions = new ArrayList<Long>();
    beginningPositions.add(0L);
    long bytesRead = 0L;
    long bytesReadIncrement = 0L;
    int oomFail = 0;
    boolean oomFailed = false;
    // Form streams of sorted integers. 
    while (bytesReadIncrement != a1.length()) {
      while (true) {
        try {
          oomFail = in.readInt();
          bytesReadIncrement += 4;
          readIn.add(oomFail);
        } catch (EOFException e) {
          break;
        } catch (OutOfMemoryError e) {
          oomFailed = true;
          break;
        }
      }
      while(!readIn.isEmpty()){
        out.writeInt(readIn.poll());
      }
      out.flush();
      long inFilePointer = a1.getFilePointer();
      long outFilePointer = b1.getFilePointer();
      
      beginningPositions.add(outFilePointer);
      if (oomFailed) {
        readIn.add(oomFail);
        oomFailed = false;
      }
    }
    
    // Free memory!
    readIn = null;
//    in.close();
    in = null;
//    out.close();
    out = null;
    
    // If there is only one block, they are all sorted. Rename the file and return.
    if (beginningPositions.size() == 1) {
      a1.close();
      a1 = null;
      b1.close();
      b1 = null;
      File file = new File(b);
      File file2 = new File(a);
      file2.delete();
      return;
    }
    
    // Perform the n-way merge.
    Queue<Pair<Integer, Byte>> pq =
       new PriorityQueue<Pair<Integer, Byte>>(beginningPositions.size());
    
    // Initialise queue
    
    List<DataInputStream> streams = new ArrayList<DataInputStream>();

    long[] bytesToRead = new long[beginningPositions.size()-1];
    for (int i = 0; i < bytesToRead.length; i++) {
      bytesToRead[i] = beginningPositions.get(i+1) - beginningPositions.get(i);
    }
    
    for (int i = 0; i < beginningPositions.size() - 1; i++) {
      RandomAccessFile f = new RandomAccessFile (b, "r");
      f.seek(beginningPositions.get(i));
      DataInputStream inStream = buildDataInputStream(f);
      streams.add(inStream);
      pq.add(new Pair<Integer, Byte>(inStream.readInt(), (byte) i));
      bytesToRead[i] -= 4;
    }
    
    a1.seek(0);
    out = buildDataOutputStream(a1);
    
    Pair<Integer, Byte> popped;
    int index;
    // while the queue is not empty.
    while (!pq.isEmpty()) {
      popped = pq.poll();
      index = popped.getValue();
      out.writeInt(popped.getKey());
      try {
        // If we are not at the beginning of a new block.
        if (bytesToRead[index] >= 4) {
          bytesToRead[index] -= 4;
          pq.add(new Pair<Integer, Byte>(streams.get(index).readInt(), (byte) index));
        } else {
//          System.out.println("end of block");
        }
      } catch (EOFException e) {
        e.printStackTrace();
//        System.out.println("end of   file " + popped.getValue() + " " + popped.getKey());
        // We've reached the end of the file - do nothing.
      }
    }
    out.flush();
	}
	
	private static DataOutputStream buildDataOutputStream(RandomAccessFile f) throws IOException {
	  DataOutputStream d = new DataOutputStream(
	      new BufferedOutputStream(new FileOutputStream(f.getFD())));
	  return d;
	}
  
  private static DataInputStream buildDataInputStream(RandomAccessFile f) throws IOException {
    DataInputStream d = new DataInputStream(
        new BufferedInputStream(new FileInputStream(f.getFD())));
    return d;
  }
	
	private static String byteToHex(byte b) {
		String r = Integer.toHexString(b);
		if (r.length() == 8) {
			return r.substring(6);
		}
		return r;
	}

	public static String checkSum(String f) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			DigestInputStream ds = new DigestInputStream(
					new FileInputStream(f), md);
			byte[] b = new byte[512];
			while (ds.read(b) != -1)
			  ;
			ds.close();
			String computed = "";
			for(byte v : md.digest()) 
				computed += byteToHex(v);

			return computed;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "<error computing checksum>";
	}

	public static void main(String[] args) throws Exception {
//	  testMethod();
//	  for (int i = 1; i < 18; i++) {
//	    int TESTCASE = i;
//	    String f1 = "/Users/George/Downloads/test-suite/test" + TESTCASE + "a.dat";
//	    String f2 = "/Users/George/Downloads/test-suite/test" + TESTCASE + "b.dat";
//	    String f1Copy = f1 + ".copy";
//	    String f2Copy = f2 + ".copy";
//	    copyFileUsingChannel(new File(f1), new File(f1Copy));
//	    copyFileUsingChannel(new File(f2), new File(f2Copy));
//	    sort(f1Copy, f2Copy);
//	    System.out.println(i + " : " + checkSum(f1Copy));
//	  }
    String f1 = args[0];
    String f2 = args[1];
    sort(f1, f2);
    System.out.println("The checksum is: " + checkSum(f1));
	}
	
	private static void testMethod() {
	  Queue<Pair<Integer, Byte>> pq = new PriorityQueue<Pair<Integer, Byte>>();
	  pq.add(new Pair<Integer, Byte>(2, (byte) 1));
    pq.add(new Pair<Integer, Byte>(3, (byte) 10));
    pq.add(new Pair<Integer, Byte>(1, (byte) 100));
    
    while(!pq.isEmpty()){
      System.out.println(pq.poll().getKey());
    }
	}
	
  private static void copyFileUsingChannel(File source, File dest) throws IOException {
    FileChannel sourceChannel = null;
    FileChannel destChannel = null;
    try {
      sourceChannel = new FileInputStream(source).getChannel();
      destChannel = new FileOutputStream(dest).getChannel();
      destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
    } finally {
      sourceChannel.close();
      destChannel.close();
    }
  }
	
  public static class Pair<A extends Comparable<A>, B> implements Comparable<Pair> {

    private final A key;
    private final B value;
    
    public Pair(A key, B value) {
      this.key = key;
      this.value = value;
    }
    
    public A getKey() {
      return key;
    }

    public B getValue() {
      return value;
    }
    
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((key == null) ? 0 : key.hashCode());
      result = prime * result + ((value == null) ? 0 : value.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Pair other = (Pair) obj;
      if (key == null) {
        if (other.key != null)
          return false;
      } else if (!key.equals(other.key))
        return false;
      if (value == null) {
        if (other.value != null)
          return false;
      } else if (!value.equals(other.value))
        return false;
      return true;
    }
    
    @Override
    public int compareTo(Pair p) {
      return this.key.compareTo((A) p.getKey());
    }
  }
	
}
