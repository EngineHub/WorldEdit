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

package joptsimple.internal;

import java.util.Iterator;
import java.util.List;
import static java.lang.System.*;
import static java.util.Arrays.*;

/**
 * @author <a href="mailto:pholser@alumni.rice.edu">Paul Holser</a>
 * @version $Id: Strings.java,v 1.16 2009/08/13 01:05:35 pholser Exp $
 */
public final class Strings {
    public static final String EMPTY = "";
    public static final String SINGLE_QUOTE = "'";
    public static final String LINE_SEPARATOR = getProperty( "line.separator" );

    static {
        new Strings();
    }

    private Strings() {
        // nothing to do here
    }

    /**
     * <p>Gives a string consisting of the given character repeated the given number of
     * times.</p>
     *
     * @param ch the character to repeat
     * @param count how many times to repeat the character
     * @return the resultant string
     */
    public static String repeat( char ch, int count ) {
        StringBuilder buffer = new StringBuilder();

        for ( int i = 0; i < count; ++i )
            buffer.append( ch );

        return buffer.toString();
    }

    /**
     * <p>Tells whether the given string is either {@code} or consists solely of
     * whitespace characters.</p>
     *
     * @param target string to check
     * @return {@code true} if the target string is null or empty
     */
    public static boolean isNullOrEmpty( String target ) {
        return target == null || EMPTY.equals( target );
    }


    /**
     * <p>Gives a string consisting of a given string prepended and appended with
     * surrounding characters.</p>
     *
     * @param target a string
     * @param begin character to prepend
     * @param end character to append
     * @return the surrounded string
     */
    public static String surround( String target, char begin, char end ) {
        return begin + target + end;
    }

    /**
     * Gives a string consisting of the elements of a given array of strings, each
     * separated by a given separator string.
     *
     * @param pieces the strings to join
     * @param separator the separator
     * @return the joined string
     */
    public static String join( String[] pieces, String separator ) {
        return join( asList( pieces ), separator );
    }

    /**
     * Gives a string consisting of the string representations of the elements of a
     * given array of objects, each separated by a given separator string.
     *
     * @param pieces the elements whose string representations are to be joined
     * @param separator the separator
     * @return the joined string
     */
    public static String join( List<String> pieces, String separator ) {
        StringBuilder buffer = new StringBuilder();

        for ( Iterator<String> iter = pieces.iterator(); iter.hasNext(); ) {
            buffer.append( iter.next() );

            if ( iter.hasNext() )
                buffer.append( separator );
        }

        return buffer.toString();
    }
}
