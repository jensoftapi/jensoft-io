package com.jensoft.catalog.server.config;


import java.io.IOException;
import java.io.InputStream;


public class Closing {

    public static interface Closure {
        public void f(InputStream in) throws IOException;
    }

    public static Closing with(final InputStream in) {
        return new Closing(in);
    }
    
    private final InputStream in;

    public Closing(final InputStream in) {
        this.in = in;
    }

    public void f(final Closure c) throws IOException {
        if (in == null) {
            return;
        }
        try {
            c.f(in);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                throw ex;
            }
        }
    }
}
