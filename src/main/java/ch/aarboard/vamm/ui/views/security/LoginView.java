package ch.aarboard.vamm.ui.views.security;

import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "login", autoLayout = false)
@PageTitle("Login")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

    private LoginForm loginForm;

    public LoginView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        loginForm = new LoginForm();
        loginForm.setAction("login");
        loginForm.setForgotPasswordButtonVisible(false);

        // Customize the login form
        loginForm.getElement().setAttribute("no-autofocus", "");

        add(loginForm);
    }
}