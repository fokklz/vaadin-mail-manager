package ch.aarboard.vamm.views.domains;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Domains")
@Route("domains")
@Menu(order = 1, icon = LineAwesomeIconUrl.GLOBE_EUROPE_SOLID)
@Uses(Icon.class)
@PermitAll
@RolesAllowed("ROLE_SITE_ADMIN")
public class DomainsView extends Composite<VerticalLayout> {

    public DomainsView() {
//        Grid basicGrid = new Grid(SamplePerson.class);
//        getContent().setWidth("100%");
//        getContent().getStyle().set("flex-grow", "1");
//        basicGrid.setWidth("100%");
//        basicGrid.setHeight("100%");
//        setGridSampleData(basicGrid);
//        getContent().add(basicGrid);
    }

    private void setGridSampleData(Grid grid) {
//        grid.setItems(query -> samplePersonService.list(VaadinSpringDataHelpers.toSpringPageRequest(query)).stream());
    }
//
//    @Autowired()
//    private SamplePersonService samplePersonService;
}
