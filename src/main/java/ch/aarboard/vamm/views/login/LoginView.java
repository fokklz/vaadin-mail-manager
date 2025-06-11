package ch.aarboard.vamm.views.login;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Login")
@Route("login")
@Menu(order = 5, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
public class LoginView extends Composite<VerticalLayout> {

    public LoginView() {
        LoginForm loginForm = new LoginForm();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        getContent().setJustifyContentMode(JustifyContentMode.CENTER);
        getContent().setAlignItems(Alignment.CENTER);
        getContent().setAlignSelf(FlexComponent.Alignment.CENTER, loginForm);
        loginForm.setWidth("min-content");
        getContent().add(loginForm);
    }
}
