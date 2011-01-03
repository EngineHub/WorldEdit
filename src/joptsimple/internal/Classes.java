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

/**
 * @author <a href="mailto:pholser@alumni.rice.edu">Paul Holser</a>
 * @version $Id: Classes.java,v 1.10 2009/08/13 01:05:35 pholser Exp $
 */
public final class Classes {
    static {
        new Classes();
    }

    private Classes() {
        // nothing to do here
    }

    /**
     * Gives the "short version" of the given class name.  Somewhat naive to inner
     * classes.
     *
     * @param className class name to chew on
     * @return the short name of the class
     */
    public static String shortNameOf( String className ) {
        return className.substring( className.lastIndexOf( '.' ) + 1 );
    }
}
