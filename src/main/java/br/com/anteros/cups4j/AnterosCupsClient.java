package br.com.anteros.cups4j;

import java.net.URL;
import java.util.List;

import br.com.anteros.cups4j.operations.IppOperation;
import br.com.anteros.cups4j.operations.cups.CupsGetDefaultOperation;
import br.com.anteros.cups4j.operations.cups.CupsGetPrintersOperation;
import br.com.anteros.cups4j.operations.cups.CupsMoveJobOperation;
import br.com.anteros.cups4j.operations.ipp.*;

/**
 * Main Client for accessing CUPS features like
 * <p>
 * - get printers
 * </p>
 * <p>
 * - print documents
 * </p>
 * <p>
 * - get job attributes
 * </p>
 * <p>
 * - ...
 * </p>
 */
public class AnterosCupsClient {
  public static final String DEFAULT_HOST = "localhost";
  public static final int DEFAULT_PORT = 631;
  public static final String DEFAULT_USER = System.getProperty("user.name", "anonymous");

  private String host = null;
  private int port = -1;
  private String user = null;

  private IppOperation ippOperation;

  /**
   * Creates a CupsClient for localhost port 631 with user anonymous
   * 
   * @throws Exception
   */
  public AnterosCupsClient() throws Exception {
    this(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_USER);
  }

  /**
   * Creates a CupsClient for provided host and port with user anonymous
   * 
   * @param host
   * @param port
   * @throws Exception
   */
  public AnterosCupsClient(String host, int port) throws Exception {
    this(host, port, DEFAULT_USER);
  }

  /**
   * Creates a CupsClient for provided host, port and user
   * 
   * @param host
   * @param port
   * @param userName
   * @throws Exception
   */
  public AnterosCupsClient(String host, int port, String userName) throws Exception {
    if (host != null && !"".equals(host)) {
      this.host = host;
    } else {
      throw new Exception("The hostname specified: <" + host + "> is not valid");
    }

    if (port > 0) {
      this.port = port;
    } else {
      throw new Exception("The specified port number: <" + port + "> is not valid");
    }

    if (userName != null && !"".equals(userName)) {
      this.user = userName;
    }
  }

  /**
   * Returns all available printers
   * 
   * @return List of Printers
   * @throws Exception
   */
  public List<AnterosCupsPrinter> getPrinters() throws Exception {
    List<AnterosCupsPrinter> printers = new CupsGetPrintersOperation().getPrinters(host, port);
    // add default printer if available
    AnterosCupsPrinter defaultPrinter = null;

    defaultPrinter = getDefaultPrinter();

    for (AnterosCupsPrinter p : printers) {
      if (defaultPrinter != null && p.getPrinterURL().toString().equals(defaultPrinter.getPrinterURL().toString())) {
        p.setDefault(true);
      }
    }

    return printers;
  }

  /**
   * Returns all available printers except CUPS specific default printer
   * 
   * @return List of Printers
   * @throws Exception
   */
  public List<AnterosCupsPrinter> getPrintersWithoutDefault() throws Exception {
    CupsGetPrintersOperation cgp = new CupsGetPrintersOperation();
    this.ippOperation = cgp;
    List<AnterosCupsPrinter> result = cgp.getPrinters(host, port);
    ippOperation = null;
    return result;
  }

  /**
   * Cancel the current running job if possible.
   * <p>
   * This is especially necessary when using Cups4j within Android
   * </p>
   */
  public void cancelOperation() {
    if (ippOperation != null) {
      ippOperation.cancel();
    }
  }

  /**
   * Returns default printer
   * 
   * @return default printer
   * @throws Exception
   */
  public AnterosCupsPrinter getDefaultPrinter() throws Exception {
    return new CupsGetDefaultOperation().getDefaultPrinter(host, port);
  }

  /**
   * Returns the printer for the provided URL
   * 
   * @param printerURL
   *          an URL like http://localhost:631/printers/printername
   * @return printer
   * @throws Exception
   */
  public AnterosCupsPrinter getPrinter(URL printerURL) throws Exception {
    List<AnterosCupsPrinter> printers = getPrinters();
    for (AnterosCupsPrinter printer : printers) {
      if (printer.getPrinterURL().toString().equals(printerURL.toString()))
        return printer;
    }
    return null;
  }

  /**
   * Returns the printer for the provided name
   *
   * @param printerName the printer name
   * @return printer
   * @throws Exception
   */
  public AnterosCupsPrinter getPrinter(String printerName) throws Exception {
    List<AnterosCupsPrinter> printers = getPrinters();
    for (AnterosCupsPrinter printer : printers) {
      if (printer.getName().equals(printerName))
        return printer;
    }
    return null;
  }

  /**
   * Returns job attributes for the job associated with the provided jobID
   * 
   * @param jobID
   * @return Job attributes
   * @throws Exception
   */
  public PrintJobAttributes getJobAttributes(int jobID) throws Exception {
    return getJobAttributes(host, user, jobID);
  }

  /**
   * Returns job attributes for the job associated with the provided jobID and
   * user name
   * 
   * @param userName
   * @param jobID
   * @return Job attributes
   * @throws Exception
   */
  public PrintJobAttributes getJobAttributes(String userName, int jobID) throws Exception {
    return getJobAttributes(host, userName, jobID);
  }

  /**
   * Returns job attributes for the job associated with the provided jobID on
   * provided host and port
   * 
   * @param hostname
   * @param jobID
   * @return Job attributes
   * @throws Exception
   */
  private PrintJobAttributes getJobAttributes(String hostname, String userName, int jobID) throws Exception {
    if (userName == null || "".equals(userName)) {
      userName = DEFAULT_USER;
    }
    if (hostname == null || "".equals(hostname)) {
      hostname = DEFAULT_HOST;
    }

    return new IppGetJobAttributesOperation().getPrintJobAttributes(hostname, userName, port, jobID);
  }

  /**
   * Returns all jobs for given printer and user Name
   * <p>
   * Currently all Jobs on the server are returned by this method.
   * </p>
   * <p>
   * user and printer names are provided in the resulting PrintJobAttributes
   * </p>
   * 
   * @param printer
   * @param userName
   * @return List of job attributes
   * @throws Exception
   */
  public List<PrintJobAttributes> getJobs(AnterosCupsPrinter printer, WhichJobsEnum whichJobs, String userName, boolean myJobs)
      throws Exception {
    return new IppGetJobsOperation().getPrintJobs(printer, whichJobs, userName, myJobs);
  }

  /**
   * Cancel the job with the provided jobID on the current host wit current user
   * 
   * @param jobID
   * @return boolean success
   * @throws Exception
   */
  public boolean cancelJob(int jobID) throws Exception {
    return new IppCancelJobOperation().cancelJob(host, user, jobID);
  }

  /**
   * Cancel the job with the provided jobID, hostname for provided userName
   * 
   * @param hostname
   * @param userName
   * @param jobID
   * @return boolean success
   * @throws Exception
   */
  public boolean cancelJob(String hostname, String userName, int jobID) throws Exception {
    return new IppCancelJobOperation().cancelJob(hostname, userName, jobID);
  }

  /**
   * Hold the job with the provided jobID on the current host wit current set
   * user
   * 
   * @param jobID
   * @return boolean success
   * @throws Exception
   */
  public boolean holdJob(int jobID) throws Exception {
    return new IppHoldJobOperation().holdJob(host, user, jobID);
  }

  /**
   * Hold the job with the provided jobID, hostname for provided userName
   * 
   * @param hostname
   * @param userName
   * @param jobID
   * @return boolean success
   * @throws Exception
   */
  public boolean holdJob(String hostname, String userName, int jobID) throws Exception {
    return new IppHoldJobOperation().holdJob(hostname, userName, jobID);
  }

  /**
   * Release the held job with the provided jobID on the current host wit
   * current set user
   * 
   * @param jobID
   * @return boolean success
   * @throws Exception
   */
  public boolean releaseJob(int jobID) throws Exception {
    return new IppReleaseJobOperation().releaseJob(host, user, jobID);
  }

  /**
   * Release the job with the provided jobID, hostname for provided userName
   * 
   * @param hostname
   * @param userName
   * @param jobID
   * @return boolean success
   * @throws Exception
   */
  public boolean releaseJob(String hostname, String userName, int jobID) throws Exception {
    return new IppReleaseJobOperation().releaseJob(host, user, jobID);
  }

  /**
   * Moves the print job with job ID jobID from currentPrinter to targetPrinter
   * 
   * @param jobID
   * @param userName
   * @param currentPrinter
   * @param targetPrinter
   * @return boolean successs
   * @throws Exception
   */
  public boolean moveJob(int jobID, String userName, AnterosCupsPrinter currentPrinter, AnterosCupsPrinter targetPrinter)
      throws Exception {
    String currentHost = currentPrinter.getPrinterURL().getHost();

    return new CupsMoveJobOperation().moveJob(currentHost, userName, jobID, targetPrinter.getPrinterURL());
  }

}
