package micro.utils;

public final class UrlBuilder {

    public static String createInUrl(String base){
        return base + "-in";
    }

    public static String createOutUrl(String base){
        return base + "-out";
    }

    public static String createGetDataUrl(String base){
        return base + "-getdata";
    }

    public static String createGetDataHitCountUrl(String base){
        return base + "-hitcount";
    }

    public static String createGetDataUMatUrl(String base){
        return base + "-umat";
    }

    public static String createGetDataWeightsUrl(String base){
        return base + "-weight";
    }

    public static String createStopUrl(String base){
        return base + "-stop";
    }

}
