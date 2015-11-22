package ben.misc.jsoup;


import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

public class TemplateSpecProcessorTest {

    TemplateSpecProcessor processor = processor();

    @Test
    public void testBlockCount() {
        processor.process();
        assertTrue(processor.fragmentMap.get("blocks").children.size() == 4);
    }

    @Test
    public void testPreservesTextNode() {
        processor.process();
        assertTrue(processor.fragmentMap.get("block1").content.contains("Some text here"));
    }

    @Test
    public void testFragmentCount() {
        int numFragments = processor.root.select("fragment").size();
        processor.process();
        assertEquals(numFragments, processor.fragmentMap.size());
    }

    @Test(expected=IllegalStateException.class)
    public void testValidation() {
        processor.root.append("<fragment>test</fragment>");
        processor.process();
    }

    TemplateSpecProcessor processor() {
        try {
            return TemplateSpecProcessor.fromResource("jsoup/email-sample.html");
        }
        catch (IOException e) {
           throw new RuntimeException(e);
        }
    }


}
