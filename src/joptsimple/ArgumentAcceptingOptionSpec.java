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
import java.util.List;
import java.util.StringTokenizer;
import static java.util.Collections.*;

import joptsimple.internal.ReflectionException;
import static joptsimple.internal.Objects.*;
import static joptsimple.internal.Reflection.*;
import static joptsimple.internal.Strings.*;

/**
 * <p>Specification of an option that accepts an argument.</p>
 *
 * <p>Instances are returned from {@link OptionSpecBuilder} methods to allow the formation
 * of parser directives as sentences in a "fluent interface" language.  For example:</p>
 *
 * <pre>
 *   <code>
 *   OptionParser parser = new OptionParser();
 *   parser.accepts( "c" ).withRequiredArg().<strong>ofType( Integer.class )</strong>;
 *   </code>
 * </pre>
 *
 * <p>If no methods are invoked on an instance of this class, then that instance's option
 * will treat its argument as a {@link String}.</p>
 *
 * @param <V> represents the type of the arguments this option accepts
 * @author <a href="mailto:pholser@alumni.rice.edu">Paul Holser</a>
 * @version $Id: ArgumentAcceptingOptionSpec.java,v 1.43 2009/10/25 18:37:06 pholser Exp $
 */
public abstract class ArgumentAcceptingOptionSpec<V> extends AbstractOptionSpec<V> {
    private static final char NIL_VALUE_SEPARATOR = '\u0000';

    private final boolean argumentRequired;
    private ValueConverter<V> converter;
    private String argumentDescription = "";
    private String valueSeparator = String.valueOf( NIL_VALUE_SEPARATOR );
    private final List<V> defaultValues = new ArrayList<V>();

    ArgumentAcceptingOptionSpec( String option, boolean argumentRequired ) {
        super( option );

        this.argumentRequired = argumentRequired;
    }

    ArgumentAcceptingOptionSpec( Collection<String> options, boolean argumentRequired, String description ) {
        super( options, description );

        this.argumentRequired = argumentRequired;
    }

    /**
     * <p>Specifies a type to which arguments of this spec's option are to be
     * converted.</p>
     *
     * <p>JOpt Simple accepts types that have either:</p>
     *
     * <ol>
     *   <li>a public static method called {@code valueOf} which accepts a single
     *   argument of type {@link String} and whose return type is the same as the class
     *   on which the method is declared.  The {@code java.lang} primitive wrapper
     *   classes have such methods.</li>
     *
     *   <li>a public constructor which accepts a single argument of type
     *   {@link String}.</li>
     * </ol>
     *
     * <p>This class converts arguments using those methods in that order; that is,
     * {@code valueOf} would be invoked before a one-{@link String}-arg constructor
     * would.</p>
     *
     * <p>Invoking this method will trump any previous calls to this method or to
     * {@link #withValuesConvertedBy(ValueConverter)}.
     *
     * @param <T> represents the runtime class of the desired option argument type
     * @param argumentType desired type of arguments to this spec's option
     * @return self, so that the caller can add clauses to the fluent interface sentence
     * @throws NullPointerException if the type is {@code null}
     * @throws IllegalArgumentException if the type does not have the standard conversion
     * methods
     */
    public final <T> ArgumentAcceptingOptionSpec<T> ofType( Class<T> argumentType ) {
        return withValuesConvertedBy( findConverter( argumentType ) );
    }

    /**
     * <p>Specifies a converter to use to translate arguments of this spec's option into
     * Java objects.  This is useful when converting to types that do not have the
     * requisite factory method or constructor for {@link #ofType(Class)}.</p>
     *
     * <p>Invoking this method will trump any previous calls to this method or to
     * {@link #ofType(Class)}.
     *
     * @param <T> represents the runtime class of the desired option argument type
     * @param aConverter the converter to use
     * @return self, so that the caller can add clauses to the fluent interface sentence
     * @throws NullPointerException if the converter is {@code null}
     */
    @SuppressWarnings( "unchecked" )
    public final <T> ArgumentAcceptingOptionSpec<T> withValuesConvertedBy( ValueConverter<T> aConverter ) {
        if ( aConverter == null )
            throw new NullPointerException( "illegal null converter" );

        converter = (ValueConverter<V>) aConverter;
        return (ArgumentAcceptingOptionSpec<T>) this;
    }

    /**
     * <p>Specifies a description for the argument of the option that this spec
     * represents.  This description is used when generating help information about
     * the parser.</p>
     *
     * @param description describes the nature of the argument of this spec's
     * option
     * @return self, so that the caller can add clauses to the fluent interface sentence
     */
    public final ArgumentAcceptingOptionSpec<V> describedAs( String description ) {
        argumentDescription = description;
        return this;
    }

    /**
     * <p>Specifies a value separator for the argument of the option that this spec
     * represents.  This allows a single option argument to represent multiple values
     * for the option.  For example:</p>
     *
     * <pre>
     *   <code>
     *   parser.accepts( "z" ).withRequiredArg()
     *       .<strong>withValuesSeparatedBy( ',' )</strong>;
     *   OptionSet options = parser.parse( new String[] { "-z", "foo,bar,baz", "-z",
     *       "fizz", "-z", "buzz" } );
     *   </code>
     * </pre>
     *
     * <p>Then {@code options.valuesOf( "z" )} would yield the list {@code [foo, bar,
     * baz, fizz, buzz]}.</p>
     *
     * <p>You cannot use Unicode U+0000 as the separator.</p>
     *
     * @param separator a character separator
     * @return self, so that the caller can add clauses to the fluent interface sentence
     * @throws IllegalArgumentException if the separator is Unicode U+0000
     */
    public final ArgumentAcceptingOptionSpec<V> withValuesSeparatedBy( char separator ) {
        if ( separator == NIL_VALUE_SEPARATOR )
            throw new IllegalArgumentException( "cannot use U+0000 as separator" );

        valueSeparator = String.valueOf( separator );
        return this;
    }

    /**
     * <p>Specifies a set of default values for the argument of the option that this spec
     * represents.</p>
     *
     * @param value the first in the set of default argument values for this spec's option
     * @param values the (optional) remainder of the set of default argument values for this spec's option
     * @return self, so that the caller can add clauses to the fluent interface sentence
     * @throws NullPointerException if {@code value}, {@code values}, or any elements of {@code values} are
     * {@code null}
     */
    public ArgumentAcceptingOptionSpec<V> defaultsTo( V value, V... values ) {
        addDefaultValue( value );
        for ( V each : values )
            addDefaultValue( each );

        return this;
    }

    private void addDefaultValue( V value ) {
        ensureNotNull( value );
        defaultValues.add( value );
    }

    @Override
    final void handleOption( OptionParser parser, ArgumentList arguments, OptionSet detectedOptions,
        String detectedArgument ) {

        if ( isNullOrEmpty( detectedArgument ) )
            detectOptionArgument( parser, arguments, detectedOptions );
        else
            addArguments( detectedOptions, detectedArgument );
    }

    protected void addArguments( OptionSet detectedOptions, String detectedArgument ) {
        StringTokenizer lexer = new StringTokenizer( detectedArgument, valueSeparator );
        if ( !lexer.hasMoreTokens() )
            detectedOptions.addWithArgument( this, detectedArgument );
        else {
            while ( lexer.hasMoreTokens() )
                detectedOptions.addWithArgument( this, lexer.nextToken() );
        }
    }

    protected abstract void detectOptionArgument( OptionParser parser, ArgumentList arguments,
        OptionSet detectedOptions );

    @SuppressWarnings( "unchecked" )
    @Override
    protected final V convert( String argument ) {
        if ( converter == null )
            return (V) argument;

        try {
            return converter.convert( argument );
        }
        catch ( ReflectionException ex ) {
            throw new OptionArgumentConversionException( options(), argument, converter.valueType(), ex );
        }
        catch ( ValueConversionException ex ) {
            throw new OptionArgumentConversionException( options(), argument, converter.valueType(), ex );
        }
    }

    protected boolean canConvertArgument( String argument ) {
        StringTokenizer lexer = new StringTokenizer( argument, valueSeparator );

        try {
            while ( lexer.hasMoreTokens() )
                convert( lexer.nextToken() );
            return true;
        }
        catch ( OptionException ignored ) {
            return false;
        }
    }

    protected boolean isArgumentOfNumberType() {
        return converter != null && Number.class.isAssignableFrom( converter.valueType() );
    }

    @Override
    boolean acceptsArguments() {
        return true;
    }

    @Override
    boolean requiresArgument() {
        return argumentRequired;
    }

    String argumentDescription() {
        return argumentDescription;
    }

    String typeIndicator() {
        if ( converter == null )
            return null;

        String pattern = converter.valuePattern();
        return pattern == null ? converter.valueType().getName() : pattern;
    }

    @Override
    List<V> defaultValues() {
        return unmodifiableList( defaultValues );
    }

    @Override
    public boolean equals( Object that ) {
        if ( !super.equals( that ) )
            return false;

        ArgumentAcceptingOptionSpec<?> other = (ArgumentAcceptingOptionSpec<?>) that;
        return requiresArgument() == other.requiresArgument();
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ ( argumentRequired ? 0 : 1 );
    }
}
