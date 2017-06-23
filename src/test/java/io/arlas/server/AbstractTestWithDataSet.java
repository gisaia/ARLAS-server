package io.arlas.server;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import io.arlas.server.model.request.Filter;
import io.arlas.server.model.request.Request;

public abstract class AbstractTestWithDataSet extends AbstractTest {
    
    public static String COLLECTION_NAME = "geodata";
    static DataSetTool dataset = null;
    
    protected static Request request = new Request();
    static{
        request.filter = new Filter();
    }

    @BeforeClass
    public static void beforeClass() {
        try {
           dataset = DataSetTool.init();
           dataset.loadDataSet();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void afterClass() {
        dataset.clearDataSet();
    }
}
