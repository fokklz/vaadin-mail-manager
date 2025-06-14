package ch.aarboard.vamm.ui.views;

import ch.aarboard.vamm.data.entries.JammMailAccount;
import ch.aarboard.vamm.data.entries.JammMailAlias;
import ch.aarboard.vamm.data.entries.JammVirtualDomain;
import ch.aarboard.vamm.events.DomainContentChangedEvent;
import ch.aarboard.vamm.services.JammMailAccountManagementService;
import ch.aarboard.vamm.services.JammMailAliasManagemeentService;
import ch.aarboard.vamm.services.JammVirtualDomainManagementService;
import ch.aarboard.vamm.ui.dialogs.CreateAccountDialog;
import ch.aarboard.vamm.ui.dialogs.CreateAliasDialog;
import ch.aarboard.vamm.ui.layouts.breadcrumbs.BreadcrumbItem;
import ch.aarboard.vamm.ui.layouts.breadcrumbs.BreadcrumbLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
@PageTitle("Manage Domain")
@Route("domain/:domain")
@PermitAll
@RolesAllowed({"ROLE_SITE_ADMIN", "ROLE_DOMAIN_ADMIN"})
public class ManageDomainView extends BreadcrumbLayout implements BeforeEnterObserver {

    private final JammMailAccountManagementService accountManagementService;
    private final JammMailAliasManagemeentService aliasManagementService;
    private final JammVirtualDomainManagementService domainManagementService;
    private final ApplicationEventPublisher eventPublisher;

    private final Grid<JammMailAccount> accountGrid;
    private final Grid<JammMailAlias> aliasGrid;

    private CreateAccountDialog createAccountDialog;
    private CreateAliasDialog createAliasDialog;

    private String currentDomainName = null;
    private JammVirtualDomain currentDomain = null;

    // UI Components
    private H1 pageTitle;
    private Span domainStats;
    private Button createAccountBtn;
    private Button createAliasBtn;


    public ManageDomainView(
            @Autowired JammMailAccountManagementService accountManagementService,
            @Autowired JammMailAliasManagemeentService aliasManagementService,
            @Autowired JammVirtualDomainManagementService domainManagementService,
            @Autowired ApplicationEventPublisher eventPublisher) {
        super(List.of(new BreadcrumbItem("Domains", "domains")));

        this.accountManagementService = accountManagementService;
        this.aliasManagementService = aliasManagementService;
        this.domainManagementService = domainManagementService;
        this.eventPublisher = eventPublisher;

        setSizeFull();
        addClassName("manage-domain-view");
        setId("manage-domain-view");

        this.accountGrid = createAccountGrid();
        this.aliasGrid = createAliasGrid();

        createLayout();
    }


    public void createLayout(){
        add(createPageHeader());
        add(createAccountsSection());
        add(createAliasesSection());
    }


    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        RouteParameters params = event.getRouteParameters();
        currentDomainName = params.get("domain").orElse("");

        if (currentDomainName == null || currentDomainName.trim().isEmpty()) {
            // Redirect to domain management if no domain specified
            event.forwardTo(DomainsView.class);
            return;
        }

        try {
            // Validate domain exists and load domain info
            currentDomain = domainManagementService.getDomain(currentDomainName);
            if (currentDomain == null) {
                Notification.show("Domain not found: " + currentDomainName, 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                event.forwardTo(DomainsView.class);
                return;
            }

            // Update UI with domain info
            updateContent();
            enableControls(true);
            refreshGrids();

        } catch (Exception e) {
            Notification.show("Error loading domain: " + e.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            event.forwardTo(DomainsView.class);
        }
    }

    private void updateBreadcrumb() {
        updateBreadcrumb(List.of(
                new BreadcrumbItem("Domains", "domains"),
                new BreadcrumbItem(currentDomainName != null ? currentDomainName : "Account & Alias Management", "")
        ));
    }

    private VerticalLayout createPageHeader() {
        pageTitle = new H1("Account & Alias Management");
        pageTitle.addClassNames(LumoUtility.Margin.Bottom.NONE);

        domainStats = new Span();
        domainStats.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.MEDIUM);

        VerticalLayout header = new VerticalLayout(pageTitle, domainStats);
        header.setPadding(false);
        header.setSpacing(false);
        header.addClassNames(LumoUtility.Margin.Bottom.LARGE);

        return header;
    }

    private void updateContent() {
        if (currentDomain != null) {
            pageTitle.setText("Manage " + currentDomain.getJvd());

            updateBreadcrumb();

            // Load fresh stats
            int accountCount = accountManagementService.getAccountCount(currentDomainName);
            int aliasCount = aliasManagementService.getAliasCount(currentDomainName);

            domainStats.setText(String.format("%d accounts • %d aliases • %s",
                    accountCount,
                    aliasCount,
                    currentDomain.isActive() ? "Active" : "Inactive"));
        }
    }

    private VerticalLayout createAccountsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        H2 title = new H2("Mail Accounts");
        title.addClassNames(LumoUtility.Margin.Bottom.NONE, LumoUtility.Margin.Top.SMALL);

        createAccountBtn = new Button("Create Account", new Icon(VaadinIcon.PLUS));
        createAccountBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createAccountBtn.addClickListener(e -> openCreateAccountDialog());
        createAccountBtn.setEnabled(false); // Enable when domain is loaded

        header.add(title, createAccountBtn);

        section.add(header, accountGrid);
        return section;
    }

    private VerticalLayout createAliasesSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        H2 title = new H2("Mail Aliases");
        title.addClassNames(LumoUtility.Margin.Bottom.NONE, LumoUtility.Margin.Top.MEDIUM);

        createAliasBtn = new Button("Create Alias", new Icon(VaadinIcon.PLUS));
        createAliasBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createAliasBtn.addClickListener(e -> openCreateAliasDialog());
        createAliasBtn.setEnabled(false); // Enable when domain is loaded

        header.add(title, createAliasBtn);

        section.add(header, aliasGrid);
        return section;
    }

    private Grid<JammMailAccount> createAccountGrid() {
        Grid<JammMailAccount> grid = new Grid<>(JammMailAccount.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setHeight("30vh");

        // Email column with status indicator
        grid.addColumn(new ComponentRenderer<>(account -> {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setAlignItems(Alignment.CENTER);
            layout.setSpacing(true);

            // Status indicator
            Span statusIndicator = new Span();
            statusIndicator.getElement().getStyle().set("width", "8px").set("height", "8px")
                    .set("min-width", "8px").set("min-height", "8px")
                    .set("max-width", "8px").set("max-height", "8px")
                    .set("border-radius", "50%").set("display", "inline-block");

            if (account.isActive()) {
                statusIndicator.getElement().getStyle().set("background-color", "var(--lumo-success-color)");
            } else {
                statusIndicator.getElement().getStyle().set("background-color", "var(--lumo-error-color)");
            }

            Span email = new Span(account.getMail());
            email.getElement().setAttribute("title", account.getMail());
            email.addClassNames(LumoUtility.FontWeight.SEMIBOLD);

            layout.add(statusIndicator, email);
            return layout;
        })).setHeader("Email Address").setAutoWidth(true).setFlexGrow(1);

        // Status column
        grid.addColumn(new ComponentRenderer<>(account -> {
            Span status = new Span(account.isActive() ? "Active" : "Inactive");
            status.getElement().getThemeList().add(account.isActive() ? "badge success" : "badge error");
            return status;
        })).setHeader("Status").setAutoWidth(true);

        // Quota column
        grid.addColumn(account -> account.getQuota() != null ? account.getQuota() : "No limit")
                .setHeader("Quota").setAutoWidth(true);

        // Description column
        grid.addColumn(JammMailAccount::getDescription)
                .setHeader("Description").setFlexGrow(1);

        // Last modified column
        grid.addColumn(new ComponentRenderer<>(account -> {
            if (account.getLastChange() != null) {
                LocalDateTime dateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(account.getLastChangeAsLong()),
                        ZoneId.systemDefault()
                );
                return new Span(dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            return new Span("-");
        })).setHeader("Last Modified").setAutoWidth(true);

        // Actions column
        grid.addColumn(new ComponentRenderer<>(this::createAccountActionButtons))
                .setHeader("Actions").setAutoWidth(true).setFlexGrow(0);

        return grid;
    }

    private Grid<JammMailAlias> createAliasGrid() {
        Grid<JammMailAlias> grid = new Grid<>(JammMailAlias.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setHeight("30vh");

        // Alias email column with status indicator
        grid.addColumn(new ComponentRenderer<>(alias -> {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setAlignItems(Alignment.CENTER);
            layout.setSpacing(true);

            // Status indicator
            Span statusIndicator = new Span();
            statusIndicator.getElement().getStyle().set("width", "8px").set("height", "8px")
                    .set("min-width", "8px").set("min-height", "8px")
                    .set("max-width", "8px").set("max-height", "8px")
                    .set("border-radius", "50%").set("display", "inline-block");

            if (alias.isActive()) {
                statusIndicator.getElement().getStyle().set("background-color", "var(--lumo-success-color)");
            } else {
                statusIndicator.getElement().getStyle().set("background-color", "var(--lumo-error-color)");
            }

            Span email = new Span(alias.getMail());
            email.getElement().setAttribute("title", alias.getMail());
            email.addClassNames(LumoUtility.FontWeight.SEMIBOLD);

            // Add catch-all indicator
            if (alias.isCatchAll()) {
                Span catchAllBadge = new Span("CATCH-ALL");
                catchAllBadge.getElement().getThemeList().add("badge contrast");
                layout.add(statusIndicator, email, catchAllBadge);
            } else {
                layout.add(statusIndicator, email);
            }

            return layout;
        })).setHeader("Alias Email").setAutoWidth(true).setFlexGrow(1).setWidth("25%").addClassNames(LumoUtility.TextOverflow.ELLIPSIS);
        // Destinations column
        grid.addColumn(new ComponentRenderer<>(alias -> {
            List<String> destinations = alias.getDestinations();
            if (destinations.isEmpty()) {
                return new Span("No destinations");
            }

            String text = destinations.size() == 1 ?
                    destinations.get(0) :
                    destinations.size() + " destinations";

            Span span = new Span(text);
            span.setTitle(String.join(", ", destinations));
            return span;
        })).setHeader("Destinations").setFlexGrow(1);

        // Status column
        grid.addColumn(new ComponentRenderer<>(alias -> {
            Span status = new Span(alias.isActive() ? "Active" : "Inactive");
            status.getElement().getThemeList().add(alias.isActive() ? "badge success" : "badge error");
            return status;
        })).setHeader("Status").setAutoWidth(true);

        // Description column
        grid.addColumn(JammMailAlias::getDescription)
                .setHeader("Description").setFlexGrow(1);

        // Actions column
        grid.addColumn(new ComponentRenderer<>(this::createAliasActionButtons))
                .setHeader("Actions").setAutoWidth(true).setFlexGrow(0);

        return grid;
    }

    private HorizontalLayout createAccountActionButtons(JammMailAccount account) {
        Button toggleButton = new Button();
        toggleButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        if (account.isActive()) {
            toggleButton.setIcon(new Icon(VaadinIcon.PAUSE));
            toggleButton.setText("Deactivate");
            toggleButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        } else {
            toggleButton.setIcon(new Icon(VaadinIcon.PLAY));
            toggleButton.setText("Activate");
            toggleButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        }

        toggleButton.addClickListener(e -> toggleAccountStatus(account));

        Button editButton = new Button("Edit", new Icon(VaadinIcon.EDIT));
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editButton.addClickListener(e -> UI.getCurrent().navigate("domain/" + account.getDomain() + "/account/" + account.getAccountName()));

        Button deleteButton = new Button("Delete", new Icon(VaadinIcon.TRASH));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> confirmDeleteAccount(account));

        HorizontalLayout actions = new HorizontalLayout(toggleButton, editButton, deleteButton);
        actions.setSpacing(true);
        return actions;
    }

    private HorizontalLayout createAliasActionButtons(JammMailAlias alias) {
        Button toggleButton = new Button();
        toggleButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        if (alias.isActive()) {
            toggleButton.setIcon(new Icon(VaadinIcon.PAUSE));
            toggleButton.setText("Deactivate");
            toggleButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        } else {
            toggleButton.setIcon(new Icon(VaadinIcon.PLAY));
            toggleButton.setText("Activate");
            toggleButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        }

        toggleButton.addClickListener(e -> toggleAliasStatus(alias));

        Button editButton = new Button("Edit", new Icon(VaadinIcon.EDIT));
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editButton.addClickListener(e -> UI.getCurrent().navigate("domain/" + alias.getDomain() + "/alias/" + alias.getAliasName()));

        Button deleteButton = new Button("Delete", new Icon(VaadinIcon.TRASH));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> confirmDeleteAlias(alias));

        HorizontalLayout actions = new HorizontalLayout(toggleButton, editButton, deleteButton);
        actions.setSpacing(true);
        return actions;
    }

    // Event handlers
    private void refreshGrids() {
        if (currentDomainName != null) {
            refreshAccountGrid();
            refreshAliasGrid();
            updateContent(); // Refresh stats
        }
    }

    private void refreshAccountGrid() {
        try {
            List<JammMailAccount> accounts = accountManagementService.getAccountsByDomain(currentDomainName);
            accountGrid.setItems(accounts);
        } catch (Exception e) {
            Notification.show("Error loading accounts: " + e.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void refreshAliasGrid() {
        try {
            List<JammMailAlias> aliases = aliasManagementService.getAliasesByDomain(currentDomainName);
            aliasGrid.setItems(aliases);
        } catch (Exception e) {
            Notification.show("Error loading aliases: " + e.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void enableControls(boolean enabled) {
//        accountSearchField.setEnabled(enabled);
//        aliasSearchField.setEnabled(enabled);
        createAccountBtn.setEnabled(enabled);
        createAliasBtn.setEnabled(enabled);
    }

    // Account actions
    private void openCreateAccountDialog() {
        if (createAccountDialog == null) {
            createAccountDialog = new CreateAccountDialog(accountManagementService, this::refreshGrids, eventPublisher);
        }
        createAccountDialog.setDomain(currentDomainName);
        createAccountDialog.open();
    }

    private void toggleAccountStatus(JammMailAccount account) {
        try {
            accountManagementService.toggleAccountStatus(account.getMail());
            refreshGrids();

            eventPublisher.publishEvent(new DomainContentChangedEvent(this, account.getDomain(), DomainContentChangedEvent.ContentType.ACCOUNT_UPDATED));

            String action = account.isActive() ? "deactivated" : "activated";
            Notification.show("Account " + account.getMail() + " has been " + action, 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Error updating account status: " + e.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void confirmDeleteAccount(JammMailAccount account) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete Account");
        dialog.setText("Are you sure you want to delete account '" + account.getMail() +
                "'? This action cannot be undone.");

        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> deleteAccount(account));
        dialog.open();
    }

    private void deleteAccount(JammMailAccount account) {
        try {
            accountManagementService.deleteAccount(account.getMail());
            refreshGrids();

            eventPublisher.publishEvent(new DomainContentChangedEvent(this, account.getDomain(), DomainContentChangedEvent.ContentType.ACCOUNT_UPDATED));

            Notification.show("Account " + account.getMail() + " has been deleted", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Error deleting account: " + e.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    // Alias actions
    private void openCreateAliasDialog() {
        if (createAliasDialog == null) {
            createAliasDialog = new CreateAliasDialog(aliasManagementService, this::refreshGrids, eventPublisher);
        }
        createAliasDialog.setDomain(currentDomainName);
        createAliasDialog.open();
    }

    private void toggleAliasStatus(JammMailAlias alias) {
        try {
            aliasManagementService.toggleAliasStatus(alias.getMail());
            refreshGrids();

            eventPublisher.publishEvent(new DomainContentChangedEvent(this, alias.getDomain(), DomainContentChangedEvent.ContentType.ALIAS_UPDATED));

            String action = alias.isActive() ? "deactivated" : "activated";
            Notification.show("Alias " + alias.getMail() + " has been " + action, 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Error updating alias status: " + e.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void confirmDeleteAlias(JammMailAlias alias) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete Alias");
        dialog.setText("Are you sure you want to delete alias '" + alias.getMail() +
                "'? This action cannot be undone.");

        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> deleteAlias(alias));
        dialog.open();
    }

    private void deleteAlias(JammMailAlias alias) {
        try {
            aliasManagementService.deleteAlias(alias.getMail());
            refreshGrids();

            eventPublisher.publishEvent(new DomainContentChangedEvent(this, alias.getDomain(), DomainContentChangedEvent.ContentType.ALIAS_UPDATED));

            Notification.show("Alias " + alias.getMail() + " has been deleted", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Error deleting alias: " + e.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
