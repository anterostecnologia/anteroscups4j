package br.com.anteros.cups4j.operations.cups;

/**
 * Copyright (C) 2009 Harald Weyhing
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * See the GNU Lesser General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with this program; if not, see
 * <http://www.gnu.org/licenses/>.
 */
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.anteros.cups4j.AnterosCupsPrinter;
import br.com.anteros.cups4j.PrinterStateEnum;
import br.com.anteros.cups4j.ipp.attributes.Attribute;
import br.com.anteros.cups4j.ipp.attributes.AttributeGroup;
import br.com.anteros.cups4j.ipp.attributes.AttributeValue;
import br.com.anteros.cups4j.ippclient.IppResult;
import br.com.anteros.cups4j.operations.IppOperation;

public class CupsGetPrintersOperation extends IppOperation {
  private static final Logger LOG = LoggerFactory.getLogger(CupsGetPrintersOperation.class);
  
  public CupsGetPrintersOperation() {
    operationID = 0x4002;
    bufferSize = 8192;
  }

  public CupsGetPrintersOperation(int port) {
    this();
    this.ippPort = port;
  }

  public List<AnterosCupsPrinter> getPrinters(String hostname, int port) throws Exception {
    List<AnterosCupsPrinter> printers = new ArrayList<AnterosCupsPrinter>();

    Map<String, String> map = new HashMap<String, String>();
    map.put(
        "requested-attributes",
        "copies-supported page-ranges-supported printer-name printer-info printer-state printer-location printer-make-and-model printer-uri-supported media-supported media-default sides-supported sides-default orientation-requested-supported printer-resolution-supported printer printer-resolution-default number-up-default number-up-supported document-format-supported print-color-mode-supported print-color-mode-default");
    // map.put("requested-attributes", "all");
    this.ippPort = port;

    IppResult result = request(new URL("http://" + hostname + "/printers"), map);

    // IppResultPrinter.print(result);

    for (AttributeGroup group : result.getAttributeGroupList()) {
      AnterosCupsPrinter printer = null;
      if (group.getTagName().equals("printer-attributes-tag")) {
        String printerURI = null;
        String printerName = null;
        String printerLocation = null;
        String printerDescription = null;
        PrinterStateEnum printerState = null;
        List<String> mediaSupportedList = new ArrayList<String>();
        String mediaDefault = null;
        List<String> printerResolutionSupported = new ArrayList<String>();
        String printerResolutionDefault = null;
        List<String> printerColorModeSupported = new ArrayList<String>();
        String printerColorModeDefault = null;
        List<String> mimeTypesSupported = new ArrayList<String>();
        String sidesDefault = null;
        List<String> sidesSupported = new ArrayList<String>();
        String numberUpDefault = null;
        List<String> numberUpSupported = new ArrayList<String>();

        for (Attribute attr : group.getAttribute()) {
          if (attr.getName().equals("printer-uri-supported")) {
            printerURI = getAttributeValue(attr).replace("ipp://", "http://");
          } else if (attr.getName().equals("printer-name")) {
            printerName = getAttributeValue(attr);
          } else if (attr.getName().equals("printer-location")) {
            printerLocation = getAttributeValue(attr);
          } else if (attr.getName().equals("printer-info")) {
            printerDescription = getAttributeValue(attr);
          } else if (attr.getName().equals("media-default")) {
            mediaDefault = getAttributeValue(attr);
          } else if (attr.getName().equals("media-supported")) {
            mediaSupportedList = getAttributeValues(attr);
          } else if (attr.getName().equals("number-up-default")) {
            numberUpDefault = getAttributeValue(attr);
          } else if (attr.getName().equals("number-up-supported")) {
            numberUpSupported = getAttributeValues(attr);
          } else if (attr.getName().equals("printer-resolution-default")) {
            printerResolutionDefault = getAttributeValue(attr);
          } else if (attr.getName().equals("printer-resolution-supported")) {
            printerResolutionSupported = getAttributeValues(attr);
          } else if (attr.getName().equals("print-color-mode-default")) {
            printerColorModeDefault = getAttributeValue(attr);
          } else if (attr.getName().equals("print-color-mode-supported")) {
            printerColorModeSupported = getAttributeValues(attr);
          } else if (attr.getName().equals("document-format-supported")) {
            mimeTypesSupported = getAttributeValues(attr);
          } else if (attr.getName().equals("sides-supported")) {
            sidesSupported = getAttributeValues(attr);
          } else if (attr.getName().equals("sides-default")) {
            sidesDefault = getAttributeValue(attr);
          } else if (attr.getName().equals("printer-state")) {
            printerState = PrinterStateEnum.fromStringInteger(getAttributeValue(attr));
          }
        }
        URL printerUrl = null;
        try {
          printerUrl = new URL(printerURI);
        } catch (Throwable t) {
          t.printStackTrace();
          LOG.error("Error encountered building URL from printer uri of printer " + printerName
              + ", uri returned was [" + printerURI + "].  Attribute group tag/description: [" + group.getTagName()
              + "/" + group.getDescription());
          throw new Exception(t);
        }

        printer = new AnterosCupsPrinter(printerUrl, printerName, false);
        printer.setState(printerState);
        printer.setLocation(printerLocation);
        printer.setDescription(printerDescription);
        printer.setMediaDefault(mediaDefault);
        printer.setMediaSupported(mediaSupportedList);
        printer.setResolutionDefault(printerResolutionDefault);
        printer.setResolutionSupported(printerResolutionSupported);
        printer.setColorModeDefault(printerColorModeDefault);
        printer.setColorModeSupported(printerColorModeSupported);
        printer.setMimeTypesSupported(mimeTypesSupported);
        printer.setSidesDefault(sidesDefault);
        printer.setSidesSupported(sidesSupported);
        printer.setNumberUpDefault(numberUpDefault);
        printer.setNumberUpSupported(numberUpSupported);

        printers.add(printer);
      }
    }

    return printers;
  }

  protected List<String> getAttributeValues(Attribute attr) {
    List<String> values = new ArrayList<String>();
    if (attr.getAttributeValue() != null && attr.getAttributeValue().size() > 0) {
      for (AttributeValue value : attr.getAttributeValue()) {
        values.add(value.getValue());
      }
    }
    return values;
  }

  protected String getAttributeValue(Attribute attr) {
    String result = null;
    if (attr.getAttributeValue() != null && attr.getAttributeValue().size() > 0) {
      result = attr.getAttributeValue().get(0).getValue();
    }
    return result;
  }
}
