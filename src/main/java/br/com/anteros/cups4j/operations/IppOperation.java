package br.com.anteros.cups4j.operations;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.anteros.cups4j.AnterosCupsClient;
import br.com.anteros.cups4j.ipp.attributes.Attribute;
import br.com.anteros.cups4j.ippclient.IppResponse;
import br.com.anteros.cups4j.ippclient.IppResult;
import br.com.anteros.cups4j.ippclient.IppTag;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Map;

public abstract class IppOperation {
  protected short operationID = -1; // IPP operation ID
  protected short bufferSize = 8192; // BufferSize for this operation
  protected int ippPort = AnterosCupsClient.DEFAULT_PORT;

  protected final static String IPP_MIME_TYPE = "application/ipp";
  private HttpPost httpCall;

  //
  private String httpStatusLine = null;
  private int httpStatusCode = -1;

  private static final Logger LOG = LoggerFactory.getLogger(IppOperation.class);

  /**
   * Gets the IPP header
   * 
   * @param url
   * @return IPP header
   * @throws UnsupportedEncodingException
   */
  public ByteBuffer getIppHeader(URL url) throws UnsupportedEncodingException {
    return getIppHeader(url, null);
  }

  public IppResult request(URL url, Map<String, String> map) throws Exception {
    return sendRequest(url, getIppHeader(url, map));
  }

  public IppResult request(URL url, Map<String, String> map, InputStream document) throws Exception {
    return sendRequest(url, getIppHeader(url, map), document);
  }

  /**
   * Gets the IPP header
   * 
   * @param url
   * @param map
   * @return IPP header
   * @throws UnsupportedEncodingException
   */
  public ByteBuffer getIppHeader(URL url, Map<String, String> map) throws UnsupportedEncodingException {
    if (url == null) {
      LOG.error("IppGetJObsOperation.getIppHeader(): uri is null");
      return null;
    }

    ByteBuffer ippBuf = ByteBuffer.allocateDirect(bufferSize);
    ippBuf = IppTag.getOperation(ippBuf, operationID);
    ippBuf = IppTag.getUri(ippBuf, "printer-uri", stripPortNumber(url));

    if (map == null) {
      ippBuf = IppTag.getEnd(ippBuf);
      ippBuf.flip();
      return ippBuf;
    }

    ippBuf = IppTag.getNameWithoutLanguage(ippBuf, "requesting-user-name", map.get("requesting-user-name"));

    if (map.get("limit") != null) {
      int value = Integer.parseInt(map.get("limit"));
      ippBuf = IppTag.getInteger(ippBuf, "limit", value);
    }

    if (map.get("requested-attributes") != null) {
      String[] sta = map.get("requested-attributes").split(" ");
      if (sta != null) {
        ippBuf = IppTag.getKeyword(ippBuf, "requested-attributes", sta[0]);
        int l = sta.length;
        for (int i = 1; i < l; i++) {
          ippBuf = IppTag.getKeyword(ippBuf, null, sta[i]);
        }
      }
    }

    ippBuf = IppTag.getEnd(ippBuf);
    ippBuf.flip();
    return ippBuf;
  }

  /**
   * Sends a request to the provided URL
   * 
   * @param url
   * @param ippBuf
   * 
   * @return result
   * @throws Exception 
   */
  private IppResult sendRequest(URL url, ByteBuffer ippBuf) throws Exception  {
    IppResult result = sendRequest(url, ippBuf, null);
    if (result.getHttpStatusCode() >= 300) {
      throw new IOException("HTTP error! Status code:  " + result.getHttpStatusResponse());
    }
    return result;
  }

  /**
   * Sends a request to the provided url
   * 
   * @param url
   * @param ippBuf
   * 
   * @param documentStream
   * @return result
   * @throws Exception
   */
  private IppResult sendRequest(URL url, ByteBuffer ippBuf, InputStream documentStream) throws Exception {
    IppResult ippResult = null;
    if (ippBuf == null) {
      return null;
    }

    if (url == null) {
      return null;
    }

    // HttpClient client = new DefaultHttpClient();
    //
    // // will not work with older versions of CUPS!
    // client.getParams().setParameter("http.protocol.version",
    // HttpVersion.HTTP_1_1);
    // client.getParams().setParameter("http.socket.timeout", new
    // Integer(10000));
    // client.getParams().setParameter("http.connection.timeout", new
    // Integer(10000));
    // client.getParams().setParameter("http.protocol.content-charset",
    // "UTF-8");
    // client.getParams().setParameter("http.method.response.buffer.warnlimit",
    // new Integer(8092));
    //
    // // probabaly not working with older CUPS versions
    // client.getParams().setParameter("http.protocol.expect-continue",
    // Boolean.valueOf(true));

    HttpClient client = HttpClientBuilder.create().build();

    HttpPost httpPost = new HttpPost(new URI("http://" + url.getHost() + ":" + ippPort) + url.getPath());
    httpPost.setConfig(getRequestConfig());
    httpCall = httpPost;

    // httpPost.getParams().setParameter("http.socket.timeout", new
    // Integer(10000));

    byte[] bytes = new byte[ippBuf.limit()];
    ippBuf.get(bytes);

    ByteArrayInputStream headerStream = new ByteArrayInputStream(bytes);

    // If we need to send a document, concatenate InputStreams
    InputStream inputStream = headerStream;
    if (documentStream != null) {
      inputStream = new SequenceInputStream(headerStream, documentStream);
    }

    // set length to -1 to advice the entity to read until EOF
    InputStreamEntity requestEntity = new InputStreamEntity(inputStream, -1);

    requestEntity.setContentType(IPP_MIME_TYPE);
    httpPost.setEntity(requestEntity);

    httpStatusLine = null;
    httpStatusCode = -1;

    ResponseHandler<byte[]> handler = new ResponseHandler<byte[]>() {
      public byte[] handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
        HttpEntity entity = response.getEntity();
        httpStatusLine = response.getStatusLine().toString();
        httpStatusCode = response.getStatusLine().getStatusCode();
        if (entity != null) {
          return EntityUtils.toByteArray(entity);
        } else {
          return null;
        }
      }
    };
    
    byte[] result = client.execute(httpPost, handler);

    IppResponse ippResponse = new IppResponse();

    ippResult = ippResponse.getResponse(ByteBuffer.wrap(result));
    ippResult.setHttpStatusResponse(httpStatusLine);
    ippResult.setHttpStatusCode(httpStatusCode);

    // IppResultPrinter.print(ippResult);

    client.getConnectionManager().shutdown();
    httpCall = null;
    return ippResult;
  }

  protected static RequestConfig getRequestConfig() {
    int timeout = Integer.parseInt(System.getProperty("cups4j.timeout", "10000"));
    return RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout).build();
  }

  public void cancel() {
    if (httpCall != null) {
      httpCall.abort();
    }
  }

  /**
   * Removes the port number in the submitted URL
   * 
   * @param url
   * @return url without port number
   */
  protected String stripPortNumber(URL url) {
    String protocol = url.getProtocol();
    if ("ipp".equals(protocol)) {
      protocol = "http";
    }

    return protocol + "://" + url.getHost() + url.getPath();
  }

  protected String getAttributeValue(Attribute attr) {
    return attr.getAttributeValue().get(0).getValue();
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + ":" + ippPort;
  }

}
