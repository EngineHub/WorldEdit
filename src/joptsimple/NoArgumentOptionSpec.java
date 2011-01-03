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

import java.util.Collection;
import java.util.List;

import static java.util.Collections.*;

/**
 * <p>A specification for an option that does not accept arguments.</p>
 *
 * @author <a href="mailto:pholser@alumni.rice.edu">Paul Holser</a>
 * @version $Id: NoArgumentOptionSpec.java,v 1.16 2009/10/04 00:13:41 pholser Exp $
 */
class NoArgumentOptionSpec extends AbstractOptionSpec<Void> {
    NoArgumentOptionSpec( String option ) {
        this( singletonList( option ), "" );
    }

    NoArgumentOptionSpec( Collection<String> options, String description ) {
        super( options, description );
    }

    @Override
    void handleOption( OptionParser parser, ArgumentList arguments,
        OptionSet detectedOptions, String detectedArgument ) {

        detectedOptions.add( this );
    }

    @Override
    boolean acceptsArguments() {
        return false;
    }

    @Override
    boolean requiresArgument() {
        return false;
    }

    @Override
    void accept( OptionSpecVisitor visitor ) {
        visitor.visit( this );
    }

    @Override
    protected Void convert( String argument ) {
        return null;
    }

    @Override
    List<Void> defaultValues() {
        return emptyList();
    }
}
