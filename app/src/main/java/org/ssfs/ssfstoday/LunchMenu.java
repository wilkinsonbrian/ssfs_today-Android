package org.ssfs.ssfstoday;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wilkibr on 7/5/2016.
 * Last modified 1/12/2017 to remove soups as they are no longer included
 * on the menu.
 */
public class LunchMenu {

    private String newMenu;
    ArrayList<String> individualDayMenus = new ArrayList<>();

    //Regular Expressions to get the full menu for each day
    private String[] regExpForDays = {"MONDAY(.*?)TUESDAY","TUESDAY(.*?)WEDNESDAY",
            "WEDNESDAY(.*?)THURSDAY", "THURSDAY(.*?)FRIDAY", "FRIDAY.[0-9](.*?)DINNER ENTREE"};

    public LunchMenu(String rawXML) {
        /*
        Constructor receives the raw XML file from the server and passes it to the function
        that will remove all tags and keep only the text data.  The second method call creates
        a complete menu (lunch, veggie, sides, etc.) for each of the days.
         */
        newMenu = stripOutXML(rawXML);
        getCompleteMenuForEachDay();
    }

    /**
     * In the Word XML file, all pertinent text is enclosed by the <w:t> and the </w:t> tags.
     * In some cases there is extra text after the opening tag.  The regular expression below
     * compensates for this and returns a solid block of text without any XML information.
     * @param rawXML xml data returned from the webserver, contains tags.
     * @return a string containing only the text from the original Word document.
     */
    private String stripOutXML(String rawXML) {

        StringBuilder menu = new StringBuilder();
        Pattern pattern = Pattern.compile("<w:t( .*?)?>(.*?)</w:t>");
        Matcher m = pattern.matcher(rawXML);
        while (m.find()) {
            menu.append(m.group(2)); //group 2 refers to the second set of parenthesis
        }
        String completeMenu = new String(menu);
        completeMenu = completeMenu.replace("&amp;", "&"); // Removes the ugly html leftover
        return completeMenu;
    }

    /**
     * End result of this function is an arraylist containing strings of a complete menu, one for
     * each day of the week.  These will later be processed and the individual items will be
     * teased out.
     */
    private void getCompleteMenuForEachDay() {
        // Add blank string for Saturday.
        individualDayMenus.add("");

        for (String expression : regExpForDays) {
            Pattern pattern = Pattern.compile(expression);
            Matcher m = pattern.matcher(newMenu);
            if (m.find()) {
                individualDayMenus.add(m.group(1));
            } else individualDayMenus.add("");
        }
        // Add blank string for Saturday.
        individualDayMenus.add("");
    }

    /**
     * Uses Regex matching to find the lunch entree for a given day. If none exists, a blank
     * string will be returned.
     * @param dayOfWeek an int - Monday = 0, Friday = 4.
     * @return the lunch entree for the given day.
     */
    public String getLunchEntree(int dayOfWeek) {
        Pattern pattern;
        if (dayOfWeek == 1) {
            pattern = Pattern.compile("VEGETARIAN ENTRÉE(.*?)VEGETARIAN ENTRÉE");
        } else {
            pattern = Pattern.compile("LUNCH ENTRÉE(.*?)VEGETARIAN|DINNER");
        }

        Matcher m = pattern.matcher(individualDayMenus.get(dayOfWeek));
        if (m.find()) {
            return m.group(1);
        } else return "No Lunch Information Found";
    }


    public String getVegetarianEntree(int dayOfWeek) {
        Pattern pattern = Pattern.compile("VEGETARIAN ENTRÉE(.*?)SIDES");
        Matcher m = pattern.matcher(individualDayMenus.get(dayOfWeek));
        if (dayOfWeek == 1) {
            if (m.find()) {
                Pattern newPattern = Pattern.compile("VEGETARIAN ENTRÉE(.*)");
                Matcher n = newPattern.matcher(m.group(1));
                if (n.find()) {
                    return n.group(1);
                } else return "No Vegetarian Information Found";
            } else return "No Vegetarian Information Found";
        } else {
            if (m.find()) {
                return m.group(1);
            } else return "No Vegetarian Information Found";
        }

    }

    public String getSides(int dayOfWeek) {
        Pattern pattern;
        if (dayOfWeek == 1) {
            pattern = Pattern.compile("SIDES(.*?)DINNER");
        } else {
            pattern = Pattern.compile("SIDES(.*?)DOWNTOWN DELI");
        }

        Matcher m = pattern.matcher(individualDayMenus.get(dayOfWeek));
        if (m.find()) {
            // Will insert a space if there are more than one side
            // Answer courtesy of David Levine

            return m.group(1).replaceAll("(?<=[a-z])[A-Z]", ", $0");
        } else return "No Sides Information Found";
    }

    public String getDeli(int dayOfWeek) {
        Pattern pattern = Pattern.compile("DOWNTOWN DELI(.*?)DINNER");
        Matcher m = pattern.matcher(individualDayMenus.get(dayOfWeek));
        if (m.find()) {
            return m.group(1);
        } else return "No Deli Information Found";
    }
}
