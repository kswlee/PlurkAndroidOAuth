package com.plurk.android;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
 
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;
 
import android.util.Log;
 
public class ThinMultipartEntity implements HttpEntity {
 
  private final static char[] MULTIPART_CHARS = 
    "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    .toCharArray();
 
  private String boundary = null;
 
  ByteArrayOutputStream out = new ByteArrayOutputStream();
  boolean isSetLast = false;
  boolean isSetFirst = false;
 
  public ThinMultipartEntity() {
    StringBuffer buf = new StringBuffer();
    Random rand = new Random();
    for (int i = 0; i < 30; i++) {
      buf.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
    }
    this.boundary = buf.toString();
 
  }
 
  public void writeFirstBoundary() {
    if (!isSetFirst) {
      try {
        out.write(("--" + boundary + "\r\n").getBytes());
      } catch (IOException e) {
        Log.e("ThinMultipartEntity", e.getMessage());
        e.printStackTrace();
      }
    }
    isSetFirst = true;
  }
 
  public void writeLastBoundary() {
    if (isSetLast) {
      return;
    }
    try {
      out.write(("\r\n--" + boundary + "--\r\n").getBytes());
    } catch (IOException e) {
      Log.e("ThinMultipartEntity", e.getMessage());
      e.printStackTrace();
    }
    isSetLast = true;
  }
 
  public void addPart(String key, String value) {
    writeFirstBoundary();
    try {
      out.write(("Content-Disposition: form-data; name=\"" 
        + key + "\"\r\n").getBytes());
      out.write("Content-Type: text/plain; charset=UTF-8\r\n".getBytes());
      out.write("Content-Transfer-Encoding: 8bit\r\n\r\n".getBytes());
      out.write(value.getBytes());
      out.write(("\r\n--" + boundary + "\r\n").getBytes());
    } catch (IOException e) {
      Log.e("ThinMultipartEntity", e.getMessage());
      e.printStackTrace();
    }
  }
 
  public void addPart(String key, File value) {
    writeFirstBoundary();
    try {
      out.write(("Content-Disposition: form-data; name=\"" + key
          + "\"; filename=\"" + value.getName() + "\"\r\n")
          .getBytes());
      out.write("Content-Type: application/octet-stream\r\n".getBytes());
      out.write("Content-Transfer-Encoding: binary\r\n\r\n".getBytes());
 
      FileInputStream fin = new FileInputStream(value);
      int data = fin.read();
      while (data != -1) {
        out.write(data);
        data = fin.read();
      }
 
    } catch (IOException e) {
      Log.e("ThinMultipartEntity", e.getMessage());
      e.printStackTrace();
    }
  }
 
  public long getContentLength() {
    writeLastBoundary();
    return out.toByteArray().length;
  }
 
  public Header getContentType() {
    return new BasicHeader("Content-Type", "multipart/form-data; boundary="
        + boundary);
  }
 
  public boolean isChunked() {
    return false;
  }
 
  public boolean isRepeatable() {
    return false;
  }
 
  public boolean isStreaming() {
    return false;
  }
 
  public void writeTo(OutputStream outstream) throws IOException {
    outstream.write(out.toByteArray());
  }
 
  public Header getContentEncoding() {
    return null;
  }
 
  public void consumeContent() throws IOException,
      UnsupportedOperationException {
    throw new UnsupportedOperationException(
        "Streaming entity does not implement #consumeContent()");
  }
 
  public InputStream getContent() throws IOException,
      UnsupportedOperationException {
    throw new UnsupportedOperationException(
        "Multipart form entity does not implement #getContent()");
  }
 
}