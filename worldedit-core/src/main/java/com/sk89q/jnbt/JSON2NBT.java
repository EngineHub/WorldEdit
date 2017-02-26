package com.sk89q.jnbt;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.extension.input.InputParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.regex.Pattern;

public class JSON2NBT {
    private static final Pattern INT_ARRAY_MATCHER = Pattern.compile("\\[[-+\\d|,\\s]+\\]");

    private JSON2NBT() {
    }

    public static CompoundTag getTagFromJson(String jsonString) throws InputParseException {
        jsonString = jsonString.trim();
        if(!jsonString.startsWith("{")) {
            throw new InputParseException("Invalid tag encountered, expected \'{\' as first char.");
        } else if(topTagsCount(jsonString) != 1) {
            throw new InputParseException("Encountered multiple top tags, only one expected");
        } else {
            return (CompoundTag)nameValueToNBT("tag", jsonString).parse();
        }
    }

    public static int topTagsCount(String str) throws InputParseException {
        int i = 0;
        boolean flag = false;
        Stack stack = new Stack();

        for(int j = 0; j < str.length(); ++j) {
            char c0 = str.charAt(j);
            if(c0 == 34) {
                if(isCharEscaped(str, j)) {
                    if(!flag) {
                        throw new InputParseException("Illegal use of \\\": " + str);
                    }
                } else {
                    flag = !flag;
                }
            } else if(!flag) {
                if(c0 != 123 && c0 != 91) {
                    if(c0 == 125 && (stack.isEmpty() || ((Character)stack.pop()).charValue() != 123)) {
                        throw new InputParseException("Unbalanced curly brackets {}: " + str);
                    }

                    if(c0 == 93 && (stack.isEmpty() || ((Character)stack.pop()).charValue() != 91)) {
                        throw new InputParseException("Unbalanced square brackets []: " + str);
                    }
                } else {
                    if(stack.isEmpty()) {
                        ++i;
                    }

                    stack.push(Character.valueOf(c0));
                }
            }
        }

        if(flag) {
            throw new InputParseException("Unbalanced quotation: " + str);
        } else if(!stack.isEmpty()) {
            throw new InputParseException("Unbalanced brackets: " + str);
        } else {
            if(i == 0 && !str.isEmpty()) {
                i = 1;
            }

            return i;
        }
    }

    private static JSON2NBT.Any joinStrToNBT(String... args) throws InputParseException {
        return nameValueToNBT(args[0], args[1]);
    }

    private static JSON2NBT.Any nameValueToNBT(String key, String value) throws InputParseException {
        value = value.trim();
        String s;
        boolean c0;
        char c01;
        if(value.startsWith("{")) {
            value = value.substring(1, value.length() - 1);

            JSON2NBT.Compound JSON2NBT$list1;
            for(JSON2NBT$list1 = new JSON2NBT.Compound(key); value.length() > 0; value = value.substring(s.length() + 1)) {
                s = nextNameValuePair(value, true);
                if(s.length() > 0) {
                    c0 = false;
                    JSON2NBT$list1.tagList.add(getTagFromNameValue(s, false));
                }

                if(value.length() < s.length() + 1) {
                    break;
                }

                c01 = value.charAt(s.length());
                if(c01 != 44 && c01 != 123 && c01 != 125 && c01 != 91 && c01 != 93) {
                    throw new InputParseException("Unexpected token \'" + c01 + "\' at: " + value.substring(s.length()));
                }
            }

            return JSON2NBT$list1;
        } else if(value.startsWith("[") && !INT_ARRAY_MATCHER.matcher(value).matches()) {
            value = value.substring(1, value.length() - 1);

            JSON2NBT.List JSON2NBT$list;
            for(JSON2NBT$list = new JSON2NBT.List(key); value.length() > 0; value = value.substring(s.length() + 1)) {
                s = nextNameValuePair(value, false);
                if(s.length() > 0) {
                    c0 = true;
                    JSON2NBT$list.tagList.add(getTagFromNameValue(s, true));
                }

                if(value.length() < s.length() + 1) {
                    break;
                }

                c01 = value.charAt(s.length());
                if(c01 != 44 && c01 != 123 && c01 != 125 && c01 != 91 && c01 != 93) {
                    throw new InputParseException("Unexpected token \'" + c01 + "\' at: " + value.substring(s.length()));
                }
            }

            return JSON2NBT$list;
        } else {
            return new JSON2NBT.Primitive(key, value);
        }
    }

    private static JSON2NBT.Any getTagFromNameValue(String str, boolean isArray) throws InputParseException {
        String s = locateName(str, isArray);
        String s1 = locateValue(str, isArray);
        return joinStrToNBT(new String[]{s, s1});
    }

    private static String nextNameValuePair(String str, boolean isCompound) throws InputParseException {
        int i = getNextCharIndex(str, ':');
        int j = getNextCharIndex(str, ',');
        if(isCompound) {
            if(i == -1) {
                throw new InputParseException("Unable to locate name/value separator for string: " + str);
            }

            if(j != -1 && j < i) {
                throw new InputParseException("Name error at: " + str);
            }
        } else if(i == -1 || i > j) {
            i = -1;
        }

        return locateValueAt(str, i);
    }

    private static String locateValueAt(String str, int index) throws InputParseException {
        Stack stack = new Stack();
        int i = index + 1;
        boolean flag = false;
        boolean flag1 = false;
        boolean flag2 = false;

        for(int j = 0; i < str.length(); ++i) {
            char c0 = str.charAt(i);
            if(c0 == 34) {
                if(isCharEscaped(str, i)) {
                    if(!flag) {
                        throw new InputParseException("Illegal use of \\\": " + str);
                    }
                } else {
                    flag = !flag;
                    if(flag && !flag2) {
                        flag1 = true;
                    }

                    if(!flag) {
                        j = i;
                    }
                }
            } else if(!flag) {
                if(c0 != 123 && c0 != 91) {
                    if(c0 == 125 && (stack.isEmpty() || ((Character)stack.pop()).charValue() != 123)) {
                        throw new InputParseException("Unbalanced curly brackets {}: " + str);
                    }

                    if(c0 == 93 && (stack.isEmpty() || ((Character)stack.pop()).charValue() != 91)) {
                        throw new InputParseException("Unbalanced square brackets []: " + str);
                    }

                    if(c0 == 44 && stack.isEmpty()) {
                        return str.substring(0, i);
                    }
                } else {
                    stack.push(Character.valueOf(c0));
                }
            }

            if(!Character.isWhitespace(c0)) {
                if(!flag && flag1 && j != i) {
                    return str.substring(0, j + 1);
                }

                flag2 = true;
            }
        }

        return str.substring(0, i);
    }

    private static String locateName(String str, boolean isArray) throws InputParseException {
        if(isArray) {
            str = str.trim();
            if(str.startsWith("{") || str.startsWith("[")) {
                return "";
            }
        }

        int i = getNextCharIndex(str, ':');
        if(i == -1) {
            if(isArray) {
                return "";
            } else {
                throw new InputParseException("Unable to locate name/value separator for string: " + str);
            }
        } else {
            return str.substring(0, i).trim();
        }
    }

    private static String locateValue(String str, boolean isArray) throws InputParseException {
        if(isArray) {
            str = str.trim();
            if(str.startsWith("{") || str.startsWith("[")) {
                return str;
            }
        }

        int i = getNextCharIndex(str, ':');
        if(i == -1) {
            if(isArray) {
                return str;
            } else {
                throw new InputParseException("Unable to locate name/value separator for string: " + str);
            }
        } else {
            return str.substring(i + 1).trim();
        }
    }

    private static int getNextCharIndex(String str, char targetChar) {
        int i = 0;

        for(boolean flag = true; i < str.length(); ++i) {
            char c0 = str.charAt(i);
            if(c0 == 34) {
                if(!isCharEscaped(str, i)) {
                    flag = !flag;
                }
            } else if(flag) {
                if(c0 == targetChar) {
                    return i;
                }

                if(c0 == 123 || c0 == 91) {
                    return -1;
                }
            }
        }

        return -1;
    }

    private static boolean isCharEscaped(String str, int index) {
        return index > 0 && str.charAt(index - 1) == 92 && !isCharEscaped(str, index - 1);
    }

    private static class Primitive extends JSON2NBT.Any {
        private static final Pattern DOUBLE = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+[d|D]");
        private static final Pattern FLOAT = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+[f|F]");
        private static final Pattern BYTE = Pattern.compile("[-+]?[0-9]+[b|B]");
        private static final Pattern LONG = Pattern.compile("[-+]?[0-9]+[l|L]");
        private static final Pattern SHORT = Pattern.compile("[-+]?[0-9]+[s|S]");
        private static final Pattern INTEGER = Pattern.compile("[-+]?[0-9]+");
        private static final Pattern DOUBLE_UNTYPED = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+");
        private static final Splitter SPLITTER = Splitter.on(',').omitEmptyStrings();
        protected String jsonValue;

        public Primitive(String jsonIn, String valueIn) {
            this.json = jsonIn;
            this.jsonValue = valueIn;
        }

        public Tag parse() throws InputParseException {
            try {
                if(DOUBLE.matcher(this.jsonValue).matches()) {
                    return new DoubleTag(Double.parseDouble(this.jsonValue.substring(0, this.jsonValue.length() - 1)));
                }

                if(FLOAT.matcher(this.jsonValue).matches()) {
                    return new FloatTag(Float.parseFloat(this.jsonValue.substring(0, this.jsonValue.length() - 1)));
                }

                if(BYTE.matcher(this.jsonValue).matches()) {
                    return new ByteTag(Byte.parseByte(this.jsonValue.substring(0, this.jsonValue.length() - 1)));
                }

                if(LONG.matcher(this.jsonValue).matches()) {
                    return new LongTag(Long.parseLong(this.jsonValue.substring(0, this.jsonValue.length() - 1)));
                }

                if(SHORT.matcher(this.jsonValue).matches()) {
                    return new ShortTag(Short.parseShort(this.jsonValue.substring(0, this.jsonValue.length() - 1)));
                }

                if(INTEGER.matcher(this.jsonValue).matches()) {
                    return new IntTag(Integer.parseInt(this.jsonValue));
                }

                if(DOUBLE_UNTYPED.matcher(this.jsonValue).matches()) {
                    return new DoubleTag(Double.parseDouble(this.jsonValue));
                }

                if("true".equalsIgnoreCase(this.jsonValue) || "false".equalsIgnoreCase(this.jsonValue)) {
                    return new ByteTag((byte)(Boolean.parseBoolean(this.jsonValue)?1:0));
                }
            } catch (NumberFormatException var6) {
                this.jsonValue = this.jsonValue.replaceAll("\\\\\"", "\"");
                return new StringTag(this.jsonValue);
            }

            if(this.jsonValue.startsWith("[") && this.jsonValue.endsWith("]")) {
                String var7 = this.jsonValue.substring(1, this.jsonValue.length() - 1);
                String[] var8 = (String[])((String[]) Iterables.toArray(SPLITTER.split(var7), String.class));

                try {
                    int[] var5 = new int[var8.length];

                    for(int j = 0; j < var8.length; ++j) {
                        var5[j] = Integer.parseInt(var8[j].trim());
                    }

                    return new IntArrayTag(var5);
                } catch (NumberFormatException var51) {
                    return new StringTag(this.jsonValue);
                }
            } else {
                if(this.jsonValue.startsWith("\"") && this.jsonValue.endsWith("\"")) {
                    this.jsonValue = this.jsonValue.substring(1, this.jsonValue.length() - 1);
                }

                this.jsonValue = this.jsonValue.replaceAll("\\\\\"", "\"");
                StringBuilder stringbuilder = new StringBuilder();

                for(int i = 0; i < this.jsonValue.length(); ++i) {
                    if(i < this.jsonValue.length() - 1 && this.jsonValue.charAt(i) == 92 && this.jsonValue.charAt(i + 1) == 92) {
                        stringbuilder.append('\\');
                        ++i;
                    } else {
                        stringbuilder.append(this.jsonValue.charAt(i));
                    }
                }

                return new StringTag(stringbuilder.toString());
            }
        }
    }

    private static class List extends JSON2NBT.Any {
        protected java.util.List<JSON2NBT.Any> tagList = Lists.newArrayList();

        public List(String json) {
            this.json = json;
        }

        public Tag parse() throws InputParseException {
            ArrayList<Tag> list = new ArrayList();
            Iterator var2 = this.tagList.iterator();

            while(var2.hasNext()) {
                JSON2NBT.Any JSON2NBT$any = (JSON2NBT.Any)var2.next();
                list.add(JSON2NBT$any.parse());
            }
            Class<? extends Tag> tagType = list.isEmpty() ? CompoundTag.class : list.get(0).getClass();
            return new ListTag(tagType, list);
        }
    }

    private static class Compound extends JSON2NBT.Any {
        protected java.util.List<JSON2NBT.Any> tagList = Lists.newArrayList();

        public Compound(String jsonIn) {
            this.json = jsonIn;
        }

        public Tag parse() throws InputParseException {
            HashMap<String, Tag> map = new HashMap<String, Tag>();
            Iterator var2 = this.tagList.iterator();

            while(var2.hasNext()) {
                JSON2NBT.Any JSON2NBT$any = (JSON2NBT.Any)var2.next();
                map.put(JSON2NBT$any.json, JSON2NBT$any.parse());
            }

            return new CompoundTag(map);
        }
    }

    private abstract static class Any {
        protected String json;

        Any() {
        }

        public abstract Tag parse() throws InputParseException;
    }
}
