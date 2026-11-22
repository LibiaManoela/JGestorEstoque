package br.com.estoque.controller;

import br.com.estoque.model.ItemEstoque;
import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

//Tela 3: Exibe itens com estoque abaixo do mínimo e permite ajuste rápido de quantidade (PUT).
public class ReposicaoController implements PrincipalController.ControladorFilho {

    @FXML private TableView<ItemEstoque> tabelaCriticos;
    @FXML private TableColumn<ItemEstoque, String> colSku;
    @FXML private TableColumn<ItemEstoque, String> colNome;
    @FXML private TableColumn<ItemEstoque, Integer> colQuantidade;
    @FXML private TableColumn<ItemEstoque, Integer> colMinimo;
    @FXML private Spinner<Integer> spinnerQuantidadeRepor;
    @FXML private Button botaoRepor;

    private PrincipalController principal;
    private FilteredList<ItemEstoque> itensCriticos;

    @FXML
    public void initialize() {
        colSku.setCellValueFactory(new PropertyValueFactory<>("sku"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colMinimo.setCellValueFactory(new PropertyValueFactory<>("estoqueMinimo"));

        spinnerQuantidadeRepor.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 500, 10));

        tabelaCriticos.getSelectionModel().selectedItemProperty().addListener((obs, antigo, selecionado) -> {
            botaoRepor.setDisable(selecionado == null);
        });
    }

    @Override
    public void setPrincipalController(PrincipalController principal) {
        this.principal = principal;
        this.itensCriticos = new FilteredList<>(principal.getListaItens(), ItemEstoque::isEstoqueCritico);
        tabelaCriticos.setItems(itensCriticos);
    }

    @FXML
    public void registrarReposicao() {
        ItemEstoque selecionado = tabelaCriticos.getSelectionModel().getSelectedItem();
        if (selecionado == null) return;

        int adicionar = spinnerQuantidadeRepor.getValue();
        int quantidadeAnterior = selecionado.getQuantidade();
        selecionado.setQuantidade(quantidadeAnterior + adicionar);

        principal.exibirCarregando(true);
        principal.ocultarBanner();

        principal.getApiService().atualizar(selecionado)
                .thenAccept(resposta -> Platform.runLater(() -> {
                    principal.exibirCarregando(false);
                    tabelaCriticos.refresh();
                    principal.exibirSucesso("Reposição de +" + adicionar + " un. registrada para \"" + selecionado.getNome() + "\".");
                }))
                .exceptionally(erro -> {
                    Platform.runLater(() -> {
                        selecionado.setQuantidade(quantidadeAnterior);
                        tabelaCriticos.refresh();
                        principal.exibirCarregando(false);
                        principal.exibirErro("Falha ao sincronizar reposição com o servidor.", this::registrarReposicao);
                    });
                    return null;
                });
    }
}
