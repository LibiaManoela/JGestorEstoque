package br.com.estoque.controller;

import br.com.estoque.model.ItemEstoque;
import br.com.estoque.service.EstoqueApiService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.List;

//Controla a navegação entre as 3 telas, o estado da lista em memória e os avisos de carregamento/erro.
public class PrincipalController {
    @FXML private StackPane areaConteudo;
    @FXML private ProgressIndicator indicadorCarregamento;
    @FXML private HBox bannerStatus;
    @FXML private Label textoStatus;
    @FXML private Button botaoTentarNovamente;
    @FXML private ToggleButton botaoSimularFalha;

    private final EstoqueApiService apiService = new EstoqueApiService();
    private final ObservableList<ItemEstoque> listaItens = FXCollections.observableArrayList();
    private Runnable ultimaAcaoFalha;

    @FXML
    public void initialize() {
        ocultarBanner();
        carregarDadosIniciais();
        abrirCatalogo();
    }

    public EstoqueApiService getApiService() {
        return apiService;
    }

    public ObservableList<ItemEstoque> getListaItens() {
        return listaItens;
    }

    public void carregarDadosIniciais() {
        exibirCarregando(true);
        ocultarBanner();

        apiService.listarTodos()
                .thenAccept(itens -> Platform.runLater(() -> {
                    exibirCarregando(false);
                    listaItens.setAll(itens);
                    exibirSucesso("Catálogo sincronizado com a API (" + itens.size() + " itens).");
                }))
                .exceptionally(erro -> {
                    Platform.runLater(() -> {
                        exibirCarregando(false);
                        exibirErro("Falha de comunicação ao carregar o catálogo.", this::carregarDadosIniciais);
                    });
                    return null;
                });
    }

    @FXML
    public void abrirCatalogo() {
        carregarTela("/fxml/catalogo.fxml");
    }

    @FXML
    public void abrirNovoItem() {
        abrirFormulario(null);
    }

    public void abrirFormulario(ItemEstoque itemParaEditar) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/formulario.fxml"));
            Node tela = loader.load();
            FormularioController controller = loader.getController();
            controller.configurar(this, itemParaEditar);
            areaConteudo.getChildren().setAll(tela);
        } catch (IOException e) {
            e.printStackTrace();
            exibirErro("Erro interno ao abrir o formulário.", null);
        }
    }

    @FXML
    public void abrirReposicao() {
        carregarTela("/fxml/reposicao.fxml");
    }

    private void carregarTela(String caminhoFxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(caminhoFxml));
            Node tela = loader.load();
            Object controller = loader.getController();

            if (controller instanceof ControladorFilho filho) {
                filho.setPrincipalController(this);
            }

            areaConteudo.getChildren().setAll(tela);
        } catch (IOException e) {
            e.printStackTrace();
            exibirErro("Erro interno ao carregar a tela.", null);
        }
    }

    @FXML
    public void alternarSimulacaoFalha() {
        boolean ativo = botaoSimularFalha.isSelected();
        apiService.setSimularFalha(ativo);
        botaoSimularFalha.setText(ativo ? "Simulação de Falha: ATIVA" : "Simular Falha de Rede");
    }

    @FXML
    public void executarTentativaNovamente() {
        if (ultimaAcaoFalha != null) {
            ultimaAcaoFalha.run();
        }
    }

    public void exibirCarregando(boolean carregando) {
        indicadorCarregamento.setVisible(carregando);
        indicadorCarregamento.setManaged(carregando);
    }

    public void exibirSucesso(String mensagem) {
        bannerStatus.getStyleClass().removeAll("banner-erro");
        if (!bannerStatus.getStyleClass().contains("banner-sucesso")) {
            bannerStatus.getStyleClass().add("banner-sucesso");
        }
        textoStatus.setText(mensagem);
        botaoTentarNovamente.setVisible(false);
        botaoTentarNovamente.setManaged(false);
        bannerStatus.setVisible(true);
        bannerStatus.setManaged(true);
    }

    public void exibirErro(String mensagem, Runnable acaoRetry) {
        this.ultimaAcaoFalha = acaoRetry;
        bannerStatus.getStyleClass().removeAll("banner-sucesso");
        if (!bannerStatus.getStyleClass().contains("banner-erro")) {
            bannerStatus.getStyleClass().add("banner-erro");
        }
        textoStatus.setText(mensagem);
        boolean temRetry = acaoRetry != null;
        botaoTentarNovamente.setVisible(temRetry);
        botaoTentarNovamente.setManaged(temRetry);
        bannerStatus.setVisible(true);
        bannerStatus.setManaged(true);
    }

    public void ocultarBanner() {
        bannerStatus.setVisible(false);
        bannerStatus.setManaged(false);
    }

    public interface ControladorFilho {
        void setPrincipalController(PrincipalController principal);
    }
}
