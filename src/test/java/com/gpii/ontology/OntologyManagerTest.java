package com.gpii.ontology;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class OntologyManagerTest extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public OntologyManagerTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( OntologyManagerTest.class );
    }

    public void testOntologyManager()
    {
         Assert.assertEquals(OntologyManager.getInstance().testHello("guest"), "Hello guest!");
    }
}
