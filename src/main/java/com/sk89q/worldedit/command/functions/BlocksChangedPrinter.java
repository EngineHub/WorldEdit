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

import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.function.operation.AffectedCounter;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.OperationFuture;
import com.sk89q.worldedit.function.util.WEConsumer;

import java.util.concurrent.ExecutionException;

/**
 * An OperationFuture consumer that prints the number of changed blocks.
 */
public class BlocksChangedPrinter implements WEConsumer<OperationFuture> {
    private Player player;

    public BlocksChangedPrinter(Player player) {
        this.player = player;
    }

    @Override
    public void accept(OperationFuture operationFuture) {
        try {
            Operation op = operationFuture.getOriginalOperation();
            if (op instanceof AffectedCounter) {
                printChanged((AffectedCounter) op);
                return;
            }
            op = operationFuture.get();
            if (op instanceof AffectedCounter) {
                printChanged((AffectedCounter) op);
                return;
            }
            System.err.println("[WorldEdit] BlocksChangedPrinter used for an operation that doesn't count blocks!");
        } catch (InterruptedException impossible) {
            impossible.printStackTrace();
        } catch (ExecutionException impossible) {
            impossible.printStackTrace();
        }
    }

    private void printChanged(AffectedCounter op) {
        int count = op.getAffected();
        if (count == 0) {
            player.print("No blocks were changed.");
        } else if (count == 1) {
            player.print("1 block was changed.");
        } else {
            player.print(count + " blocks have been changed.");
        }
    }
}
