package ch.aarboard.vamm.ui.layouts.breadcrumbs;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.List;

public class BreadcrumbLayout extends VerticalLayout {



    private HorizontalLayout breadcrumbLayout;

    public BreadcrumbLayout() {
        breadcrumbLayout = new HorizontalLayout();
        breadcrumbLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        breadcrumbLayout.setSpacing(false);
        breadcrumbLayout.addClassNames(LumoUtility.Display.FLEX, LumoUtility.Gap.SMALL);
        add(breadcrumbLayout);
    }

    public BreadcrumbLayout(List<BreadcrumbItem> items) {
        this();
        updateBreadcrumb(items);
    }

    private Icon createSeparator() {
        Icon separator = new Icon(VaadinIcon.ANGLE_RIGHT);
        separator.addClassNames(LumoUtility.TextColor.SECONDARY);
        return separator;
    }

    private Anchor createAnchor(BreadcrumbItem item) {
        Anchor anchor = new Anchor(item.getHref(), item.getText() != null ? item.getText() : "");
        anchor.addClassNames(LumoUtility.TextColor.PRIMARY);
        if(item.getHref() == null || item.getHref().isEmpty()) {
            anchor.setEnabled(false);
        }
        return anchor;
    }

    public void updateBreadcrumb(List<BreadcrumbItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        breadcrumbLayout.removeAll();

        for (int i = 0; i < items.size(); i++) {
            BreadcrumbItem item = items.get(i);
            breadcrumbLayout.add(createAnchor(item));
            if (i < items.size() - 1) {
                breadcrumbLayout.add(createSeparator());
            }
        }

    }
}