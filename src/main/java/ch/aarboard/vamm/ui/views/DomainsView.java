package ch.aarboard.vamm.ui.views;

import ch.aarboard.vamm.data.entries.JammVirtualDomain;
import ch.aarboard.vamm.services.JammVirtualDomainManagementService;
import ch.aarboard.vamm.ui.dialogs.CreateDomainDialog;
import ch.aarboard.vamm.ui.layouts.breadcrumbs.BreadcrumbLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@PageTitle("Domains")
@Route("domains")
@Menu(order = 1, icon = LineAwesomeIconUrl.GLOBE_EUROPE_SOLID)
@PermitAll
@RolesAllowed("ROLE_SITE_ADMIN")
public class DomainsView extends VerticalLayout {

    private final JammVirtualDomainManagementService domainService;
    private final Grid<JammVirtualDomain> grid;
    private CreateDomainDialog createDomainDialog;


    public DomainsView(@Autowired JammVirtualDomainManagementService domainService) {
        this.domainService = domainService;

        setSizeFull();
        addClassNames("domains-view");


        createHeader();

        grid = createGrid();
        add(grid);

        // Load data
        refreshGrid();

        // Create dialog
        createDomainDialog = new CreateDomainDialog(domainService, this::refreshGrid);
    }

    private void createHeader() {
        H2 title = new H2("Domains");
        title.addClassNames(LumoUtility.Margin.Bottom.NONE, LumoUtility.Margin.Top.SMALL);

        Button createButton = new Button("Create Domain", new Icon(VaadinIcon.PLUS));
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClickListener(e -> createDomainDialog.open());

        Button refreshButton = new Button("Refresh", new Icon(VaadinIcon.REFRESH));
        refreshButton.addClickListener(e -> refreshGrid());

        HorizontalLayout header = new HorizontalLayout(new HorizontalLayout(title, createButton), refreshButton);
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);
        header.addClassNames(LumoUtility.Padding.Bottom.MEDIUM);

        add(header);
    }

    private Grid<JammVirtualDomain> createGrid() {
        Grid<JammVirtualDomain> domainGrid = new Grid<>(JammVirtualDomain.class, false);
        domainGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);
        domainGrid.setSizeFull();

        // Domain name column with status indicator
        domainGrid.addColumn(new ComponentRenderer<>(domain -> {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setAlignItems(Alignment.CENTER);
            layout.setSpacing(true);

            // Status indicator
            Span statusIndicator = new Span();
            statusIndicator.getElement().getStyle().set("width", "8px").set("height", "8px")
                    .set("border-radius", "50%").set("display", "inline-block");

            if (domain.isActive()) {
                statusIndicator.getElement().getStyle().set("background-color", "var(--lumo-success-color)");
            } else {
                statusIndicator.getElement().getStyle().set("background-color", "var(--lumo-error-color)");
            }

            Span domainName = new Span(domain.getJvd());
            domainName.addClassNames(LumoUtility.FontWeight.SEMIBOLD);

            layout.add(statusIndicator, domainName);
            return layout;
        })).setHeader("Domain").setAutoWidth(true).setFlexGrow(1);

        // Status column
        domainGrid.addColumn(new ComponentRenderer<>(domain -> {
            Span status = new Span(domain.isActive() ? "Active" : "Inactive");
            status.getElement().getThemeList().add(domain.isActive() ? "badge success" : "badge error");
            return status;
        })).setHeader("Status").setAutoWidth(true);

        // Account count
        domainGrid.addColumn(JammVirtualDomain::getAccountCount)
                .setHeader("Accounts").setAutoWidth(true);

        // Alias count
        domainGrid.addColumn(JammVirtualDomain::getAliasCount)
                .setHeader("Aliases").setAutoWidth(true);

        // Description
        domainGrid.addColumn(JammVirtualDomain::getDescription)
                .setHeader("Description").setFlexGrow(1);

        // Last modified
        domainGrid.addColumn(new ComponentRenderer<>(domain -> {
            if (domain.getLastChange() != null) {
                LocalDateTime dateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(domain.getLastChangeAsLong()),
                        ZoneId.systemDefault()
                );
                return new Span(dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            return new Span("-");
        })).setHeader("Last Modified").setAutoWidth(true);

        // Actions column
        domainGrid.addColumn(new ComponentRenderer<>(this::createActionButtons))
                .setHeader("Actions").setAutoWidth(true).setFlexGrow(0);

        return domainGrid;
    }

    private HorizontalLayout createActionButtons(JammVirtualDomain domain) {
        Button manageButton = new Button("Manage", new Icon(VaadinIcon.USERS));
        manageButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        manageButton.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("domain/" + domain.getJvd())));

        Button toggleButton = new Button();
        toggleButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        if (domain.isActive()) {
            toggleButton.setIcon(new Icon(VaadinIcon.PAUSE));
            toggleButton.setText("Deactivate");
            toggleButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        } else {
            toggleButton.setIcon(new Icon(VaadinIcon.PLAY));
            toggleButton.setText("Activate");
            toggleButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        }

        toggleButton.addClickListener(e -> toggleDomainStatus(domain));

        Button editButton = new Button("Edit", new Icon(VaadinIcon.EDIT));
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editButton.addClickListener(e -> editDomain(domain));

        Button deleteButton = new Button("Delete", new Icon(VaadinIcon.TRASH));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> confirmDeleteDomain(domain));

        HorizontalLayout actions = new HorizontalLayout(manageButton, toggleButton, editButton, deleteButton);
        actions.setSpacing(true);
        actions.addClassNames(LumoUtility.JustifyContent.EVENLY);
        return actions;
    }

    private void toggleDomainStatus(JammVirtualDomain domain) {
        try {
            JammVirtualDomain updatedDomain = domainService.toggleDomainStatus(domain.getJvd());
            refreshGrid();

            String action = updatedDomain.isActive() ? "activated" : "deactivated";
            Notification.show("Domain " + domain.getJvd() + " has been " + action, 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Error updating domain status: " + e.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void editDomain(JammVirtualDomain domain) {
        // TODO: Implement edit domain dialog
        Notification.show("Edit functionality coming soon!", 3000, Notification.Position.BOTTOM_END);
    }

    private void confirmDeleteDomain(JammVirtualDomain domain) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete Domain");
        dialog.setText("Are you sure you want to delete domain '" + domain.getJvd() +
                "'? This will also delete all associated accounts and aliases. This action cannot be undone.");

        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> deleteDomain(domain));
        dialog.open();
    }

    private void deleteDomain(JammVirtualDomain domain) {
        try {
            domainService.deleteDomain(domain.getJvd());
            refreshGrid();

            Notification.show("Domain " + domain.getJvd() + " has been deleted", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Error deleting domain: " + e.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void refreshGrid() {
        var items = domainService.getAllDomainsWithStats();
        try {
            grid.setItems(items);
        } catch (Exception e) {
            Notification.show("Error loading domains: " + e.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
