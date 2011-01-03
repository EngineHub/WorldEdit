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

import static joptsimple.ParserRules.*;

/**
 * <p>Abstraction of parser state; mostly serves to model how a parser behaves depending
 * on whether end-of-options has been detected.</p>
 *
 * @author <a href="mailto:pholser@alumni.rice.edu">Paul Holser</a>
 * @version $Id: OptionParserState.java,v 1.8 2009/09/28 01:12:48 pholser Exp $
 */
abstract class OptionParserState {
    static OptionParserState noMoreOptions() {
        return new OptionParserState() {
            @Override
            protected void handleArgument( OptionParser parser, ArgumentList arguments, OptionSet detectedOptions ) {
                detectedOptions.addNonOptionArgument( arguments.next() );
            }
        };
    }

    static OptionParserState moreOptions( final boolean posixlyCorrect ) {
        return new OptionParserState() {
            @Override
            protected void handleArgument( OptionParser parser, ArgumentList arguments, OptionSet detectedOptions ) {
                String candidate = arguments.next();
                if ( isOptionTerminator( candidate ) )
                    parser.noMoreOptions();
                else if ( isLongOptionToken( candidate ) )
                    parser.handleLongOptionToken( candidate, arguments, detectedOptions );
                else if ( isShortOptionToken( candidate ) )
                    parser.handleShortOptionToken( candidate, arguments, detectedOptions );
                else {
                    if ( posixlyCorrect )
                        parser.noMoreOptions();

                    detectedOptions.addNonOptionArgument( candidate );
                }
            }
        };
    }

    protected abstract void handleArgument( OptionParser parser, ArgumentList arguments, OptionSet detectedOptions );
}
