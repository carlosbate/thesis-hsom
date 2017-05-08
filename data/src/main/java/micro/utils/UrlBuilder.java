package micro.utils;

public final class UrlBuilder {

    public static String createInUrl(String base){
        return base + "-in";
    }

    public static String createOutUrl(String base){
        return base + "-out";
    }

    public static String createKillUrl(String base){
        return base + "-kill";
    }

}
