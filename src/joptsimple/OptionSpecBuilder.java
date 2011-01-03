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

/**
 * <p>Allows callers to specify whether a given option accepts arguments (required or
 * optional).</p>
 *
 * <p>Instances are returned from {@link OptionParser#accepts(String)} to allow the
 * formation of parser directives as sentences in a "fluent interface" language.  For
 * example:</p>
 *
 * <pre><code>
 *   OptionParser parser = new OptionParser();
 *   parser.accepts( "c" ).<strong>withRequiredArg()</strong>.ofType( Integer.class );
 * </code></pre>
 *
 * <p>If no methods are invoked on an instance of this class, then that instance's option
 * will accept no argument.</p>
 *
 * <p>Note that you should not use the fluent interface clauses in a way that would
 * defeat the typing of option arguments:</p>
 *
 * <pre><code>
 *   OptionParser parser = new OptionParser();
 *   ArgumentAcceptingOptionSpec&lt;String&gt; optionC =
 *       parser.accepts( "c" ).withRequiredArg();
 *   <strong>optionC.ofType( Integer.class );  // DON'T THROW AWAY THE TYPE!</strong>
 *
 *   String value = parser.parse( "-c", "2" ).valueOf( optionC );  // ClassCastException
 * </code></pre>
 *
 * @author <a href="mailto:pholser@alumni.rice.edu">Paul Holser</a>
 * @version $Id: OptionSpecBuilder.java,v 1.19 2009/10/25 18:37:06 pholser Exp $
 */
public class OptionSpecBuilder extends NoArgumentOptionSpec {
    private final OptionParser parser;

    OptionSpecBuilder( OptionParser parser, Collection<String> options, String description ) {
        super( options, description );

        this.parser = parser;
        attachToParser();
    }

    private void attachToParser() {
        parser.recognize( this );
    }

    /**
     * <p>Informs an option parser that this builder's option requires an argument.</p>
     *
     * @return a specification for the option
     */
    public ArgumentAcceptingOptionSpec<String> withRequiredArg() {
        ArgumentAcceptingOptionSpec<String> newSpec =
            new RequiredArgumentOptionSpec<String>( options(), description() );
        parser.recognize( newSpec );

        return newSpec;
    }

    /**
     * <p>Informs an option parser that this builder's option accepts an optional
     * argument.</p>
     *
     * @return a specification for the option
     */
    public ArgumentAcceptingOptionSpec<String> withOptionalArg() {
        ArgumentAcceptingOptionSpec<String> newSpec =
            new OptionalArgumentOptionSpec<String>( options(), description() );
        parser.recognize( newSpec );

        return newSpec;
    }
}
