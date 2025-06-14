package ch.aarboard.vamm.events;

import org.springframework.context.ApplicationEvent;

/**
 * This event was introduced to notify the navigation bar and other components
 */
public class DomainContentChangedEvent extends ApplicationEvent {

    private final String domainName;
    private final ContentType contentType;

    public enum ContentType {
        ACCOUNT_CREATED,
        ACCOUNT_UPDATED,
        ACCOUNT_DELETED,
        ALIAS_CREATED,
        ALIAS_UPDATED,
        ALIAS_DELETED
    }

    public DomainContentChangedEvent(Object source, String domainName, ContentType contentType) {
        super(source);
        this.domainName = domainName;
        this.contentType = contentType;
    }

    public String getDomainName() {
        return domainName;
    }

    public ContentType getContentType() {
        return contentType;
    }
}