/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.logstash.logback.composite.loggingevent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import ch.qos.logback.classic.spi.ILoggingEvent;
import net.logstash.logback.fieldnames.LogstashFieldNames;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonGenerator;

@ExtendWith(MockitoExtension.class)
public class MdcJsonProviderTest {

    private MdcJsonProvider provider = new MdcJsonProvider();

    @Mock
    private JsonGenerator generator;

    @Mock
    private ILoggingEvent event;

    private Map<String, String> mdc;

    @BeforeEach
    public void setup() {
        mdc = new LinkedHashMap<String, String>();
        mdc.put("name1", "value1");
        mdc.put("name2", "value2");
        mdc.put("name3", "value3");
        //when(event.getMDCPropertyMap()).thenReturn(mdc);
    }

    @Test
    public void testUnwrapped() throws IOException {

        provider.writeTo(generator, event);

        verify(generator).writeFieldName("name1");
        verify(generator).writeObject("value1");
        verify(generator).writeFieldName("name2");
        verify(generator).writeObject("value2");
        verify(generator).writeFieldName("name3");
        verify(generator).writeObject("value3");
    }

    @Test
    public void testWrapped() throws IOException {
        provider.setFieldName("mdc");

        provider.writeTo(generator, event);

        InOrder inOrder = inOrder(generator);
        inOrder.verify(generator).writeObjectFieldStart("mdc");
        inOrder.verify(generator).writeFieldName("name1");
        inOrder.verify(generator).writeObject("value1");
        inOrder.verify(generator).writeFieldName("name2");
        inOrder.verify(generator).writeObject("value2");
        inOrder.verify(generator).writeFieldName("name3");
        inOrder.verify(generator).writeObject("value3");
        inOrder.verify(generator).writeEndObject();
    }

    @Test
    public void testWrappedUsingFieldNames() throws IOException {
        LogstashFieldNames fieldNames = new LogstashFieldNames();
        fieldNames.setMdc("mdc");

        provider.setFieldNames(fieldNames);

        provider.writeTo(generator, event);

        InOrder inOrder = inOrder(generator);
        inOrder.verify(generator).writeObjectFieldStart("mdc");
        inOrder.verify(generator).writeFieldName("name1");
        inOrder.verify(generator).writeObject("value1");
        inOrder.verify(generator).writeFieldName("name2");
        inOrder.verify(generator).writeObject("value2");
        inOrder.verify(generator).writeFieldName("name3");
        inOrder.verify(generator).writeObject("value3");
        inOrder.verify(generator).writeEndObject();
    }

    @Test
    public void testInclude() throws IOException {

        provider.setIncludeMdcKeyNames(Collections.singletonList("name1"));
        provider.writeTo(generator, event);

        verify(generator).writeFieldName("name1");
        verify(generator).writeObject("value1");
        verify(generator, never()).writeFieldName("name2");
        verify(generator, never()).writeObject("value2");
        verify(generator, never()).writeFieldName("name3");
        verify(generator, never()).writeObject("value3");
    }

    @Test
    public void testExclude() throws IOException {

        provider.setExcludeMdcKeyNames(Collections.singletonList("name1"));
        provider.writeTo(generator, event);

        verify(generator, never()).writeFieldName("name1");
        verify(generator, never()).writeObject("value1");
        verify(generator).writeFieldName("name2");
        verify(generator).writeObject("value2");
        verify(generator).writeFieldName("name3");
        verify(generator).writeObject("value3");
    }

    @Test
    public void testAlternateFieldName() throws IOException {
        provider.addMdcKeyFieldName("name1=alternateName1");

        provider.writeTo(generator, event);

        verify(generator).writeFieldName("alternateName1");
        verify(generator).writeObject("value1");
        verify(generator).writeFieldName("name2");
        verify(generator).writeObject("value2");
        verify(generator).writeFieldName("name3");
        verify(generator).writeObject("value3");
    }

	@Test
	public void tryConvertToNumber() {
		System.out.println(Long.MAX_VALUE);
		System.out.println(Integer.MAX_VALUE);

		assertEquals(1, MdcJsonProvider.tryConvertToNumber("1"));
		assertEquals(1, MdcJsonProvider.tryConvertToNumber("+1"));
		assertEquals(-1, MdcJsonProvider.tryConvertToNumber("-1"));

		assertEquals(1.1D, MdcJsonProvider.tryConvertToNumber("1.1"));
		assertEquals(1.1D, MdcJsonProvider.tryConvertToNumber("+1.1"));
		assertEquals(-1.1D, MdcJsonProvider.tryConvertToNumber("-1.1"));

		assertEquals(123456789, MdcJsonProvider.tryConvertToNumber("123456789"));
		assertEquals(123456789, MdcJsonProvider.tryConvertToNumber("+123456789"));
		assertEquals(-123456789, MdcJsonProvider.tryConvertToNumber("-123456789"));

		assertEquals(1234567890L, MdcJsonProvider.tryConvertToNumber("1234567890"));
		assertEquals(1234567890L, MdcJsonProvider.tryConvertToNumber("+1234567890"));
		assertEquals(-1234567890L, MdcJsonProvider.tryConvertToNumber("-1234567890"));

		assertEquals((long) Integer.MAX_VALUE, MdcJsonProvider.tryConvertToNumber(String.valueOf(Integer.MAX_VALUE)));
		assertEquals((long) Integer.MIN_VALUE, MdcJsonProvider.tryConvertToNumber(String.valueOf(Integer.MIN_VALUE)));

		assertEquals(Long.MAX_VALUE, MdcJsonProvider.tryConvertToNumber(String.valueOf(Long.MAX_VALUE)));
		assertEquals(Long.MIN_VALUE, MdcJsonProvider.tryConvertToNumber(String.valueOf(Long.MIN_VALUE)));

		assertEquals("value1", MdcJsonProvider.tryConvertToNumber("value1"));
	}
}
