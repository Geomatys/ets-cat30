package org.opengis.cite.cat30.opensearch;

import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;
import org.opengis.cite.cat30.CAT3;
import org.opengis.cite.cat30.CommonFixture;
import org.opengis.cite.cat30.ETSAssert;
import org.opengis.cite.cat30.ErrorMessage;
import org.opengis.cite.cat30.ErrorMessageKeys;
import org.opengis.cite.cat30.Namespaces;
import org.opengis.cite.cat30.SuiteAttribute;
import org.opengis.cite.cat30.util.ServiceMetadataUtils;
import org.opengis.cite.cat30.util.TestSuiteLogger;
import org.opengis.cite.validation.RelaxNGValidator;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Verifies the structure and content of the OpenSearch description document
 * obtained from the SUT. The document is obtained in response to a GET request
 * submitted to the base service endpoint where the <code>Accept</code> request
 * header expresses a preference for any of the following media types:
 *
 * <ul>
 * <li><code>application/vnd.a9.opensearchdescription+xml</code></li>
 * <li><code>application/opensearchdescription+xml</code></li>
 * </ul>
 *
 * <p>
 * <strong>Note:</strong> None of the media types listed above appear in the
 * IANA <a href="http://www.iana.org/assignments/media-types/media-types.xhtml"
 * target="_blank">media type registry</a>. Registrations in the standards tree
 * must be approved by the IESG or originate from a recognized standards body.
 * </p>
 *
 * @see
 * <a href="http://www.opensearch.org/Specifications/OpenSearch/1.1#OpenSearch_description_elements"
 * target="_blank">OpenSearch description elements</a>
 */
public class OpenSearchDescriptionTests extends CommonFixture {

    private RelaxNGValidator osdValidator;
    private URI baseUri;

    /**
     * Initializes the test fixture as follows:
     * <ul>
     * <li>builds a Relax NG validator for an OpenSearch description document;
     * the schema resource is located on the classpath at this location:
     * <code>/org/opengis/cite/cat30/rnc/osd-1.1-draft5.rnc</code></li>
     * <li>obtains a client component from the test context</li>
     * <li>extracts base GetCapabilities endpoint (GET method) from the
     * capabilities document</li>
     * </ul>
     *
     * @param testContext The test context containing various suite attributes.
     *
     */
    @BeforeClass
    public void initFixture(ITestContext testContext) {
        URL schemaUrl = getClass().getResource(ROOT_PKG_PATH
                + "rnc/osd-1.1-draft5.rnc");
        try {
            this.osdValidator = new RelaxNGValidator(schemaUrl);
        } catch (Exception ex) {
            TestSuiteLogger.log(Level.WARNING, "buildValidator: ", ex);
        }
        Document cswCapabilities = (Document) testContext.getSuite().getAttribute(
                SuiteAttribute.TEST_SUBJECT.getName());
        this.baseUri = ServiceMetadataUtils.getOperationEndpoint(
                cswCapabilities, CAT3.GET_CAPABILITIES, HttpMethod.GET);
    }

    /**
     * [Test] Requests an OpenSearch description document as the most preferred
     * media type. The generic XML media type is included in the Accept header
     * with a q parameter value &lt; 1 ("application/xml; q=0.5").
     */
    @Test(description = "Test-008")
    public void preferOpenSearchDescription() {
        WebResource resource = this.client.resource(this.baseUri);
        String xmlNotPreferred = MediaType.APPLICATION_XML + "; q=0.5";
        resource.accept(xmlNotPreferred, CAT3.APP_OPENSEARCH_XML);
        Document entity = resource.get(Document.class);
        QName osdDocElemName = new QName(Namespaces.OSD11, "OpenSearchDescription");
        ETSAssert.assertQualifiedName(entity.getDocumentElement(), osdDocElemName);
    }

    /**
     * [Test] Retrieves an OpenSearch description document and validates it. A
     * GET request is submitted to the base URL for the GetCapabilities
     * endpoint. The <code>Accept</code> header indicates a preference for
     * either of the following media types:
     * <ul>
     * <li>{@value org.opengis.cite.cat30.CAT3#APP_VND_OPENSEARCH_XML}</li>
     * <li>{@value org.opengis.cite.cat30.CAT3#APP_OPENSEARCH_XML}</li>
     * </ul>
     *
     * @throws SAXException If the response entity cannot be parsed.
     * @throws IOException If an I/O error occurs while trying to access the
     * service endpoint.
     */
    @Test(description = "Test-021")
    public void getOpenSearchDescription() throws SAXException, IOException {
        WebResource resource = this.client.resource(this.baseUri);
        resource.accept(CAT3.APP_VND_OPENSEARCH_XML, CAT3.APP_OPENSEARCH_XML);
        Document entity = resource.get(Document.class);
        this.osdValidator.validate(new DOMSource(entity));
        ValidationErrorHandler err = osdValidator.getErrorHandler();
        Assert.assertFalse(err.errorsDetected(),
                ErrorMessage.format(ErrorMessageKeys.NOT_SCHEMA_VALID,
                        err.getErrorCount(), err.toString()));
    }

}
