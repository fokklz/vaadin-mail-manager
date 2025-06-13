package ch.aarboard.vamm.ui.dialogs;

import ch.aarboard.vamm.services.JammMailAccountManagementService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

public class CreateAccountDialog extends Dialog {

    private final JammMailAccountManagementService accountService;
    private final Runnable onSuccess;

    private final TextField emailField;
    private final PasswordField passwordField;
    private final TextField quotaField;
    private final TextArea descriptionField;

    private String selectedDomain;

    public CreateAccountDialog(JammMailAccountManagementService accountService, Runnable onSuccess) {
        this.accountService = accountService;
        this.onSuccess = onSuccess;

        setModal(true);
        setDraggable(true);
        setResizable(true);
        setWidth("500px");

        // Create form fields
        emailField = new TextField("Email Address");
        emailField.setPlaceholder("user@domain.com");
        emailField.setRequired(true);
        emailField.setRequiredIndicatorVisible(true);

        passwordField = new PasswordField("Password");
        passwordField.setRequired(true);
        passwordField.setRequiredIndicatorVisible(true);

        quotaField = new TextField("Quota");
        quotaField.setPlaceholder("e.g., 1G, 500M (leave empty for no limit)");

        descriptionField = new TextArea("Description");
        descriptionField.setPlaceholder("Optional description");

        // Create form layout
        FormLayout formLayout = new FormLayout();
        formLayout.add(emailField, passwordField, quotaField, descriptionField);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        // Create buttons
        Button saveButton = new Button("Create Account", e -> createAccount());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        // Add to dialog
        add(new H2("Create New Account"), formLayout, buttonLayout);
    }

    public void setDomain(String domain) {
        this.selectedDomain = domain;
        if (domain != null) {
            emailField.setPlaceholder("user@" + domain);
            emailField.setSuffixComponent(new Span("@" + domain));
        }
    }

    private void createAccount() {
        if (!validateForm()) {
            return;
        }

        try {
            String email = emailField.getValue();
            if (selectedDomain != null && !email.contains("@")) {
                email = email + "@" + selectedDomain;
            }

            accountService.createAccount(
                    email,
                    passwordField.getValue(),
                    null,
                    quotaField.getValue().trim().isEmpty() ? null : quotaField.getValue(),
                    descriptionField.getValue().trim().isEmpty() ? null : descriptionField.getValue()
            );

            Notification.show("Account created successfully: " + email, 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            close();
            clearForm();
            if (onSuccess != null) {
                onSuccess.run();
            }

        } catch (Exception e) {
            Notification.show("Error creating account: " + e.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        if (emailField.getValue() == null || emailField.getValue().trim().isEmpty()) {
            emailField.setErrorMessage("Email is required");
            emailField.setInvalid(true);
            valid = false;
        } else {
            emailField.setInvalid(false);
        }

        if (passwordField.getValue() == null || passwordField.getValue().trim().isEmpty()) {
            passwordField.setErrorMessage("Password is required");
            passwordField.setInvalid(true);
            valid = false;
        } else {
            passwordField.setInvalid(false);
        }

        return valid;
    }

    private void clearForm() {
        emailField.clear();
        passwordField.clear();
        quotaField.clear();
        descriptionField.clear();
        emailField.setInvalid(false);
        passwordField.setInvalid(false);
    }

    @Override
    public void open() {
        clearForm();
        super.open();
        emailField.focus();
    }
}