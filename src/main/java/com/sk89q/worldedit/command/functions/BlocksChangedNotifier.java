/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.command.functions;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.function.operation.AffectedCounter;
import com.sk89q.worldedit.function.operation.Operation;

/**
 * Prints the number of changed blocks from an operation.
 */
public class BlocksChangedNotifier extends AbstractNotifier {
    private String extraMessage;
    private boolean messageAfter;

    public BlocksChangedNotifier(Player player) {
        super(player);
    }

    public BlocksChangedNotifier(Player player, String message, boolean messageAfter) {
        super(player);
        this.extraMessage = message;
        this.messageAfter = messageAfter;
    }

    @Override
    public void onSuccess(Operation result) {
        if (result instanceof AffectedCounter) {
            printChanged((AffectedCounter) result);
            return;
        }

        WorldEdit.logger.warning("BlocksChangedPrinter used for an operation that doesn't count blocks!");
        WorldEdit.logger.warning("For operation: " + result.getClass().getName());

        // Print a backup message
        player.print("Command complete.");
    }

    private void printChanged(AffectedCounter op) {
        if (extraMessage != null) {
            if (messageAfter) {
                player.print(getChangedString(op.getAffected()) + " " + extraMessage);
            } else {
                player.print(extraMessage + " (" + getChangedString(op.getAffected()) + ")");
            }
        } else {
            player.print(getChangedString(op.getAffected()));
        }
    }

    private String getChangedString(int count) {
        if (count == 0) {
            return "No blocks were changed.";
        } else if (count == 1) {
            return "1 block was changed.";
        } else {
            return count + " blocks have been changed.";
        }
    }
}
