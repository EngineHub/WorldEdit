package com.sk89q.worldedit.command.functions;

import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.function.operation.OperationFuture;
import com.sk89q.worldedit.function.util.WEConsumer;

/**
 * Notifies players when an operation fails.
 */
public class FailureNotifier implements WEConsumer<OperationFuture> {
    private final Player player;

    public FailureNotifier(Player player) {
        this.player = player;
    }

    @Override
    public void accept(OperationFuture operationFuture) {
        if (operationFuture.isCancelled()) {
            player.printError("Command was cancelled.");
        } else {
            Throwable t = operationFuture.getThrown();
            String message = t.getMessage();
            if (message != null && !message.equals("")) {
                player.printError(message);
            } else {
                player.printError("An error occured: " + t.getClass().getName());
            }
        }
    }
}
