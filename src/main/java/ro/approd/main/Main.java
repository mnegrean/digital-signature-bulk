package ro.approd.main;

import ro.approd.gui.MainView;
import ro.approd.init.AppLoader;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Main {


    private final static List<String> SYNONYMS_FOR_GREAT = Arrays.asList("COLOSSAL", "ENDLESS", "ENORMOUS", "GIGANTIC", "IMMEASURABLE", "INFINITE", "LIMITLESS",
            "MAMMOTH", "MONSTROUS", "MONUMENTAL", "TREMENDOUS", "ETERNAL", "INTERMINABLE", "UNBOUNDED", "MEASURELESS", "TITANIC");

    private final static List<String> SYNONYMS_FOR_SUCCESS = Arrays.asList("ACCOMPLISHMENT", "ACHIEVEMENT", "BENEFIT", "FAME", "GAIN", "HAPPINESS", "PROGRESS", "PROSPERITY",
            "TRIUMPH", "VICTORY", "FRUITION");

    public static void main(String[] args) {
        if (args.length > 0) {
            String path = args[0];
            new AppLoader().start(path);
            logImmeasurableTriumph();
        } else {
            new MainView();
        }
    }

    private static void logImmeasurableTriumph() {
        String great = selectFromList(SYNONYMS_FOR_GREAT);
        String success = selectFromList(SYNONYMS_FOR_SUCCESS);

        System.out.println();
        System.out.println("------------------------------------------------------------------------");
        System.out.printf("%s %s.%s", great, success, System.lineSeparator());
        System.out.println("------------------------------------------------------------------------");
    }

    private static String selectFromList(List<String> list) {
        Random randomizer = new Random();
        return list.get(randomizer.nextInt(list.size()));
    }
}
