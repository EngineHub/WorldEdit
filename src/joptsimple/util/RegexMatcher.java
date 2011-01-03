/*
 The MIT License

 Copyright (c) 2009 Paul R. Holser, Jr.

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package joptsimple.util;

import java.util.regex.Pattern;
import static java.util.regex.Pattern.*;

import joptsimple.ValueConversionException;
import joptsimple.ValueConverter;

/**
 * Ensures that values entirely match a regular expression.
 *
 * @author <a href="mailto:pholser@alumni.rice.edu">Paul Holser</a>
 * @version $Id: RegexMatcher.java,v 1.6 2009/10/25 18:37:09 pholser Exp $
 */
public class RegexMatcher implements ValueConverter<String> {
    private final Pattern pattern;

    /**
     * Creates a matcher that uses the given regular expression, modified by the given
     * flags.
     *
     * @param pattern the regular expression pattern
     * @param flags modifying regex flags
     * @throws IllegalArgumentException if bit values other than those corresponding to
     * the defined match flags are set in {@code flags}
     * @throws java.util.regex.PatternSyntaxException if the expression's syntax is
     * invalid
     */
    public RegexMatcher( String pattern, int flags ) {
        this.pattern = compile( pattern, flags );
    }

    /**
     * Gives a matcher that uses the given regular expression.
     *
     * @param pattern the regular expression pattern
     * @return the new converter
     * @throws java.util.regex.PatternSyntaxException if the expression's syntax is
     * invalid
     */
    public static ValueConverter<String> regex( String pattern ) {
        return new RegexMatcher( pattern, 0 );
    }

    public String convert( String value ) {
        if ( !pattern.matcher( value ).matches() ) {
            throw new ValueConversionException(
                "Value [" + value + "] did not match regex [" + pattern.pattern() + ']' );
        }

        return value;
    }

    public Class<String> valueType() {
        return String.class;
    }

    public String valuePattern() {
        return pattern.pattern();
    }
}
