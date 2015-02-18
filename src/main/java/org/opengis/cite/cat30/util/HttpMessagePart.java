package org.opengis.cite.cat30.util;

/**
 * Enumeration for parts of an HTTP message. This type is intended for use as
 * the key in an EnumMap object that contains message components.
 */
public enum HttpMessagePart {

    /**
     * Target resource (from request line).
     */
    TARGET,
    /**
     * Status code (from status line).
     */
    STATUS,
    /**
     * Header fields.
     */
    HEADERS,
    /**
     * Message body.
     */
    BODY
}
