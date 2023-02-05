package br.com.anteros.cups4j;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;

public class Teste {

	public void print(String zpl_data, String printer) throws IOException {

        PrintService myPrintService = findPrintService(printer);

        DocPrintJob job = myPrintService.createPrintJob();
        DocFlavor flvr = DocFlavor.BYTE_ARRAY.AUTOSENSE;
        Doc doc = new SimpleDoc(zpl_data.getBytes(), flvr, null);
        try {
            job.print(doc, null);
            System.out.println("Print Done!");
        } catch (PrintException e) {
            System.out.println(e.getCause());
        }
    }

    public static PrintService findPrintService(String printerName) {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(
                null, null);
        for (PrintService printService : printServices) {
            if (printService.getName().equals(printerName)) {
                System.out.println(printService);
                return printService;
            }
        }
        return null;
    }

    public static void main(String[] args) throws InterruptedException, Exception {
        String g = "^Q100,3\n\r" + 
        		"^W102\n\r" + 
        		"^H5\n\r" + 
        		"^P1\n\r" + 
        		"^S2\n\r" + 
        		"^AD\n\r" + 
        		"^C1\n\r" + 
        		"^R0\n\r" + 
        		"~Q+0\n\r" + 
        		"^O0\n\r" + 
        		"^D0\n\r" + 
        		"^E12\n\r" + 
        		"~R255\n\r" + 
        		"^L\n\r" + 
        		"Dy2-me-dd\n\r" + 
        		"Th:m:s\n\r" + 
        		"R75,129,764,282,4,4\n\r" + 
        		"BA3,198,502,4,10,80,0,1,1234\n\r" + 
        		"AD,216,182,1,1,0,0E,TESTANDO IMPRESSORA\n\r" + 
        		"E\n\r";
        
//        Teste teste = new Teste();
//        teste.print(g, "TESTE");
        
        
        
        AnterosCupsClient cupsClient = new AnterosCupsClient("127.0.0.1", 631);
        URL printerURL = new URL("http://127.0.0.1:631/printers/TESTE");
        
        
        List<AnterosCupsPrinter> printers = cupsClient.getPrinters();
        
        AnterosCupsPrinter cupsPrinter = cupsClient.getPrinter(printerURL);

        PrintJob printJob = new PrintJob.Builder(g.getBytes()).build();
        
       PrintRequestResult print = cupsPrinter.print(printJob);
        System.out.println(print);
        
        
//        PrintRaw pr = new PrintRaw();
//        
//        pr.setPrintService(findPrintService("TESTE"));
//        pr.append(g.getBytes());
//        
////        pr.setOutputSocket("10.0.0.160", 9100);
//        pr.print();
        
    }
}
