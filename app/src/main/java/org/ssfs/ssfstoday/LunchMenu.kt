package org.ssfs.ssfstoday

import java.util.ArrayList
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by wilkibr on 7/5/2016.
 * Last modified 1/12/2017 to remove soups as they are no longer included
 * on the menu.
 */
class LunchMenu(rawXML: String) {

    private val newMenu: String
    internal var individualDayMenus = ArrayList<String>()

    //Regular Expressions to get the full menu for each day
    private val regExpForDays = arrayOf("MONDAY(.*?)TUESDAY", "TUESDAY(.*?)WEDNESDAY", "WEDNESDAY(.*?)THURSDAY", "THURSDAY(.*?)FRIDAY", "FRIDAY.[0-9](.*?)DINNER ENTREE")

    init {
        /*
        Constructor receives the raw XML file from the server and passes it to the function
        that will remove all tags and keep only the text data.  The second method call creates
        a complete menu (lunch, veggie, sides, etc.) for each of the days.
         */
        newMenu = stripOutXML(rawXML)
        getCompleteMenuForEachDay()
    }

    /**
     * In the Word XML file, all pertinent text is enclosed by the <w:t> and the </w:t> tags.
     * In some cases there is extra text after the opening tag.  The regular expression below
     * compensates for this and returns a solid block of text without any XML information.
     * @param rawXML xml data returned from the webserver, contains tags.
     * @return a string containing only the text from the original Word document.
     */
    private fun stripOutXML(rawXML: String): String {

        val menu = StringBuilder()
        val pattern = Pattern.compile("<w:t( .*?)?>(.*?)</w:t>")
        val m = pattern.matcher(rawXML)
        while (m.find()) {
            menu.append(m.group(2)) //group 2 refers to the second set of parenthesis
        }
        var completeMenu = String(menu)
        completeMenu = completeMenu.replace("&amp;", "&") // Removes the ugly html leftover
        return completeMenu
    }

    /**
     * End result of this function is an arraylist containing strings of a complete menu, one for
     * each day of the week.  These will later be processed and the individual items will be
     * teased out.
     */
    private fun getCompleteMenuForEachDay() {
        // Add blank string for Saturday.
        individualDayMenus.add("")

        for (expression in regExpForDays) {
            val pattern = Pattern.compile(expression)
            val m = pattern.matcher(newMenu)
            if (m.find()) {
                individualDayMenus.add(m.group(1))
            } else
                individualDayMenus.add("")
        }
        // Add blank string for Saturday.
        individualDayMenus.add("")
    }

    /**
     * Uses Regex matching to find the lunch entree for a given day. If none exists, a blank
     * string will be returned.
     * @param dayOfWeek an int - Monday = 0, Friday = 4.
     * @return the lunch entree for the given day.
     */
    fun getLunchEntree(dayOfWeek: Int): String {
        val pattern: Pattern
        if (dayOfWeek == 1) {
            pattern = Pattern.compile("VEGETARIAN ENTR[ÉE]E(.*?)VEGETARIAN ENTR[ÉE]E")
        } else {
            pattern = Pattern.compile("LUNCH ENTR[ÉE]E(.*?)VEGETARIAN|DINNER")
        }

        val m = pattern.matcher(individualDayMenus[dayOfWeek])
        return if (m.find()) {
            m.group(1)
        } else
            "No Lunch Information Found"
    }


    fun getVegetarianEntree(dayOfWeek: Int): String {
        val pattern = Pattern.compile("VEGETARIAN ENTR[ÉE]E(.*?)SIDES")
        val m = pattern.matcher(individualDayMenus[dayOfWeek])
        if (dayOfWeek == 1) {
            if (m.find()) {
                val newPattern = Pattern.compile("VEGETARIAN ENTR[ÉE]E(.*)")
                val n = newPattern.matcher(m.group(1))
                return if (n.find()) {
                    n.group(1)
                } else
                    "No Vegetarian Information Found"
            } else
                return "No Vegetarian Information Found"
        } else {
            return if (m.find()) {
                m.group(1)
            } else
                "No Vegetarian Information Found"
        }

    }

    fun getSides(dayOfWeek: Int): String {
        val pattern: Pattern
        if (dayOfWeek == 1) {
            pattern = Pattern.compile("SIDES(.*?)(DOWNTOWN|DINNER)")
        } else {
            pattern = Pattern.compile("SIDES(.*?)DOWNTOWN DELI")
        }

        val m = pattern.matcher(individualDayMenus[dayOfWeek])
        return if (m.find()) {
            // Will insert a space if there are more than one side
            // Answer courtesy of David Levine

            m.group(1).replace("(?<=[a-z])[A-Z]".toRegex(), ", $0")
        } else
            "No Sides Information Found"
    }

    fun getDeli(dayOfWeek: Int): String {
        val pattern = Pattern.compile("DOWNTOWN DELI(.*?)DINNER")
        val m = pattern.matcher(individualDayMenus[dayOfWeek])
        return if (m.find()) {
            m.group(1)
        } else
            "No Deli Information Found"
    }
}
