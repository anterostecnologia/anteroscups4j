package br.com.anteros.cups4j.ippclient;

import org.junit.Test;

import br.com.anteros.cups4j.ipp.attributes.AttributeGroup;

import java.util.List;

import static org.junit.Assert.*;

public class IppResultTest {

    @Test
    public void testGetAttributeGroupList() {
        IppResult ippResult = new IppResult();
        List<AttributeGroup> attributeGroupList = ippResult.getAttributeGroupList();
        assertNotNull(attributeGroupList);
        assertTrue(attributeGroupList.isEmpty());
    }

}
