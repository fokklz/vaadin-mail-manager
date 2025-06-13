package ch.aarboard.vamm.ui.dialogs;

import ch.aarboard.vamm.services.JammVirtualDomainManagementService;
import ch.aarboard.vamm.utils.MailUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.StringLengthValidator;

public class CreateDomainDialog extends Dialog {

    private final JammVirtualDomainManagementService domainManagementService;
    private final Runnable onSuccess;

    private final TextField domainNameField;
    private final TextArea descriptionField;
    private final Button saveButton;
    private final Button cancelButton;
    private final Binder<DomainFormData> binder;

    public CreateDomainDialog(JammVirtualDomainManagementService domainManagementService, Runnable onSuccess) {
        this.domainManagementService = domainManagementService;
        this.onSuccess = onSuccess;

        setHeaderTitle("Create New Domain");
        setModal(true);
        setDraggable(true);
        setResizable(true);

        // Create form fields
        domainNameField = new TextField("Domain Name");
        domainNameField.setPlaceholder("example.com");
        domainNameField.setRequired(true);
        domainNameField.setHelperText("Enter a valid domain name (e.g., example.com)");

        descriptionField = new TextArea("Description");
        descriptionField.setPlaceholder("Optional description for this domain");
        descriptionField.setMaxLength(255);

        // Create form layout
        FormLayout formLayout = new FormLayout();
        formLayout.add(domainNameField, descriptionField);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1)
        );

        // Create buttons
        saveButton = new Button("Create Domain");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> saveDomain());

        cancelButton = new Button("Cancel");
        cancelButton.addClickListener(e -> close());

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, saveButton);
        buttonLayout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);

        // Setup binder for validation
        binder = new Binder<>(DomainFormData.class);
        setupValidation();

        // Add components to dialog
        add(formLayout, buttonLayout);

        // Focus domain name field when opened
        addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                domainNameField.focus();
                clearForm();
            }
        });
    }

    private void setupValidation() {
        binder.forField(domainNameField)
                .withValidator(name -> name != null && !name.trim().isEmpty(), "Domain name is required")
                .withValidator(MailUtils::isValidDomainName, "Invalid domain name format")
                .bind(DomainFormData::getDomainName, DomainFormData::setDomainName);

        binder.forField(descriptionField)
                .withValidator(new StringLengthValidator("Description too long", 0, 255))
                .withConverter(
                        // Convert empty string to null for optional field
                        value -> value == null || value.trim().isEmpty() ? null : value.trim(),
                        value -> value == null ? "" : value
                )
                .bind(DomainFormData::getDescription, DomainFormData::setDescription);
    }

    private void saveDomain() {
        try {
            DomainFormData formData = new DomainFormData();
            binder.writeBean(formData);

            // Create domain through service
            domainManagementService.createDomain(formData.getDomainName().trim(), formData.getDescription());

            // Show success notification
            Notification.show("Domain '" + formData.getDomainName() + "' created successfully!",
                            3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Refresh parent view and close dialog
            onSuccess.run();
            close();

        } catch (ValidationException e) {
            Notification.show("Please correct the form errors", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("Error creating domain: " + e.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void clearForm() {
        domainNameField.clear();
        descriptionField.clear();
        binder.readBean(new DomainFormData());
    }


    @Override
    public void open() {
        clearForm();
        super.open();
        domainNameField.focus();
    }

    // Form data class for binding
    public static class DomainFormData {
        private String domainName;
        private String description;

        public String getDomainName() { return domainName; }
        public void setDomainName(String domainName) { this.domainName = domainName; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}