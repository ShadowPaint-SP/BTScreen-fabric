package de.drvlabs.btscreen.config;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import fi.dy.masa.malilib.MaLiLib;
import fi.dy.masa.malilib.config.options.ConfigInteger;

public class ConfigTicks extends ConfigInteger {
    public ConfigTicks(String name, int defaultValue) {
        super(name, defaultValue);
    }

    public ConfigTicks(String name, int defaultValue, int minValue, int maxValue) {
        super(name, defaultValue, minValue, maxValue);
    }

    public ConfigTicks(String name, int defaultValue, int minValue, int maxValue, boolean useSlider) {
        super(name, defaultValue, minValue, maxValue, useSlider);
    }

    public ConfigTicks(String name, int defaultValue, int minValue, int maxValue, boolean useSlider, String comment) {
        super(name, defaultValue, minValue, maxValue, useSlider, comment);
    }

    public ConfigTicks(String name, int defaultValue, int minValue, int maxValue, boolean useSlider, String comment,
            String prettyName) {
        super(name, defaultValue, minValue, maxValue, useSlider, comment, prettyName);
    }

    public ConfigTicks(String name, int defaultValue, int minValue, int maxValue, boolean useSlider, String comment,
            String prettyName, String translatedName) {
        super(name, defaultValue, minValue, maxValue, useSlider, comment, prettyName, translatedName);
    }

    public ConfigTicks(String name, int defaultValue, int minValue, int maxValue, String comment) {
        super(name, defaultValue, minValue, maxValue, comment);
    }

    public ConfigTicks(String name, int defaultValue, int minValue, int maxValue, String comment, String prettyName) {
        super(name, defaultValue, minValue, maxValue, comment, prettyName);
    }

    public ConfigTicks(String name, int defaultValue, int minValue, int maxValue, String comment, String prettyName,
            String translatedName) {
        super(name, defaultValue, minValue, maxValue, comment, prettyName, translatedName);
    }

    public ConfigTicks(String name, int defaultValue, String comment) {
        super(name, defaultValue, comment);
    }

    public ConfigTicks(String name, int defaultValue, String comment, String prettyName) {
        super(name, defaultValue, comment, prettyName);
    }

    public ConfigTicks(String name, int defaultValue, String comment, String prettyName, String translatedName) {
        super(name, defaultValue, comment, prettyName, translatedName);
    }

    @Override
    public String getDefaultStringValue() {
        return int2String(this.defaultValue);
    }

    @Override
    public String getStringValue() {
        return int2String(this.value);
    }

    @Override
    public void setValueFromString(String value) {
        try {
            this.setIntegerValue(string2Int(value));
        } catch (Exception e) {
            MaLiLib.LOGGER.warn("Failed to set config value for {} from the string '{}'", this.getName(), value, e);
        }
    }

    private static String int2String(int i) {
        if (i == 0) {
            return "0t";
        }
        StringBuilder s = new StringBuilder();
        for (TimeUnit unit : TimeUnit.SORTED_BY_TICKS_DESC) {
            if (i >= unit.ticks) {
                int val = i / unit.ticks;
                i %= unit.ticks;
                s.append(val).append(unit.unit);
            }
        }
        return s.toString();
    }

    private static int string2Int(String s) {
        s = s.toLowerCase().trim();

        // Handle plain numbers as ticks
        if (s.matches("^\\d+$")) {
            return Integer.parseInt(s);
        }

        int totalTicks = 0;
        // Regex to find pairs of numbers and letters
        Pattern pattern = Pattern.compile("(\\d+)([a-zA-Z])");
        Matcher matcher = pattern.matcher(s);

        int lastMatchEnd = 0;
        while (matcher.find()) {
            // Check for unparsed characters between matches
            if (matcher.start() != lastMatchEnd) {
                throw new NumberFormatException("Invalid characters in time string: " + s);
            }

            int value = Integer.parseInt(matcher.group(1));
            char unitChar = matcher.group(2).charAt(0);

            boolean unitFound = false;
            for (TimeUnit unit : TimeUnit.values()) {
                if (unit.unit == unitChar) {
                    totalTicks += value * unit.ticks;
                    unitFound = true;
                    break;
                }
            }

            if (!unitFound) {
                throw new NumberFormatException("Unknown time unit '" + unitChar + "' in string: " + s);
            }

            lastMatchEnd = matcher.end();
        }

        // Check if the entire string was parsed
        if (lastMatchEnd != s.length()) {
            throw new NumberFormatException("Could not fully parse time string: " + s);
        }

        return totalTicks;
    }

    private enum TimeUnit {
        TICKS('t', 1),
        SECONDS('s', TICKS_PER_SECOND),
        MINUTES('m', SECONDS.ticks * 60),
        HOURS('h', MINUTES.ticks * 60),
        DAYS('d', HOURS.ticks * 24),
        WEEKS('w', DAYS.ticks * 7);

        final char unit;
        final int ticks;

        TimeUnit(char unit, int ticks) {
            this.unit = unit;
            this.ticks = ticks;
        }

        static final List<TimeUnit> SORTED_BY_TICKS_DESC = Arrays.stream(values())
                .sorted(Comparator.comparingInt(o -> -o.ticks))
                .collect(Collectors.toList());
    }
}
