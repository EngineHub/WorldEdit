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

import java.text.BreakIterator;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import static java.lang.System.*;
import static java.text.BreakIterator.*;

import static joptsimple.internal.Strings.*;

/**
 * @author <a href="mailto:pholser@alumni.rice.edu">Paul Holser</a>
 * @version $Id: Column.java,v 1.16 2009/10/25 18:37:08 pholser Exp $
 */
public class Column {
    static final Comparator<Column> BY_HEIGHT = new Comparator<Column>() {
        public int compare( Column first, Column second ) {
            if ( first.height() < second.height() )
                return -1;
            return first.height() == second.height() ? 0 : 1;
        }
    };

    private final String header;
    private final List<String> data;
    private final int width;
    private int height;

    Column( String header, int width ) {
        this.header = header;
        this.width = Math.max( width, header.length() );
        data = new LinkedList<String>();
        height = 0;
    }

    int addCells( Object cellCandidate ) {
        int originalHeight = height;

        String source = String.valueOf( cellCandidate ).trim();
        for ( String eachPiece : source.split( getProperty( "line.separator" ) ) )
            processNextEmbeddedLine( eachPiece );

        return height - originalHeight;
    }

    private void processNextEmbeddedLine( String line ) {
        BreakIterator words = BreakIterator.getLineInstance( Locale.US );
        words.setText( line );

        StringBuilder nextCell = new StringBuilder();

        int start = words.first();
        for ( int end = words.next(); end != DONE; start = end, end = words.next() )
            nextCell = processNextWord( line, nextCell, start, end );

        if ( nextCell.length() > 0 )
            addCell( nextCell.toString() );
    }

    private StringBuilder processNextWord( String source, StringBuilder nextCell, int start, int end ) {
        StringBuilder augmented = nextCell;

        String word = source.substring( start, end );
        if ( augmented.length() + word.length() > width ) {
            addCell( augmented.toString() );
            augmented = new StringBuilder( "  " ).append( word );
        }
        else
            augmented.append( word );

        return augmented;
    }

    void addCell( String newCell ) {
        data.add( newCell );
        ++height;
    }

    void writeHeaderOn( StringBuilder buffer, boolean appendSpace ) {
        buffer.append( header ).append( repeat( ' ', width - header.length() ) );

        if ( appendSpace )
            buffer.append( ' ' );
    }

    void writeSeparatorOn( StringBuilder buffer, boolean appendSpace ) {
        buffer.append( repeat( '-', header.length() ) ).append( repeat( ' ', width - header.length() ) );
        if ( appendSpace )
            buffer.append( ' ' );
    }

    void writeCellOn( int index, StringBuilder buffer, boolean appendSpace ) {
        if ( index < data.size() ) {
            String item = data.get( index );

            buffer.append( item ).append( repeat( ' ', width - item.length() ) );
            if ( appendSpace )
                buffer.append( ' ' );
        }
    }

    int height() {
        return height;
    }
}
