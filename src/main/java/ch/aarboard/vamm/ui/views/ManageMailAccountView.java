package ch.aarboard.vamm.ui.views;

import ch.aarboard.vamm.data.entries.JammMailAccount;
import ch.aarboard.vamm.events.DomainContentChangedEvent;
import ch.aarboard.vamm.services.JammMailAccountManagementService;
import ch.aarboard.vamm.ui.layouts.breadcrumbs.BreadcrumbItem;
import ch.aarboard.vamm.ui.layouts.breadcrumbs.BreadcrumbLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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

@PageTitle("Manage Mail Account")
@Route("domain/:domain/account/:username")
@PermitAll
@RolesAllowed({"ROLE_SITE_ADMIN", "ROLE_DOMAIN_ADMIN"})
public class ManageMailAccountView extends BreadcrumbLayout implements BeforeEnterObserver {


    private final JammMailAccountManagementService accountService;
    private final ApplicationEventPublisher eventPublisher;

    private String domainName;
    private String username;
    private JammMailAccount account;

    private H1 pageTitle;
    private FormLayout detailsForm;
    private HorizontalLayout breadcrumbLayout;

    public ManageMailAccountView(@Autowired JammMailAccountManagementService accountService,
                                 @Autowired ApplicationEventPublisher eventPublisher) {
        super(List.of(new BreadcrumbItem("Domains", "domains")));
        this.accountService = accountService;
        this.eventPublisher = eventPublisher;

        setSizeFull();
        addClassName("account-detail-view");

        createLayout();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        RouteParameters params = event.getRouteParameters();
        String userId = params.get("domain").orElse("");
        String orderId = params.get("username").orElse("");

        this.domainName = userId;
        this.username = orderId;

        if (domainName.isEmpty() || username.isEmpty()) {
            event.forwardTo("domains");
            return;
        }

        try {
            String email = username + "@" + domainName;
            account = accountService.getAccount(email);

            if (account == null) {
                Notification.show("Account not found: " + email, 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                event.forwardTo("domain/" + domainName);
                return;
            }

            updateContent();

        } catch (Exception e) {
            Notification.show("Error loading account: " + e.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            event.forwardTo("domain/" + domainName);
        }
    }

    private void createLayout() {
        // Breadcrumb
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
                new BreadcrumbItem(domainName, "domain/" + domainName),
                new BreadcrumbItem(account.getMail(), null)
        ));
    }

    private HorizontalLayout createActionButtons() {
        Button toggleButton = new Button();
        toggleButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        toggleButton.addClickListener(e -> toggleAccountStatus());

        Button deleteButton = new Button("Delete", new Icon(VaadinIcon.TRASH));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> confirmDelete());

        HorizontalLayout actions = new HorizontalLayout(toggleButton, deleteButton);
        actions.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

        return actions;
    }

    private void confirmDelete() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete Account");
        dialog.setText("Are you sure you want to delete account '" + account.getMail() +
                "'? This action cannot be undone.");

        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> deleteAccount());
        dialog.open();
    }

    private void deleteAccount() {
        try {
            accountService.deleteAccount(account.getMail());
            eventPublisher.publishEvent(new DomainContentChangedEvent(this, domainName, DomainContentChangedEvent.ContentType.ACCOUNT_DELETED));
            UI.getCurrent().navigate("domain/" + domainName);

            Notification.show("Account " + account.getMail() + " has been deleted", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Error deleting account: " + e.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateContent() {
        if (account == null) return;

        // Update page title
        pageTitle.setText(account.getMail());

        // Update breadcrumb with actual domain if needed
        if (domainName == null) {
            domainName = account.getDomain();
        }
        updateBreadcrumb();

        // Clear and rebuild form
        detailsForm.removeAll();

        // Basic info
        addFormField("Email", account.getMail());
        addFormField("Status", account.isActive() ? "Active" : "Inactive");
        addFormField("Home Directory", account.getHomeDirectory());
        addFormField("Mailbox", account.getMailbox());
        addFormField("Quota", account.getQuota() != null ? account.getQuota() : "No limit");
        addFormField("Description", account.getDescription());

        // System info
        addFormField("UID", account.getUid());
        addFormField("UID Number", account.getUidNumber() != null ? account.getUidNumber().toString() : "");
        addFormField("GID Number", account.getGidNumber() != null ? account.getGidNumber().toString() : "");

        // Last modified
        if (account.getLastChange() != null) {
            try {
                LocalDateTime dateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(account.getLastChangeAsLong()),
                        ZoneId.systemDefault()
                );
                addFormField("Last Modified", dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } catch (Exception e) {
                addFormField("Last Modified", account.getLastChange());
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
                    if (account.isActive()) {
                        toggleButton.setText("Deactivate");
                        toggleButton.setIcon(new Icon(VaadinIcon.PAUSE));
                    } else {
                        toggleButton.setText("Activate");
                        toggleButton.setIcon(new Icon(VaadinIcon.PLAY));
                    }
                });
    }

    private void toggleAccountStatus() {
        try {
            accountService.toggleAccountStatus(account.getMail());
            account = accountService.getAccount(account.getMail()); // Reload
            updateContent();
            eventPublisher.publishEvent(new DomainContentChangedEvent(this, domainName, DomainContentChangedEvent.ContentType.ACCOUNT_UPDATED));

            String action = account.isActive() ? "activated" : "deactivated";
            Notification.show("Account has been " + action, 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        } catch (Exception e) {
            Notification.show("Error updating account: " + e.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
