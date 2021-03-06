package jsonblob

import groovy.json.JsonSlurper
import org.grails.jaxrs.itest.IntegrationTestCase
import org.junit.Test

import static org.junit.Assert.*

class JsonBlobApiTest extends IntegrationTestCase {

    @Test
    void testPostAndGet() {
        def headers = ['Content-Type':'application/json', 'Accept':'application/json']

        def jsonBuilder = new groovy.json.JsonBuilder()
        jsonBuilder.dogs {
            bella {
                breed 'bernese mountain dog'
                age '5'
                address(city: 'Denver', country: 'USA', zip: 80210)
            }
        }

        def apiBase = '/api/jsonBlob'

        // create new blob
        sendRequest(apiBase, 'POST', headers, jsonBuilder.toString().bytes)

        def locationHeader = response.getHeader('Location')
        assertNotNull(locationHeader)
        def relativePath = locationHeader.substring(locationHeader.indexOf(apiBase))
        def blobId = relativePath.split("/").last()

        assertEquals(201, response.status)
        assertTrue(response.contentAsString.length() > 0)
        assertTrue(response.getHeader('Content-Type').startsWith('application/json'))
        assertTrue(relativePath.startsWith("$apiBase/"))
        assertFalse(response.contentAsString.contains("_id"))

        // get the newly created blob
        sendRequest(relativePath, 'GET', headers)

        assertEquals(200, response.status)
        assertTrue(response.getHeader('Content-Type').startsWith('application/json'))
        assertTrue(response.contentAsString.length() > 0)
        assertFalse(response.contentAsString.contains("_id"))

        JsonSlurper slurper = new JsonSlurper()
        def result = slurper.parseText(response.contentAsString)
        assertTrue(result.dogs.size() == 1)

        //get the blob using a wildcard url with only a header
        sendRequest("/api/testing/wtf", 'GET', headers + ['X-jsonblob':blobId])

        assertEquals(200, response.status)

        //get the blob using a wildcard url
        sendRequest("/api/testing/$blobId/wtf", 'GET', headers)

        assertEquals(200, response.status)

        jsonBuilder.pigs {
            wilbur {
                breed 'rambunctious pig'
                age '2'
            }
        }

        // update blob
        sendRequest(relativePath, 'PUT', headers, jsonBuilder.toString().bytes)

        assertEquals(200, response.status)
        assertTrue(response.getHeader('Content-Type').startsWith('application/json'))
        assertTrue(response.contentAsString.contains("pigs"))
        assertFalse(response.contentAsString.contains("_id"))

//        // delete blob
//        sendRequest(relativePath, 'DELETE', headers)
//
//        assertEquals(204, response.status)
//        assertTrue(response.getHeader('Content-Type').startsWith('application/json'))
//        assertTrue(response.contentAsString == null || response.contentAsString == "")
//
//        // get deleted blob
//        sendRequest(relativePath, 'GET', headers)
//
//        assertEquals(404, response.status)
    }

}