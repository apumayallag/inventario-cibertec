package com.cibertec.views;

import com.cibertec.database.manager.ClienteManager;
import com.cibertec.database.manager.ProductoManager;
import com.cibertec.database.manager.VentasManager;
import com.cibertec.database.model.Cliente;
import com.cibertec.database.model.Producto;
import com.cibertec.database.model.Ventas;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Optional;

@PageTitle("Ventas")
@Route(value = "ventas", layout = MainLayout.class)
@Uses(Icon.class)
public class VentasView extends Div implements BeforeEnterObserver {

    private final String VENTAS_ID = "ventasID";

    private final Grid<Ventas> grid = new Grid<>(Ventas.class, false);
    private ComboBox<Producto> productos;
    private ComboBox<Cliente> clientes;
    private NumberField cantidad;
    private NumberField precioUnidad;

    private final Button cancel = new Button("Cancelar");
    private final Button save = new Button("Guardar");

    private final BeanValidationBinder<Ventas> binder;

    private Ventas ventasBean;

    private final VentasManager ventasManager;
    private final ProductoManager productoManager;
    private final ClienteManager clienteManager;

    public VentasView(VentasManager ventasManager, ProductoManager productoManager, ClienteManager clienteManager) {
        this.ventasManager = ventasManager;
        this.productoManager = productoManager;
        this.clienteManager = clienteManager;
        addClassNames("cliente-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("id").setHeader("Id").setAutoWidth(true);
        grid.addColumn("producto").setHeader("Nombres").setAutoWidth(true);
        grid.addColumn("cantidad").setHeader("Apellidos").setAutoWidth(true);
        grid.addColumn("precioUnidad").setHeader("Nro. Documento").setAutoWidth(true);
        grid.addColumn("precioTotal").setHeader("precio Total").setAutoWidth(true);
        grid.addColumn("cliente").setHeader("Nro. Documento").setAutoWidth(true);

        grid.setItems(query -> ventasManager.getAll(
                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setSelectionMode(Grid.SelectionMode.NONE);


        // Configure Form
        binder = new BeanValidationBinder<>(Ventas.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.ventasBean == null) {
                    this.ventasBean = new Ventas();
                }
                binder.writeBean(this.ventasBean);
                ventasBean.setProducto(productos.getValue());
                ventasBean.setCliente(clientes.getValue());
                if (!validarVentas(this.ventasBean)){
                    Notification.show("No se tiene el stock disponible");
                    return;
                }
                ventasManager.save(this.ventasBean);
                clearForm();
                refreshGrid();
                Notification.show("Data updated");
                UI.getCurrent().navigate(VentasView.class);
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

    private boolean validarVentas(Ventas ventasBean) {
        Producto producto = ventasBean.getProducto();
        if (producto.getStock() < ventasBean.getCantidad()) {
            return false;
        }
        return true;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> samplePersonId = event.getRouteParameters().get(VENTAS_ID).map(Long::parseLong);
        if (samplePersonId.isPresent()) {
            Optional<Ventas> samplePersonFromBackend = ventasManager.getById(samplePersonId.get());
            if (samplePersonFromBackend.isPresent()) {
                populateForm(samplePersonFromBackend.get());
                productos.setValue(samplePersonFromBackend.get().getProducto());
                clientes.setValue(samplePersonFromBackend.get().getCliente());
            } else {
                Notification.show(
                        String.format("The requested samplePerson was not found, ID = %s", samplePersonId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                refreshGrid();
                event.forwardTo(VentasView.class);
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
        precioUnidad = new NumberField("Precio Unitario");
        productos = new ComboBox<>("Productos");
        productos.setItems(productoManager.getAll());
        clientes = new ComboBox<>("Clientes");
        clientes.setItems(clienteManager.getAll());

        formLayout.add(productos, clientes, cantidad, precioUnidad);

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

    private void populateForm(Ventas value) {
        this.ventasBean = value;
        binder.readBean(this.ventasBean);

    }
}
