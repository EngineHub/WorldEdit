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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.*;

import joptsimple.internal.AbbreviationMap;
import joptsimple.util.KeyValuePair;
import static joptsimple.OptionException.*;
import static joptsimple.OptionParserState.*;
import static joptsimple.ParserRules.*;

/**
 * <p>Parses command line arguments, using a syntax that attempts to take from the best
 * of POSIX {@code getopt()} and GNU {@code getopt_long()}.</p>
 *
 * <p>This parser supports short options and long options.</p>
 *
 * <ul>
 *   <li><dfn>Short options</dfn> begin with a single hyphen ("<kbd>-</kbd>") followed
 *   by a single letter or digit, or question mark ("<kbd>?</kbd>"), or dot
 *   ("<kbd>.</kbd>").</li>
 *
 *   <li>Short options can accept single arguments.  The argument can be made required or
 *   optional.  The option's argument can occur:
 *     <ul>
 *       <li>in the slot after the option, as in <kbd>-d /tmp</kbd></li>
 *       <li>right up against the option, as in <kbd>-d/tmp</kbd></li>
 *       <li>right up against the option separated by an equals sign (<kbd>"="</kbd>),
 *       as in <kbd>-d=/tmp</kbd></li>
 *     </ul>
 *   To specify <var>n</var> arguments for an option, specify the option <var>n</var>
 *   times, once for each argument, as in <kbd>-d /tmp -d /var -d /opt</kbd>; or, when
 *   using the {@linkplain ArgumentAcceptingOptionSpec#withValuesSeparatedBy(char)
 *   "separated values"} clause of the "fluent interface" (see below), give multiple
 *   values separated by a given character as a single argument to the option.</li>
 *
 *   <li>Short options can be clustered, so that <kbd>-abc</kbd> is treated as
 *   <kbd>-a -b -c</kbd>, if none of those options can accept arguments.</li>
 *
 *   <li>An argument consisting only of two hyphens (<kbd>"--"</kbd>) signals that the
 *   remaining arguments are to be treated as non-options.</li>
 *
 *   <li>An argument consisting only of a single hyphen is considered a non-option
 *   argument (though it can be an argument of an option).  Many Unix programs treat
 *   single hyphens as stand-ins for the standard input or standard output streams.</li>
 *
 *   <li><dfn>Long options</dfn> begin with two hyphens (<kbd>"--"</kbd>), followed
 *   by multiple letters, digits, hyphens, question marks, or dots.  A hyphen cannot be
 *   the first character of a long option specification when configuring the parser.</li>
 *
 *   <li>You can abbreviate long options, so long as the abbreviation is unique.</li>
 *
 *   <li>Long options can accept single arguments.  The argument can be made required or
 *   optional.  The option's argument can occur:
 *     <ul>
 *       <li>in the slot after the option, as in <kbd>--directory /tmp</kbd></li>
 *       <li>right up against the option separated by an equals sign (<kbd>"="</kbd>),
 *       as in <kbd>--directory=/tmp</kbd>
 *     </ul>
 *   Specify multiple arguments for a long option in the same manner as for short options
 *   (see above).</li>
 *
 *   <li>You can use a single hyphen (<kbd>"-"</kbd>) instead of a double hyphen
 *   (<kbd>"--"</kbd>) for a long option.</li>
 *
 *   <li>The option <kbd>-W</kbd> is reserved.  If you tell the parser to {@linkplain
 *   #recognizeAlternativeLongOptions(boolean) recognize alternative long options}, then
 *   it will treat, for example, <kbd>-W foo=bar</kbd> as the long option
 *   <kbd>foo</kbd> with argument <kbd>bar</kbd>, as though you had written
 *   <kbd>--foo=bar</kbd>.</li>
 *
 *   <li>You can specify <kbd>-W</kbd> as a valid short option, or use it as an
 *   abbreviation for a long option, but {@linkplain
 *   #recognizeAlternativeLongOptions(boolean) recognizing alternative long options} will
 *   always supersede this behavior.</li>
 *
 *   <li>You can specify a given short or long option multiple times on a single command
 *   line.  The parser collects any arguments specified for those options as a list.</li>
 *
 *   <li>If the parser detects an option whose argument is optional, and the next argument
 *   "looks like" an option, that argument is not treated as the argument to the option,
 *   but as a potentially valid option.  If, on the other hand, the optional argument is
 *   typed as a derivative of {@link Number}, then that argument is treated as the
 *   negative number argument of the option, even if the parser recognizes the
 *   corresponding numeric option.  For example:
 *   <pre><code>
 *     OptionParser parser = new OptionParser();
 *     parser.accepts( "a" ).withOptionalArg().ofType( Integer.class );
 *     parser.accepts( "2" );
 *     OptionSet options = parser.parse( "-a", "-2" );
 *   </code></pre>
 *   In this case, the option set contains <kbd>"a"</kbd> with argument <kbd>-2</kbd>,
 *   not both <kbd>"a"</kbd> and <kbd>"2"</kbd>.  Swapping the elements in the
 *   <var>args</var> array gives the latter.</li>
 * </ul>
 *
 * <p>There are two ways to tell the parser what options to recognize:</p>
 *
 * <ol>
 *   <li>A "fluent interface"-style API for specifying options, available since
 *   version 2.  Sentences in this fluent interface language begin with a call to
 *   {@link #accepts(String) accepts} or {@link #acceptsAll(Collection) acceptsAll}
 *   methods; calls on the ensuing chain of objects describe whether the options can take
 *   an argument, whether the argument is required or optional, to what type arguments of
 *   the options should be converted if any, etc.  Since version 3, these calls return
 *   an instance of {@link OptionSpec}, which can subsequently be used to retrieve the
 *   arguments of the associated option in a type-safe manner.</li>
 *
 *   <li>Since version 1, a more concise way of specifying short options has been to use
 *   the special {@linkplain #OptionParser(String) constructor}.  Arguments of options
 *   specified in this manner will be of type {@link String}.  Here are the rules for the
 *   format of the specification strings this constructor accepts:
 *
 *     <ul>
 *       <li>Any letter or digit is treated as an option character.</li>
 *
 *       <li>If an option character is followed by a single colon (<kbd>":"</kbd>),
 *       then the option requires an argument.</li>
 *
 *       <li>If an option character is followed by two colons (<kbd>"::"</kbd>), then
 *       the option accepts an optional argument.</li>
 *
 *       <li>Otherwise, the option character accepts no argument.</li>
 *
 *       <li>If the option specification string begins with a plus sign (<kbd>"+"</kbd>),
 *       the parser will behave "POSIX-ly correct".</li>
 *
 *       <li>If the option specification string contains the sequence <kbd>"W;"</kbd>
 *       (capital W followed by a semicolon), the parser will recognize the alternative
 *       form of long options.</li>
 *     </ul>
 *   </li>
 * </ol>
 *
 * <p>Each of the options in a list of options given to {@link #acceptsAll(Collection)
 * acceptsAll} is treated as a synonym of the others.  For example:
 *   <pre>
 *     <code>
 *     OptionParser parser = new OptionParser();
 *     parser.acceptsAll( asList( "w", "interactive", "confirmation" ) );
 *     OptionSet options = parser.parse( "-w" );
 *     </code>
 *   </pre>
 * In this case, <code>options.{@link OptionSet#has(String) has}</code> would answer
 * {@code true} when given arguments <kbd>"w"</kbd>, <kbd>"interactive"</kbd>, and
 * <kbd>"confirmation"</kbd>. The {@link OptionSet} would give the same responses to
 * these arguments for its other methods as well.</p>
 *
 * <p>By default, as with GNU {@code getopt()}, the parser allows intermixing of options
 * and non-options.  If, however, the parser has been created to be "POSIX-ly correct",
 * then the first argument that does not look lexically like an option, and is not a
 * required argument of a preceding option, signals the end of options.  You can still
 * bind optional arguments to their options using the abutting (for short options) or
 * <kbd>=</kbd> syntax.</p>
 *
 * <p>Unlike GNU {@code getopt()}, this parser does not honor the environment variable
 * {@code POSIXLY_CORRECT}.  "POSIX-ly correct" parsers are configured by either:</p>
 *
 * <ol>
 *   <li>using the method {@link #posixlyCorrect(boolean)}, or</li>
 *
 *   <li>using the {@linkplain #OptionParser(String) constructor} with an argument whose
 *   first character is a plus sign (<kbd>"+"</kbd>)</li>
 * </ol>
 *
 * @author <a href="mailto:pholser@alumni.rice.edu">Paul Holser</a>
 * @version $Id: OptionParser.java,v 1.38 2009/10/25 18:37:06 pholser Exp $
 * @see <a href="http://www.gnu.org/software/libc/manual">The GNU C Library </a>
 */
public class OptionParser {
    private final AbbreviationMap<AbstractOptionSpec<?>> recognizedOptions;
    private OptionParserState state;
    private boolean posixlyCorrect;

    /**
     * <p>Creates an option parser that initially recognizes no options, and does not
     * exhibit "POSIX-ly correct" behavior.</p>
     */
    public OptionParser() {
        recognizedOptions = new AbbreviationMap<AbstractOptionSpec<?>>();
        state = moreOptions( false );
    }

    /**
     * <p>Creates an option parser and configures it to recognize the short options
     * specified in the given string.</p>
     *
     * <p>Arguments of options specified this way will be of type {@link String}.</p>
     *
     * @param optionSpecification an option specification
     * @throws NullPointerException if <var>optionSpecification</var> is
     * {@code null}
     * @throws OptionException if the option specification contains illegal characters
     * or otherwise cannot be recognized
     */
    public OptionParser( String optionSpecification ) {
        this();

        new OptionSpecTokenizer( optionSpecification ).configure( this );
    }

    /**
     * <p>Tells the parser to recognize the given option.</p>
     *
     * <p>This method returns an instance of {@link OptionSpecBuilder} to allow the
     * formation of parser directives as sentences in a fluent interface language.
     * For example:</p>
     *
     * <pre><code>
     *   OptionParser parser = new OptionParser();
     *   parser.<strong>accepts( "c" )</strong>.withRequiredArg().ofType( Integer.class );
     * </code></pre>
     *
     * <p>If no methods are invoked on the returned {@link OptionSpecBuilder}, then the
     * parser treats the option as accepting no argument.</p>
     *
     * @param option the option to recognize
     * @return an object that can be used to flesh out more detail about the option
     * @throws OptionException if the option contains illegal characters
     * @throws NullPointerException if the option is {@code null}
     */
    public OptionSpecBuilder accepts( String option ) {
        return acceptsAll( singletonList( option ) );
    }

    /**
     * <p>Tells the parser to recognize the given option.</p>
     *
     * @see #accepts(String)
     * @param option the option to recognize
     * @param description a string that describes the purpose of the option.  This is
     * used when generating help information about the parser.
     * @return an object that can be used to flesh out more detail about the option
     * @throws OptionException if the option contains illegal characters
     * @throws NullPointerException if the option is {@code null}
     */
    public OptionSpecBuilder accepts( String option, String description ) {
        return acceptsAll( singletonList( option ), description );
    }

    /**
     * <p>Tells the parser to recognize the given options, and treat them as
     * synonymous.</p>
     *
     * @see #accepts(String)
     * @param options the options to recognize and treat as synonymous
     * @return an object that can be used to flesh out more detail about the options
     * @throws OptionException if any of the options contain illegal characters
     * @throws NullPointerException if the option list or any of its elements are
     * {@code null}
     */
    public OptionSpecBuilder acceptsAll( Collection<String> options ) {
        return acceptsAll( options, "" );
    }

    /**
     * <p>Tells the parser to recognize the given options, and treat them as
     * synonymous.</p>
     *
     * @see #acceptsAll(Collection)
     * @param options the options to recognize and treat as synonymous
     * @param description a string that describes the purpose of the option.  This is
     * used when generating help information about the parser.
     * @return an object that can be used to flesh out more detail about the options
     * @throws OptionException if any of the options contain illegal characters
     * @throws NullPointerException if the option list or any of its elements are
     * {@code null}
     * @throws IllegalArgumentException if the option list is empty
     */
    public OptionSpecBuilder acceptsAll( Collection<String> options,
        String description ) {

        if ( options.isEmpty() )
            throw new IllegalArgumentException( "need at least one option" );

        ensureLegalOptions( options );

        return new OptionSpecBuilder( this, options, description );
    }

    /**
     * <p>Tells the parser whether or not to behave "POSIX-ly correct"-ly.</p>
     *
     * @param setting {@code true} if the parser should behave "POSIX-ly correct"-ly
     */
    public void posixlyCorrect( boolean setting ) {
        posixlyCorrect = setting;
        state = moreOptions( setting );
    }

    boolean posixlyCorrect() {
        return posixlyCorrect;
    }

    /**
     * <p>Tells the parser either to recognize or ignore <kbd>"-W"</kbd>-style long
     * options.</p>
     *
     * @param recognize {@code true} if the parser is to recognize the special style
     * of long options
     */
    public void recognizeAlternativeLongOptions( boolean recognize ) {
        if ( recognize )
            recognize( new AlternativeLongOptionSpec() );
        else
            recognizedOptions.remove( String.valueOf( RESERVED_FOR_EXTENSIONS ) );
    }

    void recognize( AbstractOptionSpec<?> spec ) {
        recognizedOptions.putAll( spec.options(), spec );
    }

    /**
     * <p>Writes information about the options this parser recognizes to the given output
     * sink.</p>
     *
     * <p>The output sink is flushed, but not closed.</p>
     *
     * @param sink the sink to write information to
     * @throws IOException if there is a problem writing to the sink
     * @throws NullPointerException if <var>sink</var> is {@code null}
     * @see #printHelpOn(Writer)
     */
    public void printHelpOn( OutputStream sink ) throws IOException {
        printHelpOn( new OutputStreamWriter( sink ) );
    }

    /**
     * <p>Writes information about the options this parser recognizes to the given output
     * sink.</p>
     *
     * <p>The output sink is flushed, but not closed.</p>
     *
     * @param sink the sink to write information to
     * @throws IOException if there is a problem writing to the sink
     * @throws NullPointerException if <var>sink</var> is {@code null}
     * @see #printHelpOn(OutputStream)
     */
    public void printHelpOn( Writer sink ) throws IOException {
        sink.write( new HelpFormatter().format( recognizedOptions.toJavaUtilMap() ) );
        sink.flush();
    }

    /**
     * <p>Parses the given command line arguments according to the option specifications
     * given to the parser.</p>
     *
     * @param arguments arguments to parse
     * @return an {@link OptionSet} describing the parsed options, their arguments, and
     * any non-option arguments found
     * @throws OptionException if problems are detected while parsing
     * @throws NullPointerException if the argument list is {@code null}
     */
    public OptionSet parse( String... arguments ) {
        ArgumentList argumentList = new ArgumentList( arguments );
        OptionSet detected = new OptionSet( defaultValues() );

        while ( argumentList.hasMore() )
            state.handleArgument( this, argumentList, detected );

        reset();
        return detected;
    }

    void handleLongOptionToken( String candidate, ArgumentList arguments, OptionSet detected ) {
        KeyValuePair optionAndArgument = parseLongOptionWithArgument( candidate );

        if ( !isRecognized( optionAndArgument.key ) )
            throw unrecognizedOption( optionAndArgument.key );

        AbstractOptionSpec<?> optionSpec = specFor( optionAndArgument.key );
        optionSpec.handleOption( this, arguments, detected, optionAndArgument.value );
    }

    void handleShortOptionToken( String candidate, ArgumentList arguments, OptionSet detected ) {
        KeyValuePair optionAndArgument = parseShortOptionWithArgument( candidate );

        if ( isRecognized( optionAndArgument.key ) ) {
            specFor( optionAndArgument.key ).handleOption( this, arguments, detected, optionAndArgument.value );
        }
        else
            handleShortOptionCluster( candidate, arguments, detected );
    }

    private void handleShortOptionCluster( String candidate, ArgumentList arguments, OptionSet detected ) {
        char[] options = extractShortOptionsFrom( candidate );
        validateOptionCharacters( options );

        AbstractOptionSpec<?> optionSpec = specFor( options[ 0 ] );

        if ( optionSpec.acceptsArguments() && options.length > 1 ) {
            String detectedArgument = String.valueOf( options, 1, options.length - 1 );
            optionSpec.handleOption( this, arguments, detected, detectedArgument );
        }
        else {
            for ( char each : options )
                specFor( each ).handleOption( this, arguments, detected, null );
        }
    }

    void noMoreOptions() {
        state = OptionParserState.noMoreOptions();
    }

    boolean looksLikeAnOption( String argument ) {
        return isShortOptionToken( argument ) || isLongOptionToken( argument );
    }

    private boolean isRecognized( String option ) {
        return recognizedOptions.contains( option );
    }

    private AbstractOptionSpec<?> specFor( char option ) {
        return specFor( String.valueOf( option ) );
    }

    private AbstractOptionSpec<?> specFor( String option ) {
        return recognizedOptions.get( option );
    }

    private void reset() {
        state = moreOptions( posixlyCorrect );
    }

    private static char[] extractShortOptionsFrom( String argument ) {
        char[] options = new char[ argument.length() - 1 ];
        argument.getChars( 1, argument.length(), options, 0 );

        return options;
    }

    private void validateOptionCharacters( char[] options ) {
        for ( int i = 0; i < options.length; ++i ) {
            String option = String.valueOf( options[ i ] );

            if ( !isRecognized( option ) )
                throw unrecognizedOption( option );

            if ( specFor( option ).acceptsArguments() ) {
                if ( i > 0 )
                    throw illegalOptionCluster( option );

                // remainder of chars are the argument to the option at char 0
                return;
            }
        }
    }

    private static KeyValuePair parseLongOptionWithArgument( String argument ) {
        return KeyValuePair.valueOf( argument.substring( 2 ) );
    }

    private static KeyValuePair parseShortOptionWithArgument( String argument ) {
        return KeyValuePair.valueOf( argument.substring( 1 ) );
    }

    private Map<String, List<?>> defaultValues() {
        Map<String, List<?>> defaults = new HashMap<String, List<?>>();
        for ( Map.Entry<String, AbstractOptionSpec<?>> each : recognizedOptions.toJavaUtilMap().entrySet() )
            defaults.put( each.getKey(), each.getValue().defaultValues() );
        return defaults;
    }
}
