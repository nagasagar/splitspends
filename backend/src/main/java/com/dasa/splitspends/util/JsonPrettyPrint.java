package com.dasa.splitspends.util;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class JsonPrettyPrint {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: JsonPrettyPrint <input.json> <output.json>");
            System.exit(1);
        }
        ObjectMapper mapper = new ObjectMapper();
        Object json = mapper.readValue(new File(args[0]), Object.class);
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        writer.writeValue(new File(args[1]), json);
    }
}
