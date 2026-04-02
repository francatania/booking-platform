package com.booking.authservice.service.impl;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class MessageService {

    private final MessageSource messageSource;

    public MessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String key, Locale locale, Object... args) {
        return messageSource.getMessage(key, args, locale);
    }

    public static Locale resolveLocale(String acceptLanguage) {
        if (acceptLanguage != null && acceptLanguage.toLowerCase().startsWith("es")) {
            return new Locale("es");
        }
        return Locale.ENGLISH;
    }
}
