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

package joptsimple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import static java.util.Collections.*;

import static joptsimple.internal.Strings.*;

/**
 * <p>Thrown when a problem occurs during option parsing.</p>
 *
 * @author <a href="mailto:pholser@alumni.rice.edu">Paul Holser</a>
 * @version $Id: OptionException.java,v 1.21 2009/08/13 00:34:35 pholser Exp $
 */
public abstract class OptionException extends RuntimeException {
    private static final long serialVersionUID = -1L;

    private final List<String> options = new ArrayList<String>();

    protected OptionException( Collection<String> options ) {
        this.options.addAll( options );
    }

    protected OptionException( Collection<String> options, Throwable cause ) {
        super( cause );

        this.options.addAll( options );
    }

    /**
     * <p>Gives the option being considered when the exception was created.</p>
     *
     * @return the option being considered when the exception was created
     */
    public Collection<String> options() {
        return unmodifiableCollection( options );
    }

    protected final String singleOptionMessage() {
        return singleOptionMessage( options.get( 0 ) );
    }

    protected final String singleOptionMessage( String option ) {
        return SINGLE_QUOTE + option + SINGLE_QUOTE;
    }

    protected final String multipleOptionMessage() {
        StringBuilder buffer = new StringBuilder( "[" );

        for ( Iterator<String> iter = options.iterator(); iter.hasNext(); ) {
            buffer.append( singleOptionMessage( iter.next() ) );
            if ( iter.hasNext() )
                buffer.append( ", " );
        }

        buffer.append( ']' );

        return buffer.toString();
    }

    static OptionException illegalOptionCluster( String option ) {
        return new IllegalOptionClusterException( option );
    }

    static OptionException unrecognizedOption( String option ) {
        return new UnrecognizedOptionException( option );
    }
}
