package br.com.anteros.cups4j;

import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for {@link AnterosCupsClient} class.
 *
 * @author oliver (boehm@javatux.de)
 */
public class CupsClientTest {

  private static AnterosCupsClient client;
  private static final Logger LOG = LoggerFactory.getLogger(CupsClientTest.class);

  @BeforeClass
  public static void setUpClient() throws Exception {
    client = TestCups.getCupsClient();
  }

  @Test
  public void getPrinters() throws Exception {
    List<AnterosCupsPrinter> printers = client.getPrinters();

    for (AnterosCupsPrinter printer : printers) {
      LOG.info("printer: " + printer.getName() + "[isClass=" + printer.isPrinterClass() + "]");
    }

    assertFalse(printers.isEmpty());
  }

}
