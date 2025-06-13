package ch.aarboard.vamm.ui.layouts.breadcrumbs;

public class BreadcrumbItem {
    private String text;
    private String href;

    public BreadcrumbItem(String text, String href) {
        this.text = text;
        this.href = href;
    }

    public String getText() {
        return text;
    }

    public String getHref() {
        return href;
    }
}