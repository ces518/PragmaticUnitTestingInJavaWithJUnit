package me.june.iloveyouboss;

import java.io.IOException;

public interface Http {
    String get(String url) throws IOException;
}
