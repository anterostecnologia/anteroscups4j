package br.com.anteros.cups4j.operations.ipp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.anteros.cups4j.ippclient.IppTag;
import br.com.anteros.cups4j.operations.IppOperation;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Map;

public class IppPrintJobOperation extends IppOperation {


  private static final Logger LOG = LoggerFactory.getLogger(IppPrintJobOperation.class);
  
  public IppPrintJobOperation() {
    operationID = 0x0002;
    bufferSize = 8192;
  }

  public IppPrintJobOperation(int port) {
    this();
    this.ippPort = port;
  }

  /**
   * 
   * @param url
   *          printer-uri
   * @param map
   *          attributes
   *          i.e.job-name,ipp-attribute-fidelity,document-name,compression,
   *          document -format,document-natural-language,job-impressions
   *          ,job-media-sheets, job-template-attributes
   * @return IPP header
   * @throws UnsupportedEncodingException
   */

  public ByteBuffer getIppHeader(URL url, Map<String, String> map) throws UnsupportedEncodingException {
    if (url == null) {
      LOG.error("IppPrintJobOperation.getIppHeader(): uri is null");
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

    if (map.get("job-name") != null) {
      ippBuf = IppTag.getNameWithoutLanguage(ippBuf, "job-name", map.get("job-name"));
    }

    if (map.get("ipp-attribute-fidelity") != null) {
      boolean value = false;
      if (map.get("ipp-attribute-fidelity").equals("true")) {
        value = true;
      }
      ippBuf = IppTag.getBoolean(ippBuf, "ipp-attribute-fidelity", value);
    }

    if (map.get("document-name") != null) {
      ippBuf = IppTag.getNameWithoutLanguage(ippBuf, "document-name", map.get("document-name"));
    }

    if (map.get("compression") != null) {
      ippBuf = IppTag.getKeyword(ippBuf, "compression", map.get("compression"));
    }

    if (map.get("document-format") != null) {
      ippBuf = IppTag.getMimeMediaType(ippBuf, "document-format", map.get("document-format"));
    }

    if (map.get("document-natural-language") != null) {
      ippBuf = IppTag.getNaturalLanguage(ippBuf, "document-natural-language", map.get("document-natural-language"));
    }

    if (map.get("job-k-octets") != null) {
      int value = Integer.parseInt(map.get("job-k-octets"));
      ippBuf = IppTag.getInteger(ippBuf, "job-k-octets", value);
    }

    if (map.get("job-impressions") != null) {
      int value = Integer.parseInt(map.get("job-impressions"));
      ippBuf = IppTag.getInteger(ippBuf, "job-impressions", value);
    }

    if (map.get("job-media-sheets") != null) {
      int value = Integer.parseInt(map.get("job-media-sheets"));
      ippBuf = IppTag.getInteger(ippBuf, "job-media-sheets", value);
    }

    if (map.get("job-attributes") != null) {
      String[] attributeBlocks = map.get("job-attributes").split("#");
      ippBuf = getJobAttributes(ippBuf, attributeBlocks);
    }

    ippBuf = IppTag.getEnd(ippBuf);
    ippBuf.flip();
    return ippBuf;
  }

  /**
   * TODO: not all possibilities implemented
   * 
   * @param ippBuf
   * @param attributeBlocks
   * @return
   * @throws UnsupportedEncodingException
   */
  protected static ByteBuffer getJobAttributes(ByteBuffer ippBuf, String[] attributeBlocks)
      throws UnsupportedEncodingException {
    if (ippBuf == null) {
      LOG.error("IppPrintJobOperation.getJobAttributes(): ippBuf is null");
      return null;
    }
    if (attributeBlocks == null) {
      return ippBuf;
    }

    ippBuf = IppTag.getJobAttributesTag(ippBuf);

    int l = attributeBlocks.length;
    for (int i = 0; i < l; i++) {
      String[] attr = attributeBlocks[i].split(":");
      if ((attr == null) || (attr.length != 3)) {
        return ippBuf;
      }
      String name = attr[0];
      String tagName = attr[1];
      String value = attr[2];

      if (tagName.equals("boolean")) {
        if (value.equals("true")) {
          ippBuf = IppTag.getBoolean(ippBuf, name, true);
        } else {
          ippBuf = IppTag.getBoolean(ippBuf, name, false);
        }
      } else if (tagName.equals("integer")) {
        ippBuf = IppTag.getInteger(ippBuf, name, Integer.parseInt(value));
      } else if (tagName.equals("rangeOfInteger")) {
        String[] range = value.split("-");
        int low = Integer.parseInt(range[0]);
        int high = Integer.parseInt(range[1]);
        ippBuf = IppTag.getRangeOfInteger(ippBuf, name, low, high);
      } else if (tagName.equals("setOfRangeOfInteger")) {
        String ranges[] = value.split(",");

        for (String range : ranges) {
          range = range.trim();
          String[] values = range.split("-");

          int value1 = Integer.parseInt(values[0]);
          int value2 = value1;
          // two values provided?
          if (values.length == 2) {
            value2 = Integer.parseInt(values[1]);
          }

          // first attribute value needs name, additional values need to get the
          // "null" name
          ippBuf = IppTag.getRangeOfInteger(ippBuf, name, value1, value2);
          name = null;
        }
      } else if (tagName.equals("keyword")) {
        ippBuf = IppTag.getKeyword(ippBuf, name, value);
      } else if (tagName.equals("name")) {
        ippBuf = IppTag.getNameWithoutLanguage(ippBuf, name, value);
      } else if (tagName.equals("enum")) {
        ippBuf = IppTag.getEnum(ippBuf, name, Integer.parseInt(value));
      } else if (tagName.equals("resolution")) {
        String[] resolution = value.split(",");
        int value1 = Integer.parseInt(resolution[0]);
        int value2 = Integer.parseInt(resolution[1]);
        byte value3 = Byte.valueOf(resolution[2]);
        ippBuf = IppTag.getResolution(ippBuf, name, value1, value2, value3);
      }
    }
    return ippBuf;
  }

}
