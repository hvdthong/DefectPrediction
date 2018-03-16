package org.apache.camel.component.file.remote;

import java.io.ByteArrayOutputStream;

import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.apache.camel.impl.DefaultExchange;

public class RemoteFileExchange<T extends RemoteFileBinding> extends DefaultExchange {
    private T binding;

    public RemoteFileExchange(CamelContext context, ExchangePattern pattern, T binding) {
        super(context, pattern);
        this.binding = binding;
    }

    public RemoteFileExchange(CamelContext context, ExchangePattern pattern, T binding, String host, 
                              String fullFileName, String fileName, long fileLength, ByteArrayOutputStream outputStream) {
        this(context, pattern, binding);
        setIn(new RemoteFileMessage(host, fullFileName, fileName, fileLength, outputStream));
    }

    public T getBinding() {
        return binding;
    }

    public void setBinding(T binding) {
        this.binding = binding;
    }
}