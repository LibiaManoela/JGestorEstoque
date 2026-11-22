package br.com.estoque.controller;

import br.com.estoque.model.ItemEstoque;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

//Tela 1: Controla a tabela geral, barra de busca, filtro por categoria e exclusão com confirmação.
public class CatalogoController {

    @FXML private TextField campoBusca;
    @FXML private ComboBox<String> filtroCategoria;
    @FXML private TableView<ItemEstoque> tabelaEstoque;
    @FXML private TableColumn<ItemEstoque, String> colSku;
    @FXML private TableColumn<ItemEstoque, String> colNome;
    @FXML private TableColumn<ItemEstoque, String> colCategoria;
    @FXML private TableColumn<ItemEstoque, Integer> colQuantidade;
    @FXML private TableColumn<ItemEstoque, Integer> colMinimo;
    @FXML private TableColumn<ItemEstoque, Double> colPreco;
    @FXML private Button botaoEditar;
    @FXML private Button botaoExcluir;

    private PrincipalController principal;
    private FilteredList<ItemEstoque> listaFiltrada;

    @FXML
    public void initialize() {
        colSku.setCellValueFactory(new PropertyValueFactory<>("sku"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colMinimo.setCellValueFactory(new PropertyValueFactory<>("estoqueMinimo"));
        colPreco.setCellValueFactory(new PropertyValueFactory<>("precoUnitario"));

        tabelaEstoque.getSelectionModel().selectedItemProperty().addListener((obs, antigo, selecionado) -> {
            boolean temSelecao = selecionado != null;
            botaoEditar.setDisable(!temSelecao);
            botaoExcluir.setDisable(!temSelecao);
        });
    }

    @Override
    public void setPrincipalController(PrincipalController principal) {
        this.principal = principal;
        this.listaFiltrada = new FilteredList<>(principal.getListaItens(), p -> true);
        tabelaEstoque.setItems(listaFiltrada);

        atualizarCategorias();
        principal.getListaItens().addListener((ListChangeListener<ItemEstoque>) c -> atualizarCategorias());

        campoBusca.textProperty().addListener((obs, antigo, novo) -> aplicarFiltros());
        filtroCategoria.valueProperty().addListener((obs, antigo, novo) -> aplicarFiltros());
    }

    private void atualizarCategorias() {
        String selecaoAtual = filtroCategoria.getValue();
        Set<String> categorias = principal.getListaItens().stream()
                .map(ItemEstoque::getCategoria)
                .filter(c -> c != null && !c.isBlank())
                .collect(Collectors.toSet());

        var opcoes = FXCollections.observableArrayList("Todas as categorias");
        opcoes.addAll(categorias);
        filtroCategoria.setItems(opcoes);

        if (selecaoAtual != null && opcoes.contains(selecaoAtual)) {
            filtroCategoria.setValue(selecaoAtual);
        } else {
            filtroCategoria.setValue("Todas as categorias");
        }
    }

    private void aplicarFiltros() {
        String termo = campoBusca.getText() == null ? "" : campoBusca.getText().trim().toLowerCase();
        String categoria = filtroCategoria.getValue();

        listaFiltrada.setPredicate(item -> {
            boolean bateBusca = termo.isEmpty()
                    || (item.getNome() != null && item.getNome().toLowerCase().contains(termo))
                    || (item.getSku() != null && item.getSku().toLowerCase().contains(termo));

            boolean bateCategoria = categoria == null
                    || categoria.equals("Todas as categorias")
                    || categoria.equalsIgnoreCase(item.getCategoria());

            return bateBusca && bateCategoria;
        });
    }

    @FXML
    public void editarSelecionado() {
        ItemEstoque selecionado = tabelaEstoque.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            principal.abrirFormulario(selecionado);
        }
    }

    @FXML
    public void excluirSelecionado() {
        ItemEstoque selecionado = tabelaEstoque.getSelectionModel().getSelectedItem();
        if (selecionado == null) return;

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Exclusão");
        confirmacao.setHeaderText("Excluir item do estoque?");
        confirmacao.setContentText("Deseja remover \"" + selecionado.getNome() + "\" (SKU: " + selecionado.getSku() + ")?");
        confirmacao.getDialogPane().getStylesheets().add(getClass().getResource("/css/estilo.css").toExternalForm());

        Optional<ButtonType> resultado = confirmacao.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            executarExclusao(selecionado);
        }
    }

    private void executarExclusao(ItemEstoque item) {
        principal.exibirCarregando(true);
        principal.ocultarBanner();

        principal.getApiService().excluir(item.getId())
                .thenRun(() -> Platform.runLater(() -> {
                    principal.exibirCarregando(false);
                    principal.getListaItens().remove(item);
                    principal.exibirSucesso("Item \"" + item.getNome() + "\" excluído com sucesso.");
                }))
                .exceptionally(erro -> {
                    Platform.runLater(() -> {
                        principal.exibirCarregando(false);
                        principal.exibirErro("Falha ao excluir o item na API.", () -> executarExclusao(item));
                    });
                    return null;
                });
    }
}
