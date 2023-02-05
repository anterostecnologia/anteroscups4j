package br.com.anteros.cups4j.ipp;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.anteros.cups4j.ippclient.IppResult;
import br.com.anteros.cups4j.operations.IppOperation;
import br.com.anteros.cups4j.operations.ipp.IppCreateJobOperation;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link ResponseException}.
 *
 * @author: oboehm
 */
public final class ResponseExceptionTest {

    private static final Logger LOG = LoggerFactory.getLogger(ResponseExceptionTest.class);

    @Test
    public void testGetMessage() {
        IppOperation op = new IppCreateJobOperation();
        IppResult result = createIppResult(400, "Bad Request");
        ResponseException ex = new ResponseException(op, result);
        assertThat(ex.getMessage(), containsString("Bad Request"));
        LOG.info("msg = \"{}\"", ex.getMessage());
    }

    private static IppResult createIppResult(int sc, String response) {
        IppResult result = new IppResult();
        result.setHttpStatusCode(sc);
        result.setHttpStatusResponse(response);
        result.setIppStatusResponse("Major Version:0x02 Minor Version:0x00 Request Id:3\n" +
                "Status Code:0x0400(client-error-bad-request)");
        return result;
    }

}
