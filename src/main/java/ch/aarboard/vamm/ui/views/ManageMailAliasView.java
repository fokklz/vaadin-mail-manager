package ch.aarboard.vamm.ui.views;

import ch.aarboard.vamm.data.entries.JammMailAlias;
import ch.aarboard.vamm.events.DomainContentChangedEvent;
import ch.aarboard.vamm.services.JammMailAliasManagemeentService;
import ch.aarboard.vamm.ui.layouts.breadcrumbs.BreadcrumbItem;
import ch.aarboard.vamm.ui.layouts.breadcrumbs.BreadcrumbLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@PageTitle("Manage Mail Aliases")
@Route("domain/:domain/alias/:alias")
@PermitAll
@RolesAllowed({"ROLE_SITE_ADMIN", "ROLE_DOMAIN_ADMIN"})
public class ManageMailAliasView extends BreadcrumbLayout implements BeforeEnterObserver {

    private final JammMailAliasManagemeentService aliasService;
    private final ApplicationEventPublisher eventPublisher;

    private String domainName;
    private String aliasName;
    private JammMailAlias alias;

    private H1 pageTitle;
    private FormLayout detailsForm;
    private HorizontalLayout breadcrumbLayout;


    public ManageMailAliasView(@Autowired JammMailAliasManagemeentService aliasService,
                               @Autowired ApplicationEventPublisher eventPublisher) {
        this.aliasService = aliasService;
        this.eventPublisher = eventPublisher;

        setSizeFull();
        addClassName("alias-detail-view");

        createLayout();
    }


    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        RouteParameters params = event.getRouteParameters();
        String userId = params.get("domain").orElse("");
        String aliasParam = params.get("alias").orElse("");

        this.domainName = userId;
        this.aliasName = aliasParam;

        if (domainName.isEmpty() || aliasName.isEmpty()) {
            event.forwardTo("domains");
            return;
        }

        try {
            // Handle catch-all alias
            String email = aliasName.equals("catch-all") ?
                    "@" + domainName :
                    aliasName + "@" + domainName;

            alias = aliasService.getAlias(email);

            if (alias == null) {
                Notification.show("Alias not found: " + email, 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                event.forwardTo("domain/" + domainName);
                return;
            }

            updateContent();

        } catch (Exception e) {
            Notification.show("Error loading alias: " + e.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            event.forwardTo("domain/" + domainName);
        }
    }

    private void createLayout() {
        HorizontalLayout breadcrumb = new HorizontalLayout();
        breadcrumb.setAlignItems(Alignment.CENTER);
        breadcrumb.setSpacing(false);
        breadcrumb.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        breadcrumbLayout = breadcrumb;
        add(breadcrumbLayout);

        // Page title
        pageTitle = new H1();
        add(pageTitle);

        // Action buttons
        add(createActionButtons());

        // Details form
        detailsForm = new FormLayout();
        detailsForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        add(detailsForm);
    }

    private void updateBreadcrumb() {
        updateBreadcrumb(List.of(
                new BreadcrumbItem("Domains", "domains"),
                new BreadcrumbItem(domainName, "accounts/" + domainName),
                new BreadcrumbItem((aliasName.equals("catch-all") ? "Catch-all" : aliasName), null)
        ));
    }

    private HorizontalLayout createActionButtons() {
        Button toggleButton = new Button();
        toggleButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        toggleButton.addClickListener(e -> toggleAliasStatus());

        Button deleteButton = new Button("Delete", new Icon(VaadinIcon.TRASH));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> confirmDelete());

        HorizontalLayout actions = new HorizontalLayout(toggleButton, deleteButton);
        actions.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

        return actions;
    }

    private void updateContent() {
        if (alias == null) return;

        // Update page title
        pageTitle.setText(alias.getMail());

        // Update breadcrumb with actual domain if needed
        if (domainName == null) {
            domainName = alias.getDomain();
        }

        // Clear and rebuild form
        detailsForm.removeAll();
        updateBreadcrumb();

        // Basic info
        addFormField("Email", alias.getMail());
        addFormField("Status", alias.isActive() ? "Active" : "Inactive");
        addFormField("Type", alias.isCatchAll() ? "Catch-all alias" : "Standard alias");
        addFormField("Description", alias.getDescription());

        // Destinations
        TextArea destinationsArea = new TextArea("Destinations");
        destinationsArea.setValue(String.join("\n", alias.getDestinations()));
        destinationsArea.setReadOnly(true);
        destinationsArea.setHeight("120px");
        detailsForm.add(destinationsArea, 2); // Span 2 columns

        // System info
        addFormField("Common Name", alias.getCommonName());
        addFormField("Mail Source", alias.getMailsource());

        // Last modified
        if (alias.getLastChange() != null) {
            try {
                LocalDateTime dateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(alias.getLastChangeAsLong()),
                        ZoneId.systemDefault()
                );
                addFormField("Last Modified", dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } catch (Exception e) {
                addFormField("Last Modified", alias.getLastChange());
            }
        }

        // Update action buttons
        updateActionButtons();
    }

    private void addFormField(String label, String value) {
        TextField field = new TextField(label);
        field.setValue(value != null ? value : "");
        field.setReadOnly(true);
        detailsForm.add(field);
    }

    private void updateActionButtons() {
        // Find and update the toggle button
        getChildren()
                .filter(HorizontalLayout.class::isInstance)
                .map(HorizontalLayout.class::cast)
                .flatMap(layout -> layout.getChildren())
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .filter(btn -> btn.getThemeNames().contains("primary"))
                .findFirst()
                .ifPresent(toggleButton -> {
                    if (alias.isActive()) {
                        toggleButton.setText("Deactivate");
                        toggleButton.setIcon(new Icon(VaadinIcon.PAUSE));
                    } else {
                        toggleButton.setText("Activate");
                        toggleButton.setIcon(new Icon(VaadinIcon.PLAY));
                    }
                });
    }


    private void confirmDelete() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete Alias");
        dialog.setText("Are you sure you want to delete alias '" + alias.getMail() +
                "'? This action cannot be undone.");

        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> deleteAccount());
        dialog.open();
    }

    private void deleteAccount() {
        try {
            aliasService.deleteAlias(alias.getMail());
            eventPublisher.publishEvent(new DomainContentChangedEvent(this, domainName, DomainContentChangedEvent.ContentType.ALIAS_DELETED));
            UI.getCurrent().navigate("domain/" + domainName);

            Notification.show("Alias " + alias.getMail() + " has been deleted", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Error deleting account: " + e.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void toggleAliasStatus() {
        try {
            aliasService.toggleAliasStatus(alias.getMail());
            alias = aliasService.getAlias(alias.getMail()); // Reload
            updateContent();
            eventPublisher.publishEvent(new DomainContentChangedEvent(this, domainName, DomainContentChangedEvent.ContentType.ALIAS_UPDATED));

            String action = alias.isActive() ? "activated" : "deactivated";
            Notification.show("Alias has been " + action, 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        } catch (Exception e) {
            Notification.show("Error updating alias: " + e.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}