/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.sk89q.worldedit.extension.factory.parser.pattern;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.factory.PatternFactory;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.*;
import com.sk89q.worldedit.world.World;
import org.enginehub.linbus.format.snbt.LinStringIO;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PartiallyApplyingPatternParserTest {

    WorldEdit worldEditMock;
    World worldMock;
    PatternFactory patternFactoryMock;
    ParserContext parserContext;

    PartiallyApplyingPatternParser partiallyApplyingPatternParser;

    @BeforeEach
    void setUp() throws InputParseException {
        worldEditMock = mock(WorldEdit.class);
        worldMock = mock(World.class);
        patternFactoryMock = mock(PatternFactory.class);
        partiallyApplyingPatternParser = new PartiallyApplyingPatternParser(worldEditMock);
        parserContext = new ParserContext();
        parserContext.setWorld(worldMock);
        when(worldEditMock.getPatternFactory())
                .thenReturn(patternFactoryMock);
        when(patternFactoryMock.parseFromInput(anyString(), eq(parserContext)))
                .thenAnswer((InvocationOnMock invocation) -> new Pattern() {
                    @Override
                    public String toString() {
                        return invocation.getArgument(0);
                    }
                }
                );
    }

    /**
     * Test ensuring various commands are parsed correctly.
     * this includes all possible permutations of pattern types and uses a nbt with nesting and an included list. 
     */
    @Test
    void parseFromInput() throws InputParseException {

        Pattern pattern = partiallyApplyingPatternParser.parseFromInput("^minecraft:bamboo_wall_sign", parserContext);
        verifyTypeApplyingPattern(pattern, "minecraft:bamboo_wall_sign");

        pattern = partiallyApplyingPatternParser.parseFromInput("^[facing=east,foo=bar]", parserContext);
        verifyStateApplyingPattern(pattern, Map.of("facing","east", "foo","bar"));

        pattern = partiallyApplyingPatternParser.parseFromInput("^{Items:[{count:64,Slot:0b,id:\"minecraft:dirt\"},{count:64,Slot:1b,id:\"minecraft:dirt\"}]}", parserContext);
        verifyNbtMergingPattern(pattern, "{Items:[{count:64,Slot:0b,id:\"minecraft:dirt\"},{count:64,Slot:1b,id:\"minecraft:dirt\"}]}");

        pattern = partiallyApplyingPatternParser.parseFromInput("^{=Items:[{count:64,Slot:0b,id:\"minecraft:dirt\"},{count:64,Slot:1b,id:\"minecraft:dirt\"}]}", parserContext);
        verifyNbtApplyingPattern(pattern, "{Items:[{count:64,Slot:0b,id:\"minecraft:dirt\"},{count:64,Slot:1b,id:\"minecraft:dirt\"}]}");


        pattern = partiallyApplyingPatternParser.parseFromInput("^barrel[facing=east]", parserContext);
        List<Pattern> patterns = verifyCombinedPatternWithSubpattern(pattern);
        verifyTypeApplyingPattern(patterns.getFirst(),"barrel");
        verifyStateApplyingPattern(patterns.get(1), Map.of("facing","east"));

        pattern = partiallyApplyingPatternParser.parseFromInput("^barrel{Items:[{count:64,Slot:0b,id:\"minecraft:dirt\"},{count:64,Slot:1b,id:\"minecraft:dirt\"}]}", parserContext);
        patterns = verifyCombinedPatternWithSubpattern(pattern);
        verifyNbtMergingPattern(patterns.getFirst(), "{Items:[{count:64,Slot:0b,id:\"minecraft:dirt\"},{count:64,Slot:1b,id:\"minecraft:dirt\"}]}");
        verifyTypeApplyingPattern(patterns.get(1),"barrel");

        pattern = partiallyApplyingPatternParser.parseFromInput("^barrel{=Items:[{count:64,Slot:0b,id:\"minecraft:dirt\"},{count:64,Slot:1b,id:\"minecraft:dirt\"}]}", parserContext);
        patterns = verifyCombinedPatternWithSubpattern(pattern);
        verifyNbtApplyingPattern(patterns.getFirst(), "{Items:[{count:64,Slot:0b,id:\"minecraft:dirt\"},{count:64,Slot:1b,id:\"minecraft:dirt\"}]}");
        verifyTypeApplyingPattern(patterns.get(1),"barrel");

        pattern = partiallyApplyingPatternParser.parseFromInput("^[facing=east,foo=bar]{Items:[{count:64,Slot:0b,id:\"minecraft:dirt\"},{count:64,Slot:1b,id:\"minecraft:dirt\"}]}", parserContext);
        patterns = verifyCombinedPatternWithSubpattern(pattern);
        verifyNbtMergingPattern(patterns.getFirst(), "{Items:[{count:64,Slot:0b,id:\"minecraft:dirt\"},{count:64,Slot:1b,id:\"minecraft:dirt\"}]}");
        verifyStateApplyingPattern(patterns.get(1), Map.of("facing","east", "foo","bar"));

        pattern = partiallyApplyingPatternParser.parseFromInput("^[facing=east,foo=bar]{=Items:[{count:64,Slot:0b,id:\"minecraft:dirt\"},{count:64,Slot:1b,id:\"minecraft:dirt\"}]}", parserContext);
        patterns = verifyCombinedPatternWithSubpattern(pattern);
        verifyNbtApplyingPattern(patterns.getFirst(), "{Items:[{count:64,Slot:0b,id:\"minecraft:dirt\"},{count:64,Slot:1b,id:\"minecraft:dirt\"}]}");
        verifyStateApplyingPattern(patterns.get(1), Map.of("facing","east", "foo","bar"));


        pattern = partiallyApplyingPatternParser.parseFromInput("^dirt[facing=east,foo=bar]{=Items:[{count:64,Slot:0b,id:\"minecraft:dirt\"},{count:64,Slot:1b,id:\"minecraft:dirt\"}]}", parserContext);
        patterns = verifyCombinedPatternWithSubpattern(pattern);
        verifyNbtApplyingPattern(patterns.getFirst(), "{Items:[{count:64,Slot:0b,id:\"minecraft:dirt\"},{count:64,Slot:1b,id:\"minecraft:dirt\"}]}");
        verifyTypeApplyingPattern(patterns.get(1),"dirt");
        verifyStateApplyingPattern(patterns.get(2), Map.of("facing","east", "foo","bar"));


        //patterns with interestiing/different nested values
        pattern = partiallyApplyingPatternParser.parseFromInput("^dirt,stone,cobblestone[facing=east,foo=bar]{=Items:[{count:64,Slot:0b,id:\"minecraft:dirt\"},{count:64,Slot:1b,id:\"minecraft:dirt\"}]}", parserContext);
        patterns = verifyCombinedPatternWithSubpattern(pattern);
        verifyNbtApplyingPattern(patterns.getFirst(), "{Items:[{count:64,Slot:0b,id:\"minecraft:dirt\"},{count:64,Slot:1b,id:\"minecraft:dirt\"}]}");
        verifyTypeApplyingPattern(patterns.get(1),"dirt,stone,cobblestone");
        verifyStateApplyingPattern(patterns.get(2), Map.of("facing","east", "foo","bar"));

        pattern = partiallyApplyingPatternParser.parseFromInput("^barrel{count:64,Slot:0b,id:\"minecraft:dirt\"}", parserContext);
        patterns = verifyCombinedPatternWithSubpattern(pattern);
        verifyNbtMergingPattern(patterns.getFirst(), "{count:64,Slot:0b,id:\"minecraft:dirt\"}");
        verifyTypeApplyingPattern(patterns.get(1),"barrel");

    }

    private List<Pattern> verifyCombinedPatternWithSubpattern(Pattern pattern) {
        assertInstanceOf(ExtentBufferedCompositePattern.class, pattern);
        return List.of(((ExtentBufferedCompositePattern) pattern).getPatterns());
    }

    private void verifyStateApplyingPattern(Pattern pattern, Map<String, String> stateToValidate) {
        assertInstanceOf(StateApplyingPattern.class, pattern);
        assertEquals(stateToValidate, ((StateApplyingPattern)pattern).getStates());
    }

    private void verifyTypeApplyingPattern(Pattern pattern, String typeToValidate) {
        assertInstanceOf(TypeApplyingPattern.class, pattern);
        assertEquals(typeToValidate,((TypeApplyingPattern)pattern).getTypeProvidingPattern().toString());
    }

    private void verifyNbtMergingPattern(Pattern pattern, String nbtToValidate) {
        assertInstanceOf(NBTMergingPattern.class, pattern);
        assertNbtMatches(nbtToValidate, ((NBTMergingPattern) pattern).getNbtToMerge());
    }

    private void verifyNbtApplyingPattern(Pattern pattern, String nbtToValidate) {
        assertInstanceOf(NBTApplyingPattern.class, pattern);
        assertNbtMatches(nbtToValidate, ((NBTApplyingPattern) pattern).getNbtToApply());
    }

    private void assertNbtMatches(String nbtToValidate, LinCompoundTag nbtToApply) {
        LinCompoundTag readCompoundTag = LinStringIO.readFromStringUsing(nbtToValidate, LinCompoundTag::readFrom);
        assertEquals(readCompoundTag, nbtToApply);
    }

    private void assertNbtMatches(String nbtToValidate, Map<String, ? extends LinTag<?>> nbtToMerge) {
        LinCompoundTag readCompoundTag = LinStringIO.readFromStringUsing(nbtToValidate, LinCompoundTag::readFrom);
        assertEquals(readCompoundTag, LinCompoundTag.of(nbtToMerge));
    }
}