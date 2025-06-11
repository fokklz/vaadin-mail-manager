package ch.aarboard.vamm.views.mailalias;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Mail Alias")
@Route("domain-mail-alias")
@Menu(order = 3, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
public class MailAliasView extends Composite<VerticalLayout> {

    public MailAliasView() {
        VerticalLayout layoutColumn2 = new VerticalLayout();
        H1 h1 = new H1();
        HorizontalLayout layoutRow = new HorizontalLayout();
        Button buttonPrimary = new Button();
        Button buttonPrimary2 = new Button();
        VerticalLayout layoutColumn3 = new VerticalLayout();
        getContent().setHeightFull();
        getContent().setWidthFull();
        getContent().getStyle().set("flex-grow", "1");
        layoutColumn2.setWidthFull();
        getContent().setFlexGrow(1.0, layoutColumn2);
        layoutColumn2.setWidth("100%");
        layoutColumn2.setHeight("min-content");
        h1.setText("alias@other.com");
        h1.setWidth("max-content");
        layoutRow.setWidthFull();
        layoutColumn2.setFlexGrow(1.0, layoutRow);
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.getStyle().set("flex-grow", "1");
        buttonPrimary.setText("Deactivate");
        buttonPrimary.setWidth("min-content");
        buttonPrimary.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonPrimary2.setText("Delete");
        buttonPrimary2.setWidth("min-content");
        buttonPrimary2.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        layoutColumn3.setWidthFull();
        getContent().setFlexGrow(1.0, layoutColumn3);
        layoutColumn3.setWidth("100%");
        layoutColumn3.getStyle().set("flex-grow", "1");
        getContent().add(layoutColumn2);
        layoutColumn2.add(h1);
        layoutColumn2.add(layoutRow);
        layoutRow.add(buttonPrimary);
        layoutRow.add(buttonPrimary2);
        getContent().add(layoutColumn3);
    }
}
