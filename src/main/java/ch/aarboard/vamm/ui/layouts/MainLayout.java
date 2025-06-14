package ch.aarboard.vamm.ui.layouts;

import ch.aarboard.vamm.data.entries.JammMailAccount;
import ch.aarboard.vamm.data.entries.JammMailAlias;
import ch.aarboard.vamm.events.DomainContentChangedEvent;
import ch.aarboard.vamm.security.SecurityService;
import ch.aarboard.vamm.services.JammMailAccountManagementService;
import ch.aarboard.vamm.services.JammMailAliasManagemeentService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Layout
@PermitAll
@SpringComponent
@UIScope
public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private final ApplicationContext applicationContext;

    private SideNav mainNav;
    private VerticalLayout dynamicNavContainer;
    private String currentDomain = null;
    private boolean isDomainContext = false;

    // Lazy-loaded services
    @Autowired
    @Lazy
    private JammMailAccountManagementService accountService;

    @Autowired
    @Lazy
    private JammMailAliasManagemeentService aliasService;

    @Autowired
    @Lazy
    private SecurityService securityService;

    @Autowired
    public MainLayout(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;

        setPrimarySection(Section.DRAWER);

        // Create navigation container
        dynamicNavContainer = new VerticalLayout();
        dynamicNavContainer.setPadding(false);
        dynamicNavContainer.setSpacing(false);
//        dynamicNavContainer.addClassNames(LumoUtility.Margin.Horizontal.MEDIUM);
//        dynamicNavContainer.setWidthFull();

        // Add main navigation
        mainNav = createMainSideNav();
        dynamicNavContainer.add(mainNav);

        addToDrawer(createHeader(), new Scroller(dynamicNavContainer), createUserMenu());
    }

    @EventListener
    @Async
    public void handleDomainContentChanged(DomainContentChangedEvent event) {
        // Only update if this layout is currently showing the affected domain
        if (isDomainContext && event.getDomainName().equals(currentDomain)) {
            // Use UI.access to safely update the UI from a background thread
            getUI().ifPresent(ui -> ui.access(this::updateDynamicNavigation));
        }
    }


    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String location = event.getLocation().getPath();
        updateNavigationForLocation(location);
    }

    private void updateNavigationForLocation(String location) {
        // Extract domain from URL if we're in accounts context
        String extractedDomain = extractDomainFromLocation(location);
        boolean newIsDomainContext = extractedDomain != null;

        // Only update if context changed
        if (newIsDomainContext != isDomainContext ||
                (extractedDomain != null && !extractedDomain.equals(currentDomain))) {

            currentDomain = extractedDomain;
            isDomainContext = newIsDomainContext;
            updateDynamicNavigation();
        }
    }

    private String extractDomainFromLocation(String location) {
        // Pattern to match accounts/domain-name
        Pattern pattern = Pattern.compile("^domain/([^/]+)(?:/.*)?$");
        Matcher matcher = pattern.matcher(location);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private void updateDynamicNavigation() {
        // Clear existing dynamic navigation
        dynamicNavContainer.removeAll();

        // Add main navigation
        dynamicNavContainer.add(mainNav);

        if (isDomainContext && currentDomain != null) {
            // Add domain-specific navigation
            dynamicNavContainer.add(createDomainNavigation());
        }
    }

    private Component createDomainNavigation() {
        VerticalLayout domainSection = new VerticalLayout();
        domainSection.setPadding(false);
        domainSection.setSpacing(false);
//        domainSection.setWidth("100%");
        domainSection.addClassNames(LumoUtility.Padding.Horizontal.MEDIUM);
//        domainSection.addClassNames(LumoUtility.Margin.Horizontal.MEDIUM);

        // Add separator
        Hr separator = new Hr();
        separator.addClassNames(LumoUtility.Margin.Vertical.SMALL);
        domainSection.add(separator);

        // Domain header
        Span domainHeader = new Span("Domain: " + currentDomain);
        domainHeader.addClassNames(
                LumoUtility.FontSize.SMALL,
                LumoUtility.FontWeight.SEMIBOLD,
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.Margin.Bottom.XSMALL,
                LumoUtility.Margin.Top.SMALL
        );
        domainSection.add(domainHeader);

        // Domain-specific navigation
        SideNav domainNav = new SideNav();
        domainNav.addClassNames(LumoUtility.Margin.Left.MEDIUM);

        // Back to domains item
//        SideNavItem backItem = new SideNavItem("← Back to Domains", "domains", new Icon(VaadinIcon.ARROW_LEFT));
//        backItem.addClassNames(LumoUtility.TextColor.SECONDARY);
//        domainNav.addItem(backItem);

        // Current domain overview (accounts + aliases)
        String currentPath = UI.getCurrent().getInternals().getActiveViewLocation().getPath();
        SideNavItem overviewItem = new SideNavItem("Overview", "domain/" + currentDomain, new Icon(VaadinIcon.DASHBOARD));
        if (currentPath.equals("domain/" + currentDomain)) {
            overviewItem.addClassNames("selected");
        }
        domainNav.addItem(overviewItem);

        domainSection.add(domainNav);

        // Add accounts section
        try {
            List<JammMailAccount> accounts = getAccountService().getAccountsByDomain(currentDomain);
            if (!accounts.isEmpty()) {
                domainSection.add(createAccountsNavigation(accounts));
            }
        } catch (Exception e) {
            System.err.println("Error loading accounts for navigation: " + e.getMessage());
        }

        // Add aliases section
        try {
            List<JammMailAlias> aliases = getAliasService().getAliasesByDomain(currentDomain);
            if (!aliases.isEmpty()) {
                domainSection.add(createAliasesNavigation(aliases));
            }
        } catch (Exception e) {
            System.err.println("Error loading aliases for navigation: " + e.getMessage());
        }

        return domainSection;
    }

    private Component createAccountsNavigation(List<JammMailAccount> accounts) {
        VerticalLayout accountsSection = new VerticalLayout();
        accountsSection.setPadding(false);
        accountsSection.setSpacing(false);
        accountsSection.addClassNames(LumoUtility.Margin.Top.SMALL);

        // Accounts header
        Span accountsHeader = new Span("Accounts (" + accounts.size() + ")");
        accountsHeader.addClassNames(
                LumoUtility.FontSize.SMALL,
                LumoUtility.FontWeight.MEDIUM,
                LumoUtility.TextColor.TERTIARY,
                LumoUtility.Margin.Bottom.XSMALL,
                LumoUtility.Margin.Left.MEDIUM
        );
        accountsSection.add(accountsHeader);

        // Create navigation for each account
        SideNav accountsNav = new SideNav();
        accountsNav.addClassNames(LumoUtility.Margin.Left.LARGE);

        for (JammMailAccount account : accounts) {
            SideNavItem accountItem = createAccountNavItem(account);
            accountsNav.addItem(accountItem);
        }

        accountsSection.add(accountsNav);
        return accountsSection;
    }

    private Component createAliasesNavigation(List<JammMailAlias> aliases) {
        VerticalLayout aliasesSection = new VerticalLayout();
        aliasesSection.setPadding(false);
        aliasesSection.setSpacing(false);
        aliasesSection.addClassNames(LumoUtility.Margin.Top.SMALL);

        // Aliases header
        Span aliasesHeader = new Span("Aliases (" + aliases.size() + ")");
        aliasesHeader.addClassNames(
                LumoUtility.FontSize.SMALL,
                LumoUtility.FontWeight.MEDIUM,
                LumoUtility.TextColor.TERTIARY,
                LumoUtility.Margin.Bottom.XSMALL,
                LumoUtility.Margin.Left.MEDIUM
        );
        aliasesSection.add(aliasesHeader);

        // Create navigation for each alias
        SideNav aliasesNav = new SideNav();
        aliasesNav.addClassNames(LumoUtility.Margin.Left.LARGE);

        for (JammMailAlias alias : aliases) {
            SideNavItem aliasItem = createAliasNavItem(alias);
            aliasesNav.addItem(aliasItem);
        }

        aliasesSection.add(aliasesNav);
        return aliasesSection;
    }

    private SideNavItem createAccountNavItem(JammMailAccount account) {
        // Extract just the username part (before @)
        String username = account.getAccountName();

        // Create the navigation item
        SideNavItem item = new SideNavItem(account.getMail(), "domain/" + currentDomain + "/account/" + username);
        item.getElement().setAttribute("title", account.getMail());

        // Add status icon
        Icon statusIcon = new Icon(account.isActive() ? VaadinIcon.DOT_CIRCLE : VaadinIcon.CIRCLE);
        statusIcon.addClassNames(account.isActive() ?
                LumoUtility.TextColor.SUCCESS : LumoUtility.TextColor.ERROR);
        statusIcon.getStyle().set("width", "8px").set("height", "8px");

        item.setPrefixComponent(statusIcon);
        item.addClassNames(
                LumoUtility.FontSize.SMALL,
                LumoUtility.TextColor.BODY
        );

        // Add tooltip with full email
//        item.getElement().setAttribute("title", account.getMail());

        return item;
    }

    private SideNavItem createAliasNavItem(JammMailAlias alias) {
        // Extract just the alias name part (before @) or show catch-all indicator
        String aliasName = alias.isCatchAll() ? "catch-all" : alias.getAliasName();

        // Create the navigation item
        SideNavItem item = new SideNavItem(alias.getMail(), "domain/" + currentDomain + "/alias/" + aliasName);
        item.getElement().setAttribute("title", alias.getMail());

        // Add status icon
        Icon statusIcon = new Icon(alias.isActive() ? VaadinIcon.DOT_CIRCLE : VaadinIcon.CIRCLE);
        statusIcon.addClassNames(alias.isActive() ?
                LumoUtility.TextColor.SUCCESS : LumoUtility.TextColor.ERROR);
        statusIcon.getStyle().set("width", "8px").set("height", "8px");

        item.setPrefixComponent(statusIcon);
        item.addClassNames(
                LumoUtility.FontSize.SMALL,
                LumoUtility.TextColor.BODY
        );

        // Add special styling for catch-all
//        if (alias.isCatchAll()) {
//            item.addClassNames(LumoUtility.FontStyle.ITALIC);
//        }

        // Add tooltip with full email and destinations
        String tooltip = alias.getMail();
        if (!alias.getDestinations().isEmpty()) {
            tooltip += " → " + String.join(", ", alias.getDestinations());
        }
        item.getElement().setAttribute("title", tooltip);

        return item;
    }

    private Div createHeader() {
        var appName = new Span("Vaadin Mail Manager");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);

        var header = new Div(appName);
        header.addClassNames(LumoUtility.Display.FLEX, LumoUtility.Padding.MEDIUM, LumoUtility.Gap.MEDIUM, LumoUtility.AlignItems.CENTER);
        return header;
    }

    private SideNav createMainSideNav() {
        var nav = new SideNav();
        nav.setWidthFull();
//        nav.addClassNames(LumoUtility.Margin.Horizontal.MEDIUM);

        // Add main menu entries
        for (MenuEntry entry : MenuConfiguration.getMenuEntries()) {
            var item = createMainSideNavItem(entry);
            item.ifPresent(nav::addItem);
        }

        return nav;
    }

    private Optional<SideNavItem> createMainSideNavItem(MenuEntry menuEntry) {
        var path = menuEntry.path();

        if (path.equalsIgnoreCase("account") && !securityService.isUser()){
            return Optional.empty(); // Skip account entry for non-users
        }


        if (menuEntry.icon() != null) {
            return Optional.of(new SideNavItem(menuEntry.title(), path, new Icon(menuEntry.icon())));
        } else {
            return Optional.of(new SideNavItem(menuEntry.title(), path));
        }
    }

    private Component createUserMenu() {
        var logoutButton = new Button("Logout", new Icon(VaadinIcon.POWER_OFF));
        logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        logoutButton.addClickListener(e -> logout());
        logoutButton.addClassNames(LumoUtility.Margin.MEDIUM, LumoUtility.JustifyContent.START);
        return logoutButton;
    }

    private void logout() {
        SecurityContextHolder.clearContext();
        UI.getCurrent().getPage().setLocation("/login");
    }

    // Lazy service getters
    private JammMailAccountManagementService getAccountService() {
//        if (accountService == null) {
//            accountService = applicationContext.getBean(AccountManagementService.class);
//        }
        return accountService;
    }

    private JammMailAliasManagemeentService getAliasService() {
//        if (aliasService == null) {
//            aliasService = applicationContext.getBean(AliasManagementService.class);
//        }
        return aliasService;
    }
}