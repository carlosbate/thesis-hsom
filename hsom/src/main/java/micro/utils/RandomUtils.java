package micro.utils;

import java.util.Arrays;

public final class RandomUtils {

    public static boolean isIntParsable(String... ids){
        return Arrays.stream(ids)
                .filter(s -> !isValidInt(s))
                .count() == 0;
    }

    private static boolean isValidInt(String s){
        try{
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

}
