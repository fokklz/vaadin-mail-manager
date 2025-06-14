package ch.aarboard.vamm.ui.views;

import ch.aarboard.vamm.ui.layouts.breadcrumbs.BreadcrumbLayout;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Account")
@Route("account")
@Menu(order = 2, icon = LineAwesomeIconUrl.USER)
@Uses(Icon.class)
@PermitAll
@RolesAllowed("ROLE_USER")
public class AccountView extends BreadcrumbLayout {

    public AccountView() {
        super();
        addClassName("account-view");
        setId("account-view");
    }


}
