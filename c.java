package com.msim.seismic_datafeed.jobs.salescoverage;

import com.msim.seismic_datafeed.jobs.salescoverage.model.SalescoverageDataItem;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.verify;

public class SalescoverageDataItemWriterTest {

    private SalescoverageDataItemWriter writer;

    @Mock
    private FlatFileItemWriter<SalescoverageDataItem> delegate;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        writer = new SalescoverageDataItemWriter(delegate);
    }

    @Test
    public void testWrite() throws Exception {
        List<SalescoverageDataItem> items = Arrays.asList(new SalescoverageDataItem(), new SalescoverageDataItem());
        writer.write(Arrays.asList(items));
        verify(delegate).write(items);
    }

    @Test
    public void testOpen() {
        ExecutionContext context = new ExecutionContext();
        writer.open(context);
        verify(delegate).open(context);
    }

    @Test
    public void testUpdate() {
        ExecutionContext context = new ExecutionContext();
        writer.update(context);
        verify(delegate).update(context);
    }

    @Test
    public void testClose() {
        writer.close();
        verify(delegate).close();
    }
}
