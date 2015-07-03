package org.opengis.cite.cat30.opensearch;

import static org.mockito.Mockito.*;
import static org.mockito.Matchers.*;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opengis.cite.cat30.SuiteAttribute;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import java.io.File;
import java.net.URL;
import javax.xml.validation.Schema;
import org.mockito.Mockito;
import org.opengis.cite.cat30.util.DatasetInfo;
import org.opengis.cite.cat30.util.ValidationUtils;

public class VerifyOpenSearchGeoTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private static DocumentBuilder docBuilder;
    private static ITestContext testContext;
    private static ISuite suite;

    @BeforeClass
    public static void initTestFixture() throws Exception {
        testContext = mock(ITestContext.class);
        suite = mock(ISuite.class);
        when(testContext.getSuite()).thenReturn(suite);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        docBuilder = dbf.newDocumentBuilder();
        URL dataURL = VerifyOpenSearchGeoTests.class.getResource("/rsp/GetRecordsResponse-full.xml");
        File dataFile = new File(dataURL.toURI());
        DatasetInfo dataInfo = new DatasetInfo(dataFile);
        when(suite.getAttribute(SuiteAttribute.DATASET.getName()))
                .thenReturn(dataInfo);
        Document doc = docBuilder.parse(VerifyOpenSearchGeoTests.class.getResourceAsStream(
                "/capabilities/basic.xml"));
        when(suite.getAttribute(SuiteAttribute.TEST_SUBJECT.getName())).thenReturn(doc);
        Schema cswSchema = ValidationUtils.createCSWSchema();
        when(suite.getAttribute(SuiteAttribute.CSW_SCHEMA.getName()))
                .thenReturn(cswSchema);
        Schema atomSchema = ValidationUtils.createAtomSchema();
        when(suite.getAttribute(SuiteAttribute.ATOM_SCHEMA.getName()))
                .thenReturn(atomSchema);
    }

    @Test
    public void boundingBoxQuery_disjoint() throws SAXException, IOException {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("The envelopes do not intersect");
        Document osd = docBuilder.parse(VerifyOpenSearchGeoTests.class.getResourceAsStream(
                "/opensearch/OpenSearchDescription-valid.xml"));
        when(suite.getAttribute(SuiteAttribute.OPENSEARCH_DESCR.getName())).thenReturn(osd);
        Client client = mock(Client.class);
        when(suite.getAttribute(SuiteAttribute.CLIENT.getName()))
                .thenReturn(client);
        ClientResponse rsp = mock(ClientResponse.class);
        when(client.handle(any(ClientRequest.class))).thenReturn(rsp);
        when(rsp.getStatus()).thenReturn(
                ClientResponse.Status.OK.getStatusCode());
        Document rspEntity = docBuilder.parse(this.getClass().getResourceAsStream(
                "/rsp/feed-1.xml"));
        OpenSearchGeoTests spy = Mockito.spy(new OpenSearchGeoTests());
        Mockito.doReturn(rspEntity).when(spy).getResponseEntityAsDocument(
                any(ClientResponse.class), anyString());
        spy.initCommonFixture(testContext);
        spy.initOpenSearchGeoTestsFixture(testContext);
        spy.boundingBoxQuery();
    }

    @Test
    public void boundingBoxQuery_RSSResponse() throws SAXException, IOException {
        Document osd = docBuilder.parse(VerifyOpenSearchGeoTests.class.getResourceAsStream(
                "/opensearch/OpenSearchDescription-rss.xml"));
        when(suite.getAttribute(SuiteAttribute.OPENSEARCH_DESCR.getName())).thenReturn(osd);
        Client client = mock(Client.class);
        when(suite.getAttribute(SuiteAttribute.CLIENT.getName()))
                .thenReturn(client);
        ClientResponse rsp = mock(ClientResponse.class);
        when(client.handle(any(ClientRequest.class))).thenReturn(rsp);
        when(rsp.getStatus()).thenReturn(
                ClientResponse.Status.OK.getStatusCode());
        Document rspEntity = docBuilder.parse(this.getClass().getResourceAsStream(
                "/rsp/rss-1.xml"));
        OpenSearchGeoTests spy = Mockito.spy(new OpenSearchGeoTests());
        Mockito.doReturn(rspEntity).when(spy).getResponseEntityAsDocument(
                any(ClientResponse.class), anyString());
        spy.initCommonFixture(testContext);
        spy.initOpenSearchGeoTestsFixture(testContext);
        spy.boundingBoxQuery();
    }
}
