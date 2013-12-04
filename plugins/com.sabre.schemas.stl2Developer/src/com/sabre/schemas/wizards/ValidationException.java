/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.wizards;

/**
 * @author Agnieszka Janowska
 * 
 */
public class ValidationException extends Exception {

    private static final long serialVersionUID = -5659737667141702154L;

    public ValidationException(final String message) {
        super(message);
    }
}
