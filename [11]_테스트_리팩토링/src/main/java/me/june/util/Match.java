package me.june.util;

public class Match {
    public final String searchString;
    public final String surroundingContext;
    public final String searchTitle;

    public Match(String searchTitle, String searchString, String surroundingContext) {
        this.searchTitle = searchTitle;
        this.searchString = searchString;
        this.surroundingContext = surroundingContext;
    }
}
