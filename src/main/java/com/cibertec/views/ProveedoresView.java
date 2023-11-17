package com.cibertec.views;

import com.cibertec.database.manager.ProveedoresManager;
import com.cibertec.database.model.Proveedores;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Optional;

@PageTitle("Proveedores")
@Route(value = "proveedores/:proveedorID?/:action?(edit)", layout = MainLayout.class)
@Uses(Icon.class)
public class ProveedoresView extends Div implements BeforeEnterObserver {

    private final String PROVEEDOR_ID = "proveedorID";
    private final String PROVEEDOR_EDIT_ROUTE_TEMPLATE = "proveedores/%s/edit";

    private final Grid<Proveedores> grid = new Grid<>(Proveedores.class, false);

    private TextField tipoDocumento;
    private TextField nroDocumento;
    private TextField nombre;
    private TextField apellido;
    private TextField razonSocial;
    private TextField direccion;

    private final Button cancel = new Button("Cancelar");
    private final Button save = new Button("Guardar");

    private final BeanValidationBinder<Proveedores> binder;

    private Proveedores proveedoresBean;

    private final ProveedoresManager proveedoresManager;

    public ProveedoresView(ProveedoresManager proveedoresManager) {
        this.proveedoresManager = proveedoresManager;
        addClassNames("proveedores-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("tipoDocumento").setHeader("Tipo Documento").setAutoWidth(true);
        grid.addColumn("nroDocumento").setHeader("Nro.").setAutoWidth(true);
        grid.addColumn("nombre").setHeader("Nombres").setAutoWidth(true);
        grid.addColumn("apellido").setHeader("Apellidos").setAutoWidth(true);
        grid.addColumn("razonSocial").setHeader("Razon Social").setAutoWidth(true);
        grid.addColumn("direccion").setHeader("Direccion").setAutoWidth(true);

        grid.setItems(query -> proveedoresManager.getAll(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(PROVEEDOR_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(ProveedoresView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Proveedores.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.proveedoresBean == null) {
                    this.proveedoresBean = new Proveedores();
                }
                binder.writeBean(this.proveedoresBean);
                proveedoresManager.save(this.proveedoresBean);
                clearForm();
                refreshGrid();
                Notification.show("Data updated");
                UI.getCurrent().navigate(ProveedoresView.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid");
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> samplePersonId = event.getRouteParameters().get(PROVEEDOR_ID).map(Long::parseLong);
        if (samplePersonId.isPresent()) {
            Optional<Proveedores> samplePersonFromBackend = proveedoresManager.getById(samplePersonId.get());
            if (samplePersonFromBackend.isPresent()) {
                populateForm(samplePersonFromBackend.get());
            } else {
                Notification.show(
                        String.format("The requested samplePerson was not found, ID = %s", samplePersonId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(ProveedoresView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        tipoDocumento = new TextField("Tipo Documento");
        nroDocumento = new TextField("Nro Documento");
        nombre = new TextField("Nombre");
        apellido = new TextField("Apellido");
        razonSocial = new TextField("Razon Social");
        direccion = new TextField("Direccion");
        formLayout.add(tipoDocumento, nroDocumento, nombre, apellido, razonSocial, direccion);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(Proveedores value) {
        this.proveedoresBean = value;
        binder.readBean(this.proveedoresBean);

    }
}
