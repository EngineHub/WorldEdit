package com.sk89q.rebar.command.parametric;

/**
 * Thrown if the {@link ParametricBuilder} can't build commands from
 * an object for whatever reason.
 */
public class ParametricException extends RuntimeException {

    private static final long serialVersionUID = -5426219576099680971L;

    public ParametricException() {
        super();
    }

    public ParametricException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParametricException(String message) {
        super(message);
    }

    public ParametricException(Throwable cause) {
        super(cause);
    }

}
