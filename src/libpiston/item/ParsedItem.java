package libpiston.item;

import libpiston.ParseException;
import libpiston.util.IntervalUnion;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;

public class ParsedItem {
    public String modid;
    public String name;
    public IntervalUnion meta;
    public NBTTagCompound tag;
    public int count;

    public ParsedItem(String modid, String name, IntervalUnion meta, NBTTagCompound tag, int count) {
        this.modid = modid;
        this.name = name;
        this.meta = meta;
        this.tag = tag;
        this.count = count;
    }

    /**
     * Creates a ParsedItem used to provide defaults for parsing
     */
    public static ParsedItem defaults(IntervalUnion meta, NBTTagCompound tag, int count) {
        return new ParsedItem("", "", meta, tag, count);
    }

    /**
     * Parses a item string into a ParsedItem.
     *
     * The string should be in the format:
     *   modid:name:meta+tagxcount
     * 
     * where:
     *   - modid and name are strings
     *   - meta is an IntervalUnion (e.g. 1-3,5,7-9)
     *   - tag is a JSON object
     *   - count is an integer
     *
     * For example: minecraft:stone:1+{"display":{"Name":"Stone"}}x64
     * 
     * meta, tag, and count are optional.
     * - meta defaults to *
     * - tag defaults to null
     * - count defaults to 1
     * 
     * Or use parseWithDefault to provide defaults.
     */
    public static ParsedItem parse(String s) throws ParseException {
        return parseWithDefault(s, defaults(IntervalUnion.parse("*"), null, 1));
    }

    /**
     * Parses a item string into a ParsedItem.
     *
     * Uses the provided defaults for missing meta, tag, and count.
     */
    public static ParsedItem parseWithDefault(String s, ParsedItem defaults) throws ParseException {
        s = s.trim();
        int modidEnd = s.indexOf(':');
        if (modidEnd < 0) {
            throw new ParseException("Can't find modid", null);
        }
        String modid = s.substring(0, modidEnd);
        // modid:...

        int nameEnd = s.indexOf(':', modidEnd + 1);
        if (nameEnd < 0) {
            // modid:name
            String name = s.substring(modidEnd + 1);
            return new ParsedItem(modid, name, defaults.meta, defaults.tag, defaults.count);
        }
        String name = s.substring(modidEnd + 1, nameEnd);
        // modid:name:...

        int tagStart = s.indexOf('+', nameEnd + 1);
        if (tagStart < 0) {
            tagStart = s.length();
        }
        int countStart = s.indexOf('x', nameEnd + 1);
        if (countStart < 0) {
            countStart = s.length();
        }
        int metaEnd = Math.min(tagStart, countStart);
        String metaStr = s.substring(nameEnd + 1, metaEnd);
        if (metaStr.isEmpty()) {
            // modid:name:
            return new ParsedItem(modid, name, defaults.meta, defaults.tag, defaults.count);
        }
        IntervalUnion meta = IntervalUnion.parse(metaStr);
        // modid:name:meta...

        if (metaEnd == s.length()) {
            // modid:name:meta
            return new ParsedItem(modid, name, meta, defaults.tag, defaults.count);
        }
        // modid:name:meta[+|x]...

        String tagStr;
        String countStr = null;
        countStart = s.lastIndexOf('x');
        if (countStart < 0) {
            // modid:name:meta+{...}
            tagStr = s.substring(metaEnd + 1);
        } else {
            // modid:name:meta+{...x...
            boolean hasCount = true;
            for (int i = countStart + 1; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c < '0' || c > '9') {
                    hasCount = false;
                    break;
                }
            }
            if (hasCount) {
                // modid:name:meta+{...}x999
                tagStr = s.substring(metaEnd + 1, countStart);
                countStr = s.substring(countStart + 1);
            } else {
                // modid:name:meta+{..x...}
                tagStr = s.substring(metaEnd + 1);
            }
        }

        NBTTagCompound tag = null;
        try {
            NBTBase tagBase = JsonToNBT.func_150315_a(tagStr);
            if (!(tagBase instanceof NBTTagCompound)) {
                throw new ParseException("Tag must be an object", null);
            }
            tag = (NBTTagCompound) tagBase;
        } catch (NBTException e) {
            throw new ParseException("Failed to parse NBT from: " + tagStr, e);
        }

        if (countStr == null) {
            return new ParsedItem(modid, name, meta, tag, defaults.count);
        }

        try {
            int count = Integer.parseInt(countStr);
            return new ParsedItem(modid, name, meta, tag, count);
        } catch (NumberFormatException e) {
            throw new ParseException("Failed to parse count from: " + countStr, e);
        }

    }
}
