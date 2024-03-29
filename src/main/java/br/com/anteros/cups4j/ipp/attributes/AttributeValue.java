//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-792 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.10.14 at 12:03:17 PM MESZ 
//

package br.com.anteros.cups4j.ipp.attributes;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "attribute-value")
public class AttributeValue {

  @Element(name = "set-of-keyword", required = false)
  protected SetOfKeyword setOfKeyword;
  @Element(name = "set-of-enum", required = false)
  protected SetOfEnum setOfEnum;
  @org.simpleframework.xml.Attribute(required = true)
  protected String tag;
  @org.simpleframework.xml.Attribute(name = "tag-name", required = true)
  protected String tagName;
  @org.simpleframework.xml.Attribute(required = false)
  protected String value;
  @org.simpleframework.xml.Attribute(required = false)
  protected String description;

  /**
   * Gets the value of the setOfKeyword property.
   * 
   * @return possible object is {@link SetOfKeyword }
   * 
   */
  public SetOfKeyword getSetOfKeyword() {
    return setOfKeyword;
  }

  /**
   * Sets the value of the setOfKeyword property.
   * 
   * @param value
   *          allowed object is {@link SetOfKeyword }
   * 
   */
  public void setSetOfKeyword(SetOfKeyword value) {
    this.setOfKeyword = value;
  }

  /**
   * Gets the value of the setOfEnum property.
   * 
   * @return possible object is {@link SetOfEnum }
   * 
   */
  public SetOfEnum getSetOfEnum() {
    return setOfEnum;
  }

  /**
   * Sets the value of the setOfEnum property.
   * 
   * @param value
   *          allowed object is {@link SetOfEnum }
   * 
   */
  public void setSetOfEnum(SetOfEnum value) {
    this.setOfEnum = value;
  }

  /**
   * Gets the value of the tag property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getTag() {
    return tag;
  }

  /**
   * Sets the value of the tag property.
   * 
   * @param value
   *          allowed object is {@link String }
   * 
   */
  public void setTag(String value) {
    this.tag = value;
  }

  /**
   * Gets the value of the tagName property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getTagName() {
    return tagName;
  }

  /**
   * Sets the value of the tagName property.
   * 
   * @param value
   *          allowed object is {@link String }
   * 
   */
  public void setTagName(String value) {
    this.tagName = value;
  }

  /**
   * Gets the value of the value property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getValue() {
    return value;
  }

  /**
   * Sets the value of the value property.
   * 
   * @param value
   *          allowed object is {@link String }
   * 
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * Gets the value of the description property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the value of the description property.
   * 
   * @param value
   *          allowed object is {@link String }
   * 
   */
  public void setDescription(String value) {
    this.description = value;
  }

}
