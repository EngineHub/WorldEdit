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

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import joptsimple.internal.ColumnarData;
import static joptsimple.ParserRules.*;
import static joptsimple.internal.Classes.*;
import static joptsimple.internal.Strings.*;

/**
 * <p>Produces text for a help screen given a set of options.</p>
 *
 * @author <a href="mailto:pholser@alumni.rice.edu">Paul Holser</a>
 * @version $Id: HelpFormatter.java,v 1.19 2009/10/04 00:13:41 pholser Exp $
 */
class HelpFormatter implements OptionSpecVisitor {
    private final ColumnarData grid;

    HelpFormatter() {
        grid = new ColumnarData( "Option", "Description" );
    }

    String format( Map<String, AbstractOptionSpec<?>> options ) {
        if ( options.isEmpty() )
            return "No options specified";

        grid.clear();

        Comparator<AbstractOptionSpec<?>> comparator =
            new Comparator<AbstractOptionSpec<?>>() {
                public int compare( AbstractOptionSpec<?> first, AbstractOptionSpec<?> second ) {
                    return first.options().iterator().next().compareTo( second.options().iterator().next() );
                }
            };

        Set<AbstractOptionSpec<?>> sorted = new TreeSet<AbstractOptionSpec<?>>( comparator );
        sorted.addAll( options.values() );

        for ( AbstractOptionSpec<?> each : sorted )
            each.accept( this );

        return grid.format();
    }

    void addHelpLineFor( AbstractOptionSpec<?> spec, String additionalInfo ) {
        grid.addRow( createOptionDisplay( spec ) + additionalInfo, createDescriptionDisplay( spec ) );
    }

    public void visit( NoArgumentOptionSpec spec ) {
        addHelpLineFor( spec, "" );
    }

    public void visit( RequiredArgumentOptionSpec<?> spec ) {
        visit( spec, '<', '>' );
    }

    public void visit( OptionalArgumentOptionSpec<?> spec ) {
        visit( spec, '[', ']' );
    }

    public void visit( AlternativeLongOptionSpec spec ) {
        addHelpLineFor( spec, ' ' + surround( spec.argumentDescription(), '<', '>' ) );
    }

    private void visit( ArgumentAcceptingOptionSpec<?> spec, char begin, char end ) {
        String argDescription = spec.argumentDescription();
        String typeIndicator = typeIndicator( spec );
        StringBuilder collector = new StringBuilder();

        if ( typeIndicator.length() > 0 ) {
            collector.append( typeIndicator );

            if ( argDescription.length() > 0 )
                collector.append( ": " ).append( argDescription );
        }
        else if ( argDescription.length() > 0 )
            collector.append( argDescription );

        String helpLine = collector.length() == 0
            ? ""
            : ' ' + surround( collector.toString(), begin, end );
        addHelpLineFor( spec, helpLine );
    }

    private String createOptionDisplay( AbstractOptionSpec<?> spec ) {
        StringBuilder buffer = new StringBuilder();

        for ( Iterator<String> iter = spec.options().iterator(); iter.hasNext(); ) {
            String option = iter.next();
            buffer.append( option.length() > 1 ? DOUBLE_HYPHEN : HYPHEN );
            buffer.append( option );

            if ( iter.hasNext() )
                buffer.append( ", " );
        }

        return buffer.toString();
    }

    private String createDescriptionDisplay( AbstractOptionSpec<?> spec ) {
        List<?> defaultValues = spec.defaultValues();
        if ( defaultValues.isEmpty() )
            return spec.description();

        String defaultValuesDisplay = createDefaultValuesDisplay( defaultValues );
        return spec.description() + ' ' + surround( "default: " + defaultValuesDisplay, '(', ')' );
    }

    private String createDefaultValuesDisplay( List<?> defaultValues ) {
        return defaultValues.size() == 1 ? defaultValues.get( 0 ).toString() : defaultValues.toString();
    }

    private static String typeIndicator( ArgumentAcceptingOptionSpec<?> spec ) {
        String indicator = spec.typeIndicator();
        return indicator == null || String.class.getName().equals( indicator )
            ? ""
            : shortNameOf( indicator );
    }
}
