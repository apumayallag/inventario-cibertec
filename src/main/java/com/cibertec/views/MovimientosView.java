package com.cibertec.views;

import com.cibertec.database.manager.MovimientoManager;
import com.cibertec.database.manager.ProductoManager;
import com.cibertec.database.manager.ProveedoresManager;
import com.cibertec.database.model.Movimientos;
import com.cibertec.database.model.Producto;
import com.cibertec.database.model.Proveedores;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.NumberField;
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

@PageTitle("Movimiento")
@Route(value = "movimiento", layout = MainLayout.class)
@Uses(Icon.class)
public class MovimientosView extends Div implements BeforeEnterObserver {

    private final String MOVIMIENTO_ID = "movimientoID";

    private final Grid<Movimientos> grid = new Grid<>(Movimientos.class, false);
    private ComboBox<Producto> producto;
    private ComboBox<Proveedores> proveedores;
    private NumberField cantidad;
    private NumberField costo;

    private final Button cancel = new Button("Cancelar");
    private final Button save = new Button("Guardar");

    private final BeanValidationBinder<Movimientos> binder;

    private Movimientos movimientosBean;

    private final MovimientoManager movimientoManager;
    private final ProductoManager productoManager;
    private final ProveedoresManager proveedoresManager;

    public MovimientosView(MovimientoManager movimientoManager, ProductoManager productoManager, ProveedoresManager proveedoresManager) {
        this.movimientoManager = movimientoManager;
        this.productoManager = productoManager;
        this.proveedoresManager = proveedoresManager;
        addClassNames("productos-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("id").setHeader("Id").setAutoWidth(true);
        grid.addColumn("producto.nombre").setHeader("Nombre").setAutoWidth(true);
        grid.addColumn("proveedores").setHeader("Proveedor").setAutoWidth(true);
        grid.addColumn("cantidad").setHeader("Cantidad").setAutoWidth(true);
        grid.addColumn("costo").setHeader("Costo").setAutoWidth(true);
        grid.addColumn("fecha").setHeader("Fecha").setAutoWidth(true);

        grid.setItems(query -> movimientoManager.getAll(
                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setSelectionMode(Grid.SelectionMode.NONE);

        // Configure Form
        binder = new BeanValidationBinder<>(Movimientos.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.movimientosBean == null) {
                    this.movimientosBean = new Movimientos();
                }
                binder.writeBean(this.movimientosBean);
                movimientoManager.save(this.movimientosBean);
                clearForm();
                refreshGrid();
                Notification.show("Data updated");
                UI.getCurrent().navigate(MovimientosView.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid");
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> samplePersonId = event.getRouteParameters().get(MOVIMIENTO_ID).map(Long::parseLong);
        if (samplePersonId.isPresent()) {
            Optional<Movimientos> samplePersonFromBackend = movimientoManager.getById(samplePersonId.get());
            if (samplePersonFromBackend.isPresent()) {
                populateForm(samplePersonFromBackend.get());
                producto.setValue(samplePersonFromBackend.get().getProducto());
                proveedores.setValue(samplePersonFromBackend.get().getProveedores());
            } else {
                Notification.show(
                        String.format("The requested samplePerson was not found, ID = %s", samplePersonId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                refreshGrid();
                event.forwardTo(MovimientosView.class);
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
        cantidad = new NumberField("Cantidad");
        costo = new NumberField("Costo");
        producto = new ComboBox<>("Producto");
        producto.setItems(productoManager.getAll());
        proveedores = new ComboBox<>("Proveedores");
        proveedores.setItems(proveedoresManager.getAll());

        formLayout.add(cantidad, costo, producto, proveedores);

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

    private void populateForm(Movimientos value) {
        this.movimientosBean = value;
        binder.readBean(this.movimientosBean);

    }
}
