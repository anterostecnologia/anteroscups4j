package br.com.anteros.cups4j.ippclient;

public class IppAttributeProviderFactory {
  public static IIppAttributeProvider createIppAttributeProvider() {
    return IppAttributeProvider.getInstance();
  }
}
