package ch.aarboard.vamm.ui.dialogs;

import ch.aarboard.vamm.events.DomainContentChangedEvent;
import ch.aarboard.vamm.services.JammMailAliasManagemeentService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CreateAliasDialog extends Dialog {

    private final JammMailAliasManagemeentService aliasManagementService;
    private final Runnable onSuccess;
    private final ApplicationEventPublisher eventPublisher;

    private final TextField aliasEmailField;
    private final VerticalLayout destinationsLayout;
    private final TextArea descriptionField;

    private String selectedDomain;
    private final List<TextField> destinationFields = new ArrayList<>();

    public CreateAliasDialog(JammMailAliasManagemeentService aliasManagementService, Runnable onSuccess, ApplicationEventPublisher eventPublisher) {
        this.aliasManagementService = aliasManagementService;
        this.onSuccess = onSuccess;
        this.eventPublisher = eventPublisher;

        setModal(true);
        setDraggable(true);
        setResizable(true);
        setWidth("600px");

        // Create form fields
        aliasEmailField = new TextField("Alias Email");
        aliasEmailField.setPlaceholder("alias@domain.com");
        aliasEmailField.setRequired(true);
        aliasEmailField.setRequiredIndicatorVisible(true);

        descriptionField = new TextArea("Description");
        descriptionField.setPlaceholder("Optional description");

        // Destinations section
        destinationsLayout = new VerticalLayout();
        destinationsLayout.setPadding(false);
        destinationsLayout.setSpacing(true);

        // Add initial destination field
        addDestinationField();

        // Add destination button
        Button addDestinationBtn = new Button("Add Destination", new Icon(VaadinIcon.PLUS));
        addDestinationBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        addDestinationBtn.addClickListener(e -> addDestinationField());

        // Create form layout
        FormLayout formLayout = new FormLayout();
        formLayout.add(aliasEmailField);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        VerticalLayout destinationsSection = new VerticalLayout();
        destinationsSection.setPadding(false);
        destinationsSection.add(new H2("Destinations"), destinationsLayout, addDestinationBtn);

        // Create buttons
        Button saveButton = new Button("Create Alias", e -> createAlias());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        // Add to dialog
        VerticalLayout content = new VerticalLayout();
        content.add(new H2("Create New Alias"), formLayout, destinationsSection, descriptionField, buttonLayout);
        add(content);
    }

    public void setDomain(String domain) {
        this.selectedDomain = domain;
        if (domain != null) {
            aliasEmailField.setPlaceholder("alias@" + domain);
            aliasEmailField.setSuffixComponent(new Span("@" + domain));
        }
    }

    private void addDestinationField() {
        TextField destinationField = new TextField();
        destinationField.setPlaceholder("destination@example.com");
        destinationField.setWidthFull();
        destinationField.setRequired(true);

        Button removeButton = new Button(new Icon(VaadinIcon.MINUS));
        removeButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        removeButton.addClickListener(e -> removeDestinationField(destinationField));
        removeButton.setEnabled(destinationFields.size() > 0); // Keep at least one field

        HorizontalLayout fieldLayout = new HorizontalLayout(destinationField, removeButton);
        fieldLayout.setAlignItems(FlexComponent.Alignment.END);
        fieldLayout.setWidthFull();
        fieldLayout.setFlexGrow(1, destinationField);

        destinationFields.add(destinationField);
        destinationsLayout.add(fieldLayout);

        // Update remove button states
        updateRemoveButtonStates();
    }

    private void removeDestinationField(TextField fieldToRemove) {
        // Find and remove the field and its layout
        destinationFields.remove(fieldToRemove);

        destinationsLayout.getChildren()
                .filter(HorizontalLayout.class::isInstance)
                .map(HorizontalLayout.class::cast)
                .filter(layout -> layout.getChildren().anyMatch(child -> child == fieldToRemove))
                .findFirst()
                .ifPresent(destinationsLayout::remove);

        updateRemoveButtonStates();
    }

    private void updateRemoveButtonStates() {
        boolean canRemove = destinationFields.size() > 1;

        destinationsLayout.getChildren()
                .filter(HorizontalLayout.class::isInstance)
                .map(HorizontalLayout.class::cast)
                .forEach(layout -> {
                    layout.getChildren()
                            .filter(Button.class::isInstance)
                            .map(Button.class::cast)
                            .forEach(button -> button.setEnabled(canRemove));
                });
    }

    private void createAlias() {
        if (!validateForm()) {
            return;
        }

        try {
            String aliasEmail = aliasEmailField.getValue();
            if (selectedDomain != null && !aliasEmail.contains("@")) {
                aliasEmail = aliasEmail + "@" + selectedDomain;
            }

            List<String> destinations = destinationFields.stream()
                    .map(TextField::getValue)
                    .filter(value -> value != null && !value.trim().isEmpty())
                    .collect(Collectors.toList());

            aliasManagementService.createAlias(
                    aliasEmail,
                    destinations,
                    descriptionField.getValue().trim().isEmpty() ? null : descriptionField.getValue()
            );

            Notification.show("Alias created successfully: " + aliasEmail, 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            close();
            clearForm();
            eventPublisher.publishEvent(new DomainContentChangedEvent(this, selectedDomain, DomainContentChangedEvent.ContentType.ALIAS_CREATED));
            if (onSuccess != null) {
                onSuccess.run();
            }

        } catch (Exception e) {
            Notification.show("Error creating alias: " + e.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        if (aliasEmailField.getValue() == null || aliasEmailField.getValue().trim().isEmpty()) {
            aliasEmailField.setErrorMessage("Alias email is required");
            aliasEmailField.setInvalid(true);
            valid = false;
        } else {
            aliasEmailField.setInvalid(false);
        }

        // Validate destinations
        List<String> destinations = destinationFields.stream()
                .map(TextField::getValue)
                .filter(value -> value != null && !value.trim().isEmpty())
                .collect(Collectors.toList());

        if (destinations.isEmpty()) {
            Notification.show("At least one destination is required", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            valid = false;
        }

        // Validate each destination field
        for (TextField field : destinationFields) {
            String value = field.getValue();
            if (value != null && !value.trim().isEmpty()) {
                if (!value.contains("@")) {
                    field.setErrorMessage("Invalid email format");
                    field.setInvalid(true);
                    valid = false;
                } else {
                    field.setInvalid(false);
                }
            }
        }

        return valid;
    }

    private void clearForm() {
        aliasEmailField.clear();
        descriptionField.clear();
        aliasEmailField.setInvalid(false);

        // Clear destinations and reset to one field
        destinationFields.clear();
        destinationsLayout.removeAll();
        addDestinationField();
    }

    @Override
    public void open() {
        clearForm();
        super.open();
        aliasEmailField.focus();
    }
}