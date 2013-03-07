
package org.enginehub.worldedit;


/**
 * Thrown when a WorldEdit operation fails during runtime due to some problem, such
 * as {@link MaxChangedBlocksException}.
 * 
 * <p>Warning: This exception is unchecked!</p>
 */
public class OperationException extends RuntimeException {

    private static final long serialVersionUID = 7790664066180359524L;

    public OperationException() {
    }

    public OperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public OperationException(String message) {
        super(message);
    }

    public OperationException(Throwable cause) {
        super(cause);
    }

}
