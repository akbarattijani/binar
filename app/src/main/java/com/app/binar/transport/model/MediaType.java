package com.app.binar.transport.model;

/**
 * @author AKBAR <akbar.attijani@gmail.com>
 */
public enum MediaType {
    /*** application/json; charset=utf-8*/                  JSON("application/json; charset=utf-8"),
    /*** application/atom+xml; charset=utf-8*/              ATOM_XML("application/atom+xml; charset=utf-8"),
    /*** application/x-www-form-urlencoded*/                FORM_URLENCODED("application/x-www-form-urlencoded"),
    /*** application/octet-stream; charset=utf-8*/          OCTET_STREAM("application/octet-stream; charset=utf-8"),
    /*** application/svg+xml; charset=utf-8*/               SVG_XML("application/svg+xml; charset=utf-8"),
    /*** application/xhtml+xml; charset=utf-8*/             XHTML_XML("application/xhtml+xml; charset=utf-8"),
    /*** application/xml; charset=utf-8*/                   XML("application/xml; charset=utf-8"),
    /*** multipart/form-data; boundary=*/                   MULTIPART("multipart/form-data; boundary="),
    /*** text/html; charset=utf-8*/                         HTML("text/html; charset=utf-8"),
    /*** text/plain; charset=utf-8*/                        PLAIN("text/plain; charset=utf-8"),
    /*** text/xml; charset=utf-8*/                          TEXT_XML("text/xml; charset=utf-8"),
    /*** image/png*/                                        IMAGE_PNG("image/png"),
    /*** image/gif*/                                        IMAGE_GIF("image/gif"),
    /*** image/jpeg*/                                       IMAGE_JPEG("image/jpeg");

    private final String responseCode;

    MediaType(final String response) {
        responseCode = response;
    }

    @Override
    public String toString() {
        return responseCode;
    }
}
