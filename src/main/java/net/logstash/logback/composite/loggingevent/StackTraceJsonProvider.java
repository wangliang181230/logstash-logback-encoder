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

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

import ch.qos.logback.classic.pattern.ExtendedThrowableProxyConverter;
import ch.qos.logback.classic.pattern.ThrowableHandlingConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import net.logstash.logback.composite.AbstractFieldJsonProvider;
import net.logstash.logback.composite.FieldNamesAware;
import net.logstash.logback.composite.JsonWritingUtils;
import net.logstash.logback.fieldnames.LogstashFieldNames;

public class StackTraceJsonProvider extends AbstractFieldJsonProvider<ILoggingEvent> implements FieldNamesAware<LogstashFieldNames> {

    public static final String FIELD_STACK_TRACE = "stack_trace";
    public static final String FIELD_CAUSE = "cause";
    public static final String FIELD_CAUSE_MESSAGE = "causeMsg";

    /**
     * Used to format throwables as Strings.
     * 
     * Uses an {@link ExtendedThrowableProxyConverter} from logstash by default.
     * 
     * Consider using a
     * {@link net.logstash.logback.stacktrace.ShortenedThrowableConverter ShortenedThrowableConverter}
     * for more customization options. 
     */
    private ThrowableHandlingConverter throwableConverter = new ExtendedThrowableProxyConverter();
    
    public StackTraceJsonProvider() {
        setFieldName(FIELD_STACK_TRACE);
    }
    
    @Override
    public void start() {
        this.throwableConverter.start();
        super.start();
    }
    
    @Override
    public void stop() {
        this.throwableConverter.stop();
        super.stop();
    }
    
    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy != null) {
			// 记录堆栈
            JsonWritingUtils.writeStringField(generator, getFieldName(), throwableConverter.convert(event));

			// 记录cause类型名称（递归）
            writeCauseTo(generator, throwableProxy, 1);
        }
    }

    public void writeCauseTo(JsonGenerator generator, IThrowableProxy cause, int depth) throws IOException {
        if (cause == null) {
            return;
        }

        // 记录异常类型名称
        if (cause.getClassName() != null && !cause.getClassName().isEmpty()) {
            JsonWritingUtils.writeStringField(generator, FIELD_CAUSE + depth, cause.getClassName());
        }
        // 记录异常信息
        if (cause.getMessage() != null && !cause.getMessage().isEmpty()) {
            JsonWritingUtils.writeStringField(generator, FIELD_CAUSE_MESSAGE + depth, cause.getMessage());
        }

        // 递归
        writeCauseTo(generator, cause.getCause(), depth + 1);
    }
    
    @Override
    public void setFieldNames(LogstashFieldNames fieldNames) {
        setFieldName(fieldNames.getStackTrace());
    }
    
    public ThrowableHandlingConverter getThrowableConverter() {
        return throwableConverter;
    }
    public void setThrowableConverter(ThrowableHandlingConverter throwableConverter) {
        this.throwableConverter = throwableConverter;
    }
}
